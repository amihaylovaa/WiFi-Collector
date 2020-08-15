package com.example.wi_ficollector.dialogfragment;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.wi_ficollector.R;
import com.example.wi_ficollector.activity.MainActivity;

public class IntroDialogFragment extends DialogFragment {

    private AlertDialog mAlertDialog;

    public interface IntroDialogFragmentListener {
        void ok();
    }

    public static IntroDialogFragment newInstance() {
        return new IntroDialogFragment();
    }

    public IntroDialogFragment() {
        // Needed when dialog fragment is recreated
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Activity activity = getActivity();

        if (activity != null) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity, R.style.LocationPermissionDialogFragment)
                    .setMessage(R.string.intro)
                    .setCancelable(false)
                    .setPositiveButton(R.string.OK, (dialog, whichButton) -> {
                        ((MainActivity) activity).ok();
                        dismiss();
                    });
            mAlertDialog = alertDialogBuilder.create();
            mAlertDialog.setCanceledOnTouchOutside(false);
        }
        return mAlertDialog;
    }
}