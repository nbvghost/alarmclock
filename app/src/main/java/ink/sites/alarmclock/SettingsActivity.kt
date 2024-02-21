package ink.sites.alarmclock

//import androidx.appcompat.app.AppCompatActivity
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SeekBarPreference


private const val TITLE_TAG = "settingsActivityTitle"

class SettingsActivity : AppCompatActivity(),
    PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)




        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, HeaderFragment())
                .commit()
        } else {
            title = savedInstanceState.getCharSequence(TITLE_TAG)
        }
        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount == 0) {
                title = "设置"
            }
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save current activity title so we can set it again after a configuration change
        outState.putCharSequence(TITLE_TAG, title)
    }

    override fun onSupportNavigateUp(): Boolean {
        if (supportFragmentManager.popBackStackImmediate()) {
            return true
        }
        onBackPressedDispatcher.onBackPressed()
        return true//super.onSupportNavigateUp()
    }

    override fun onPreferenceStartFragment(caller: PreferenceFragmentCompat, pref: Preference): Boolean {
        // Instantiate the new Fragment
        val prefFragment = pref.fragment!!
        val args = pref.extras
        val fragment = supportFragmentManager.fragmentFactory.instantiate(
            classLoader,
            prefFragment
        ).apply {
            arguments = args
            setTargetFragment(caller, 0)
        }
        // Replace the existing Fragment with the new Fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.settings, fragment)
            .addToBackStack(null)
            .commit()
        title = pref.title
        return true
    }

    class HeaderFragment : PreferenceFragmentCompat() {
        private lateinit var ringtonePickerIntent: Intent

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.header_preferences, rootKey)

            val context:Context = requireActivity()

            ringtonePickerIntent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
            ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL)
            ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "设置通知铃声")

            val ringtoneTimePreference: Preference? = findPreference("ringtone_time")
            val ringtoneDelayPreference: Preference? = findPreference("ringtone_delay")
            //val ringtoneDelaySeekBarPreference: SeekBarPreference? = findPreference("ringtone_delay")


            val ringtoneTimeUri =  Uri.parse(PreferenceManager.getDefaultSharedPreferences(context).getString("ringtone_time",""))
            val ringtoneDelayUri =  Uri.parse(PreferenceManager.getDefaultSharedPreferences(context).getString("ringtone_delay",""))

            val activityResultLauncherTime = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                Log.d("man", it.toString())

                val pickedUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // TIRAMISU
                    it.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
                } else {
                    it.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
                }

                //val pickedUri = it.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI,Uri::class.java)
                //val pickedUri = it.data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
                PreferenceManager.getDefaultSharedPreferences(context).edit().putString("ringtone_time", pickedUri.toString()).apply()
                Log.d("man", pickedUri.toString())
                ringtoneTimePreference?.summaryProvider = Preference.SummaryProvider<Preference> { preference ->
                    val text = getRingtoneName(context,pickedUri)
                    if (text.isNullOrEmpty()) {
                        "未选择"
                    } else {
                        text
                    }
                }
            }

            val activityResultLauncherDelay = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                Log.d("man", it.toString())

                val pickedUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // TIRAMISU
                    it.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
                } else {
                    it.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
                }

                PreferenceManager.getDefaultSharedPreferences(context).edit().putString("ringtone_delay", pickedUri.toString()).apply()
                Log.d("man", pickedUri.toString())
                ringtoneDelayPreference?.summaryProvider = Preference.SummaryProvider<Preference> { preference ->
                    val text = getRingtoneName(context,pickedUri)
                    if (text.isNullOrEmpty()) {
                        "未选择"
                    } else {
                        text
                    }
                }
            }




            ringtoneTimePreference?.setOnPreferenceClickListener {
                ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,ringtoneTimeUri)
                activityResultLauncherTime.launch(ringtonePickerIntent)
                true
            }


            ringtoneDelayPreference?.setOnPreferenceClickListener {
                ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,ringtoneDelayUri)
                activityResultLauncherDelay.launch(ringtonePickerIntent)
                true
            }

            ringtoneTimePreference?.summaryProvider = Preference.SummaryProvider<Preference> { preference ->
                val text = getRingtoneName(context,ringtoneTimeUri)
                if (text.isNullOrEmpty()) {
                    "未选择"
                } else {
                    text
                }
            }
            ringtoneDelayPreference?.summaryProvider = Preference.SummaryProvider<Preference> { preference ->
                val text = getRingtoneName(context,ringtoneDelayUri)
                if (text.isNullOrEmpty()) {
                    "未选择"
                } else {
                    text
                }
            }
        }
        fun getRingtoneName(context:Context,uri: Uri?): String? {
            val r = RingtoneManager.getRingtone(context, uri)
            return r.getTitle(context)
        }
    }

    class MessagesFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.messages_preferences, rootKey)
        }
    }

    class SyncFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.sync_preferences, rootKey)
        }
    }
}