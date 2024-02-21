package ink.sites.alarmclock.receiver

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import ink.sites.alarmclock.service.CoreService

class CoreReceiver : BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        intent.action?.let {
            Log.d("CoreReceiver", it)
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
        //context.startService(Intent(context, CoreService::class.java))

        //context.bindService(intent, serviceConnection,  Context.BIND_AUTO_CREATE)
        //val binder = context.applicationContext.peekService(context, Intent(context, CoreService::class.java))
        //Log.d("dfd",binder.toString())

        /* StringBuilder().apply {
             append("Action: ${intent.action}\n")
             append("URI: ${intent.toUri(Intent.URI_INTENT_SCHEME)}\n")
             toString().also { log ->
                 Log.d("CoreReceiver", log)

                 val binding = ActivityNameBinding.inflate(layoutInflater)
                 val view = binding.root
                 setContentView(view)
                 Snackbar.make(view, log, Snackbar.LENGTH_LONG).show()
             }
         }*/
    }
    var serviceConnection =  object : ServiceConnection {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder: CoreService.LocalBinder = service as CoreService.LocalBinder
            binder.getService().notificationTimer() // 获取到的Service即MyService
        }

        override fun onServiceDisconnected(name: ComponentName?) {

        }
    }
}