package de.nereide.stromschnelle

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.navigation.compose.rememberNavController
import de.nereide.stromschnelle.ui.nav.AppNavHost
import de.nereide.stromschnelle.ui.nav.Routes
import de.nereide.stromschnelle.ui.theme.StromschnelleTheme

/**
 * Single-activity host. Handles the `stromschnelle://todo/{id}` deep link by
 * navigating straight to the todo's detail screen once the nav graph is up.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            StromschnelleTheme {
                val navController = rememberNavController()
                val deepLinkId = remember(intent) { deepLinkTodoId(intent) }

                LaunchedEffect(deepLinkId) {
                    if (deepLinkId != null) {
                        navController.navigate(Routes.detail(deepLinkId))
                    }
                }

                AppNavHost(navController = navController)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun deepLinkTodoId(intent: Intent?): Long? {
        val uri: Uri = intent?.data ?: return null
        if (uri.scheme != "stromschnelle" || uri.host != "todo") return null
        return uri.lastPathSegment?.toLongOrNull()
    }
}
