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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter

@Composable
fun BrowserChooserScreen(incomingUrl: Uri?) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("whichbrowser_prefs", Context.MODE_PRIVATE)

    val domain = remember(incomingUrl) {
        val originalHost = incomingUrl?.host ?: ""
        // If it's a Google redirector, try to extract the real destination host
        val targetUrl = if (originalHost.contains("google.")) {
            incomingUrl?.getQueryParameter("url") ?: incomingUrl?.getQueryParameter("q")
        } else null

        val effectiveHost = targetUrl?.let { Uri.parse(it).host } ?: originalHost
        effectiveHost.removePrefix("www.").lowercase()
    }

    val savedBrowserForDomain = prefs.getString("domain_$domain", null)

    // If we have a saved browser for this domain → auto open and finish
    if (savedBrowserForDomain != null && incomingUrl != null) {
        LaunchedEffect(Unit) {
            val intent = Intent(Intent.ACTION_VIEW, incomingUrl)
                .setPackage(savedBrowserForDomain)
            
            Toast.makeText(context, "Opening saved browser for $domain", Toast.LENGTH_SHORT).show()
            
            context.startActivity(intent)
            (context as? ComponentActivity)?.finish()
        }

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // No saved browser for this domain → show chooser
    var rememberChoice by rememberSaveable { mutableStateOf(false) }

    val browserList = listOf(
        BrowserOption("Chrome", "com.android.chrome", getIconForPackage(context, "com.android.chrome")),
        BrowserOption("Edge", "com.microsoft.emmx", getIconForPackage(context, "com.microsoft.emmx")),
        BrowserOption("DuckDuckGo", "com.duckduckgo.mobile.android", getIconForPackage(context, "com.duckduckgo.mobile.android"))
    )

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
            modifier = Modifier.size(90.dp)
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
                        val intent = Intent(Intent.ACTION_VIEW, incomingUrl)
                            .setPackage(browser.packageName)

                        if (rememberChoice) {
                            prefs.edit().putString("domain_$domain", browser.packageName).apply()
                            Toast.makeText(context, "✓ Saved ${browser.label} for $domain", Toast.LENGTH_SHORT).show()
                        }

                        context.startActivity(intent)
                        (context as? ComponentActivity)?.finish()
                    }
                )
            }
        }

        if (incomingUrl != null) {
            val urlString = incomingUrl.toString()
            val displayedUrl = if (urlString.length > 200) urlString.take(200) + "..." else urlString
            Text(
                text = displayedUrl,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(60.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Absolute.Left
        ) {
            Checkbox(
                checked = rememberChoice,
                onCheckedChange = { rememberChoice = it }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Remember my choice for $domain",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Spacer(modifier = Modifier.height(1.dp))
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun BrowserChooserScreenPreview() {
    BrowserChooserScreen(incomingUrl = Uri.parse("https://www.google.com"))
}

data class BrowserOption(
    val label: String,
    val packageName: String,
    val icon: Drawable
)

@Composable
fun BrowserRow(browser: BrowserOption, onClick: () -> Unit) {
    Surface(
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Image(
                painter = rememberAsyncImagePainter(browser.icon),
                contentDescription = browser.label,
                modifier = Modifier.size(40.dp)
            )
            Text(
                text = browser.label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

private fun getIconForPackage(context: Context, packageName: String): Drawable {
    return try {
        context.packageManager.getApplicationIcon(packageName)
    } catch (_: PackageManager.NameNotFoundException) {
        ContextCompat.getDrawable(context, android.R.drawable.ic_menu_compass)!!
    }
}
