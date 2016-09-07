package sonyxperiadev.extendedsettings;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.util.Log;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class ExtendedSettingsActivity extends AppCompatPreferenceActivity {

    private static String TAG = "ExtendedSettings";
    protected static String PREF_ID_POLL_ENABLED = "sys.device.usb.id_poll_enable";
    protected static String PREF_ADB_NETWORK_COM = "adb.network.port";
    private static String PREF_ADB_NETWORK_READ = "service.adb.tcp.port";
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
                        setSystemProperty(PREF_ID_POLL_ENABLED, "false");
                    }
                    break;
                case "adbon_switch":
                    if ((Boolean) value) {
                        confirmEnablingADBON();
                    } else {
                        setSystemProperty(PREF_ADB_NETWORK_COM, "-1");
                        updateADBSummary();
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
        mFragmentManager = getFragmentManager();

        /** Set, only for OTG, the value to the sysfs value
         * This prevents setting OTG enabled at boot
         * We want to make people activate OTG only when then use it, as there might be a
         * risk of damaging the device when submerging it into water with OTG (currency on the USB)
         * enabled.
         */

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        String idPoll = getSystemProperty(PREF_ID_POLL_ENABLED);
        String adbN = getSystemProperty(PREF_ADB_NETWORK_READ);
        if (idPoll != null)
            editor.putBoolean("otg_switch", Boolean.valueOf(idPoll));
        if (adbN != null) {
            editor.putBoolean("adbon_switch", !adbN.equals("-1"));
        }
        updateADBSummary();
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

    protected static String getSystemProperty(String key) {
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

    private static void confirmEnablingOTG() {
        DialogFragment newFragment = new EnableOTGDialog();
        newFragment.show(mFragmentManager, "otg");
    }

    private static void confirmEnablingADBON() {
        DialogFragment newFragment = new EnableADBONDialog();
        newFragment.show(mFragmentManager, "adb");
    }

    protected static void updateADBSummary() {
        boolean enabled = !getSystemProperty(PREF_ADB_NETWORK_READ).equals("-1");
        SwitchPreference mAdbOverNetwork = (SwitchPreference) ExtendedSettingsActivity.mActivity.findPreference("adbon_switch");

        if (enabled) {
            WifiManager wifiManager = (WifiManager) mActivity.getSystemService(WIFI_SERVICE);
            int ipAddress = wifiManager.getConnectionInfo().getIpAddress();

            // Convert little-endian to big-endianif needed
            if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
                ipAddress = Integer.reverseBytes(ipAddress);
            }

            byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();

            String ipAddressString;
            try {
                ipAddressString = InetAddress.getByAddress(ipByteArray).getHostAddress();
            } catch (UnknownHostException ex) {
                Log.e("WIFIIP", "Unable to get host address.");
                ipAddressString = null;
            }
            if (ipAddressString != null) {
                mAdbOverNetwork.setSummary(ipAddressString + ":" + getSystemProperty(PREF_ADB_NETWORK_READ));
            } else {
                mAdbOverNetwork.setSummary(R.string.error_connect_to_wifi);
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("adbon_switch", false);
                editor.apply();
                setSystemProperty(PREF_ADB_NETWORK_COM, "-1");
                mAdbOverNetwork.setChecked(false);
            }
        } else {
            mAdbOverNetwork.setSummary(R.string.pref_description_adbonswitch);
        }
    }
}