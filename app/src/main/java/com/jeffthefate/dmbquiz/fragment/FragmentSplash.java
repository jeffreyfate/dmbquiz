package com.jeffthefate.dmbquiz.fragment;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
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

public class FragmentSplash extends FragmentBase {
    
    private EditText loginUsername;
    private EditText loginPassword;
    private LinearLayout emailButtonLayout;
    private LinearLayout buttonLayout;
    private TextView loginButton;
    private TextView signupButton;
    private TextView resetButton;
    private ImageViewEx facebookLogin;
    private ImageViewEx twitterLogin;
    private TextView playButton;
    private TextView emailButton;
    private LinearLayout emailLayout;
    
    private String username;
    private String password;
    
    private boolean isSignedUp = false;
    
    public FragmentSplash() {}
    
    @Override
    public void onAttach(Activity activity) {
    	super.onAttach(activity);
    	if (mCallback != null)
    		mCallback.setHomeAsUp(true);
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!SharedPreferencesSingleton.instance(getActivity()).contains(
        		ResourcesSingleton.instance(getActivity()).getString(R.string.notification_key))) {
        	SharedPreferencesSingleton.putBoolean(R.string.notification_key, true);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.splash, container, false);
        background = (ImageViewEx) v.findViewById(R.id.Background);
        setBackgroundBitmap(getActivity(), mCallback.getBackground(), "splash");
        loginUsername = (EditText) v.findViewById(R.id.LoginUsername);
        loginUsername.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId,
                    KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    String username = v.getText().toString().trim();
                    checkSignedUp(username);
                }
                return false;
            }
        });
        loginUsername.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String username = ((TextView)v).getText().toString().trim();
                    checkSignedUp(username);
                }
            } 
        });
        loginPassword = (EditText) v.findViewById(R.id.LoginPassword);
        loginPassword.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId,
                    KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    InputMethodManager imm = 
                        (InputMethodManager) getActivity().getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    processLogin(!isSignedUp);
                    return true;
                }
                else
                    return false;
            } 
        });
        emailButtonLayout = 
                (LinearLayout) v.findViewById(R.id.EmailButtonLayout);
        buttonLayout = (LinearLayout) v.findViewById(R.id.ButtonLayout);
        loginButton = (TextView) v.findViewById(R.id.LoginButton);
        loginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                tracker.send(new HitBuilders.EventBuilder()
                        .setCategory(Constants.CATEGORY_FRAGMENT_UI)
                        .setAction(Constants.ACTION_BUTTON_PRESS)
                        .setLabel("splashEmailLogin")
                        .setValue(1L)
                        .build());
                InputMethodManager imm = 
                    (InputMethodManager) getActivity().getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                processLogin(false);
            } 
        });
        signupButton = (TextView) v.findViewById(R.id.SignupButton);
        signupButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                tracker.send(new HitBuilders.EventBuilder()
                        .setCategory(Constants.CATEGORY_FRAGMENT_UI)
                        .setAction(Constants.ACTION_BUTTON_PRESS)
                        .setLabel("splashEmailSignup")
                        .setValue(1L)
                        .build());
                InputMethodManager imm = 
                    (InputMethodManager) getActivity().getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                processLogin(true);
            } 
        });
        resetButton = (TextView) v.findViewById(R.id.ResetButton);
        resetButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                tracker.send(new HitBuilders.EventBuilder()
                        .setCategory(Constants.CATEGORY_FRAGMENT_UI)
                        .setAction(Constants.ACTION_BUTTON_PRESS)
                        .setLabel("splashResetPassword")
                        .setValue(1L)
                        .build());
                InputMethodManager imm = 
                    (InputMethodManager) getActivity().getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                if (mCallback != null)
                	mCallback.resetPassword(
                			loginUsername.getText().toString().trim());
            } 
        });
        facebookLogin = (ImageViewEx) v.findViewById(R.id.FacebookLoginButton);
        facebookLogin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                tracker.send(new HitBuilders.EventBuilder()
                        .setCategory(Constants.CATEGORY_FRAGMENT_UI)
                        .setAction(Constants.ACTION_BUTTON_PRESS)
                        .setLabel("splashFacebook")
                        .setValue(1L)
                        .build());
                if (mCallback != null)
                    mCallback.onLoginPressed(FragmentBase.LOGIN_FACEBOOK, null,
                            null);
            }
        });
        twitterLogin = (ImageViewEx) v.findViewById(R.id.TwitterLoginButton);
        twitterLogin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                tracker.send(new HitBuilders.EventBuilder()
                        .setCategory(Constants.CATEGORY_FRAGMENT_UI)
                        .setAction(Constants.ACTION_BUTTON_PRESS)
                        .setLabel("splashTwitter")
                        .setValue(1L)
                        .build());
                if (mCallback != null)
                    mCallback.onLoginPressed(FragmentBase.LOGIN_TWITTER, null,
                            null);
            }
        });
        playButton = (TextView) v.findViewById(R.id.JustPlayButton);
        playButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                tracker.send(new HitBuilders.EventBuilder()
                        .setCategory(Constants.CATEGORY_FRAGMENT_UI)
                        .setAction(Constants.ACTION_BUTTON_PRESS)
                        .setLabel("splashJustPlay")
                        .setValue(1L)
                        .build());
                if (mCallback != null) {
                    /*
                	if (!SharedPreferencesSingleton.instance().contains(
                				res.getString(R.string.justplay_key)) ||
                				SharedPreferencesSingleton.instance().getBoolean(
            					res.getString(R.string.quicktip_key), false)) {
                		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD)
	                		SharedPreferencesSingleton.instance().edit().putBoolean(
	                        		res.getString(R.string.justplay_key), true)
	                    		.commit();
                		else
	                		SharedPreferencesSingleton.instance().edit().putBoolean(
	                        		res.getString(R.string.justplay_key), true)
	                    		.apply();
                        showQuickTipMenu(quickTipMenuView, "Stats & Standings" +
	                    		" isn't available for Just Play users",
	                    		Constants.QUICK_TIP_TOP);
                    }
                    */
                    mCallback.onLoginPressed(FragmentBase.LOGIN_ANON, null,
                            null);
                }
            } 
        });
        emailLayout = (LinearLayout) v.findViewById(R.id.EmailLayout);
        emailButton = (TextView) v.findViewById(R.id.EmailButton);
        emailButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                tracker.send(new HitBuilders.EventBuilder()
                        .setCategory(Constants.CATEGORY_FRAGMENT_UI)
                        .setAction(Constants.ACTION_BUTTON_PRESS)
                        .setLabel("splashEmail")
                        .setValue(1L)
                        .build());
                playButton.clearAnimation();
                playButton.setVisibility(View.INVISIBLE);
                emailButton.clearAnimation();
                emailButton.setVisibility(View.INVISIBLE);
                emailLayout.setVisibility(View.VISIBLE);
                if (buttonLayout != null)
                    buttonLayout.setVisibility(View.GONE);
            } 
        });
        return v;
    }
    
    @Override
    public void onResume() {
    	super.onResume();
        Log.i(Constants.LOG_TAG, "FragmentSplash onResume");
	    /*
        showQuickTipMenu(quickTipLeftView, "Swipe from left for menu",
        		Constants.QUICK_TIP_LEFT);
		*/
    	SharedPreferencesSingleton.putBoolean(R.string.menu_key, true);
    }
    
    private void processLogin(boolean signUp) {
        username = loginUsername.getText().toString().trim();
        password = loginPassword.getText().toString().trim();
        if (loginUsername.getText().length() > 0 &&
                username.matches(
                        "[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?" +
                        "^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+" +
                        "[a-z0-9](?:[a-z0-9-]*[a-z0-9])?")) {
            if (loginPassword.getText().length() >= 4) {
                if (signUp) {
                    if (mCallback != null)
                        mCallback.onLoginPressed(FragmentBase.SIGNUP_EMAIL,
                                username, password);
                }
                else {
                    if (mCallback != null)
                        mCallback.onLoginPressed(FragmentBase.LOGIN_EMAIL,
                                username, password);
                }
            }
            else
                ApplicationEx.showLongToast(
                        "Password must be at least 4 characters");
        }
        else
            ApplicationEx.showLongToast("Enter valid email");
    }

    // TODO Use AsyncTask instead
    private void checkSignedUp(final String username) {
        BackendlessDataQuery query = new BackendlessDataQuery();
        query.setWhereClause("username = '" + username + "'");
        Backendless.Persistence.find(BackendlessUser.class, query,
                new AsyncCallback<BackendlessCollection<BackendlessUser>>() {
            @Override
            public void handleResponse(BackendlessCollection<BackendlessUser> userCollection) {
                List<BackendlessUser> userList = userCollection.getCurrentPage();
                if (emailButtonLayout != null) {
                    emailButtonLayout.setVisibility(View.VISIBLE);
                }
                if (userList.isEmpty()) {
                    loginButton.setVisibility(View.GONE);
                    signupButton.setVisibility(View.VISIBLE);
                    resetButton.setVisibility(View.GONE);
                    isSignedUp = false;
                }
                else {
                    signupButton.setVisibility(View.GONE);
                    loginButton.setVisibility(View.VISIBLE);
                    resetButton.setVisibility(View.VISIBLE);
                    isSignedUp = true;
                    userList.get(0).setEmail(username);
                    Backendless.UserService.update(userList.get(0), new AsyncCallback<BackendlessUser>() {
                        @Override
                        public void handleResponse(BackendlessUser response) {
                            Log.i(Constants.LOG_TAG, response.toString());
                        }

                        @Override
                        public void handleFault(BackendlessFault fault) {
                            Log.i(Constants.LOG_TAG, fault.toString());
                        }
                    });
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                // TODO
            }
        });
    }
    
}