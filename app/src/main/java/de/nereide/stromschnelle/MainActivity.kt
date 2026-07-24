package de.nereide.stromschnelle

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.navigation.compose.rememberNavController
import de.nereide.stromschnelle.ui.nav.AppNavHost
import de.nereide.stromschnelle.ui.nav.Routes
import de.nereide.stromschnelle.ui.theme.StromschnelleTheme

/**
 * Single-activity host. Handles the `stromschnelle://todo/{id}` deep link by
 * navigating to the todo's detail screen.
 *
 * The activity is `singleTop` (see the manifest), so tapping the widget while
 * the app is already running reuses this instance and delivers the new intent
 * via [onNewIntent] instead of stacking a second activity. The deep-link
 * navigation pops back to the list first, so the back stack is always
 * `[list, detail]` — pressing back returns to the list, and again exits.
 */
class MainActivity : ComponentActivity() {

    // Backing Compose state so a deep link arriving via onNewIntent recomposes.
    private var pendingIntent by mutableStateOf<Intent?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pendingIntent = intent

        setContent {
            StromschnelleTheme {
                val navController = rememberNavController()
                val current = pendingIntent

                LaunchedEffect(current) {
                    val id = deepLinkTodoId(current)
                    if (id != null) {
                        navController.navigate(Routes.detail(id)) {
                            popUpTo(Routes.LIST) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                    // Consume the intent so back navigation / recomposition does
                    // not re-trigger the deep link.
                    pendingIntent = null
                }

                AppNavHost(navController = navController)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingIntent = intent
    }

    private fun deepLinkTodoId(intent: Intent?): Long? {
        val uri: Uri = intent?.data ?: return null
        if (uri.scheme != "stromschnelle" || uri.host != "todo") return null
        return uri.lastPathSegment?.toLongOrNull()
    }
}
