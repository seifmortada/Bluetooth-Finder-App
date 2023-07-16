package c.tlgbltcn.library

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast




/**
 * Created by tolga bolatcan on 24.01.2019
 */
abstract class BluetoothDeviceFounderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        val action = intent?.action

        if (action == BluetoothDevice.ACTION_FOUND) {
            val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
            val rssi =
                intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MAX_VALUE).toString()

            getFoundDevices(device,rssi)

            }
        }



    abstract fun getFoundDevices(device: BluetoothDevice?,rssi:String)
}