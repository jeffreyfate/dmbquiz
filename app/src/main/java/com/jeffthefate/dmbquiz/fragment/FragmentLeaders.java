package com.jeffthefate.dmbquiz.fragment;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.jeffthefate.dmbquiz.ApplicationEx;
import com.jeffthefate.dmbquiz.ApplicationEx.DatabaseHelperSingleton;
import com.jeffthefate.dmbquiz.ApplicationEx.ResourcesSingleton;
import com.jeffthefate.dmbquiz.ApplicationEx.SharedPreferencesSingleton;
import com.jeffthefate.dmbquiz.DatabaseHelper;
import com.jeffthefate.dmbquiz.ImageViewEx;
import com.jeffthefate.dmbquiz.LeaderAdapter;
import com.jeffthefate.dmbquiz.R;

public class FragmentLeaders extends FragmentBase {
    
    private TextView userText;
    private TextView userAnswerText;
    private TextView userAnswers;
    private TextView userHintText;
    private TextView userHints;
    
    private TextView leaderText;
    private ListView leaderList;
    
    private TextView userRank;
    private TextView userName;
    private TextView userScore;
    
    private TextView createdText;
    private TextView createdDate;
    
    private ArrayList<String> rankList;
    private ArrayList<String> userList;
    private ArrayList<String> scoreList;
    private ArrayList<String> userIdList;
    
    public FragmentLeaders() {}
    
    @Override
    public void onAttach(Activity activity) {
    	super.onAttach(activity);
    	if (mCallback != null) {
    		mCallback.setHomeAsUp(true);
    		mCallback.setInSetlist(false);
    	}
    }
    
    private boolean isRestored = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.leaders, container, false);
        background = (ImageViewEx) v.findViewById(R.id.Background);
        setBackgroundBitmap(getActivity(), mCallback.getBackground(), "leaders");
        userText = (TextView) v.findViewById(R.id.UserText);
        userAnswerText = (TextView) v.findViewById(R.id.Stat1Name);
        userAnswers = (TextView) v.findViewById(R.id.Stat1Score);
        userHintText = (TextView) v.findViewById(R.id.Stat2Name);
        userHints = (TextView) v.findViewById(R.id.Stat2Score);
        leaderText = (TextView) v.findViewById(R.id.LeadersText);
        leaderList = (ListView) v.findViewById(android.R.id.list);
        userRank = (TextView) v.findViewById(R.id.UserRank);
        userName = (TextView) v.findViewById(R.id.UserName);
        userScore = (TextView) v.findViewById(R.id.UserScore);
        createdText = (TextView) v.findViewById(R.id.LastQuestionText);
        createdDate = (TextView) v.findViewById(R.id.LastQuestionDate);
        if (savedInstanceState != null) {
            userText.setText(savedInstanceState.getString("userText"));
            userAnswerText.setText(savedInstanceState.getString(
                    "userAnswerText"));
            userAnswers.setText(savedInstanceState.getString("userAnswers"));
            userHintText.setText(savedInstanceState.getString("userHintText"));
            userHints.setText(savedInstanceState.getString("userHints"));
            userRank.setText(savedInstanceState.getString("userRank"));
            userName.setText(savedInstanceState.getString("userName"));
            userScore.setText(savedInstanceState.getString("userScore"));
            leaderText.setText(savedInstanceState.getString("leaderText"));
            rankList = savedInstanceState.getStringArrayList("rank");
            userList = savedInstanceState.getStringArrayList("user");
            scoreList = savedInstanceState.getStringArrayList("score");
            userIdList = savedInstanceState.getStringArrayList("userIdList");
            createdText.setText(savedInstanceState.getString("createdText"));
            createdDate.setText(savedInstanceState.getString("createdDate"));
            if (mCallback != null && mCallback.getUserId() != null &&
                    rankList != null && userList != null && scoreList != null &&
                    userIdList != null) {
                leaderList.setAdapter(new LeaderAdapter(getActivity(),
                        mCallback.getUserId(), rankList, userList, scoreList, userIdList,
                        R.layout.row_standings, new String[] {"name", "score"},
                        new int[] {R.id.text1, R.id.text2}));
                isRestored = true;
            }
        }
        else if (mCallback != null && (mCallback.getLeadersState() == null ||
                mCallback.getLeadersState().isEmpty())) {
            if (mCallback.getUserId() != null) {
                userText.setText(DatabaseHelperSingleton.instance(getActivity()).getUserStringValue(
                        DatabaseHelper.COL_USER_TEXT, mCallback.getUserId()));
                userAnswerText.setText(
                        DatabaseHelperSingleton.instance(getActivity()).getUserStringValue(
                                DatabaseHelper.COL_USER_ANSWER_TEXT,
                                mCallback.getUserId()));
                userAnswers.setText(DatabaseHelperSingleton.instance(getActivity()).getUserStringValue(
                        DatabaseHelper.COL_USER_ANSWERS,
                                mCallback.getUserId()));
                userHintText.setText(DatabaseHelperSingleton.instance(getActivity()).getUserStringValue(
                        DatabaseHelper.COL_USER_HINT_TEXT,
                                mCallback.getUserId()));
                userHints.setText(DatabaseHelperSingleton.instance(getActivity()).getUserStringValue(
                        DatabaseHelper.COL_USER_HINTS, mCallback.getUserId()));
                userRank.setText(DatabaseHelperSingleton.instance(getActivity()).getUserStringValue(
                		DatabaseHelper.COL_USER_RANK_TEXT,
                				mCallback.getUserId()));
                userName.setText(DatabaseHelperSingleton.instance(getActivity()).getUserStringValue(
                        DatabaseHelper.COL_USER_NAME_TEXT,
                                mCallback.getUserId()));
                userScore.setText(DatabaseHelperSingleton.instance(getActivity()).getUserStringValue(
                        DatabaseHelper.COL_USER_SCORE_TEXT,
                                mCallback.getUserId()));
                leaderText.setText(DatabaseHelperSingleton.instance(getActivity()).getUserStringValue(
                        DatabaseHelper.COL_LEADER_TEXT, mCallback.getUserId()));
                createdText.setText(DatabaseHelperSingleton.instance(getActivity()).getUserStringValue(
                        DatabaseHelper.COL_CREATED_TEXT,
                                mCallback.getUserId()));
                createdDate.setText(DatabaseHelperSingleton.instance(getActivity()).getUserStringValue(
                        DatabaseHelper.COL_CREATED_DATE,
                                mCallback.getUserId()));
                rankList = DatabaseHelperSingleton.instance(getActivity()).getLeaderRanks();
                userList = DatabaseHelperSingleton.instance(getActivity()).getLeaderUsers();
                scoreList = DatabaseHelperSingleton.instance(getActivity()).getLeaderScores();
                userIdList = DatabaseHelperSingleton.instance(getActivity()).getLeaderIds();
                if (mCallback.getUserId() != null && rankList != null &&
                        userList != null && scoreList != null &&
                        userIdList != null) {
                    leaderList.setAdapter(new LeaderAdapter(
                            getActivity(), mCallback.getUserId(),
                            rankList, userList, scoreList, userIdList,
                            R.layout.row_standings,
                            new String[] {"name", "score"},
                            new int[] {R.id.text1, R.id.text2}));
                    isRestored = true;
                }
            }
        }
        return v;
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("userText", userText.getText().toString());
        outState.putString("userAnswerText",
                userAnswerText.getText().toString());
        outState.putString("userAnswers", userAnswers.getText().toString());
        outState.putString("userHintText", userHintText.getText().toString());
        outState.putString("userHints", userHints.getText().toString());
        outState.putString("userRank", userRank.getText().toString());
        outState.putString("userName", userName.getText().toString());
        outState.putString("userScore", userScore.getText().toString());
        outState.putString("leaderText", leaderText.getText().toString());
        outState.putStringArrayList("rank", rankList);
        outState.putStringArrayList("user", userList);
        outState.putStringArrayList("score", scoreList);
        outState.putStringArrayList("userIdList", userIdList);
        outState.putString("createdText", createdText.getText().toString());
        outState.putString("createdDate", createdDate.getText().toString());
        super.onSaveInstanceState(outState);
    }
    
    @Override
    public void onPause() {
        DatabaseHelperSingleton.instance(getActivity()).setUserValue(userText.getText().toString(),
                DatabaseHelper.COL_USER_TEXT, mCallback.getUserId());
        DatabaseHelperSingleton.instance(getActivity()).setUserValue(userAnswerText.getText().toString(),
                DatabaseHelper.COL_USER_ANSWER_TEXT, mCallback.getUserId());
        DatabaseHelperSingleton.instance(getActivity()).setUserValue(userAnswers.getText().toString(),
                DatabaseHelper.COL_USER_ANSWERS, mCallback.getUserId());
        DatabaseHelperSingleton.instance(getActivity()).setUserValue(userHintText.getText().toString(),
                DatabaseHelper.COL_USER_HINT_TEXT, mCallback.getUserId());
        DatabaseHelperSingleton.instance(getActivity()).setUserValue(userHints.getText().toString(),
                DatabaseHelper.COL_USER_HINTS, mCallback.getUserId());
        DatabaseHelperSingleton.instance(getActivity()).setUserValue(userRank.getText().toString(),
                DatabaseHelper.COL_USER_RANK_TEXT, mCallback.getUserId());
        DatabaseHelperSingleton.instance(getActivity()).setUserValue(userName.getText().toString(),
                DatabaseHelper.COL_USER_NAME_TEXT, mCallback.getUserId());
        DatabaseHelperSingleton.instance(getActivity()).setUserValue(userScore.getText().toString(),
                DatabaseHelper.COL_USER_SCORE_TEXT, mCallback.getUserId());
        DatabaseHelperSingleton.instance(getActivity()).setUserValue(leaderText.getText().toString(),
                DatabaseHelper.COL_LEADER_TEXT, mCallback.getUserId());
        DatabaseHelperSingleton.instance(getActivity()).setUserValue(createdText.getText().toString(),
                DatabaseHelper.COL_CREATED_TEXT, mCallback.getUserId());
        DatabaseHelperSingleton.instance(getActivity()).setUserValue(createdDate.getText().toString(),
                DatabaseHelper.COL_CREATED_DATE, mCallback.getUserId());
        super.onPause();
    }
    
    @Override
    public void onStart() {
    	super.onStart();
    	tracker.setScreenName("ActivityMain/FragmentLeaders");
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }
    
    @Override
    public void onResume() {
        super.onResume();
        if (mCallback != null && !SharedPreferencesSingleton.instance(getActivity()).contains(
        		ResourcesSingleton.instance(getActivity()).getString(R.string.dialog_key))) {
            mCallback.showScoreDialog();
            SharedPreferencesSingleton.putBoolean(R.string.dialog_key, true);
        }
        if (!isRestored && mCallback != null &&
                mCallback.getLeadersState() != null) {
            Bundle leadersBundle = mCallback.getLeadersState();
            userAnswers.setText(leadersBundle.getString("userAnswers"));
            userHints.setText(leadersBundle.getString("userHints"));
            userRank.setText(leadersBundle.getString("userRank"));
            userName.setText(leadersBundle.getString("userName"));
            userScore.setText(leadersBundle.getString("userScore"));
            rankList = leadersBundle.getStringArrayList("rank");
            userList = leadersBundle.getStringArrayList("user");
            scoreList = leadersBundle.getStringArrayList("score");
            userIdList = leadersBundle.getStringArrayList("userIdList");
            createdText.setText("Latest question");
            createdDate.setText(leadersBundle.getString("lastQuestion"));
            if (mCallback.getUserId() != null && rankList != null &&
                    userList != null && scoreList != null &&
                    userIdList != null) {
                leaderList.setAdapter(new LeaderAdapter(getActivity(),
                        mCallback.getUserId(), rankList, userList, scoreList,
                        userIdList, R.layout.row_standings,
                        new String[] {"name", "score"},
                        new int[] {R.id.text1, R.id.text2}));
            }
        }
        if (!SharedPreferencesSingleton.instance(getActivity()).contains(
        		ResourcesSingleton.instance(getActivity()).getString(R.string.menu_key))) {
            /*
	        showQuickTipMenu(quickTipLeftView, "Swipe from left for menu",
	        		Constants.QUICK_TIP_LEFT);
	        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD)
	        	SharedPreferencesSingleton.instance().edit().putBoolean(
	        			res.getString(R.string.menu_key), true).commit();
	        else
	        	SharedPreferencesSingleton.instance().edit().putBoolean(
	        			res.getString(R.string.menu_key), true).apply();
			*/
        }
    }
    
    @Override
    public void setDisplayName(String displayName) {
        if (userName != null && displayName != null) {
            userName.setText(displayName);
            if (mCallback != null)
                mCallback.setUserName(displayName);
        }
    }
    
    @Override
    public void setBackground(Context context, Bitmap newBackground) {
    	if (background != null && newBackground != null) {
        	background.setImageDrawable(null);
        	BitmapDrawable bitmapDrawable = new BitmapDrawable(
        			ResourcesSingleton.instance(context), newBackground);
        	bitmapDrawable.setColorFilter(new PorterDuffColorFilter(
        			ResourcesSingleton.instance(context).getColor(
        					R.color.background_dark),
					PorterDuff.Mode.SRC_ATOP));
        	background.setImageDrawable(bitmapDrawable);
        }
    }
    
}