package com.example.android.bluetoothbeacon

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.BeaconParser
import org.altbeacon.beacon.Region
import org.altbeacon.beacon.powersave.BackgroundPowerSaver
import org.altbeacon.beacon.startup.BootstrapNotifier
import org.altbeacon.beacon.startup.RegionBootstrap

class BeaconReferenceApplication : Application(), BootstrapNotifier {

    private var regionBootstrap: RegionBootstrap? = null
    private lateinit var backgroundPowerSaver: BackgroundPowerSaver
    private var haveDetectedBeaconsSinceBoot = false
    private var mainActivity: MainActivity? = null
    private var cumulativeLog = ""


    override fun onCreate() {
        super.onCreate()
        val beaconManger = BeaconManager.getInstanceForApplication(this)

        beaconManger.beaconParsers.add(BeaconParser().setBeaconLayout(BeaconParser.ALTBEACON_LAYOUT))
        beaconManger.beaconParsers.add(BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_TLM_LAYOUT))
        beaconManger.beaconParsers.add(BeaconParser().setBeaconLayout(BeaconParser.URI_BEACON_LAYOUT))
        beaconManger.beaconParsers.add(BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT))
        beaconManger.beaconParsers.add(BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_URL_LAYOUT))
        beaconManger.beaconParsers.clear()
        beaconManger.beaconParsers.add(BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"))

        BeaconManager.setDebug(false)

        beaconManger.enableForegroundServiceScanning(createNotification().build(), 456)
        beaconManger.setEnableScheduledScanJobs(false)
        beaconManger.backgroundBetweenScanPeriod = 0
        beaconManger.backgroundScanPeriod = 1100

        val region = Region("backgroundregion", null, null, null)

        regionBootstrap = RegionBootstrap(this, region)
        backgroundPowerSaver = BackgroundPowerSaver(this)


    }

    fun disableMonitoring() {
        if (regionBootstrap != null) {
            regionBootstrap!!.disable()
            regionBootstrap = null
        }
    }

    fun enableMonitoring() {
        val region :Region= Region("backgroundRegion", null, null,null)
        regionBootstrap = RegionBootstrap(this, region)
    }

    override fun didDetermineStateForRegion(state: Int, p1: Region?) {
        if (state == 1) {
            logToDisplay("Current region state is: INSIDE")
        } else {
            logToDisplay("Current region state is: OUTSIDE($state)")
        }


    }

    override fun didEnterRegion(p0: Region?) {
        if (!haveDetectedBeaconsSinceBoot) {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            haveDetectedBeaconsSinceBoot = true
        } else {
            if (mainActivity != null) {
                logToDisplay("I see a beacon again")
            } else {
                sendNotification()
            }
        }

    }

    private fun logToDisplay(message: String) {
        cumulativeLog += (message + "\n")
        if (this.mainActivity != null) {
            this.mainActivity?.updateLog(cumulativeLog)
        }
    }

    fun getLog(): String {
        return cumulativeLog
    }

    override fun didExitRegion(p0: Region?) {
        logToDisplay("I no longer see a beacon")
    }

    private fun createNotification(): Notification.Builder {
        val builder = Notification.Builder(this)
        builder.setSmallIcon(R.drawable.ic_stat_bluetooth_connected)
        builder.setContentTitle("Scanning for Beacons")
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        builder.setContentIntent(pendingIntent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("My Notification Channel ID", "My Notification Name", NotificationManager.IMPORTANCE_DEFAULT)
            channel.description = "My Notification Channel Description"
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            builder.setChannelId(channel.id)
        }
        return builder
    }

    private fun sendNotification() {
        val builder = NotificationCompat.Builder(this)
                .setContentTitle("Beacon Reference Application")
                .setContentText("An Beacon is nearby")
                .setSmallIcon(R.drawable.ic_stat_bluetooth_connected)
        val stackBuilder = TaskStackBuilder.create(this)
        stackBuilder.addNextIntent(Intent(this, MainActivity::class.java))
        val resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        builder.setContentIntent(resultPendingIntent)
        val notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, builder.build())

    }

    fun setMonitoringActivity(activity: MainActivity?) {
        this.mainActivity = activity
    }


}
