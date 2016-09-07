package de.myself5.xperiasettings;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class XperiaSettingsActivity extends AppCompatPreferenceActivity {

    /*    private static String TAG = "XperiaSettings";
        protected static String mXperiaOTGPath;*/
    protected static String PREF_ID_POLL_ENABLED = "sys.device.usb.id_poll_enable";
    protected static String PREF_ADB_NETWORK = "adb.network.port";
    private static FragmentManager mFragmentManager;
    protected static AppCompatPreferenceActivity mActivity;
    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener mPreferenceListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            switch (preference.getKey()) {
                case "otg_switch":
                    if ((Boolean) value) {
                        confirmEnablingOTG();
                    } else {
                        /*writeSysFs(mXperiaOTGPath, "0");*/
                        setSystemProperty(PREF_ID_POLL_ENABLED, "false");
                    }
                    break;
                case "adbon_switch":
                    if ((Boolean) value) {
                        confirmEnablingADBON();
                    } else {
                        setSystemProperty(PREF_ADB_NETWORK, "-1");
                        /*restartADBD();*/
                    }
                    break;
                default:
                    break;
            }
            return true;
        }
    };

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
        mActivity = this;
        addPreferencesFromResource(R.xml.pref_general);
        findPreference("otg_switch").setOnPreferenceChangeListener(mPreferenceListener);
        findPreference("adbon_switch").setOnPreferenceChangeListener(mPreferenceListener);
        /*mXperiaOTGPath = getSystemProperty("ro.xperia.otgpath");*/
        mFragmentManager = getFragmentManager();

        /** Set, only for OTG, the value to the sysfs value
         * This prevents setting OTG enabled at boot
         * We want to make people activate OTG only when then use it, as there might be a
         * risk of damaging the device when submerging it into water with OTG (currency on the USB)
         * enabled.
         */

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        /*editor.putBoolean("otg_switch", readSysFs(mXperiaOTGPath));*/
        String idPoll = getSystemProperty(PREF_ID_POLL_ENABLED);
        String adbN = getSystemProperty(PREF_ADB_NETWORK);
        if (idPoll != null)
            editor.putBoolean("otg_switch", Boolean.valueOf(idPoll));
        if (adbN != null)
            editor.putBoolean("adbon_switch", adbN.equals("5555"));
        editor.apply();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName);
    }

/*    protected static void writeSysFs(String path, String string) {
        try {
            FileOutputStream fos = new FileOutputStream(path);
            fos.write(string.getBytes(Charset.forName("UTF-8")));
            fos.close();
        } catch (Exception e) {
            Log.e(TAG, "writeSysFs failed with error: " + e.getMessage());
        }
    }

    private static Boolean readSysFs(String filePath) {
        int value = 0;
        String text = null;
        File file = new File(filePath);
        if (file.exists()) {
            try {
                FileInputStream fs = new FileInputStream(file);
                InputStreamReader sr = new InputStreamReader(fs);
                BufferedReader br = new BufferedReader(sr);
                text = br.readLine();

                br.close();
                sr.close();
                fs.close();
            } catch (Exception ex) {
                Log.e(TAG, ex.getMessage());
                ex.printStackTrace();
            }

            if (text != null) {
                try {
                    value = Integer.parseInt(text);
                } catch (NumberFormatException nfe) {
                    Log.e(TAG, nfe.getMessage());
                    value = 0;
                }
            }
        }
        return value == 1;
    }*/

    private static String getSystemProperty(String key) {
        String value = null;
        try {
            value = (String) Class.forName("android.os.SystemProperties")
                    .getMethod("get", String.class).invoke(null, key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    protected static void setSystemProperty(String key, String value) {
        try {
            Class.forName("android.os.SystemProperties")
                    .getMethod("set", String.class, String.class).invoke(null, key, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

/*    protected static void restartADBD(){
        try{
            Process su = Runtime.getRuntime().exec("su");
            DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());

            outputStream.writeBytes("stop adbd\n");
            outputStream.flush();

            outputStream.writeBytes("start adbd\n");
            outputStream.flush();

            outputStream.writeBytes("exit\n");
            outputStream.flush();
            su.waitFor();
        }catch(IOException | InterruptedException e){
            Log.e(TAG, e.getMessage());
        }
    }*/

    private static void confirmEnablingOTG() {
        DialogFragment newFragment = new EnableOTGDialog();
        newFragment.show(mFragmentManager, "otg");
    }

    private static void confirmEnablingADBON() {
        DialogFragment newFragment = new EnableADBONDialog();
        newFragment.show(mFragmentManager, "adb");
    }
}