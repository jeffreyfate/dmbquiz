package com.jeffthefate.dmbquiz.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.jeffthefate.dmbquiz.R;
import com.jeffthefate.dmbquiz.activity.ActivityMain;

public class FragmentNameDialog extends DialogFragment {
    
    private ActivityMain mActivity;

    private Tracker tracker;
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    	tracker = GoogleAnalytics.getInstance(activity).newTracker(R.xml.global_tracker);
        mActivity = (ActivityMain) activity;
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View v = inflater.inflate(R.layout.name, null);
        final EditText newName = (EditText) v.findViewById(R.id.NameDialogText);
        newName.setText(mActivity.getDisplayName());
        newName.setSelection(newName.getText().length());
        newName.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId,
                    KeyEvent event) {
                String entry = null;
                if (actionId == EditorInfo.IME_ACTION_DONE ||
                        event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    entry = v.getEditableText().toString();
                    InputMethodManager imm = 
                        (InputMethodManager) getActivity().getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    mActivity.setDisplayName(entry);
                    return true;
                }
                else
                    return false;
            } 
        });
        builder.setView(v).setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {}
                });
        builder.setView(v).setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = newName.getEditableText().toString();
                        mActivity.setDisplayName(name);
                    }
                });
        return builder.create();
    }
    
    @Override
    public void onStart() {
    	super.onStart();
        tracker.setScreenName("ActivityMain/FragmentLeaders/FragmentNameDialog");
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }
}