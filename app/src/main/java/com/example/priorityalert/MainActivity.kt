package com.example.priorityalert

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.priorityalert.ui.theme.PriorityAlertTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PriorityAlertTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PriorityAlertScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun PriorityAlertScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val priorityAlertManager = remember { PriorityAlertManager(context) }
    val focusManager = LocalFocusManager.current

    var enabled by remember { mutableStateOf(priorityAlertManager.getEnabled()) }
    var keyword by remember { mutableStateOf(priorityAlertManager.getKeyword() ?: "") }
    var selectedContactNumbers by remember { mutableStateOf(priorityAlertManager.getContacts()) }
    var selectedRingtone by remember { mutableStateOf<Uri?>(priorityAlertManager.getRingtone()) }

    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact(),
        onResult = { uri: Uri? ->
            uri?.let {
                var contactId: String? = null
                val projection = arrayOf(ContactsContract.Contacts._ID, ContactsContract.Contacts.HAS_PHONE_NUMBER)
                context.contentResolver.query(it, projection, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val idIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID)
                        val hasPhoneIndex = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)
                        if (idIndex != -1 && hasPhoneIndex != -1) {
                            if (cursor.getInt(hasPhoneIndex) > 0) {
                                contactId = cursor.getString(idIndex)
                            }
                        }
                    }
                }

                contactId?.let { id ->
                    context.contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        arrayOf(id),
                        null
                    )?.use { phoneCursor ->
                        if (phoneCursor.moveToFirst()) {
                            val numberIndex = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                            if (numberIndex != -1) {
                                val newContact = phoneCursor.getString(numberIndex)
                                val updatedContacts = selectedContactNumbers + newContact
                                selectedContactNumbers = updatedContacts
                                priorityAlertManager.saveContacts(updatedContacts)
                                Toast.makeText(context, "Contact Added", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    )

    val ringtonePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
                }
                selectedRingtone = uri
                uri?.let {
                    priorityAlertManager.saveRingtone(it)
                    Toast.makeText(context, "Ringtone Saved", Toast.LENGTH_SHORT).show()
                }
            }
        }
    )

    val permissions = arrayOf(
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.RECEIVE_SMS
    )
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* TODO: Handle permission results */ }

    LaunchedEffect(Unit) {
        launcher.launch(permissions)
    }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Enabled")
            Spacer(modifier = Modifier.weight(1f))
            Switch(
                checked = enabled,
                onCheckedChange = {
                    enabled = it
                    priorityAlertManager.saveEnabled(it)
                }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { contactPickerLauncher.launch(null) }) {
            Text("Add Contact")
        }
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(selectedContactNumbers.toList()) { contact ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        text = contact,
                        modifier = Modifier.weight(1f)
                    )
                    Button(onClick = {
                        val updatedContacts = selectedContactNumbers - contact
                        selectedContactNumbers = updatedContacts
                        priorityAlertManager.saveContacts(updatedContacts)
                        Toast.makeText(context, "Contact Removed", Toast.LENGTH_SHORT).show()
                    }) {
                        Text("Remove")
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL)
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Ringtone")
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, selectedRingtone)
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
            ringtonePickerLauncher.launch(intent)
        }) {
            Text(selectedRingtone?.let { getRingtoneName(context, it) } ?: "Select Ringtone")
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = keyword,
            onValueChange = {
                keyword = it
                priorityAlertManager.saveKeyword(it.trim())
            },
            label = { Text("Keywords") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            val intent = Intent(context, AlertActivity::class.java).apply {
                putExtra("sender", "Test")
            }
            context.startActivity(intent)
        }) {
            Text("Test Alert")
        }
    }
}

fun getRingtoneName(context: Context, uri: Uri): String? {
    return RingtoneManager.getRingtone(context, uri)?.getTitle(context)
}

@Preview(showBackground = true)
@Composable
fun PriorityAlertScreenPreview() {
    PriorityAlertTheme {
        PriorityAlertScreen()
    }
}
