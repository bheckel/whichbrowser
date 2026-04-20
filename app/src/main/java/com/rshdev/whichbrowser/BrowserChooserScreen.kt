// Copyright (c) 2026 Robert S. Heckel Jr.
//
//This program is free software: you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation, version 3.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
package com.rshdev.whichbrowser

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter

@Composable
fun BrowserChooserScreen(
    incomingUrl: Uri?,
    onOpenSettings: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("whichbrowser_prefs", Context.MODE_PRIVATE) }

    val domain = remember(incomingUrl) {
        val originalHost = incomingUrl?.host ?: ""
        // Restoring the specific unwrapping logic for Google inside the chooser as requested
        val targetUrl = if (originalHost.contains("google.")) {
            incomingUrl?.getQueryParameter("url") ?: incomingUrl?.getQueryParameter("q")
        } else null

        val effectiveHost = targetUrl?.let { Uri.parse(it).host } ?: originalHost
        effectiveHost.removePrefix("www.").lowercase()
    }

    val savedBrowserForDomain = remember(domain) { prefs.getString("domain_$domain", null) }

    // Auto-open logic
    if (savedBrowserForDomain != null && incomingUrl != null) {
        LaunchedEffect(domain) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, incomingUrl).setPackage(savedBrowserForDomain)
                context.startActivity(intent)
                (context as? ComponentActivity)?.finish()
            } catch (e: Exception) {
                Toast.makeText(context, "Error opening saved browser", Toast.LENGTH_SHORT).show()
            }
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    var rememberChoice by rememberSaveable { mutableStateOf(false) }

    // Dynamic browser listing (Feature #1) optimized to avoid ANRs
    val browserList = remember {
        val pm = context.packageManager
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com"))
        val resolveInfos = pm.queryIntentActivities(browserIntent, PackageManager.MATCH_ALL)
        
        resolveInfos.map { info ->
            BrowserOption(
                label = info.loadLabel(pm).toString(),
                packageName = info.activityInfo.packageName,
                icon = info.loadIcon(pm)
            )
        }.filter { it.packageName != context.packageName } // Don't list ourselves
         .distinctBy { it.packageName }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .statusBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Image(
            painter = painterResource(id = R.drawable.witch2),
            contentDescription = "App Logo",
            modifier = Modifier
                .size(90.dp)
                .clickable { onOpenSettings() }
        )

        Text(
            text = "Which Browser?",
            style = MaterialTheme.typography.headlineMedium,
            color = Color(0xFFFF8C00)
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(browserList) { browser ->
                BrowserRow(
                    browser = browser,
                    onClick = {
                        if (incomingUrl != null) {
                            val intent = Intent(Intent.ACTION_VIEW, incomingUrl).setPackage(browser.packageName)
                            if (rememberChoice) {
                                prefs.edit().putString("domain_$domain", browser.packageName).apply()
                            }
                            context.startActivity(intent)
                            (context as? ComponentActivity)?.finish()
                        }
                    }
                )
            }
        }

        if (incomingUrl != null) {
            Text(
                text = incomingUrl.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth().clickable { rememberChoice = !rememberChoice },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = rememberChoice, onCheckedChange = { rememberChoice = it })
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Remember choice for $domain",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun BrowserChooserScreenPreview() {
    BrowserChooserScreen(
        incomingUrl = Uri.parse("https://www.google.com"),
        onOpenSettings = {}
    )
}

data class BrowserOption(val label: String, val packageName: String, val icon: Drawable)

@Composable
fun BrowserRow(browser: BrowserOption, onClick: () -> Unit) {
    Surface(
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.clickable(onClick = onClick).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Image(
                painter = rememberAsyncImagePainter(browser.icon),
                contentDescription = browser.label,
                modifier = Modifier.size(40.dp)
            )
            Text(text = browser.label, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

private fun getIconForPackage(context: Context, packageName: String): Drawable {
    return try {
        context.packageManager.getApplicationIcon(packageName)
    } catch (_: Exception) {
        ContextCompat.getDrawable(context, android.R.drawable.ic_menu_compass)!!
    }
}
