package ink.sites.alarmclock.data

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.preference.PreferenceManager

object MainViewModel : ViewModel() {
    var current = MutableLiveData<Long>(0)
    var isRun = MutableLiveData<Boolean>(false)
    var isPause = MutableLiveData<Boolean>(false)
    var isPlaySound = MutableLiveData<Boolean>(true)
    var taskIndex = MutableLiveData<Int>(0)
    var taskList = MutableLiveData<List<Long>>(listOf())
}