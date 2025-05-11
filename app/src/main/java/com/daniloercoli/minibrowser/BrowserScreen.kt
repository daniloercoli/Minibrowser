package com.daniloercoli.minibrowser

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
//import kotlinx.coroutines.flow.collectAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserScreen(
    initialUrl: String,
    navController: NavController
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    // Stati della UI
    var url by remember { mutableStateOf(initialUrl) }
    var currentUrl by remember { mutableStateOf(initialUrl) }
    var progress by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(false) }

    // DataStore per il delay JavaScript
    val dataStore = remember { DelayDataStore(context) }
    val jsDelayMs by dataStore.getJsDelay().collectAsState(initial = 200L)

    // Creazione unica del WebView
    val webView = remember(context) {
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            @SuppressLint("SetJavaScriptEnabled")
            settings.javaScriptEnabled = true

            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView, pageUrl: String, favicon: Bitmap?) {
                    isLoading = true
                    currentUrl = pageUrl
                    url = pageUrl
                    settings.javaScriptEnabled = true
                }

                override fun onPageFinished(view: WebView, finishedUrl: String) {
                    isLoading = false
                    postDelayed({ settings.javaScriptEnabled = false }, jsDelayMs)
                }
            }
            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView, newProgress: Int) {
                    progress = newProgress
                }
            }
            loadUrl(normalizeUrl(initialUrl))
        }
    }

    // Distruggi il WebView quando il Composable esce
    DisposableEffect(webView) {
        onDispose { webView.destroy() }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Inserisci URL") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            if (url.isNotBlank()) {
                                webView.loadUrl(normalizeUrl(url))
                            }
                        }
                    )
                )
            },
            actions = {
                IconButton(onClick = {
                    focusManager.clearFocus()
                    if (url.isNotBlank()) webView.loadUrl(normalizeUrl(url))
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Vai")
                }
                IconButton(onClick = { webView.reload() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Ricarica")
                }
                IconButton(onClick = { navController.navigate("settings") }) {
                    Icon(Icons.Default.Settings, contentDescription = "Impostazioni")
                }
            }
        )

        if (isLoading) {
            LinearProgressIndicator(
                progress = progress / 100f,
                modifier = Modifier.fillMaxWidth()
            )
        }

        AndroidView(
            factory = { webView },
            modifier = Modifier.fillMaxSize(),
            update = { /* nessuna update necessaria */ }
        )
    }
}

private fun normalizeUrl(url: String): String =
    if (!url.startsWith("http://") && !url.startsWith("https://")) "https://$url" else url
