package com.daniloercoli.minibrowser

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.compose.rememberNavController
import com.daniloercoli.minibrowser.ui.theme.MiniBrowserTheme

// 1. Costanti per destinazioni e intent actions
object Destinations {
    const val Browser = "browser"
    const val Settings = "settings"
    const val BrowserWithArg = "$Browser?url={url}"
}

object IntentActions {
    const val SEND = Intent.ACTION_SEND
    const val VIEW = Intent.ACTION_VIEW
    const val EXTRA_TEXT = Intent.EXTRA_TEXT
}

class MainActivity : ComponentActivity() {
    // Stato condiviso dell'URL ricevuto da share-intent
    private val sharedUrlState = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Estrai l'URL iniziale
        sharedUrlState.value = extractSharedUrl(intent)

        setContent {
            MiniBrowserTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavHost(sharedUrlState)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Aggiorna lo state per la composizione
        sharedUrlState.value = extractSharedUrl(intent)
    }

    private fun extractSharedUrl(intent: Intent): String? {
        if (intent.action == IntentActions.SEND) {
            val mime = intent.type ?: ""
            if (mime.startsWith("text/") || mime == "*/*") {
                val text = intent.getStringExtra(IntentActions.EXTRA_TEXT)?.trim().orEmpty()
                if (text.isNotEmpty()) {
                    // Prendi l’ULTIMO URL presente nel testo (di solito messo in coda)
                    val url = extractLastUrlFromText(text)
                    if (url != null) return url
                }
            }
        }

        // Link aperto direttamente (ACTION_VIEW)
        if (intent.action == IntentActions.VIEW && intent.data != null) {
            return intent.dataString
        }

        return null
    }

    /**
     * Estrae l’ultimo URL dal testo condiviso.
     * Casi gestiti:
     *   - http/https completi
     *   - domini senza schema (es. "example.com/foo")
     *   - rimozione di punteggiatura finale tipo .,),],}
     */
    private fun extractLastUrlFromText(text: String): String? {
        // Regex: (1) http/https completi  (2) dominio.tld[/...]
        val urlRegex = Regex(
            pattern = "(https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+)|([\\w.-]+\\.[A-Za-z]{2,}(?:/[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]*)?)",
            options = setOf(RegexOption.IGNORE_CASE)
        )

        val matches = urlRegex.findAll(text).toList()
        if (matches.isEmpty()) return null

        // Prendi l’ultimo match (URL spesso è in coda)
        var raw = matches.last().value

        // Rimuovi punteggiatura appiccicata alla fine (es. "…/foo).")
        raw = raw.trim()
            .trimEnd('.', ',', ';', ':', ')', ']', '}', '>', '«', '»', '”', '“', '’', '\'')

        // Normalizza schema
        return if (raw.startsWith("http://", true) || raw.startsWith("https://", true)) {
            raw
        } else {
            "https://$raw"
        }
    }

    @Composable
    fun AppNavHost(sharedUrlState: State<String?>) {
        // 2. NavController locale e non mutabile
        val navController = rememberNavController()
        val sharedUrl by sharedUrlState

        // Navigazione side-effect quando arriva un URL condiviso
        LaunchedEffect(sharedUrl) {
            sharedUrl?.let { url ->
                navController.navigate("${Destinations.Browser}?url=$url") {
                    popUpTo(Destinations.Browser) { inclusive = true }
                }
                // Reset per evitare navigazioni duplicate
                (sharedUrlState as MutableState<String?>).value = null
            }
        }

        // 3. NavHost con args
        NavHost(
            navController = navController,
            startDestination = Destinations.Browser
        ) {
            composable(
                route = Destinations.BrowserWithArg,
                arguments = listOf(
                    navArgument("url") {
                        type = NavType.StringType
                        defaultValue = "https://repubblica.it"
                    }
                )
            ) { backStackEntry ->
                val url = backStackEntry.arguments?.getString("url")!!
                BrowserScreen(initialUrl = url, navController = navController)
            }
            composable(Destinations.Settings) {
                SettingsScreen(navController = navController)
            }
        }
    }
}