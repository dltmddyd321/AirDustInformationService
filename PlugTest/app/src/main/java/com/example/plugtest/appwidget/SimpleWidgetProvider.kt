package com.example.plugtest.appwidget

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.example.plugtest.R
import com.example.plugtest.airquality.Grade
import com.example.plugtest.data.Repository
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import java.lang.Exception

class SimpleWidgetProvider:AppWidgetProvider() {

    override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        ContextCompat.startForegroundService(
            context!!,
            Intent(context, UpdateWidgetService::class.java)
        )
    }

    class UpdateWidgetService : LifecycleService() {
        override fun onCreate() {
            super.onCreate()

            createChannelNeed()
            startForeground(
                NOTIFICATION_ID,
                createNotification()
            )
        }

        override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                val updateViews = RemoteViews(packageName,R.layout.widget_simple).apply {
                    setTextViewText( //TextView의 Text 변경
                        R.id.resultTextView,
                        "권한 없음"
                    )

                    setViewVisibility((R.id.labelTextView), View.GONE)
                    setViewVisibility((R.id.gradeLabelTextView), View.GONE)
                }
                updateWidget(updateViews)
                stopSelf()

                return super.onStartCommand(intent, flags, startId)
            }
            LocationServices.getFusedLocationProviderClient(this).lastLocation
                .addOnSuccessListener { location ->
                    lifecycleScope.launch {
                        try {
                            val nearbyMonitoringStation = Repository.getNearbyMonitoringState(location.latitude, location.longitude)
                            val measuredValue = Repository.getLatestAirQualityData(nearbyMonitoringStation!!.stationName!!)
                            val updateViews = RemoteViews(packageName,R.layout.widget_simple).apply{
                                setViewVisibility((R.id.labelTextView), View.VISIBLE)
                                setViewVisibility((R.id.gradeLabelTextView), View.VISIBLE)

                                val currentGrade = (measuredValue?.khaiGrade ?: Grade.UNKNOWN)
                                setTextViewText(R.id.resultTextView, currentGrade.emoji)
                                setTextViewText(R.id.gradeLabelTextView, currentGrade.label)
                            }
                            updateWidget(updateViews)
                        } catch (exception: Exception) {
                            exception.printStackTrace()
                        } finally {
                            stopSelf()
                        }
                    }
                }
            return super.onStartCommand(intent, flags, startId)
        }

        override fun onDestroy() {
            super.onDestroy()
            stopForeground(true)
        }

        private fun createChannelNeed() {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { //API Level 26 이상이라면
                (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
                    ?.createNotificationChannel(
                        NotificationChannel(
                            WIDGET_REFRESH_CHANNEL_ID,
                            "위젯 갱신 채널",
                            NotificationManager.IMPORTANCE_LOW
                        )
                    )
            }
        }

        private fun createNotification() : Notification =
            NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_refresh)
                .setChannelId(WIDGET_REFRESH_CHANNEL_ID)
                .build()

        private fun updateWidget(updateViews: RemoteViews) {
            val widgetProvider = ComponentName(this,SimpleWidgetProvider::class.java)
            AppWidgetManager.getInstance(this).updateAppWidget(widgetProvider, updateViews)
        }
    }

    companion object {
        private const val WIDGET_REFRESH_CHANNEL_ID = "WIDGET_REFRESH_CHANNEL_ID"
        private const val NOTIFICATION_ID = 101
    }

}