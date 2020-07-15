package com.example.wi_ficollector.dialog_fragment;

import android.app.Dialog;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.wi_ficollector.activity.ScanActivity;


// todo attach
// todo dismiss
public class GPSRequirementDialogFragment extends DialogFragment {

    public interface GPSDialogListener {
        void startResolution();
    }

    private static final String TITLE;
    private static final String MESSAGE;
    private static final String BUTTON_KEY;
    private AlertDialog mAlertDialog;

    static {
        TITLE = "title";
        MESSAGE = "message";
        BUTTON_KEY = "buttonValue";
    }

    public GPSRequirementDialogFragment() {
        // Needed when dialog fragment is recreated
    }

    public static GPSRequirementDialogFragment newInstance(String title, String message, String buttonOk) {
        GPSRequirementDialogFragment gpsRequirementDialogFragment = new GPSRequirementDialogFragment();
        Bundle args = new Bundle();

        args.putString(TITLE, title);
        args.putString(MESSAGE, message);
        args.putString(BUTTON_KEY, buttonOk);
        gpsRequirementDialogFragment.setArguments(args);

        return gpsRequirementDialogFragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Bundle args = getArguments();
        String title = null;
        String message = null;
        String buttonOk = null;

        if (args != null) {
            title = args.getString(TITLE);
            message = args.getString(MESSAGE);
            buttonOk = args.getString(BUTTON_KEY);
        }

        if (mAlertDialog == null) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity())
                    .setTitle(title)
                    .setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton(buttonOk, (dialog, whichButton) -> {
                        ((ScanActivity) getActivity()).startResolution();
                        dismiss();
                    });

            mAlertDialog = alertDialogBuilder.create();
            mAlertDialog.setCanceledOnTouchOutside(false);
        }
        return mAlertDialog;
    }
}
