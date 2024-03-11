package ink.sites.alarmclock.service


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
import android.util.Log
import android.widget.Toast
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
import kotlinx.coroutines.withContext

class CoreService : LifecycleService() {
    companion object {
        const val TimeRequestID = 45645
        const val DelayRequestID = 55646
        var TimerNotificationChannelID = "ink.sites.alarmclock.service.TimerNotificationChannel"
        var DelayNotificationChannelID = "ink.sites.alarmclock.service.DelayNotificationChannel"


        fun getPendingIntentForTime(context: Context): PendingIntent {
            val coreReceiverIntent = Intent(context, TimerReceiver::class.java)
            coreReceiverIntent.setAction("ink.sites.alarmclock.receiver.Timer")
            return PendingIntent.getBroadcast(context, TimeRequestID, coreReceiverIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        fun getPendingIntentForDelay(context: Context): PendingIntent {
            val coreReceiverIntent = Intent(context, DelayReceiver::class.java)
            coreReceiverIntent.setAction("ink.sites.alarmclock.receiver.Delay")
            return PendingIntent.getBroadcast(context, DelayRequestID, coreReceiverIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        fun alarmManagerCancelAll(context: Context) {
            /*val a = getPendingIntentForDelay(context)
            val b = getPendingIntentForTime(context)
            val alarmManager = context.getSystemService(ComponentActivity.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(a)
            alarmManager.cancel(b)*/
        }
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

    private var mediaPlayer: MediaPlayer? = null
    private fun playTimeSound() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this@CoreService)
        val ringtone = sharedPreferences.getString("ringtone_time", "")
        if (ringtone.isNullOrEmpty()) {
            return
        }

        mediaPlayer?.pause()
        mediaPlayer?.release()
        mediaPlayer=null
        mediaPlayer = MediaPlayer.create(baseContext, Uri.parse(ringtone))
        mediaPlayer?.let {
            it.isLooping = true
            if (!it.isPlaying) {
                it.seekTo(0)
                it.isLooping = true
                it.start()
            }
        }
    }

    private fun playDelaySound() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this@CoreService)
        val ringtone = sharedPreferences.getString("ringtone_delay", "")

        if (ringtone.isNullOrEmpty()) {
            return
        }

        mediaPlayer?.pause()
        mediaPlayer?.release()
        mediaPlayer=null
        mediaPlayer = MediaPlayer.create(baseContext, Uri.parse(ringtone))
        mediaPlayer?.let {
            it.isLooping = true
            if (!it.isPlaying) {
                it.seekTo(0)
                it.isLooping = true
                it.start()
            }
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
                /*val delay = sharedPreferences.getInt("delay", 1)
                val alarmManager = context.getSystemService(ComponentActivity.ALARM_SERVICE) as AlarmManager


                //val pendingIntent = PendingIntent.getService(this, requestId, intent, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
                val pendingIntent = getPendingIntentForDelay(context)//PendingIntent.getBroadcast(context, DelayRequestID, coreReceiverIntent, PendingIntent.FLAG_UPDATE_CURRENT)

                alarmManager.cancel(pendingIntent)


                alarmManager.set(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + delay * 60 * 1000,
                    pendingIntent
                )*/
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


            /*val time = sharedPreferences.getInt("time", 1)
            val alarmManager = context.getSystemService(ComponentActivity.ALARM_SERVICE) as AlarmManager
            val coreReceiverIntent = Intent(context, TimerReceiver::class.java)
            coreReceiverIntent.setAction("ink.sites.alarmclock.receiver.Timer")
            //val pendingIntent = PendingIntent.getService(this, requestId, intent, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
            val pendingIntent = getPendingIntentForTime(context)//PendingIntent.getBroadcast(context, CoreService.TimeRequestID, coreReceiverIntent, PendingIntent.FLAG_UPDATE_CURRENT)

            alarmManager.cancel(pendingIntent)

            alarmManager.set(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + time * 60 * 1000,
                pendingIntent
            )*/
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this@CoreService)

        val runServiceNotificationChannelID = "ink.sites.alarmclock.service.RunningForegroundChannel"

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val chan = NotificationChannel(runServiceNotificationChannelID, "服务运行", NotificationManager.IMPORTANCE_NONE)
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        notificationManager.createNotificationChannel(chan)

        val builder = NotificationCompat.Builder(this, runServiceNotificationChannelID)
        builder.setContentText("ddd")
        builder.setContentTitle("dd")
        val notification = builder.setOngoing(false).setSmallIcon(R.mipmap.ic_launcher).setPriority(NotificationCompat.PRIORITY_MIN).setCategory(Notification.CATEGORY_SERVICE).build()

        notification.flags = Notification.FLAG_ONGOING_EVENT
        notification.flags = notification.flags or Notification.FLAG_NO_CLEAR
        notification.flags = notification.flags or Notification.FLAG_FOREGROUND_SERVICE
        startForeground(445454, notification)




        CoroutineScope(Dispatchers.Main).launch {
            var kk = SystemClock.elapsedRealtime()
            while (true) {
                val i = (SystemClock.elapsedRealtime() - kk) - 1000
                //Toast.makeText(this@CoreService, "service:${Build.VERSION.SDK_INT}:$i",Toast.LENGTH_LONG).show()
                Log.d("show time", i.toString())

                if (MainViewModel.isRun.value == true) {

                    if (MainViewModel.isPause.value == false) {
                        var current = MainViewModel.current.value!! + 1
                        val time = MainViewModel.taskList.value!![MainViewModel.taskIndex.value!!] * 60
                        if (current>=time) {
                            if (MainViewModel.taskList.value!!.size == MainViewModel.taskIndex.value!! + 1) {
                                MainViewModel.taskIndex.postValue(0)
                                MainViewModel.current.postValue(0)
                                playDelaySound()
                            }else{
                                MainViewModel.taskIndex.postValue(MainViewModel.taskIndex.value!!+1)
                                MainViewModel.current.postValue(0)
                                playTimeSound()
                            }
                            current=0
                        }
                        MainViewModel.current.postValue(current)
                    }
                }

                kk = SystemClock.elapsedRealtime()
                delay(1000)
            }
        }


        MainViewModel.isPlaySound.observe(this@CoreService) { it ->
            if (!it) {
                mediaPlayer?.pause()
                MainViewModel.isPlaySound.postValue(true)
            }
        }

        /*CoroutineScope(Dispatchers.IO).launch {

            while (false) {
                val isRun = sharedPreferences.getBoolean("is-run", false)
                val isLoop = sharedPreferences.getBoolean("loop", false)
                val type = sharedPreferences.getInt("type", 0)
                var current = sharedPreferences.getLong("current", 0)
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
                            putLong("current", current)
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
                                putLong("current", current)
                                putBoolean("is-run", false)
                                apply()
                            }
                        } else {
                            sharedPreferences.edit().apply {
                                putInt("type", 0)
                                putLong("current", current)
                                apply()
                            }
                        }
                        playDelaySound()
                        continue
                    }
                    MainViewModel.current.postValue(current)
                    sharedPreferences.edit().apply {
                        putLong("current", current)
                        apply()
                    }
                }
                delay(1000L)
            }
        }*/

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


