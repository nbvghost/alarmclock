package ink.sites.alarmclock

import android.app.Application

class MainApplication : Application() {
    companion object {
        private lateinit var instance: MainApplication

        fun getInstance(): MainApplication {
            return instance
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}