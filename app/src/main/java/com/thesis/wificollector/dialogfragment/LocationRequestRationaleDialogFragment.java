package com.thesis.wificollector.dialogfragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.wi_ficollector.R;
import com.thesis.wificollector.listener.LocationPermissionRequestRationaleListener;

public class LocationRequestRationaleDialogFragment extends DialogFragment {

    private AlertDialog mAlertDialog;
    private LocationPermissionRequestRationaleListener mLocationPermissionRequestRationaleListener;

    public LocationRequestRationaleDialogFragment() {
        // Required during dialog fragment recreation
    }

    public static LocationRequestRationaleDialogFragment newInstance() {
        return new LocationRequestRationaleDialogFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mLocationPermissionRequestRationaleListener = (LocationPermissionRequestRationaleListener) context;
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
                        mLocationPermissionRequestRationaleListener.disagree();
                        dismiss();
                    })
                    .setPositiveButton(R.string.agree, (dialog, whichButton) -> {
                        mLocationPermissionRequestRationaleListener.agree();
                        dismiss();
                    });

            mAlertDialog = alertDialogBuilder.create();
            mAlertDialog.setCanceledOnTouchOutside(false);
        }
        return mAlertDialog;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mLocationPermissionRequestRationaleListener = null;
    }
}