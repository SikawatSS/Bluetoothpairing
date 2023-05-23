package com.example.bluetoothpairing

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class MainActivity : AppCompatActivity() {
    private val REQUEST_PERMISSION_BLUETOOTH = 1
    private val REQUEST_ENABLE_BT = 1
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var discoveredDevices: MutableList<BluetoothDevice>
    private lateinit var deviceReceiver: BroadcastReceiver
    private lateinit var devicesTextView: TextView
    private lateinit var devicesRecyclerView: RecyclerView
    private lateinit var devicesAdapter: DevicesAdapter
    var checkOnOffState : Boolean = true
    val BLUETOOTH_PERMISSIONS_S =
        arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        discoveredDevices = mutableListOf()
        val searchDevice: Button = findViewById(R.id.searchDevice)
        searchDevice.setOnClickListener {
            checkBluetoothPermissions()
            //getBluetoothDeviceAddress()
            discoveredDevices.clear()
        }

        devicesTextView = findViewById(R.id.deviceView)
        devicesRecyclerView = findViewById(R.id.devices_recycler_view)
        devicesAdapter = DevicesAdapter()
        devicesRecyclerView.adapter = devicesAdapter
        devicesRecyclerView.layoutManager = LinearLayoutManager(this)

        val openBluetoothButton: Button = findViewById(R.id.openBluetooth)
        openBluetoothButton.setOnClickListener {
            openBluetooth()
        }

        deviceReceiver = object : BroadcastReceiver() {
            @SuppressLint("MissingPermission")
            override fun onReceive(context: Context?, intent: Intent?) {
                val action = intent?.action
                if (BluetoothDevice.ACTION_FOUND == action) {
                    val device =
                        intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    device?.let {
                        var newit = it.name
                        var newit2 = it.name
//                        discoveredDevices.add(it)
//                        devicesAdapter.notifyDataSetChanged()
                        if (newit != null ){
                            if (discoveredDevices.filter { it.name == newit2 }.size == 1){

                            }else {
                                discoveredDevices.add(it)
                                devicesAdapter.notifyDataSetChanged()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun checkBluetoothPermissions() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_PERMISSION_BLUETOOTH
            )
        } else {
            startBluetoothDiscovery()
        }
    }
    private fun startBluetoothDiscovery() {
        if (bluetoothAdapter.isEnabled) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_SCAN
                ) == PackageManager.PERMISSION_GRANTED
            ) {
            }
            if (bluetoothAdapter.isDiscovering) {
                    bluetoothAdapter.cancelDiscovery()
                }
                discoveredDevices.clear()
                val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
                registerReceiver(deviceReceiver, filter)
                bluetoothAdapter.startDiscovery()
                //Toast.makeText(this, "Discovering Bluetooth devices...", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Please enable Bluetooth first", Toast.LENGTH_SHORT).show()
        }
    }
    private fun updateDeviceList() {
        val stringBuilder = StringBuilder()
        for (device in discoveredDevices) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED
            ) {
            }
            stringBuilder.append("Device Name: ${device.name}\n")
            stringBuilder.append("Device Address: ${device.address}\n")
            stringBuilder.append("\n")
        }
        devicesTextView.text = stringBuilder.toString()
    }
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(deviceReceiver)
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_BLUETOOTH) {
            //Toast.makeText(this, requestCode.toString(), Toast.LENGTH_SHORT).show()
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startBluetoothDiscovery()
            } else {
                Toast.makeText(
                    this,
                    "Location permission is required to discover Bluetooth devices",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    inner class DevicesAdapter : RecyclerView.Adapter<DevicesAdapter.DeviceViewHolder>() {

        inner class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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
            if (ActivityCompat.checkSelfPermission( this@MainActivity, Manifest.permission.BLUETOOTH_SCAN
                ) == PackageManager.PERMISSION_GRANTED) {
            }
            val deviceName =  device.name
            val deviceAddress = device.address
            Log.e("this",discoveredDevices[position].toString())
               holder.nameTextView.text = "Device Name: $deviceName"
               holder.addressTextView.text = "Device Address: $deviceAddress"
               holder.itemView.setOnClickListener {
                   pairWithDevice(device)
               }
        }

        override fun getItemCount(): Int {
            return discoveredDevices.size
        }
    }
    @SuppressLint("MissingPermission")
    private fun pairWithDevice(device: BluetoothDevice) {
        val bondState = device.bondState
        when (bondState) {
            BluetoothDevice.BOND_BONDED -> {
                Toast.makeText(
                    this,
                    "Device is already paired",
                    Toast.LENGTH_SHORT
                ).show()
            }
            BluetoothDevice.BOND_NONE -> {
                val bondRequestIntent = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
                registerReceiver(bondRequestReceiver, bondRequestIntent)
                device.createBond()
            }
        }
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
                        Toast.makeText(
                            this@MainActivity,
                            "Pairing in progress...",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    BluetoothDevice.BOND_BONDED -> {
                        Toast.makeText(
                            this@MainActivity,
                            "Pairing successful",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    BluetoothDevice.BOND_NONE -> {
                        Toast.makeText(
                            this@MainActivity,
                            "Pairing failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                unregisterReceiver(this)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun openBluetooth() {
//        if (ContextCompat.checkSelfPermission(
//                this@MainActivity,
//                Manifest.permission.BLUETOOTH_CONNECT
//            ) != PackageManager.PERMISSION_DENIED
//        ) {
//            if (checkOnOffState == false)
//                {
//                    bluetoothAdapter.disable()
//                    checkOnOffState = true
//                }
//            else{
//                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
//                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
//                checkOnOffState = false
//
//            }
//        }
        if (checkOnOffState == false)
        {
            bluetoothAdapter.disable()
            checkOnOffState = true
        }
        else{
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            checkOnOffState = false

        }
    }
    private fun getBluetoothDeviceAddress() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
         Toast.makeText(this,bluetoothAdapter.address.toString(),Toast.LENGTH_LONG).show()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                // Bluetooth is enabled by the user
                // Perform your desired Bluetooth operations here
            } else {
                Toast.makeText(this, "Bluetooth is required for device discovery", Toast.LENGTH_SHORT).show()

                // The user denied enabling Bluetooth
                // Handle the situation accordingly
            }
        }
    }


}


