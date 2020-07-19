package com.example.wi_ficollector.dialog_fragment;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.wi_ficollector.R;
import com.example.wi_ficollector.activity.ScanActivity;

public class BackgroundPermissionDialogFragment extends DialogFragment {

    public interface BackgroundPermissionRationaleListener {
        void showRationale();
    }

    AlertDialog alertDialog;


    public BackgroundPermissionDialogFragment() {
        // Needed when dialog fragment is recreated
    }

    public static BackgroundPermissionDialogFragment newInstance() {
        return new BackgroundPermissionDialogFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        if (alertDialog == null) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity(), R.style.AlertDialogCustom)
                    .setTitle(R.string.background_permission_rationale_fragment_dialog)
                    .setMessage(R.string.background_permission_rationale_dialog_fragment_message)
                    .setCancelable(false)
                    .setPositiveButton(R.string.OK, (dialog, whichButton) -> {
                        ((ScanActivity) getActivity()).showRationale();
                        dismiss();
                    });

            alertDialog = alertDialogBuilder.create();
            alertDialog.setCanceledOnTouchOutside(false);
        }
        return alertDialog;
    }
}
