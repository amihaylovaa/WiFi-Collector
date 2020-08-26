package com.example.wi_ficollector.dialogfragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.wi_ficollector.listener.IntroDialogFragmentListener;
import com.example.wi_ficollector.R;

public class IntroDialogFragment extends DialogFragment {

    private AlertDialog mAlertDialog;
    private IntroDialogFragmentListener introDialogFragmentListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        introDialogFragmentListener = (IntroDialogFragmentListener) context;
    }

    public static IntroDialogFragment newInstance() {
        return new IntroDialogFragment();
    }

    public IntroDialogFragment() {
        // Needed when dialog fragment is recreated
    }

    @Override
    public void onDetach() {
        super.onDetach();
        introDialogFragmentListener = null;
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
                        introDialogFragmentListener.accept();
                        dismiss();
                    });
            mAlertDialog = alertDialogBuilder.create();
            mAlertDialog.setCanceledOnTouchOutside(false);
        }
        return mAlertDialog;
    }
}