package com.example.wi_ficollector.dialog_fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.wi_ficollector.R;
import com.example.wi_ficollector.activity.ScanActivity;


// todo attach
// todo dismiss
public class GPSRequirementDialogFragment extends DialogFragment {

    public interface GPSDialogListener {
        void startResolution();
    }

    private AlertDialog mAlertDialog;

    public GPSRequirementDialogFragment() {
        // Needed when dialog fragment is recreated
    }

    public static GPSRequirementDialogFragment newInstance() {
        return new GPSRequirementDialogFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        if (mAlertDialog == null) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.gps_requirements_title)
                    .setMessage(R.string.gps_requirements_fragment_dialog_message)
                    .setPositiveButton(R.string.OK, (dialog, whichButton) -> {
                        ((ScanActivity) getActivity()).startResolution();
                        dismiss();
                    });

            mAlertDialog = alertDialogBuilder.create();
            mAlertDialog.setCanceledOnTouchOutside(false);
        }
        return mAlertDialog;
    }
}
