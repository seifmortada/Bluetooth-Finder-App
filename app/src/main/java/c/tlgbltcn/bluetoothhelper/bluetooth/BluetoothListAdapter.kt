package c.tlgbltcn.bluetoothhelper.bluetooth

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import c.tlgbltcn.bluetoothhelper.databinding.ItemBluetoothDeviceBinding
import java.util.*


class BluetoothListAdapter(private var item: ArrayList<BluetoothDeviceModel>) :
    RecyclerView.Adapter<BluetoothListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemBluetoothDeviceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount() = item.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(
            item[position].name ?: "",
            item[position].macNumber ?: "",
            item[position].rssi ?: "",
            item[position].distance ?: ""
        )
    }

    class ViewHolder(val view: ItemBluetoothDeviceBinding) : RecyclerView.ViewHolder(view.root) {
        fun bind(name: String, macNumber: String, rssi: String, distace: String) {
            view.deviceName.text = name
            view.macAddress.text = macNumber
            view.rssi.text = rssi
            view.distance.text = "$distace m"
        }
    }
}