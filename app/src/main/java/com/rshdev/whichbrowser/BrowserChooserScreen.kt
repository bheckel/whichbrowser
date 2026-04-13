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

    // Load saved browser for this exact domain
    val savedBrowserForDomain = prefs.getString("domain_$domain", null)

    // If we have a saved browser for this domain → auto open and finish
    if (savedBrowserForDomain != null && incomingUrl != null) {
        LaunchedEffect(Unit) {
            val intent = Intent(Intent.ACTION_VIEW, incomingUrl)
                .setPackage(savedBrowserForDomain)
            context.startActivity(intent)
            (context as? ComponentActivity)?.finish()
        }

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "Opening in your saved browser for $domain...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
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
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        
        Image(
            painter = painterResource(id = R.drawable.witch),
            contentDescription = "App Logo",
            modifier = Modifier.size(80.dp)
        )

        Text(
            text = "Which Browser?",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(browserList) { browser ->
                BrowserRow(
                    browser = browser,
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, incomingUrl)
                            .setPackage(browser.packageName)

                        if (rememberChoice) {
                            // Save for this specific domain
                            prefs.edit().putString("domain_$domain", browser.packageName).apply()
                            Toast.makeText(
                                context,
                                "✓ Saved ${browser.label} for $domain",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                context,
                                "Opened in ${browser.label} (not saved for $domain)",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        context.startActivity(intent)
                        (context as? ComponentActivity)?.finish()

                    }
                )
            }
        }
        if (incomingUrl != null) {
            val urlString = incomingUrl.toString()
            val displayedUrl = if (urlString.length > 150) urlString.take(150) + "..." else urlString
            Text(
                text = displayedUrl,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.height(100.dp))

        // Remember choice checkbox - always starts unchecked
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
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

//        Text(
//            text = "Default is always “Do not remember” for each domain",
//            style = MaterialTheme.typography.bodySmall,
//            color = MaterialTheme.colorScheme.onSurfaceVariant,
//            textAlign = TextAlign.Center
//        )
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Image(
            painter = rememberAsyncImagePainter(browser.icon),
            contentDescription = browser.label,
            modifier = Modifier.size(48.dp)
        )
        Text(
            text = browser.label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun getFallbackIcon(context: android.content.Context): Drawable {
    return ContextCompat.getDrawable(context, android.R.drawable.ic_menu_compass)!!
}

private fun getIconForPackage(context: android.content.Context, packageName: String): Drawable {
    return try {
        context.packageManager.getApplicationIcon(packageName)
    } catch (e: PackageManager.NameNotFoundException) {
        ContextCompat.getDrawable(context, android.R.drawable.ic_menu_compass)!!
    }
}