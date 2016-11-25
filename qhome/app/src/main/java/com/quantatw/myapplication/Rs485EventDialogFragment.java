package com.quantatw.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Created by qic on 2016/5/20.
 */
public class Rs485EventDialogFragment extends DialogFragment {
    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface NoticeDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogDismiss(DialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    NoticeDialogListener mListener;

    /**
     * Create a new instance of MyDialogFragment, providing "num"
     * as an argument.
     */
    static Rs485EventDialogFragment newInstance(String msg) {
        Rs485EventDialogFragment f = new Rs485EventDialogFragment();

        // Supply msg input as an argument.
        Bundle args = new Bundle();
        args.putString("msg", msg);
        f.setArguments(args);

        return f;
    }

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (NoticeDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String msg = getArguments().getString("msg");
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(R.drawable.ic_warning_black)
                .setTitle(msg)
                .setPositiveButton("Call guards", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // call guardroom
                        mListener.onDialogPositiveClick(Rs485EventDialogFragment.this);
                    }
                })
                .setNegativeButton(R.string.dialog_dismiss, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        mListener.onDialogDismiss(Rs485EventDialogFragment.this);
    }
}
