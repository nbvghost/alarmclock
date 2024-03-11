package ink.sites.alarmclock.data

import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import ink.sites.alarmclock.MainApplication

class Preference {
    companion object {
        private var sharedPreferences: SharedPreferences? = null
        private fun getSharedPreferences(): SharedPreferences {
            if (sharedPreferences == null) {
                sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainApplication.getInstance())
            }
            return sharedPreferences!!
        }

        fun getCurrent(): Long {
            return getSharedPreferences().getLong("current", 0)
        }

        fun getTime(): Int {
            return getSharedPreferences().getInt("time", 0)
        }

        fun getDelay(): Int {
            return getSharedPreferences().getInt("delay", 0)
        }

        fun getType(): Int {
            return getSharedPreferences().getInt("type", 0)
        }

        fun isRun(): Boolean {
            return getSharedPreferences().getBoolean("is-run", false)
        }
    }
}