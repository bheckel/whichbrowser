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

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.rshdev.whichbrowser.ui.theme.WhichBrowserTheme

class MainActivity : ComponentActivity() {

    private var incomingUrlState = mutableStateOf<Uri?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // Reverting to direct intent data as requested
        incomingUrlState.value = intent?.data

        setContent {
            WhichBrowserTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val incomingUrl by incomingUrlState
                    
                    var currentScreen by remember(incomingUrl) { 
                        mutableStateOf(
                            if (incomingUrl != null) "chooser" else "settings"
                        ) 
                    }

                    if (currentScreen == "chooser") {
                        BrowserChooserScreen(
                            incomingUrl = incomingUrl,
                            onOpenSettings = { currentScreen = "settings" }
                        )
                    } else {
                        SettingsScreen(onBack = { 
                            if (incomingUrl != null) {
                                currentScreen = "chooser"
                            } else {
                                finish()
                            }
                        })
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        incomingUrlState.value = intent.data
    }
}
