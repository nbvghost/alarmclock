package ink.sites.alarmclock.data

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.preference.PreferenceManager

object MainViewModel : ViewModel() {
    var current = MutableLiveData<Int>(0)
    var type = MutableLiveData<Int>(0)
    var isRun = MutableLiveData<Boolean>(false)
    var isPlaySound = MutableLiveData<Boolean>(true)
}