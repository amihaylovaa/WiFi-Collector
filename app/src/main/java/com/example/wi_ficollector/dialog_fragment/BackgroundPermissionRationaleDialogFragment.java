package com.example.wi_ficollector.dialog_fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.wi_ficollector.activity.ScanActivity;

import static com.example.wi_ficollector.utils.Constants.BACKGROUND_PERMISSION_REQUEST_RATIONALE;

public class BackgroundPermissionRationaleDialogFragment extends DialogFragment {

    public interface BackgroundPermissionRationale {
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
            Log.d("ALERT", "DIALOG");
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity())
                    .setTitle("Background Permission")
                    .setMessage(BACKGROUND_PERMISSION_REQUEST_RATIONALE)
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

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        Log.d("DISMISSED", "STATE");
    }

}
