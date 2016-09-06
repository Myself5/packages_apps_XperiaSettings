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
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

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

    protected static String mXperiaOTGPath;
    private static FragmentManager mFragmentManager;
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
                        writeSysFs(mXperiaOTGPath, "0");
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
        addPreferencesFromResource(R.xml.pref_general);
        findPreference("otg_switch").setOnPreferenceChangeListener(mPreferenceListener);
        mXperiaOTGPath = getSystemProperty("ro.xperia.otgpath");
        mFragmentManager = getFragmentManager();

        /** Set, only for OTG, the value to the sysfs value
         * This prevents setting OTG enabled at boot
         * We want to make people activate OTG only when then use it, as there might be a
         * risk of damaging the device when submerging it into water with OTG (currency on the USB)
         * enabled.
         */

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("otg_switch", readSysFs(mXperiaOTGPath));
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

    protected static void writeSysFs(String path, String string) {
        try {
            FileOutputStream fos = new FileOutputStream(path);
            fos.write(string.getBytes(Charset.forName("UTF-8")));
            fos.close();
        } catch (Exception e) {
            // fail silently
        }
    }

    private static Boolean readSysFs(String filePath) {
        int value = 0;
        String result = "";
        File file = new File(filePath);
        if (file.exists()) {
            FileInputStream fis = null;
            try {

                fis = new FileInputStream(file);
                char current;
                while (fis.available() > 0) {
                    current = (char) fis.read();
                    result = result + String.valueOf(current);

                }
                value = Integer.parseInt(result);
            } catch (Exception e) {
                Log.d("XperiaSettings", e.toString());
            } finally {
                if (fis != null)
                    try {
                        fis.close();
                    } catch (IOException ignored) {
                    }
            }
        }
        return value == 1;
    }

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

    private static void confirmEnablingOTG() {
        DialogFragment newFragment = new EnableOTGDialog();
        newFragment.show(mFragmentManager, "missiles");
    }
}