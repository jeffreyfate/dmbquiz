package com.jeffthefate.dmbquiz.fragment;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.jeffthefate.dmbquiz.ApplicationEx;
import com.jeffthefate.dmbquiz.ApplicationEx.ResourcesSingleton;
import com.jeffthefate.dmbquiz.Constants;
import com.jeffthefate.dmbquiz.ImageViewEx;
import com.jeffthefate.dmbquiz.R;

public class FragmentLoad extends FragmentBase {
    
    private ProgressBar progress;
    private TextView networkText;
    private TextView loadingText;
    private Button retryButton;

    public FragmentLoad() {}
    
    @Override
    public void onAttach(Activity activity) {
    	super.onAttach(activity);
    	if (mCallback != null) {
    		mCallback.setHomeAsUp(false);
    		mCallback.setInSetlist(false);
    	}
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.load, container, false);
        background = (ImageViewEx) view.findViewById(R.id.Background);
        setBackgroundBitmap(getActivity(), mCallback.getBackground(), "load");
        progress = (ProgressBar) view.findViewById(R.id.Progress);
        networkText = (TextView) view.findViewById(R.id.NetworkText);
        loadingText = (TextView) view.findViewById(R.id.LoadingText);
        loadingText.setText(R.string.StatsLoading);
        retryButton = (Button) view.findViewById(R.id.RetryButton);
        retryButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                tracker.send(new HitBuilders.EventBuilder()
                        .setCategory(Constants.CATEGORY_FRAGMENT_UI)
                        .setAction(Constants.ACTION_BUTTON_PRESS)
                        .setLabel("loadRetry")
                        .setValue(1L)
                        .build());
                disableButton(true);
                if (mCallback != null) {
                    if (ApplicationEx.hasConnection()) {
                        mCallback.setNetworkProblem(false);
                        mCallback.onStatsPressed();
                    }
                    else {
                    	ApplicationEx.showLongToast(getActivity(), R.string.NoConnectionToast);
                        showNetworkProblem();
                    }
                }
            }
        });
        return view;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        if (mCallback != null) {
            if (ApplicationEx.hasConnection()) {
                mCallback.setNetworkProblem(false);
                progress.setVisibility(View.VISIBLE);
                networkText.setVisibility(View.INVISIBLE);
                loadingText.setVisibility(View.GONE);
                retryButton.setVisibility(View.INVISIBLE);
            }
            else {
                ApplicationEx.showLongToast(getActivity(), R.string.NoConnectionToast);
                showNetworkProblem();
            }
        }
    }
    
    @Override
    public void showNetworkProblem() {
        enableButton(true);
        if (mCallback != null)
            mCallback.setNetworkProblem(true);
        try {
	        progress.setVisibility(View.INVISIBLE);
	        networkText.setVisibility(View.VISIBLE);
	        loadingText.setVisibility(View.GONE);
	        retryButton.setVisibility(View.VISIBLE);
	    } catch (NullPointerException e) {}
    }
    
    @Override
    public void showLoading(String message) {
        progress.setVisibility(View.VISIBLE);
        networkText.setVisibility(View.INVISIBLE);
        retryButton.setVisibility(View.INVISIBLE);
        loadingText.setText(message);
        loadingText.setVisibility(View.VISIBLE);
    }
    
    @Override
    public void disableButton(boolean isRetry) {
        if (isRetry) {
            retryButton.setBackgroundResource(R.drawable.button_disabled);
            retryButton.setTextColor(ResourcesSingleton.instance(getActivity()).getColor(R.color.light_gray));
            retryButton.setEnabled(false);
        }
    }
    
    @Override
    public void enableButton(boolean isRetry) {
        if (isRetry) {
            retryButton.setBackgroundResource(R.drawable.button);
            retryButton.setTextColor(Color.BLACK);
            retryButton.setEnabled(true);
        }
    }
    
}
