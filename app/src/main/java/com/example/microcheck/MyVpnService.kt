package com.example.microcheck
// package keyword defines the namespace for our Kotlin file

import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
import java.io.FileInputStream
import java.io.IOException


// MyVpnService is a subclass of VpnService, inheriting its behaviors and properties.
// VpnService is a part of the Android SDK, enabling apps to create their VPNs
class MyVpnService : VpnService() {

    // -vpnThread: A Thread is a separate "path of execution." Think of it like a mini-program running
    // inside your program. Here, it's used to read data from the VPN without freezing your app.
    // -vpnInterface: This represents the VPN connection itself. You can use this to interact with the VPN.
    // -vpnInputStream: This lets you read data going through the VPN.
    private var vpnThread: Thread? = null
    private var vpnInterface: ParcelFileDescriptor? = null
    private var vpnInputStream: FileInputStream? = null

    // we override the onStartCommand method with the same name from the parent class (VpnService)
    // Parameters:
    // - intent: Intent?: This carries information about the operation to be performed. The ? indicates this parameter can be null.
    // - flags: Int: Provides additional data about this start request.
    // - startId: Int: A unique integer representing this specific request to start.
    // The : Int after the parameter list indicates that this function returns an integer.
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        // The val keyword defines a read-only variable.
        // Builder() is a method of the parent class VpnService,
        // which provides a builder to create VPN interfaces.
        val builder = Builder()
            // These methods configure the VPN connection. For example, .setSession("MicroCheck")
            // names the VPN session "MicroCheck", and .addDnsServer("8.8.8.8") sets the DNS server
            // to Google's public DNS.
            .setSession("MicroCheck")
            .setMtu(1500)
            .addAddress("10.0.0.1", 24)
            .addRoute("0.0.0.0", 0)
            .addDnsServer("8.8.8.8")

        // This calls the establish() method on our builder, which establishes the VPN
        // and returns a ParcelFileDescriptor object. This object represents the VPN interface.
        val vpnInterface = builder.establish()


        // Here, we create an input stream to read data packets flowing through the VPN.
        // The ? is Kotlin's safe call operator; it calls the method on vpnInterface only
        // if vpnInterface is not null.
        val vpnInputStream = FileInputStream(vpnInterface?.fileDescriptor)

        // This initializes a new thread. In Android, heavy tasks (like network operations)
        // are offloaded to background threads to prevent freezing the UI.
        // lambda expression (anonymous function)
        vpnThread = Thread {
            try {
                // A byte array named buffer is initialized to store data packets read from the VPN.
                // The size is set to 32,768 bytes (or 32 KB), which is a common choice for a network buffer.
                val buffer = ByteArray(32768)
                // Since the aim is to continuously monitor network traffic, an infinite loop ensures
                // that the reading process doesn't stop after just one packet.
                while (true) {
                    // This reads data packets from the VPN and stores them in the buffer.
                    // The number of bytes read is stored in the length variable
                    val length = vpnInputStream.read(buffer)
                    // If read() returns -1, it signifies the end of the stream, i.e.,
                    // there's no more data to read. Thus, the loop breaks.
                    if (length == -1) break
                    Log.d("VPNService", "Packet received with length: $length")
                }
            //  Instead of letting the whole thread or app crash due to unforeseen errors,
            //  catching exceptions allows the service to fail gracefully. Printing the
            //  stack trace aids in debugging the root cause of the error.
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        // start the thread
        vpnThread?.start()

        // The method returns START_STICKY, an Android constant indicating that the service
        // should remain running even if the component (e.g., an activity)
        // that started it is destroyed.
        // This choice ensures the VPN service keeps running, even if the app faces certain
        // disruptions, ensuring uninterrupted VPN functionality.
        return START_STICKY
    }

    override fun onDestroy() {
        // This calls the onDestroy method of the superclass (VpnService). It's a good practice to
        // call the superclass implementation when overriding lifecycle methods to ensure all
        // base functionalities are executed.
        super.onDestroy()
        vpnThread?.interrupt()  // Interrupt the thread.
        vpnInputStream?.close()  // Close the VPN input stream.

        // This code attempts to close the vpnInterface. If there's an issue (like an IOException),
        // the exception is caught and its stack trace printed.
        try {
            vpnInterface?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    // This method can be used to stop the VPN service from within.
    // stopSelf is a method of the Android Service class that stops the service from running.
    fun stopMyVpnService() {
        stopSelf()
    }
}
