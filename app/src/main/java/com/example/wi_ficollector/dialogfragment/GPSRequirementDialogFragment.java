package com.example.wi_ficollector.dialogfragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.wi_ficollector.R;
import com.example.wi_ficollector.listener.GPSRequirementsListener;


public class GPSRequirementDialogFragment extends DialogFragment {

    private AlertDialog mAlertDialog;
    private GPSRequirementsListener mGPSRequirementsListener;

    public GPSRequirementDialogFragment() {
        // Required during dialog fragment recreation
    }

    public static GPSRequirementDialogFragment newInstance() {
        return new GPSRequirementDialogFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mGPSRequirementsListener = (GPSRequirementsListener) context;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Activity activity = getActivity();

        if (activity != null) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity, R.style.GPSDialogFragment)
                    .setMessage(R.string.gps_requirements_fragment_dialog_message)
                    .setPositiveButton(R.string.OK, (dialog, whichButton) -> {
                        mGPSRequirementsListener.startGPSRequirementsResolution();
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
        mGPSRequirementsListener = null;
    }
}