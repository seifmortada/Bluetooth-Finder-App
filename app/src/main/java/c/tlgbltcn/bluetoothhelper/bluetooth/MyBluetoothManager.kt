package c.tlgbltcn.bluetoothhelper.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import java.io.IOException
import java.util.*

class MyBluetoothManager(private val deviceAddress: String) {
    private val uuid: UUID =
        UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // Standard UUID for SPP (Serial Port Profile)
    private var socket: BluetoothSocket? = null

    @SuppressLint("MissingPermission")
    fun connect(): Boolean {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        val device: BluetoothDevice? = adapter.getRemoteDevice(deviceAddress)

        try {
            socket = device?.createRfcommSocketToServiceRecord(uuid)
            socket?.connect()
            return true
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
    }

    fun disconnect() {
        try {
            socket?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun sendData(data: String) {
        try {
            val outputStream = socket?.outputStream
            outputStream?.write(data.toByteArray())

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
