package com.example.priorityalert

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.priorityalert.ui.theme.PriorityAlertTheme

class AboutActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PriorityAlertTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("About Priority Alert") },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    AboutScreenContent(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun AboutScreenContent(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "How It Works",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
        item {
            Text(
                text = "Priority Alert helps ensure you never miss an urgent message. When you receive an SMS from a selected contact that contains your chosen trigger phrase, the app will trigger a loud, full-screen alert, even if your phone is on silent."
            )
        }
        item {
            Text(
                text = "Setup Guide",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("1. Grant Permissions: The app needs permission to read contacts and receive SMS messages. You will be prompted for these when you first launch the app.")
                Text("2. Enable the Service: Use the 'Enabled' switch on the main screen to turn the monitoring service on or off.")
                Text("3. Add Contacts: Tap 'Add Contact' to select one or more contacts from your address book whose messages you want to monitor.")
                Text("4. Select a Ringtone: Choose a distinct ringtone that will play for priority alerts.")
                Text("5. Set Trigger Phrase: Enter the exact phrase you want the app to look for in incoming messages from your priority contacts (e.g., 'urgent, emergency').")
            }
        }
        item {
            Text(
                text = "Testing",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        item {
            Text(
                text = "Use the 'Test Alert' button on the main screen to see how the alert looks and sounds without needing to receive an actual SMS."
            )
        }
    }
}
