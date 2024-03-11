package ink.sites.alarmclock

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.SystemClock
import android.util.Log
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.preference.PreferenceManager
import ink.sites.alarmclock.data.MainViewModel
import ink.sites.alarmclock.service.CoreService
import ink.sites.alarmclock.service.CoreService.Companion.getPendingIntentForTime
import ink.sites.alarmclock.ui.theme.AlarmclockTheme
import ink.sites.alarmclock.ui.theme.HFDigitsFontFamily

class MainActivity : ComponentActivity() {

    //@RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        startForegroundService(Intent(this, CoreService::class.java))




        setContent {
            AlarmclockTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Greeting("Android")
                }
            }
        }
    }

    override fun finish() {

    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.repeatCount == 0) {
            // 右键处理
            moveTaskToBack(true)
        }
        return true
    }
}


@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {


    val context = LocalContext.current as MainActivity
    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)


    MainViewModel.taskList.value = listOf(sharedPreferences.getInt("time", 0).toLong(), sharedPreferences.getInt("delay", 0).toLong())


    Column(
        modifier = Modifier
            .background(
                brush = Brush.linearGradient(
                    0.0f to Color(0xff333333), 1.0f to Color(0xff333333),
                    //startY = 0.0f,
                    //endY = 50.0f,
                    tileMode = TileMode.Decal
                ), shape = RoundedCornerShape(0.dp)
            )
            .wrapContentHeight()
            .fillMaxWidth()
    ) {


        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(Color(0xff333333)), propagateMinConstraints = true
        ) {


            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(3.dp)
            ) {


                //val current by MainViewModel.current.observeAsState(initial = 0)
                var progressValue by remember { mutableFloatStateOf(0F) }

                var hT by remember { mutableStateOf("00") }
                var mT by remember { mutableStateOf("00") }
                var sT by remember { mutableStateOf("00") }


                /*val transition = rememberInfiniteTransition(label = "s")
                val animatedRestart by transition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(tween(1000 * 360, 0, easing = LinearEasing), RepeatMode.Restart), label = "b"
                )
                Text(text = animatedRestart.toInt().toString())*/

                val isPause by MainViewModel.isPause.observeAsState(initial = false)
                val isRun by MainViewModel.isRun.observeAsState(initial = false)
                val current by MainViewModel.current.observeAsState(initial = 0)
                val taskIndex by MainViewModel.taskIndex.observeAsState(initial = 0)


                //LaunchedEffect(key1 = current) {
                key(current,taskIndex) {
                    //val time = sharedPreferences.getInt("time", 0)
                    //val delay = sharedPreferences.getInt("delay", 0)
                    //val current = sharedPreferences.getLong("current", 0)
                    //val type = sharedPreferences.getInt("type", 0)

                    val time = MainViewModel.taskList.value!![MainViewModel.taskIndex.value!!] * 60
                    //var tt = MainViewModel.taskList.value!![MainViewModel.taskIndex.value!!] - current


                    var v = current.toFloat() / (time.toFloat())
                    if (v > 1) {
                        v = 0F
                    }
                    progressValue = v


                    val l = time - current


                    hT = ("00" + ((l.toFloat() / 60F / 60F) % 99F).toInt().toString()).takeLast(2)
                    mT = ("00" + ((l.toFloat() / 60F) % 60F).toInt().toString()).takeLast(2)
                    sT = ("00" + (l.toFloat() % 60F).toInt().toString()).takeLast(2)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center
                ) {

                    Text(
                        text = hT, modifier = Modifier, color = Color.White, fontSize = 96.sp, fontFamily = HFDigitsFontFamily, textAlign = TextAlign.Left, softWrap = false
                    )
                    Text(
                        text = ":", modifier = Modifier.padding(horizontal = 10.dp), color = Color.White, fontSize = 32.sp, softWrap = false
                    )
                    Text(
                        text = mT, modifier = Modifier, color = Color.White, fontSize = 96.sp, fontFamily = HFDigitsFontFamily, textAlign = TextAlign.Left, softWrap = false
                    )
                    Text(
                        text = ":", modifier = Modifier.padding(horizontal = 10.dp), color = Color.White, fontSize = 32.sp, softWrap = false
                    )
                    Text(
                        text = sT, modifier = Modifier, color = Color.White, fontSize = 96.sp, fontFamily = HFDigitsFontFamily, textAlign = TextAlign.Left, softWrap = false
                    )
                }

                //var timeLeft by remember { mutableStateOf(60) }

                //var isRun by remember { mutableStateOf(sharedPreferences.getBoolean("is-run", false)) }


                Box(contentAlignment = Alignment.Center, modifier = Modifier) {
                    CircularProgressIndicator(
                        progress = progressValue,
                        modifier = Modifier.size(120.dp),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally), modifier = Modifier.padding(vertical = 20.dp)) {
                    if (isRun && isPause == false) {
                        IconButton(onClick = {
                            /*PreferenceManager.getDefaultSharedPreferences(context).edit().apply {
                                MainViewModel.isRun.postValue(false)
                                putBoolean("is-run", MainViewModel.isRun.value!!)
                                apply()
                            }
                            CoreService.alarmManagerCancelAll(context)
                            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                            notificationManager.cancelAll()*/

                            MainViewModel.isPause.postValue(true)

                        }, modifier = Modifier.background(Color.Red, shape = RoundedCornerShape(3.dp))) {
                            Icon(Icons.Filled.Pause, null, tint = Color.White)
                        }
                    } else {
                        IconButton(onClick = {
                            /*PreferenceManager.getDefaultSharedPreferences(context).edit().apply {
                                MainViewModel.isRun.postValue(true)
                                putBoolean("is-run", MainViewModel.isRun.value!!)
                                putLong("current",sharedPreferences.getLong(""))
                                apply()
                            }*/
                            MainViewModel.isPause.postValue(false)
                            MainViewModel.isRun.postValue(true)
                            //MainViewModel.current.postValue(MainViewModel.taskList.value!![MainViewModel.taskIndex.value!!])
                            //play(context)
                        }, modifier = Modifier.background(Color.Green, shape = RoundedCornerShape(3.dp))) {
                            Icon(Icons.Filled.PlayArrow, null, tint = Color.White)
                        }
                    }
                    IconButton(onClick = {
                        Log.d("elapsedRealtime", SystemClock.elapsedRealtime().toString())
                        MainViewModel.isRun.postValue(false)
                        MainViewModel.isPause.postValue(false)
                        MainViewModel.current.postValue(0)
                        MainViewModel.taskIndex.postValue(0)
                    }, modifier = Modifier.background(Color.Gray, shape = RoundedCornerShape(3.dp))) {
                        Icon(Icons.Filled.Replay, null, tint = Color.White)
                    }
                }

                Row(modifier = Modifier.padding(vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)) {
                    Button(onClick = {
                        context.startActivity(Intent(context, SettingsActivity::class.java))
                        //.launch(Intent(context,SettingsActivity::class.java))
                    }) {
                        Text(text = "设置")
                    }
                    Button(onClick = {
                        MainViewModel.isPlaySound.postValue(false)
                        //val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        //notificationManager.cancelAll()
                    }) {
                        Text(text = "停止音乐")
                    }
                }
                if (!isRun) {
                    Text(text = "选择开始时间：")
                    Row(modifier = Modifier, horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)) {
                        Button(onClick = {
                            MainViewModel.taskIndex.postValue(0)
                        }) {
                            Text(text = MainViewModel.taskList.value!![0].toString() + "分钟")
                        }
                        Button(onClick = {
                            MainViewModel.taskIndex.postValue(1)
                        }) {
                            Text(text = MainViewModel.taskList.value!![1].toString() + "分钟")
                        }
                    }
                }
            }

        }
    }
}

fun play(context: Context) {
    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    val currentTime = sharedPreferences.getLong("current", 0)


    val time = sharedPreferences.getInt("time", 0)
    val alarmManager = context.getSystemService(ComponentActivity.ALARM_SERVICE) as AlarmManager

    //val pendingIntent = PendingIntent.getService(this, requestId, intent, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
    val pendingIntent = getPendingIntentForTime(context)
    alarmManager.cancel(pendingIntent)


    var triggerAtMillis = SystemClock.elapsedRealtime() + time * 60 * 1000

    val hasTime = SystemClock.elapsedRealtime() - currentTime
    if (hasTime > 0) {
        triggerAtMillis -= hasTime
    }

    Log.d("MainActivity", "hasTime:${hasTime}")

    //alarmManager.setInexactRepeating()
    /*alarmManager.set(
        AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtMillis, pendingIntent
    )*/
}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AlarmclockTheme {
        Greeting("Android")
    }
}