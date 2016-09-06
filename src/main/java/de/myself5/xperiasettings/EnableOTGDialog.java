package de.myself5.xperiasettings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.util.Log;

/**
 * Created by myself5 on 9/6/16.
 * Dialog for enabling OTG.
 * Base on Googles fire missile dialog
 */
public class EnableOTGDialog extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.pref_description_otgswitch)
                .setTitle(R.string.pref_title_otgswitch)
                .setPositiveButton(R.string.enable, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        XperiaSettingsActivity.writeSysFs(XperiaSettingsActivity.mXperiaOTGPath, "1");
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SwitchPreference mSwitch = (SwitchPreference)
                                XperiaSettingsActivity.mActivity.findPreference("otg_switch");
                        mSwitch.setChecked(false);
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
