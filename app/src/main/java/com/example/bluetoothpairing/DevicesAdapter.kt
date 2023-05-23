package com.example.bluetoothpairing

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.registerReceiver
import androidx.recyclerview.widget.RecyclerView


class DevicesAdapter (private val discoveredDevices: List<BlutoothDeviceModel>, val listener: View.OnClickListener): RecyclerView.Adapter<DevicesAdapter.DeviceViewHolder>() {

     class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.device_name_text_view)
        val addressTextView: TextView = itemView.findViewById(R.id.device_address_text_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_device, parent, false)
        return DeviceViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = discoveredDevices[position]
//        if (ActivityCompat.checkSelfPermission( this., Manifest.permission.BLUETOOTH_SCAN
//            ) == PackageManager.PERMISSION_GRANTED) {
//        }
        val deviceName =  device.deviceName
        val deviceAddress = device.deviceAddress
        Log.e("this",discoveredDevices[position].toString())
        if ( device.deviceName != null ){
            holder.nameTextView.text = "Device Name: $deviceName"
            holder.addressTextView.text = "Device Address: $deviceAddress"
            holder.itemView.setOnClickListener {
                //pairWithDevice(device)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun pairWithDevice(device: BluetoothDevice) {
        val bondState = device.bondState
        when (bondState) {
            BluetoothDevice.BOND_BONDED -> {
               // Toast.makeText(this, "Device is already paired", Toast.LENGTH_SHORT).show()
            }
            BluetoothDevice.BOND_NONE -> {
                val bondRequestIntent = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
               // registerReceiver(bondRequestReceiver, bondRequestIntent)
                device.createBond()
            }
        }
    }

    override fun getItemCount(): Int {
        return discoveredDevices.size
    }
    private val bondRequestReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED == action) {
                val device =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                val bondState = device?.bondState
                when (bondState) {
                    BluetoothDevice.BOND_BONDING -> {
                        Toast.makeText(context, "Pairing in progress...", Toast.LENGTH_SHORT).show()
                    }
                    BluetoothDevice.BOND_BONDED -> {
                        Toast.makeText(context, "Pairing successful", Toast.LENGTH_SHORT).show()
                    }
                    BluetoothDevice.BOND_NONE -> {
                        Toast.makeText(context, "Pairing failed", Toast.LENGTH_SHORT).show()
                    }
                }
                //unregisterReceiver(this)
            }
        }
    }
}