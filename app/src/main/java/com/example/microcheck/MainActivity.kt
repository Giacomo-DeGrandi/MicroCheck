package com.example.microcheck

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {

    private lateinit var resultLauncher: ActivityResultLauncher<Intent>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val startIntent = Intent(this, MyVpnService::class.java).apply {
                    action = MyVpnService.ACTION_START
                }
                startService(startIntent)
            }
        }

        setContent {
            MainScreen()
        }
    }

    private fun startVpnService() {
        val intent = VpnService.prepare(this)
        if (intent != null) {
            resultLauncher.launch(intent)
        } else {
            val startIntent = Intent(this, MyVpnService::class.java).apply {
                action = MyVpnService.ACTION_START
            }
            startService(startIntent)
        }
    }

    companion object {
    }

    @Composable
    fun MainScreen() {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.weight(1f))
            ShowHideTextApp()
        }
    }

    @Composable
    fun ShowHideTextApp() {
        // This state holds whether the VPN is running or not.
        var vpnActive by remember { mutableStateOf(false) }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Button(onClick = {
                vpnActive = !vpnActive
                if (vpnActive) {
                    startVpnService()
                } else {
                    val stopIntent = Intent(this@MainActivity, MyVpnService::class.java).apply {
                        action = MyVpnService.ACTION_STOP
                    }
                    startService(stopIntent)
                }
            }) {
                if (vpnActive) {
                    Text("Stop VPN")
                } else {
                    Text("Start VPN")
                }
            }
        }
    }


    @Preview(showBackground = true)
    @Composable
    fun PreviewShowHideTextApp() {
        ShowHideTextApp()
    }
}