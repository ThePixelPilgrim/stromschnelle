#!/usr/bin/env bash
#
# release.sh — cut a signed, Obtainium-ready GitHub release for Stromschnelle.
#
# Flow: confirm version -> bump version.properties -> release commit -> build &
# sign & test -> (on success) tag -> push commit + tag -> create GitHub release
# with the signed APK attached. On build/test failure the release commit is
# rolled back so the tree is left exactly as it was.
#
# The version number has a single source of truth: version.properties
# (versionName=x.y.z). The integer versionCode is derived in build.gradle.kts.
#
# Requirements: git, gh (authenticated), a JDK 17, the Android SDK, and
# app/keystore.properties (signing secrets, never committed).

set -euo pipefail

# --- locate repo root -------------------------------------------------------
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$ROOT"

VERSION_FILE="$ROOT/version.properties"
KEYSTORE_PROPS="$ROOT/app/keystore.properties"
APK_OUT="$ROOT/app/build/outputs/apk/release/app-release.apk"

info()  { printf '\033[1;34m==>\033[0m %s\n' "$*"; }
ok()    { printf '\033[1;32m ✓\033[0m %s\n' "$*"; }
err()   { printf '\033[1;31m ✗\033[0m %s\n' "$*" >&2; }
die()   { err "$*"; exit 1; }

# --- toolchain --------------------------------------------------------------
# AGP 8.7 needs JDK 17. Honour a caller-provided JAVA_HOME if it is a 17 JDK,
# otherwise try to locate one.
detect_jdk17() {
    if [[ -n "${JAVA_HOME:-}" ]] && "$JAVA_HOME/bin/java" -version 2>&1 | grep -q '"17'; then
        return 0
    fi
    for c in /usr/lib/jvm/java-17-temurin-jdk /usr/lib/jvm/temurin-17-jdk \
             /usr/lib/jvm/java-17-openjdk /usr/lib/jvm/java-17; do
        if [[ -x "$c/bin/java" ]]; then export JAVA_HOME="$c"; return 0; fi
    done
    return 1
}
detect_jdk17 || die "No JDK 17 found. Set JAVA_HOME to a JDK 17 install."

if [[ -z "${ANDROID_HOME:-}" ]]; then
    if [[ -f "$ROOT/local.properties" ]]; then
        ANDROID_HOME="$(grep -E '^sdk\.dir=' "$ROOT/local.properties" | head -1 | cut -d= -f2-)"
    fi
    ANDROID_HOME="${ANDROID_HOME:-$HOME/Android/Sdk}"
fi
export ANDROID_HOME ANDROID_SDK_ROOT="$ANDROID_HOME"
[[ -d "$ANDROID_HOME" ]] || die "Android SDK not found at $ANDROID_HOME (set ANDROID_HOME or local.properties)."

# --- preconditions ----------------------------------------------------------
command -v gh >/dev/null || die "gh (GitHub CLI) is not installed."
gh auth status >/dev/null 2>&1 || die "gh is not authenticated. Run: gh auth login"
[[ -f "$KEYSTORE_PROPS" ]] || die "Missing app/keystore.properties — a release must be signed. See android-signing-backup."

BRANCH="$(git rev-parse --abbrev-ref HEAD)"
[[ "$BRANCH" == "main" ]] || die "Releases are cut from 'main' (currently on '$BRANCH')."
[[ -z "$(git status --porcelain)" ]] || die "Working tree is dirty. Commit or stash changes first."

info "Fetching origin to check we are up to date…"
git fetch --quiet origin main || true
if [[ -n "$(git rev-list HEAD..origin/main 2>/dev/null)" ]]; then
    die "Local main is behind origin/main. Pull first."
fi

# --- version handling -------------------------------------------------------
CURRENT="$(grep -E '^versionName=' "$VERSION_FILE" | head -1 | cut -d= -f2- | tr -d '[:space:]')"
[[ "$CURRENT" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]] || die "Current versionName '$CURRENT' is not x.y.z."

# semver "strictly greater than" check
version_gt() { # $1 > $2 ?
    [[ "$1" != "$2" ]] && [[ "$(printf '%s\n%s\n' "$1" "$2" | sort -V | tail -1)" == "$1" ]]
}

# The last *released* version is the highest existing vX.Y.Z tag (may be none).
LATEST_TAG="$(git tag --list 'v[0-9]*.[0-9]*.[0-9]*' | sed 's/^v//' | sort -V | tail -1)"

# If the current versionName has not been tagged yet, release it as-is
# (this is how the very first release works). Otherwise propose the next patch.
if [[ -z "$(git tag -l "v$CURRENT")" ]]; then
    SUGGESTED="$CURRENT"
else
    IFS=. read -r MA MI PA <<<"$CURRENT"
    SUGGESTED="$MA.$MI.$((PA + 1))"
fi

echo
if [[ -z "$LATEST_TAG" ]]; then
    info "No previous release. Current version: v$CURRENT"
else
    info "Last released: v$LATEST_TAG   ·   version.properties: v$CURRENT"
fi
read -rp "Release v$SUGGESTED?  [y]es / [n]o / or type an explicit x.y.z: " REPLY
case "$REPLY" in
    y|Y|yes|YES|"") NEW="$SUGGESTED" ;;
    n|N|no|NO)      info "Aborted."; exit 0 ;;
    *)              NEW="$(echo "$REPLY" | tr -d '[:space:]')" ;;
esac

[[ "$NEW" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]] || die "'$NEW' is not a valid x.y.z version."
IFS=. read -r NMA NMI NPA <<<"$NEW"
{ [[ "$NMI" -lt 100 ]] && [[ "$NPA" -lt 100 ]]; } || die "minor and patch must be < 100 (got $NEW)."

TAG="v$NEW"
git rev-parse "$TAG" >/dev/null 2>&1 && die "Tag $TAG already exists."
# Must exceed the last released version (skipped when there is no prior release).
if [[ -n "$LATEST_TAG" ]]; then
    version_gt "$NEW" "$LATEST_TAG" || die "New version v$NEW must be greater than last released v$LATEST_TAG."
fi

echo
info "About to release: v$CURRENT  ->  v$NEW  (tag $TAG)"
read -rp "Proceed? [y/N] " CONFIRM
[[ "$CONFIRM" =~ ^[yY] ]] || { info "Aborted."; exit 0; }

# --- release commit ---------------------------------------------------------
# Snapshot the pre-release HEAD so we can roll back cleanly on failure.
PRE_RELEASE_SHA="$(git rev-parse HEAD)"
rollback() {
    err "Build/test failed — rolling back the release commit."
    git reset --hard "$PRE_RELEASE_SHA"
    git tag -d "$TAG" >/dev/null 2>&1 || true
    err "Tree restored to $PRE_RELEASE_SHA. No tag, no push, no release created."
}

info "Writing version.properties (versionName=$NEW)…"
# Replace only the versionName= line, preserve the rest of the file.
tmp="$(mktemp)"
sed "s/^versionName=.*/versionName=$NEW/" "$VERSION_FILE" >"$tmp" && mv "$tmp" "$VERSION_FILE"
git add "$VERSION_FILE"
# --allow-empty: on the first release the versionName may already equal the
# target, so there is nothing to stage — we still want a "Release vX.Y.Z" marker.
git commit --quiet --allow-empty -m "Release $TAG"
ok "Release commit created."

# --- build, sign, test ------------------------------------------------------
trap rollback ERR
info "Building signed release APK + running unit tests (JDK 17)…"
./gradlew --no-daemon clean testDebugUnitTest assembleRelease
trap - ERR

[[ -f "$APK_OUT" ]] || { rollback; die "Expected APK not found at $APK_OUT."; }

# Verify the APK is actually signed (v2+); refuse to release an unsigned one.
APKSIGNER="$(ls "$ANDROID_HOME"/build-tools/*/apksigner 2>/dev/null | sort -V | tail -1 || true)"
if [[ -n "$APKSIGNER" ]]; then
    info "Verifying APK signature…"
    "$APKSIGNER" verify --print-certs "$APK_OUT" >/dev/null || { rollback; die "APK failed signature verification."; }
    ok "APK signature verified."
fi

RELEASE_APK="$ROOT/app/build/outputs/apk/release/stromschnelle-$TAG.apk"
cp "$APK_OUT" "$RELEASE_APK"
ok "Signed APK: $RELEASE_APK"

# --- tag, push, publish -----------------------------------------------------
info "Tagging $TAG…"
git tag -a "$TAG" -m "Stromschnelle $TAG"

info "Pushing main and $TAG…"
git push origin main
git push origin "$TAG"
ok "Pushed."

info "Creating GitHub release $TAG with the APK attached…"
gh release create "$TAG" "$RELEASE_APK" \
    --title "$TAG" \
    --generate-notes
ok "Release published."

URL="$(gh release view "$TAG" --json url -q .url 2>/dev/null || echo '')"
echo
ok "Done. Stromschnelle $TAG is live${URL:+: $URL}"
echo "   Obtainium will pick up the attached APK (stromschnelle-$TAG.apk) on its next check."
