package com.example.microcheck

import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
import java.io.FileInputStream
import java.io.FileOutputStream

class MyVpnService : VpnService() {
    private var mInterface: ParcelFileDescriptor? = null
    private var packetHandlingThread: Thread? = null
    private val packetBuffer = ByteArray(32767)
    private var vpnInputStream: FileInputStream? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                mInterface = establishVpnConnection()
                vpnInputStream = FileInputStream(mInterface?.fileDescriptor)
                startPacketHandling()
            }
            ACTION_STOP -> {
                stopPacketHandling()
                mInterface?.close()
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun startPacketHandling() {
        packetHandlingThread = Thread {
            handlePackets()
        }
        packetHandlingThread?.start()
    }

    private fun stopPacketHandling() {
        packetHandlingThread?.interrupt()
        packetHandlingThread = null
        vpnInputStream?.close()
    }

    private fun handlePackets() {
        val output = FileOutputStream(mInterface?.fileDescriptor)

        while (!Thread.currentThread().isInterrupted) {
            val length = vpnInputStream?.read(packetBuffer) ?: break
            if (length > 0) {
                val packet = packetBuffer.copyOfRange(0, length)

                // Write the packet back to the VPN interface
                output.write(packet)

                //parsePacket(packet)
                //logPacketData(packet)
                //savePacketToPcap(packet)
            }
        }
        output.close()
    }


    private fun establishVpnConnection(): ParcelFileDescriptor? {
        val builder = Builder()
            .setSession("MicroCheck")
            .setMtu(1500)
            .addAddress("10.0.0.1", 24)
            .addRoute("0.0.0.0", 0)
        return builder.establish()
    }

    override fun onDestroy() {
        super.onDestroy()
        mInterface?.close()
        vpnInputStream?.close()
    }

    companion object {
        const val ACTION_START = "com.example.MicroCheck.START"
        const val ACTION_STOP = "com.example.MicroCheck.STOP"
    }
}
