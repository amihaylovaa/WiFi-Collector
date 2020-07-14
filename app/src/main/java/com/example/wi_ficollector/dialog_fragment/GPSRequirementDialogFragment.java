package com.example.wi_ficollector.dialog_fragment;


import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;


import com.example.wi_ficollector.R;
import com.example.wi_ficollector.activity.ScanActivity;

public class GPSRequirementDialogFragment extends DialogFragment {

    public static GPSRequirementDialogFragment newInstance() {
        GPSRequirementDialogFragment frag = new GPSRequirementDialogFragment();
        return frag;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setMessage(R.string.gps_requirements_fragment_dialog_message)
                .setPositiveButton(R.string.OK,
                        (dialog, whichButton) -> {
                            ((ScanActivity) getActivity()).ok();
                            dismiss();
                        }
                )
                .create();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        Log.d("Dismissed", "fragment");
    }
}
