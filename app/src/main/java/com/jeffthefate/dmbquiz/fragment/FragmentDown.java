package com.jeffthefate.dmbquiz.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.BackendlessDataQuery;
import com.google.android.gms.analytics.HitBuilders;
import com.jeffthefate.dmbquiz.ApplicationEx;
import com.jeffthefate.dmbquiz.ApplicationEx.ResourcesSingleton;
import com.jeffthefate.dmbquiz.ApplicationEx.SharedPreferencesSingleton;
import com.jeffthefate.dmbquiz.Constants;
import com.jeffthefate.dmbquiz.ImageViewEx;
import com.jeffthefate.dmbquiz.R;

import java.util.List;

public class FragmentDown extends FragmentBase {

    public FragmentDown() {}
    
    @Override
    public void onAttach(Activity activity) {
    	super.onAttach(activity);
        Log.i(Constants.LOG_TAG, "FragmentDown onAttach");
    	if (mCallback != null) {
            mCallback.setHomeAsUp(true);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	super.onCreateView(inflater, container, savedInstanceState);
        Log.i(Constants.LOG_TAG, "FragmentDown onCreateView");
        View v = inflater.inflate(R.layout.down, container, false);
        background = (ImageViewEx) v.findViewById(R.id.Background);
        setBackgroundBitmap(getActivity(), mCallback.getBackground(), "splash");
        return v;
    }
    
}