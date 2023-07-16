package c.tlgbltcn.bluetoothhelper

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import kotlin.math.pow
import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.OutputStream
import java.util.*

class Methods {
    //VARIABLES
    private val TAG = "Methods"
    private val hcAddress =
        "00:22:06:01:96:BE" // Change this to the MAC address of your HC-05 module
    private val espAddress = "CC:DB:A7:15:72:FA"

    private lateinit var bluetoothDevice: BluetoothDevice
    private lateinit var outputStream: OutputStream
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothSocket: BluetoothSocket

    //METHODS
    fun calculateDistance(rssi: Int, txPower: Int = -59, nFactor: Double = 2.0): Double {
        return 10.0.pow((txPower - rssi) / (10 * nFactor))
    }

    fun trilateration(d1: Double, d2: Double, d3: Double): Pair<Double, Double> {
        var x = 0.0
        var y = 0.0
        val x1 = 0.0f
        val y1 = 0.0f
        val x2 = 4.0f
        val y2 = 0.0f
        val x3 = 2.0f
        val y3 = 4.0f
        val delta = 4 * ((x1 - x2) * (y1 - y3) - (x1 - x3) * (y1 - y2))
        val a: Double =
            d2.pow(2.0) - d1.pow(2.0) - x2.toDouble().pow(2.0) + x1.toDouble()
                .pow(2.0) - y2.toDouble().pow(2.0) + y1.toDouble().pow(2.0)
        val b: Double =
            d3.pow(2.0) - d1.pow(2.0) - x3.toDouble().pow(2.0) + x1.toDouble()
                .pow(2.0) - y3.toDouble().pow(2.0) + y1.toDouble().pow(2.0)

        x = 1 / delta * (2 * a * (y1 - y3) - 2 * b * (y1 - y2))
        y = 1 / delta * (2 * b * (x1 - x2) - 2 * a * (x1 - x3))
        return Pair(x, y)
    }

    @Suppress("DEPRECATION")
    fun getIpAddress(context: Context): String {
        val manager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val info = manager.connectionInfo.ipAddress
        return info.toString()
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    @SuppressLint("MissingPermission")
    suspend fun sendToArduino(x: Double, y: Double) = withContext(Dispatchers.IO)
    {
        // Get the BluetoothAdapter instance
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        // Get the BluetoothDevice instance for your HC-05 module
        bluetoothDevice =
            bluetoothAdapter.getRemoteDevice(hcAddress) // Replace with your device's address

        // Establish a Bluetooth connection with the device
        try {
            bluetoothSocket =
                bluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")) // Replace with your device's UUID
            bluetoothSocket.connect()
            if (bluetoothSocket.isConnected) {
                Log.i(TAG, "hc05 connected")
                outputStream = bluetoothSocket.outputStream
                // Send the message to the Arduino board
                try {
                    val messagex = "${x},"
                    val messagey = "${y}"
                    outputStream.write(messagex.toByteArray())
                    outputStream.write(messagey.toByteArray())

                    outputStream.close()
                    bluetoothSocket.close()

                    Log.i(TAG, "hc05 closed!!")

                    Log.i(TAG, "hc05 Done Sending!!")
//                    handler.postDelayed(refreshRunnable, 2000)

                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } else {
                Log.i(TAG, "hc05 NOT CONNECTED")
                return@withContext
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

}

