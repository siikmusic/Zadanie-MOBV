package com.example.zadaniemobv.workers

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.zadaniemobv.R
import com.example.zadaniemobv.api.DataRepository

class MyWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        // Tu môžete vykonávať asynchrónnu prácu
        Log.d("MyWorker", "spustenie workera")
        DataRepository.getInstance(applicationContext).apiListGeofence()

        createNotification(applicationContext)

        return Result.success()
    }

    suspend fun createNotification(context: Context) {

        val users = DataRepository.getInstance(context).getUsersList() ?: emptyList()

        val name = "MOBV Zadanie"
        val descriptionText = "V okolí je ${users.size} používateľov"
        val text = users.joinToString { it.name }
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel_id = "kanal-1"
        val channel =
            NotificationChannel(channel_id, name, importance).apply {
                description = descriptionText
            }
        // Register the channel with the system
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        val builder =
            NotificationCompat.Builder(context, channel_id).apply {
                setContentTitle(descriptionText)
                setContentText(text)
                setSmallIcon(R.mipmap.ic_launcher_round)
                priority = NotificationCompat.PRIORITY_DEFAULT
            }

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("Notifikacia", "Chyba povolenie na notifikaciu");
            return
        }

        NotificationManagerCompat.from(context).notify(1, builder.build())
    }
}