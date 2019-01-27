package com.example.android.bluetoothbeacon

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.altbeacon.beacon.BeaconManager


class MainActivity : AppCompatActivity() {
    companion object {
        const val PERMISSION_REQUEST_COARSE_LOCATION = 1
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkLocationPermission()
        verifyBluetooth()
        start_ranging.setOnClickListener { onRangingClicked() }
        disable_monitoring.setOnClickListener { onEnableClicked() }


    }

    private fun verifyBluetooth() {
        try {
            if (!BeaconManager.getInstanceForApplication(this).checkAvailability()) {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Bluetooth not enabled")
                builder.setMessage("Please enable bluetooth in settings and restart this application.")
                builder.setPositiveButton("OK") { _, _ -> finish() }
                builder.show()


            }
        } catch (e: RuntimeException) {

            val builder = AlertDialog.Builder(this)
            builder.setTitle("Bluetooth LE not available")
            builder.setMessage("Sorry, this device does not support Bluetooth LE")
            builder.setPositiveButton("OK") { _, _ -> finish() }
            builder.show()
        }
    }

    private fun onEnableClicked() {
        val application: BeaconReferenceApplication = this.applicationContext as BeaconReferenceApplication
        if (BeaconManager.getInstanceForApplication(this).monitoredRegions.isNotEmpty()) {
            application.disableMonitoring()
            disable_monitoring.text = "Re-Enable Monitoring"
        } else {
            disable_monitoring.text = "Disable Monitoring"
            application.enableMonitoring()
        }
    }

    override fun onResume() {
        super.onResume()
        val application = this.applicationContext as BeaconReferenceApplication
        application.setMonitoringActivity(this)
        updateLog(application.getLog())
    }

    override fun onPause() {
        super.onPause()
        (this.applicationContext as BeaconReferenceApplication).setMonitoringActivity(null)
    }

    private fun checkLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("This app needs location access")
                builder.setMessage("Please grant location access so this app can detect beacons.")
                builder.setPositiveButton("Ok") { _, _ ->
                    requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), PERMISSION_REQUEST_COARSE_LOCATION)
                }

                builder.show()


            } else {
                checkLocation()
            }
        }
    }

    private fun checkLocation() {
        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var gps_enabled = false
        var network_enabled = false

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (ex: Exception) {
        }


        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (ex: Exception) {
        }


        if (!gps_enabled && !network_enabled) {
            // notify user
            val dialog = AlertDialog.Builder(this)
            dialog.setMessage("Location not enabled")
            dialog.setPositiveButton("Enable Location") { _, _ ->
                val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(myIntent)
                //get gps
            }
            dialog.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            dialog.show()
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_REQUEST_COARSE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkLocation()

            } else {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Functionality limited")
                builder.setMessage("Since location access has not been granted this app will not be able to discover beacons when in background.")
                builder.setPositiveButton("Ok") { dialog, _ ->
                    dialog.dismiss()
                }
                builder.show()
            }
        }
    }

    fun onRangingClicked() {
        val intent = Intent(this, RangingActivity::class.java)
        startActivity(intent)
    }

    fun updateLog(message: String) {
        runOnUiThread {
            monitoringText.text = message
        }
    }
}
