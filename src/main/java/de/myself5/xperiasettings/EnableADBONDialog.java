package de.myself5.xperiasettings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.SwitchPreference;

/**
 * Created by myself5 on 9/6/16.
 * Dialog for enabling ADB Over Network.
 * Base on Googles fire missile dialog
 */
public class EnableADBONDialog extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.pref_description_adbonswitchdialog)
                .setTitle(R.string.pref_title_adbonswitch)
                .setPositiveButton(R.string.enable, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        XperiaSettingsActivity.setSystemProperty(XperiaSettingsActivity.PREF_ADB_NETWORK, "5555");
/*                        XperiaSettingsActivity.restartADBD();*/
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SwitchPreference mSwitch = (SwitchPreference)
                                XperiaSettingsActivity.mActivity.findPreference("adbon_switch");
                        mSwitch.setChecked(false);
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
