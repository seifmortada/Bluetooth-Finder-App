package c.tlgbltcn.bluetoothhelper

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import c.tlgbltcn.bluetoothhelper.bluetooth.BluetoothDeviceModel
import c.tlgbltcn.bluetoothhelper.bluetooth.BluetoothListAdapter
import c.tlgbltcn.library.BluetoothHelper
import c.tlgbltcn.library.BluetoothHelperListener
import android.view.View
import android.view.Window
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import c.tlgbltcn.bluetoothhelper.bluetooth.MyBluetoothManager
import c.tlgbltcn.bluetoothhelper.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import java.lang.Runnable
import java.time.LocalDateTime
import kotlin.collections.ArrayList

@Suppress("DEPRECATION")
@SuppressLint("SetTextI18n", "MissingPermission")
class MainActivity : AppCompatActivity(), BluetoothHelperListener {
    companion object {
        private const val TAG = "MainActivity"
    }

    //esp MacAddress
    private val espAddress = "CC:DB:A7:15:72:FA"

    // getting the current time
    @RequiresApi(Build.VERSION_CODES.O)
    val currentTime = LocalDateTime.now()

    //BLUETOOTH VARIABLES
    private lateinit var bluetoothHelper: BluetoothHelper
    private lateinit var binding: ActivityMainBinding
    private lateinit var handler: Handler
    private lateinit var refreshRunnable: Runnable
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothManager: MyBluetoothManager


    private val xyObservable = ValueObservable<Pair<Double, Double>>()

    private lateinit var layout: ConstraintLayout
    private var itemList = ArrayList<BluetoothDeviceModel>()

    private var distanceFromB1: Double = 0.0
    private var distanceFromB2: Double = 0.0
    private var distanceFromB3: Double = 0.0

    private var distance1Check: Double = 0.0
    private var distance2Check: Double = 0.0
    private var distance3Check: Double = 0.0

    private var x: Double = 0.0
    private var y: Double = 0.0


    private var deviceName = ""
    private val methods = Methods()
    private val coroutineScope = lifecycleScope // or viewModelScope if using ViewModel

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n", "MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Hiding Navigation bar
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION

        // Initializing the binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root

        //Set the view to the layout
        setContentView(view)
        layout = findViewById(R.id.constraintLayout2)

        // Initializing the bluetoothHelper Library , The Bluetooth Manager and the Bluetooth Adapter
        bluetoothHelper =
            BluetoothHelper(this@MainActivity, this@MainActivity).setPermissionRequired(true)
                .create()

        bluetoothManager = MyBluetoothManager(espAddress)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        //Handler to schedule and run code on a specific thread
        handler = Handler()

        //refreshRunnable for the schedule of bluetooth scanning processes
        refreshRunnable = object : Runnable {
            override fun run() {

                if (bluetoothHelper.isBluetoothScanning()) {
                    bluetoothHelper.stopDiscovery()
                } else {
                    bluetoothHelper.startDiscovery()
                }
                // Schedule next click after 1 second
                handler.postDelayed(this, 1000)

            }

        }

        // Check the current state of Bluetooth and update the Switch state
        if (bluetoothAdapter.isEnabled) {
            binding.enableDisableSwitch.isChecked = true
            binding.blueState.text = "Bluetooth State On"
            handler.post(refreshRunnable)
        } else {

            binding.enableDisableSwitch.isChecked = false
            binding.blueState.text = "Bluetooth State Off"
            Toast.makeText(this@MainActivity, "Please enable bluetooth", Toast.LENGTH_LONG).show()
        }


        //BINDING
        xyObservable.addObserver { (newX, newY) ->
            sendToEsp(currentTime, deviceName, newX, newY)
            binding.numX.text = newX.toString()
            binding.numY.text = newY.toString()
            binding.progressBar.visibility = View.GONE

        }
        binding.enableDisableSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                // Turn on Bluetooth
                bluetoothAdapter.enable()
                binding.blueState.text = "Bluetooth State Off"
                handler.post(refreshRunnable)


            } else {
                // Turn off Bluetooth
                bluetoothAdapter.disable()
                binding.blueState.text = "Bluetooth State On"

            }
        }
        deviceName = bluetoothAdapter.name
        binding.deviceName2.text = deviceName
        viewAdapter = BluetoothListAdapter(itemList)

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = viewAdapter
        }
    }

    //The Formatted time which will be send with the XY values to the esp
    @RequiresApi(Build.VERSION_CODES.O)
    fun formatTime(): String {
        val now = LocalDateTime.now()
        val year = now.year
        val months = now.month.value
        val days = now.dayOfMonth
        var hours = now.hour
        val minutes = now.minute
        val seconds = now.second
        var hours2 = 0
        if (hours > 12) {
            hours2 = hours.minus(12)
            return "$year/$months/$days/$hours2:$minutes:$seconds"
        } else {
            return "$year/$months/$days/$hours:$minutes:$seconds"
        }

    }


    //Method of filtering the devices according to it's rssi signal and device name from the recyclerView
    // , then calculates the distance using trilateration method
    @SuppressLint("NotifyDataSetChanged")
    override fun getBluetoothDeviceList(device: BluetoothDevice?, rssi: String) {
        val distance = methods.calculateDistance(rssi.toInt())

        if (distance < 4) {
            when (device?.address) {
                "C4:F3:12:11:82:ED" -> {
                    itemList.add(
                        BluetoothDeviceModel(
                            "Device Name :" + device.name,
                            "Device Mac : " + device.address,
                            "Distance : $distance "
                        )
                    )
                    viewAdapter.notifyDataSetChanged()
                    val distance1 = methods.calculateDistance(rssi.toInt())
                    distanceFromB1 = distance1
                    distance1Check = distance1
                    binding.pos1.text = "$distance"
                    Log.i(TAG, "Device name: ${device.address}  RSSI: $rssi  Distance:$distance")
                }
                "B4:52:A9:1B:4F:E6" -> {
                    itemList.add(
                        BluetoothDeviceModel(
                            "Device Name :" + device.name,
                            "Device Mac : " + device.address,
                            "Distance : $distance "
                        )
                    )
                    viewAdapter.notifyDataSetChanged()
                    val distance2 = methods.calculateDistance(rssi.toInt())
                    distanceFromB2 = distance2
                    distance2Check = distance2
                    binding.pos2.text = "$distance"

                }
                "50:8C:B1:80:AD:42" -> {
                    itemList.add(
                        BluetoothDeviceModel(
                            "Device Name :" + device.name,
                            "Device Mac : " + device.address,
                            "Distance : $distance "
                        )
                    )
                    viewAdapter.notifyDataSetChanged()
                    val distance3 = methods.calculateDistance(rssi.toInt())
                    distanceFromB3 = distance3
                    distance3Check = distance3
                    binding.pos3.text = "$distance"
                    Log.i(TAG, "Device name: ${device.address}  RSSI: $rssi  Distance:$distance")

                }

            }
            if ((distance1Check == 0.0 || distance2Check == 0.0 || distance3Check == 0.0)) {
                Toast.makeText(this, "still refreshing", Toast.LENGTH_LONG).show()
            } else {
                x = methods.trilateration(distanceFromB1, distanceFromB2, distanceFromB3).first
                y = methods.trilateration(distanceFromB1, distanceFromB2, distanceFromB3).second
                xyObservable.setValue(x to y)
                1
                distance1Check = 0.0
                distance2Check = 0.0
                distance3Check = 0.0

                binding.pos1.text = ""
                binding.pos2.text = ""
                binding.pos3.text = ""
            }
        }
    }


    // Method of sending the values to the esp using the BluetoothManager class
    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendToEsp(time: LocalDateTime, deviceName: String, x: Double, y: Double) {
        coroutineScope.launch {
            try {
                if (bluetoothManager.connect()) {
                    Snackbar.make(layout, "Connected", Snackbar.LENGTH_SHORT).show()

                    val newTime = formatTime()
                    bluetoothManager.sendData("$deviceName,")
                    bluetoothManager.sendData("$x,")
                    bluetoothManager.sendData("$y,")
                    bluetoothManager.sendData("$newTime,")
                    Toast.makeText(this@MainActivity, "Sent successfully", Toast.LENGTH_LONG).show()

                    bluetoothManager.disconnect()
                } else {
                    Snackbar.make(layout, "Failed to connect", Snackbar.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // Handle any exceptions that occur during the coroutine execution
                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // This method is called when the Bluetooth discovery process starts,
    // It resets the distances from Beacon1, Beacon2, and Beacon3 to 0.0
    override fun onStartDiscovery() {
        distanceFromB1 = 0.0
        distanceFromB2 = 0.0
        distanceFromB3 = 0.0
    }

    // This method is called when the Bluetooth discovery process is finished. It clears the item list.
    override fun onFinishDiscovery() {
        itemList.clear()
    }

    // This method is called when the Bluetooth state is enabled. It updates the UI to indicate that the Bluetooth state is "On".
    override fun onEnabledBluetooth() {
        binding.blueState.text = "Bluetooth State On"

    }

    // This method is called when the Bluetooth state is disabled. It updates the UI to indicate that the Bluetooth state is "Off".
    override fun onDisabledBluetooh() {
        binding.blueState.text = "Bluetooth State Off"

    }

    // This method is called when the activity resumes. It calls the super method and registers for Bluetooth state change notifications.
    override fun onResume() {
        super.onResume()
        bluetoothHelper.registerBluetoothStateChanged()
    }

    // This method is called when the activity is stopped. It calls the super method and unregisters the Bluetooth state change notifications.
    override fun onStop() {
        super.onStop()
        bluetoothHelper.unregisterBluetoothStateChanged()
    }
}

