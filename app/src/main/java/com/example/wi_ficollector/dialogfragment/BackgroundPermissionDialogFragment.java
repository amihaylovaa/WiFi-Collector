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

public class BackgroundPermissionDialogFragment extends DialogFragment {

    private AlertDialog mAlertDialog;

    public interface BackgroundPermissionRequestRationaleListener {
        void showRationale();
    }

    public BackgroundPermissionDialogFragment() {
        // Needed when dialog fragment is recreated
    }

    public static BackgroundPermissionDialogFragment newInstance() {
        return new BackgroundPermissionDialogFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Activity activity = getActivity();

        if (activity != null) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity, R.style.BackgroundPermissionDialogFragment)
                    .setMessage(R.string.background_permission_rationale_dialog_fragment_message)
                    .setCancelable(false)
                    .setPositiveButton(R.string.OK, (dialog, whichButton) -> {
                        ((ScanActivity) getActivity()).showRationale();
                        dismiss();
                    });

            mAlertDialog = alertDialogBuilder.create();
            mAlertDialog.setCanceledOnTouchOutside(false);
        }
        return mAlertDialog;
    }
}
