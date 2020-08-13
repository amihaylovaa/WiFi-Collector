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

public class LocationRequestRationaleDialogFragment extends DialogFragment {

    private AlertDialog mAlertDialog;

    public interface LocationRequestRationaleListener {
        void positiveButton();

        void negativeButton();
    }

    public LocationRequestRationaleDialogFragment() {
        // Needed when dialog fragment is recreated
    }

    public static LocationRequestRationaleDialogFragment newInstance() {
        return new LocationRequestRationaleDialogFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Activity activity = getActivity();

        if (activity != null) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity, R.style.LocationPermissionDialogFragment)
                    .setMessage(R.string.location_permission_rationale_dialog_fragment_message)
                    .setCancelable(false)
                    .setNegativeButton(R.string.disagree, (dialog, whichButton) -> {
                        ((ScanActivity) getActivity()).negativeButton();
                          dismiss();
                    })
                    .setPositiveButton(R.string.agree, (dialog, whichButton) -> {
                        ((ScanActivity) getActivity()).positiveButton();
                        dismiss();
                    });

            mAlertDialog = alertDialogBuilder.create();
            mAlertDialog.setCanceledOnTouchOutside(false);
        }
        return mAlertDialog;
    }
}