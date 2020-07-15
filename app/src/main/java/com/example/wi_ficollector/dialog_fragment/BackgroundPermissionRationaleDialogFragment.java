package com.example.wi_ficollector.dialog_fragment;

import android.app.Dialog;
import android.os.Bundle;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.wi_ficollector.activity.ScanActivity;

public class BackgroundPermissionRationaleDialogFragment extends DialogFragment {

    public interface BackgroundPermissionRationaleListener {
        void showRationale();
    }

    private static final String TITLE;
    private static final String MESSAGE;
    private static final String BUTTON_KEY;
    AlertDialog alertDialog;

    static {
        TITLE = "title";
        MESSAGE = "message";
        BUTTON_KEY = "buttonValue";
    }

    public BackgroundPermissionRationaleDialogFragment() {
        // Needed when dialog fragment is recreated
    }

    public static BackgroundPermissionRationaleDialogFragment newInstance(String title, String message, String buttonOk) {
        BackgroundPermissionRationaleDialogFragment backgroundPermissionRationaleDialogFragment = new BackgroundPermissionRationaleDialogFragment();
        Bundle args = new Bundle();

        args.putString(TITLE, title);
        args.putString(MESSAGE, message);
        args.putString(BUTTON_KEY, buttonOk);
        backgroundPermissionRationaleDialogFragment.setArguments(args);

        return backgroundPermissionRationaleDialogFragment;
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

        if (alertDialog == null) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity())
                    .setTitle(title)
                    .setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton(buttonOk, (dialog, whichButton) -> {
                        ((ScanActivity) getActivity()).showRationale();
                        dismiss();
                    });

            alertDialog = alertDialogBuilder.create();
            alertDialog.setCanceledOnTouchOutside(false);
        }
        return alertDialog;
    }
}
