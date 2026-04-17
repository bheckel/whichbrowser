package com.rshdev.whichbrowser

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("whichbrowser_prefs", Context.MODE_PRIVATE)
    
    // In a real app, you'd use a ViewModel, but for now we'll read directly
    var savedDomains by remember { 
        mutableStateOf(
            prefs.all.filterKeys { it.startsWith("domain_") }
                .map { it.key.removePrefix("domain_") to it.value.toString() }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Saved Preferences") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (savedDomains.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No saved domains yet.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(savedDomains) { (domain, packageName) ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = domain, style = MaterialTheme.typography.titleMedium)
                                Text(text = packageName, style = MaterialTheme.typography.bodySmall)
                            }
                            IconButton(onClick = {
                                prefs.edit().remove("domain_$domain").apply()
                                savedDomains = savedDomains.filter { it.first != domain }
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                            }
                        }
                    }
                }
            }
        }
    }
}
