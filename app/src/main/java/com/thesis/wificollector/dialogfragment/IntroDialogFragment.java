package com.thesis.wificollector.dialogfragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.thesis.wificollector.listener.IntroDialogFragmentListener;
import com.example.wi_ficollector.R;

public class IntroDialogFragment extends DialogFragment {

    private AlertDialog mAlertDialog;
    private IntroDialogFragmentListener mIntroDialogFragmentListener;

    public IntroDialogFragment() {
        // Required during dialog fragment recreation
    }

    public static IntroDialogFragment newInstance() {
        return new IntroDialogFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mIntroDialogFragmentListener = (IntroDialogFragmentListener) context;
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
                        mIntroDialogFragmentListener.accept();
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
        mIntroDialogFragmentListener = null;
    }
}