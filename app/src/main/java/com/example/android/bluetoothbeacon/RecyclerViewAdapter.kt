package com.example.android.bluetoothbeacon

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class RecyclerViewAdapter(val bluetoothDevices: ArrayList<BeaconDataModel>, val context: Context) : RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.recycler_layout, parent, false))
    }

    override fun getItemCount() = bluetoothDevices.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.addressText.text = "Address: ${bluetoothDevices[position].bluetoothAddress}"
        holder.distanceText.text = "${bluetoothDevices[position].distanceString} meters away"
        holder.serviceUuid.text = "Uuid: ${bluetoothDevices[position].serviceUuid}"
        holder.rssiText.text = "Rssi: ${bluetoothDevices[position].rssi}"

    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val addressText = view.findViewById<TextView>(R.id.bluetooth_address)
        val serviceUuid = view.findViewById<TextView>(R.id.serviceuuid)
        val rssiText = view.findViewById<TextView>(R.id.rssi_text)
        val distanceText = view.findViewById<TextView>(R.id.distance_text)
    }
}