package com.example.wi_ficollector.dialogfragment;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.wi_ficollector.R;
import com.example.wi_ficollector.activity.ScanActivity;


public class GPSRequirementDialogFragment extends DialogFragment {

    private AlertDialog mAlertDialog;

    public interface GPSRequirementsListener {
        void startGPSResolution();
    }

    public GPSRequirementDialogFragment() {
        // Needed when dialog fragment is recreated
    }

    public static GPSRequirementDialogFragment newInstance() {
        return new GPSRequirementDialogFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            Activity activity = getActivity();

            if (activity != null) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity, R.style.GPSDialogFragment)
                        .setMessage(R.string.gps_requirements_fragment_dialog_message)
                        .setPositiveButton(R.string.OK, (dialog, whichButton) -> {
                            ((ScanActivity) activity).startGPSResolution();
                            dismiss();
                        });

                mAlertDialog = alertDialogBuilder.create();
                mAlertDialog.setCanceledOnTouchOutside(false);
            }
        return mAlertDialog;
    }
}