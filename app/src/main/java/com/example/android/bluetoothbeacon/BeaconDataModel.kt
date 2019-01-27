package com.example.android.bluetoothbeacon

data class BeaconDataModel(val bluetoothAddress:String,val serviceUuid:String,val rssi:String,val distanceString: String)
{
    constructor():this("","","","")
}