package com.example.microcheck

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.microcheck.ui.theme.MicroCheckTheme
import android.content.Intent
import android.net.VpnService
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Intent?>
    private var isMonitoring: Boolean = false // to track the monitoring state

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                startService(Intent(this, MyVpnService::class.java))
            } else {
                Toast.makeText(this, "VPN permission denied.", Toast.LENGTH_SHORT).show()
            }
        }

        setContent {
            AppUI { toggleVpnService() }
        }
    }

    private fun requestVpnPermission() {
        VpnService.prepare(this)?.let {
            requestPermissionLauncher.launch(it)
        } ?: startService(Intent(this, MyVpnService::class.java))
    }
    private fun toggleVpnService() {
        if (isMonitoring) {
            stopService(Intent(this, MyVpnService::class.java))
            isMonitoring = false
        } else {
            requestVpnPermission()
        }
    }

}

@Composable
fun AppUI(isMonitoring: Boolean, onButtonClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "MicroCheck", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Check for potential threats by monitoring network traffic.")
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onButtonClick) {
            Text(text = if (isMonitoring) "Stop Monitoring" else "Start Monitoring")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewAppUI() {
    MicroCheckTheme {
        AppUI(isMonitoring = false) {}
    }
}


