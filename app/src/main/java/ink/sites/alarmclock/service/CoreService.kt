package ink.sites.alarmclock.service

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.preference.PreferenceManager
import ink.sites.alarmclock.R
import ink.sites.alarmclock.data.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CoreService : LifecycleService() {
    companion object {
        const val TimeRequestID = 45645
        const val DelayRequestID = 55646
        var TimerNotificationChannelID = "ink.sites.alarmclock.service.TimerNotificationChannel"
        var DelayNotificationChannelID = "ink.sites.alarmclock.service.DelayNotificationChannel"
    }

    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods.
        fun getService(): CoreService = this@CoreService
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    private var mediaTimePlayer: MediaPlayer? = null
    private var mediaDelayPlayer: MediaPlayer? = null
    private fun playTimeSound() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this@CoreService)
        val ringtone = sharedPreferences.getString("ringtone_time", "")
        if (ringtone.isNullOrEmpty()) {
            return
        }

        mediaDelayPlayer?.pause()

        if (mediaTimePlayer == null) {
            mediaTimePlayer = MediaPlayer.create(baseContext, Uri.parse(ringtone))
            mediaTimePlayer?.isLooping = true
        }
        if (mediaTimePlayer?.isPlaying == false) {
            mediaTimePlayer?.seekTo(0)
            mediaTimePlayer?.isLooping = true
            mediaTimePlayer?.start()
        }
    }

    private fun playDelaySound() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this@CoreService)
        val ringtone = sharedPreferences.getString("ringtone_delay", "")

        if (ringtone.isNullOrEmpty()) {
            return
        }

        mediaTimePlayer?.pause()

        if (mediaDelayPlayer == null) {
            mediaDelayPlayer = MediaPlayer.create(baseContext, Uri.parse(ringtone))
            mediaDelayPlayer?.isLooping = true
        }
        if (mediaDelayPlayer?.isPlaying == false) {
            mediaDelayPlayer?.seekTo(0)
            mediaDelayPlayer?.isLooping = true
            mediaDelayPlayer?.start()
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun notificationTimer() {


        //val notification = builder.setOngoing(true)//.setSmallIcon(R.mipmap.ic_launcher)
        // .setPriority(NotificationCompat.PRIORITY_MIN).setCategory(Notification.CATEGORY_SERVICE).build()


        //startForeground(123456, notification)


    }

    class TimerReceiver() : BroadcastReceiver() {

        @RequiresApi(Build.VERSION_CODES.Q)
        override fun onReceive(context: Context, intent: Intent) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

            val ringtone = sharedPreferences.getString("ringtone_time", "")


            val builder = NotificationCompat.Builder(context, TimerNotificationChannelID)
            val notification = builder.setOngoing(true).setSound(Uri.parse(ringtone)).setSmallIcon(R.mipmap.ic_launcher).setPriority(NotificationCompat.PRIORITY_MIN).setCategory(Notification.CATEGORY_SERVICE).build()
            notification.flags = Notification.FLAG_INSISTENT or Notification.FLAG_NO_CLEAR or Notification.FLAG_BUBBLE
            notificationManager.notify(0, notification)


            val isLoop = sharedPreferences.getBoolean("loop", false)
            if (isLoop) {
                val delay = sharedPreferences.getInt("delay", 1)
                val alarmManager = context.getSystemService(ComponentActivity.ALARM_SERVICE) as AlarmManager
                val coreReceiverIntent = Intent(context, DelayReceiver::class.java)
                coreReceiverIntent.setAction("ink.sites.alarmclock.receiver.Delay")
                //val pendingIntent = PendingIntent.getService(this, requestId, intent, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
                val pendingIntent = PendingIntent.getBroadcast(context, DelayRequestID, coreReceiverIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                if (pendingIntent != null) {
                    alarmManager.cancel(pendingIntent)
                }
                alarmManager.set(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + delay * 60 * 1000,
                    pendingIntent
                )
            }
        }
    }
    class DelayReceiver() : BroadcastReceiver() {
        @RequiresApi(Build.VERSION_CODES.Q)
        override fun onReceive(context: Context, intent: Intent) {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val ringtone = sharedPreferences.getString("ringtone_delay", "")

            val builder = NotificationCompat.Builder(context, DelayNotificationChannelID)
            val notification = builder.setSound(Uri.parse(ringtone)).setOngoing(true).setSmallIcon(R.mipmap.ic_launcher).setPriority(NotificationCompat.PRIORITY_MIN).setCategory(Notification.CATEGORY_SERVICE).build()
            notification.flags = Notification.FLAG_INSISTENT or Notification.FLAG_NO_CLEAR or Notification.FLAG_BUBBLE
            notificationManager.notify(0, notification)



            val time = sharedPreferences.getInt("time", 1)
            val alarmManager = context.getSystemService(ComponentActivity.ALARM_SERVICE) as AlarmManager
            val coreReceiverIntent = Intent(context, TimerReceiver::class.java)
            coreReceiverIntent.setAction("ink.sites.alarmclock.receiver.Timer")
            //val pendingIntent = PendingIntent.getService(this, requestId, intent, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
            val pendingIntent = PendingIntent.getBroadcast(context, CoreService.TimeRequestID, coreReceiverIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
            }
            alarmManager.set(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + time * 60 * 1000,
                pendingIntent
            )
        }
    }

    override fun onCreate() {
        super.onCreate()

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this@CoreService)


        /*MainViewModel.isPlaySound.observe(this@CoreService) { it ->
            if (!it) {
                mediaTimePlayer?.pause()
                mediaDelayPlayer?.pause()

                MainViewModel.isPlaySound.postValue(true)
            }
        }*/


        CoroutineScope(Dispatchers.IO).launch {

            while (false) {
                val isRun = sharedPreferences.getBoolean("is-run", false)
                val isLoop = sharedPreferences.getBoolean("loop", false)
                val type = sharedPreferences.getInt("type", 0)
                var current = sharedPreferences.getInt("current", 0)
                val time = sharedPreferences.getInt("time", 0) * 60
                val delay = sharedPreferences.getInt("delay", 0) * 60

                MainViewModel.isRun.postValue(isRun)

                if (isRun) {
                    current += 1
                    if (type == 0 && current >= time) {
                        current = 0
                        MainViewModel.current.postValue(current)
                        sharedPreferences.edit().apply {
                            putInt("type", 1)
                            putInt("current", current)
                            apply()
                        }
                        playTimeSound()
                        continue
                    }
                    if (type == 1 && current >= delay) {
                        current = 0
                        MainViewModel.current.postValue(current)
                        if (!isLoop) {
                            sharedPreferences.edit().apply {
                                putInt("type", 0)
                                putInt("current", current)
                                putBoolean("is-run", false)
                                apply()
                            }
                        } else {
                            sharedPreferences.edit().apply {
                                putInt("type", 0)
                                putInt("current", current)
                                apply()
                            }
                        }
                        playDelaySound()
                        continue
                    }
                    MainViewModel.current.postValue(current)
                    sharedPreferences.edit().apply {
                        putInt("current", current)
                        apply()
                    }
                }
                delay(1000L)
            }
        }

    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        var chan = NotificationChannel(TimerNotificationChannelID, "计时通知", NotificationManager.IMPORTANCE_NONE)
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        notificationManager.createNotificationChannel(chan)

        chan = NotificationChannel(DelayNotificationChannelID, "间隔通知", NotificationManager.IMPORTANCE_NONE)
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        notificationManager.createNotificationChannel(chan)

        //registerReceiver(TimerReceiver(), IntentFilter("ink.sites.alarmclock.receiver.Timer"))

        //val notification: Notification
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification = getNotificationO(service, service.getSystemService(NOTIFICATION_SERVICE), channelName, channelDesc, contentTitle, contentText)
        } else {
            notification = getNotification(service, contentTitle, contentText)
        }*/
        val builder = NotificationCompat.Builder(this, DelayNotificationChannelID)
        val notification = builder.setOngoing(false).setSmallIcon(R.mipmap.ic_launcher).setPriority(NotificationCompat.PRIORITY_MIN).setCategory(Notification.CATEGORY_SERVICE).build()

        notification.flags = Notification.FLAG_ONGOING_EVENT
        notification.flags = notification.flags or Notification.FLAG_NO_CLEAR
        notification.flags = notification.flags or Notification.FLAG_FOREGROUND_SERVICE
        startForeground(65, notification)

        return START_STICKY
    }
}


