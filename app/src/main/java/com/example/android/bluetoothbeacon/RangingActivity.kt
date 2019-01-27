package com.example.android.bluetoothbeacon

import android.app.Activity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_ranging.*
import org.altbeacon.beacon.*

class RangingActivity : Activity(), BeaconConsumer {

    private val beaconManager = BeaconManager.getInstanceForApplication(this)
    private var beaconsList = mutableListOf<BeaconDataModel>()
    private lateinit var mAdapter: RecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ranging)
        no_beacons_found.visibility = View.GONE
        mAdapter = RecyclerViewAdapter(beaconsList as ArrayList<BeaconDataModel>, this)
        recycler_view.adapter = mAdapter
        recycler_view.layoutManager = LinearLayoutManager(this)
    }


    override fun onResume() {
        super.onResume()
        beaconManager.beaconParsers.add(BeaconParser().setBeaconLayout(BeaconParser.ALTBEACON_LAYOUT))
        beaconManager.beaconParsers.add(BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_TLM_LAYOUT))
        beaconManager.beaconParsers.add(BeaconParser().setBeaconLayout(BeaconParser.URI_BEACON_LAYOUT))
        beaconManager.beaconParsers.add(BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT))
        beaconManager.beaconParsers.add(BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_URL_LAYOUT))
        beaconManager.beaconParsers.clear()
        beaconManager.beaconParsers.add(BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"))
        beaconManager.bind(this)
    }

    override fun onPause() {
        super.onPause()
        beaconManager.unbind(this)
    }

    override fun onBeaconServiceConnect() {

        val rangeNotifier = RangeNotifier { beacons: Collection<Beacon>, _: Region ->
            if (beaconsList.size == 0) {
                no_beacons_found.visibility = View.VISIBLE
            } else {
                no_beacons_found.visibility = View.GONE
            }
            if (beacons.isNotEmpty()) {
                no_beacons_found.visibility = View.GONE
                for (beacon in beacons) {
                    addBeaconToList(beacon)
                }
            }

        }

        try {
            beaconManager.startRangingBeaconsInRegion(Region("myRangingUniqueId", null, null, null))
            beaconManager.addRangeNotifier(rangeNotifier)
            beaconManager.startRangingBeaconsInRegion(Region("myRangingUniqueId", null, null, null))
            beaconManager.addRangeNotifier(rangeNotifier)
        } catch (e: RuntimeException) {

        }

    }

    private fun addBeaconToList(beacon: Beacon) {
        val bluetoothAddress = if (beacon.bluetoothAddress != null) {
            beacon.bluetoothAddress
        } else {
            "Not Available"
        }

        val serviceUuid = if (beacon.serviceUuid != 0) {
            beacon.toString()
        } else {
            "Not Available"
        }

        val rssi = if (beacon.rssi != 0) {
            beacon.rssi.toString()
        } else {
            "Not Available"
        }

        val distance = if (beacon.distance != 0.0) {
            beacon.distance.toString()
        } else {
            "Not Available"
        }

        val beaconDataModel = BeaconDataModel(bluetoothAddress, serviceUuid, rssi, distance)
        addMessageToFirebase(beaconDataModel)
        var exists = false
        for (beacons in beaconsList) {
            if (beacons.bluetoothAddress == beaconDataModel.bluetoothAddress) {
                exists = true
                break
            }
        }
        if (!exists) {
            beaconsList.add(beaconDataModel)
            mAdapter.notifyDataSetChanged()
        } else {
            for (beacons in beaconsList) {
                if (beacons.bluetoothAddress == beaconDataModel.bluetoothAddress) {
                    beaconsList.remove(beacons)
                    beaconsList.add(beaconDataModel)
                    mAdapter.notifyDataSetChanged()
                }
            }
        }

    }

    private fun addMessageToFirebase(beaconDataModel: BeaconDataModel) {
        val database = FirebaseDatabase.getInstance()
        val reference = database.getReference("Beacons").child(beaconDataModel.bluetoothAddress)
        reference.setValue(beaconDataModel)
    }
}
