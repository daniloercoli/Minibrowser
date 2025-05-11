package com.daniloercoli.minibrowser

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController
) {
    val context = LocalContext.current

    // DataStore per salvare e recuperare il delay
    val dataStore = remember { DelayDataStore(context) }

    // Colleziona il valore corrente da DataStore
    val currentDelayMs by dataStore.getJsDelay().collectAsState(initial = 200L)

    // Stato del slider
    var sliderValue by remember { mutableStateOf(currentDelayMs.toFloat()) }

    // Sincronizza lo slider se il valore in DataStore cambia
    LaunchedEffect(currentDelayMs) {
        sliderValue = currentDelayMs.toFloat()
    }

    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Impostazioni") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Indietro")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Intervallo di disattivazione JavaScript",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "Dopo quanti millisecondi disattivare JavaScript al termine del caricamento della pagina:",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )

            // Slider per modificare il valore del delay
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    valueRange = 50f..1000f,
                    steps = ((1000 - 50) / 50 - 1).toInt(),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Text(
                    text = "${sliderValue.toInt()} ms",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Pulsante per salvare le impostazioni
            Button(
                onClick = {
                    coroutineScope.launch {
                        dataStore.saveJsDelay(sliderValue.toLong())
                        navController.popBackStack()
                    }
                }
            ) {
                Text("Salva")
            }
        }
    }
}
