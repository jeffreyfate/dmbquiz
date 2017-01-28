package com.jeffthefate.dmbquiz.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.google.android.gms.analytics.HitBuilders;
import com.jeffthefate.dmbquiz.ApplicationEx;
import com.jeffthefate.dmbquiz.ApplicationEx.DatabaseHelperSingleton;
import com.jeffthefate.dmbquiz.ApplicationEx.ResourcesSingleton;
import com.jeffthefate.dmbquiz.ApplicationEx.SharedPreferencesSingleton;
import com.jeffthefate.dmbquiz.Constants;
import com.jeffthefate.dmbquiz.DatabaseHelper;
import com.jeffthefate.dmbquiz.ImageViewEx;
import com.jeffthefate.dmbquiz.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class FragmentQuiz extends FragmentBase {
    
    private TextView scoreText;
    private TextView questionText;
    private EditText answerText;
    private TextView answerPlace;
    private Button answerButton;
    private RelativeLayout skipButton;
    private TextView skipText;
    private TextView skipTime;
    private RelativeLayout hintButton;
    private TextView hintText;
    private TextView hintTime;
    private ImageViewEx answerImage;
    private TextView retryText;
    private Button retryButton;
    private Button upLevelButton;
    
    private long skipTick = 17000;
    private long hintTick = 15000;
    
    private SkipTimer skipTimer;
    private HintTimer hintTimer;
    private WrongTimer wrongTimer;
    
    private HintTask hintTask;
    // TODO: Revisit when Smart Keyboard fixes text issue on rotate
    //private String savedAnswer;
    private String savedHint;
    private int currScore;
    
    private boolean hintPressed = false;
    private boolean skipPressed = false;
    
    private boolean isCorrect = false;
    
    private String answerTextHint = null;
    
    private InputMethodManager imm;
    
    private HashMap<String, Boolean> stagedMap = new HashMap<String, Boolean>();
    private HashMap<String, Boolean> saveMap = new HashMap<String, Boolean>();
    private HashMap<String, Boolean> correctMap = new HashMap<String, Boolean>();
    
    public FragmentQuiz() {
    }
    
    @Override
    public void onAttach(Activity activity) {
    	super.onAttach(activity);
    	if (mCallback != null)
    		mCallback.setHomeAsUp(true);
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (!SharedPreferencesSingleton.instance().contains(
        		ResourcesSingleton.instance().getString(R.string.sound_key))) {
        	SharedPreferencesSingleton.putBoolean(R.string.sound_key, true);
        }
        if (!SharedPreferencesSingleton.instance().contains(
        		ResourcesSingleton.instance().getString(R.string.quicktip_key))) {
        	SharedPreferencesSingleton.putBoolean(R.string.quicktip_key, true);
        }
        imm = (InputMethodManager) getActivity().getSystemService(
                    Context.INPUT_METHOD_SERVICE);
        if (savedInstanceState != null) {
            /*
            savedAnswer = savedInstanceState.getString("answer");
            DatabaseHelperSingleton.instance().setUserValue(savedAnswer,
                    DatabaseHelper.COL_ANSWER, mCallback.getUserId());
            */
            savedHint = savedInstanceState.getString("hint");
            DatabaseHelperSingleton.instance().setUserValue(savedHint,
                    DatabaseHelper.COL_HINT, mCallback.getUserId());
            skipTick = savedInstanceState.getLong("skipTick");
            DatabaseHelperSingleton.instance().setUserValue((int) skipTick,
                    DatabaseHelper.COL_SKIP_TICK, mCallback.getUserId());
            hintTick = savedInstanceState.getLong("hintTick");
            DatabaseHelperSingleton.instance().setUserValue((int) hintTick,
                    DatabaseHelper.COL_HINT_TICK, mCallback.getUserId());
            hintPressed = savedInstanceState.getBoolean("hintPressed");
            DatabaseHelperSingleton.instance().setUserValue(hintPressed ? 1 : 0,
                    DatabaseHelper.COL_HINT_PRESSED, mCallback.getUserId());
            skipPressed = savedInstanceState.getBoolean("skipPressed");
            DatabaseHelperSingleton.instance().setUserValue(skipPressed ? 1 : 0,
                    DatabaseHelper.COL_SKIP_PRESSED, mCallback.getUserId());
            isCorrect = savedInstanceState.getBoolean("isCorrect");
            DatabaseHelperSingleton.instance().setUserValue(isCorrect ? 1 : 0,
                    DatabaseHelper.COL_IS_CORRECT, mCallback.getUserId());
        }
        else {
            if (mCallback.getUserId() != null) {
                /*
                savedAnswer = DatabaseHelperSingleton.instance().getUserStringValue(
                        DatabaseHelper.COL_ANSWER, mCallback.getUserId());
                if (savedAnswer != null && savedAnswer.equals(""))
                    savedAnswer = null;
                */
                savedHint = DatabaseHelperSingleton.instance().getUserStringValue(
                        DatabaseHelper.COL_HINT, mCallback.getUserId());
                skipTick = DatabaseHelperSingleton.instance().getUserIntValue(
                        DatabaseHelper.COL_SKIP_TICK, mCallback.getUserId());
                hintTick = DatabaseHelperSingleton.instance().getUserIntValue(
                        DatabaseHelper.COL_HINT_TICK, mCallback.getUserId());
                hintPressed = DatabaseHelperSingleton.instance().getUserIntValue(
                        DatabaseHelper.COL_HINT_PRESSED, mCallback.getUserId())
                            == 1 ? true : false;
                skipPressed = DatabaseHelperSingleton.instance().getUserIntValue(
                        DatabaseHelper.COL_SKIP_PRESSED, mCallback.getUserId())
                            == 1 ? true : false;
                isCorrect = DatabaseHelperSingleton.instance().getUserIntValue(
                        DatabaseHelper.COL_IS_CORRECT, mCallback.getUserId())
                            == 1 ? true : false;
            }
        }
        if (hintTick < 0)
            hintTick = 15000;
        if (skipTick < 0)
            skipTick = 17000;
        DatabaseHelperSingleton.instance().setUserValue((int) hintTick,
                DatabaseHelper.COL_HINT_TICK, mCallback.getUserId());
        DatabaseHelperSingleton.instance().setUserValue((int) skipTick,
                DatabaseHelper.COL_SKIP_TICK, mCallback.getUserId());
    }
    
    @SuppressLint("ResourceAsColor")
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.question, container, false);
        /*
        ViewTreeObserver vto = slidingMenu.getViewTreeObserver(); 
        vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() { 
            @Override 
            public void onGlobalLayout() { 
                slidingMenu.setThreshold(5);
            } 
        });
        */
		background = (ImageViewEx) v.findViewById(R.id.Background);
		setBackgroundBitmap(mCallback.getBackground(), "quiz");
        scoreText = (TextView) v.findViewById(R.id.ScoreText);
        scoreText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                tracker.send(new HitBuilders.EventBuilder()
                        .setCategory(Constants.CATEGORY_FRAGMENT_UI)
                        .setAction(Constants.ACTION_BUTTON_PRESS)
                        .setLabel("quizScore")
                        .setValue(1L)
                        .build());
                if (mCallback != null)
                    mCallback.onStatsPressed();
            }
        });
        scoreText.setText(SharedPreferencesSingleton.instance().getString(
        		ResourcesSingleton.instance().getString(R.string.scoretext_key), ""));
        questionText = (TextView) v.findViewById(R.id.QuestionText);
        questionText.setMovementMethod(new ScrollingMovementMethod());
        questionText.setText(SharedPreferencesSingleton.instance().getString(
        		ResourcesSingleton.instance().getString(R.string.questiontext_key), ""));
        answerText = (EditText) v.findViewById(R.id.QuestionAnswer);
        answerText.setOnEditorActionListener(new OnEditorActionListener() {
            @SuppressLint("NewApi")
			@Override
            public boolean onEditorAction(TextView v, int actionId,
                    KeyEvent event) {
                if (mCallback != null && !mCallback.getNetworkProblem() &&
                        !mCallback.isNewQuestion() &&
                        (actionId == EditorInfo.IME_ACTION_DONE ||
                        event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    answerButton.setBackgroundResource(
                            R.drawable.button_disabled);
                    answerButton.setTextColor(ResourcesSingleton.instance().getColor(R.color.light_gray));
                    answerButton.setText("ENTER");
                    answerButton.setEnabled(false);
                    String entry = null;
                    entry = v.getEditableText().toString();
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
                        new VerifyTask(mCallback.getUserId(),
                                mCallback.getQuestionId(0),
                                mCallback.getQuestionHint(0)).execute(entry);
                    else
                        new VerifyTask(mCallback.getUserId(),
                                mCallback.getQuestionId(0),
                                mCallback.getQuestionHint(0)).executeOnExecutor(
                                        AsyncTask.THREAD_POOL_EXECUTOR, entry);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    return true;
                }
                else
                    return false;
            } 
        });
        answerText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {}
            public void beforeTextChanged(CharSequence s, int start, int count,
                    int after){}
            public void onTextChanged(CharSequence s, int start, int before,
                    int count){
                /*
                savedAnswer = s == null ? "" : s.toString();
                DatabaseHelperSingleton.instance().setUserValue(savedAnswer,
                        DatabaseHelper.COL_ANSWER, mCallback.getUserId());
                */
            }
        });
        answerText.setText(SharedPreferencesSingleton.instance().getString(
        		ResourcesSingleton.instance().getString(R.string.answertext_key), ""));
        answerText.setHint(SharedPreferencesSingleton.instance().getString(
        		ResourcesSingleton.instance().getString(R.string.hinttext_key), ""));
        answerPlace = (TextView) v.findViewById(R.id.AnswerText);
        answerPlace.setText(SharedPreferencesSingleton.instance().getString(
        		ResourcesSingleton.instance().getString(R.string.placetext_key), ""));
        answerButton = (Button) v.findViewById(R.id.QuestionButton);
        answerButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                tracker.send(new HitBuilders.EventBuilder()
                        .setCategory(Constants.CATEGORY_FRAGMENT_UI)
                        .setAction(Constants.ACTION_BUTTON_PRESS)
                        .setLabel("quizAnswer")
                        .setValue(1L)
                        .build());
                answerButton.setBackgroundResource(R.drawable.button_disabled);
                answerButton.setTextColor(ResourcesSingleton.instance().getColor(R.color.light_gray));
                answerButton.setEnabled(false);
                if (mCallback != null) {
                    if (mCallback.isNewQuestion()) {
                    	answerButton.setText("NEXT");
                        if (Build.VERSION.SDK_INT <
                                Build.VERSION_CODES.HONEYCOMB)
                            new NextTask().execute();
                        else
                            new NextTask().executeOnExecutor(
                                    AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                    else {
                    	answerButton.setText("ENTER");
                        if (Build.VERSION.SDK_INT <
                                Build.VERSION_CODES.HONEYCOMB)
                            new VerifyTask(mCallback.getUserId(),
                                    mCallback.getQuestionId(0),
                                    mCallback.getQuestionHint(0)).execute(
                                    answerText.getEditableText().toString());
                        else
                            new VerifyTask(mCallback.getUserId(),
                                    mCallback.getQuestionId(0),
                                    mCallback.getQuestionHint(0))
                                .executeOnExecutor(
                                    AsyncTask.THREAD_POOL_EXECUTOR,
                                    answerText.getEditableText().toString());
                    }
                }
            }
        });
        skipButton = (RelativeLayout) v.findViewById(R.id.Skip);
        skipButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                tracker.send(new HitBuilders.EventBuilder()
                        .setCategory(Constants.CATEGORY_FRAGMENT_UI)
                        .setAction(Constants.ACTION_BUTTON_PRESS)
                        .setLabel("quizSkip")
                        .setValue(1L)
                        .build());
                mCallback.setIsNewQuestion(true);
                stagedMap.put(mCallback.getQuestionId(0),
                		mCallback.getQuestionHint(0));
                saveMap.put(mCallback.getQuestionId(0), true);
                if (!ApplicationEx.getSerialsList().contains(Build.SERIAL))
                	skipButton.setEnabled(false);
                playAudio("skip");
                savedHint = mCallback.getQuestionAnswer(0);
                DatabaseHelperSingleton.instance().setUserValue(savedHint,
                        DatabaseHelper.COL_HINT, mCallback.getUserId());
                answerPlace.setText(savedHint);
                hintButton.setEnabled(false);
                hintText.setTextColor(ResourcesSingleton.instance().getColor(R.color.light_gray));
                hintText.setBackgroundResource(R.drawable.button_disabled);
                answerButton.setBackgroundResource(R.drawable.button);
                answerButton.setTextColor(Color.BLACK);
                answerButton.setText("NEXT");
                answerButton.setEnabled(true);
                skipText.setTextColor(ResourcesSingleton.instance().getColor(R.color.light_gray));
                skipText.setBackgroundResource(R.drawable.button_disabled);
                skipPressed = true;
                DatabaseHelperSingleton.instance().setUserValue(skipPressed ? 1 : 0,
                        DatabaseHelper.COL_SKIP_PRESSED, mCallback.getUserId());
                if (hintTimer != null)
                    hintTimer.cancel();
                if (skipTimer != null)
                    skipTimer.cancel();
                isCorrect = true;
                DatabaseHelperSingleton.instance().setUserValue(isCorrect ? 1 : 0,
                        DatabaseHelper.COL_IS_CORRECT, mCallback.getUserId());
                hintTick = 0;
                DatabaseHelperSingleton.instance().setUserValue((int) hintTick,
                        DatabaseHelper.COL_HINT_TICK, mCallback.getUserId());
                skipTick = 0;
                DatabaseHelperSingleton.instance().setUserValue((int) skipTick,
                        DatabaseHelper.COL_SKIP_TICK, mCallback.getUserId());
            } 
        });
        skipText = (TextView) v.findViewById(R.id.SkipText);
        skipTime = (TextView) v.findViewById(R.id.SkipTime);
        if (skipTick > 0)
            skipTime.setText(Long.toString((skipTick/1000)+1));
        hintButton = (RelativeLayout) v.findViewById(R.id.Hint);
        hintButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                tracker.send(new HitBuilders.EventBuilder()
                        .setCategory(Constants.CATEGORY_FRAGMENT_UI)
                        .setAction(Constants.ACTION_BUTTON_PRESS)
                        .setLabel("quizHint")
                        .setValue(1L)
                        .build());
                hintButton.setEnabled(false);
                hintText.setTextColor(ResourcesSingleton.instance().getColor(R.color.light_gray));
                hintText.setBackgroundResource(R.drawable.button_disabled);
                hintTick = 0;
                DatabaseHelperSingleton.instance().setUserValue((int) hintTick,
                        DatabaseHelper.COL_HINT_TICK, mCallback.getUserId());
                playAudio("hint");
                indicateHint();
            } 
        });
        hintText = (TextView) v.findViewById(R.id.HintText);
        hintTime = (TextView) v.findViewById(R.id.HintTime);
        if (hintTick > 0)
            hintTime.setText(Long.toString((hintTick/1000)+1));
        answerImage = (ImageViewEx) v.findViewById(R.id.AnswerImage);
        answerImage.bringToFront();
        retryText = (TextView) v.findViewById(R.id.RetryText);
        retryButton = (Button) v.findViewById(R.id.RetryButton);
        retryButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                tracker.send(new HitBuilders.EventBuilder()
                        .setCategory(Constants.CATEGORY_FRAGMENT_UI)
                        .setAction(Constants.ACTION_BUTTON_PRESS)
                        .setLabel("quizRetry")
                        .setValue(1L)
                        .build());
                disableButton(true);
                if (mCallback != null) {
                    if (ApplicationEx.hasConnection()) {
                        mCallback.setNetworkProblem(false);
                        if (mCallback.questionIdsEmpty() || mCallback.getQuestionId(0) != null)
                            resumeQuestion();
                        else
                            mCallback.getNextQuestions(false,
                            		SharedPreferencesSingleton.instance().getInt(
                            				ResourcesSingleton.instance().getString(R.string.level_key),
                            				Constants.HARD));
                    }
                    else {
                        ApplicationEx.showLongToast(R.string.NoConnectionToast);
                        showNetworkProblem();
                    }
                }
            }
        });
        upLevelButton = (Button) v.findViewById(R.id.UpLevelButton);
        upLevelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	upLevelButton.setVisibility(View.GONE);
            	mCallback.updateLevel();
            }
        });
        if (!SharedPreferencesSingleton.instance().getString(
        		ResourcesSingleton.instance().getString(R.string.scoretext_key), "").equals("")) {
        	scoreText.setVisibility(View.VISIBLE);
        	questionText.setVisibility(View.VISIBLE);
        	answerText.setVisibility(View.VISIBLE);
        	answerPlace.setVisibility(View.VISIBLE);
        	answerButton.setVisibility(View.VISIBLE);
        	hintButton.setVisibility(View.VISIBLE);
        	skipButton.setVisibility(View.VISIBLE);
        	hintTime.setVisibility(View.VISIBLE);
        	hintText.setVisibility(View.INVISIBLE);
        	skipTime.setVisibility(View.VISIBLE);
        	skipText.setVisibility(View.INVISIBLE);
        	hintTime.setText(SharedPreferencesSingleton.instance().getString(
        			ResourcesSingleton.instance().getString(R.string.hintnum_key), ""));
        	skipTime.setText(SharedPreferencesSingleton.instance().getString(
        			ResourcesSingleton.instance().getString(R.string.skipnum_key), ""));
        }
        return v;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);
    	/*
    	background.resetColoredViews();
    	background.addColoredView(questionText,
        		ResourcesSingleton.instance().getColor(R.color.background_dark));
    	background.invalidate();
    	*/
    }
    
    private class WrongTimer extends CountDownTimer {
        public WrongTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }
        
        @Override
        public void onTick(long millisUntilFinished) {}
        
        @Override
        public void onFinish() {
            answerImage.setVisibility(View.INVISIBLE);
            questionText.setTextColor(Color.WHITE);
        }
    }
    
    private class SkipTimer extends CountDownTimer {
        public SkipTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
            //skipTime.setText("");
            skipText.setTextColor(Color.BLACK);
            skipText.setBackgroundResource(R.drawable.button);
            skipText.setVisibility(View.INVISIBLE);
            skipTime.setVisibility(View.VISIBLE);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            skipTick = millisUntilFinished;
            DatabaseHelperSingleton.instance().setUserValue((int) skipTick,
                    DatabaseHelper.COL_SKIP_TICK, mCallback.getUserId());
            skipTime.setText(Long.toString((millisUntilFinished/1000)-1));
            skipText.setVisibility(View.INVISIBLE);
            skipTime.setVisibility(View.VISIBLE);
            if (millisUntilFinished < 2000) {
                skipText.setVisibility(View.VISIBLE);
                skipText.setTextColor(Color.BLACK);
                skipText.setBackgroundResource(R.drawable.button);
                skipTime.setVisibility(View.INVISIBLE);
                skipButton.setEnabled(true);
            }
        }
        
        @Override
        public void onFinish() {
            skipTick = 0;
            DatabaseHelperSingleton.instance().setUserValue((int) skipTick,
                    DatabaseHelper.COL_SKIP_TICK, mCallback.getUserId());
        }
    }
    
    private class HintTimer extends CountDownTimer {
        public HintTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
            long text = (millisInFuture/1000)+1;
            if (text > 15)
                text = 15;
            hintTime.setText(Long.toString(text));
            hintText.setVisibility(View.INVISIBLE);
            hintTime.setVisibility(View.VISIBLE);
            skipTime.setText("");
            skipText.setTextColor(Color.BLACK);
            skipText.setBackgroundResource(R.drawable.button);
            skipText.setVisibility(View.INVISIBLE);
            skipTime.setVisibility(View.VISIBLE);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            hintTick = millisUntilFinished;
            DatabaseHelperSingleton.instance().setUserValue((int) hintTick,
                    DatabaseHelper.COL_HINT_TICK, mCallback.getUserId());
            hintTime.setText(Long.toString((millisUntilFinished/1000)+1));
            hintText.setVisibility(View.INVISIBLE);
            hintTime.setVisibility(View.VISIBLE);
        }
        
        @Override
        public void onFinish() {
            if (!isCorrect) {
                hintButton.setEnabled(true);
                if (skipTimer != null)
                    skipTimer.cancel();
                skipTimer = new SkipTimer(skipTick, 500);
                skipTimer.start();
                skipText.setVisibility(View.INVISIBLE);
                skipTime.setVisibility(View.VISIBLE);
            }
            hintText.setVisibility(View.VISIBLE);
            hintText.setTextColor(Color.BLACK);
            hintText.setBackgroundResource(R.drawable.button);
            hintTime.setVisibility(View.INVISIBLE);
            hintTick = 0;
            DatabaseHelperSingleton.instance().setUserValue((int) hintTick,
                    DatabaseHelper.COL_HINT_TICK, mCallback.getUserId());
        }
    }
    
    private class NextTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... nothing) {
            publishProgress();
            //savedAnswer = null;
            skipTick = 17000;
            DatabaseHelperSingleton.instance().setUserValue((int) skipTick,
                    DatabaseHelper.COL_SKIP_TICK, mCallback.getUserId());
            hintTick = 15000;
            DatabaseHelperSingleton.instance().setUserValue((int) hintTick,
                    DatabaseHelper.COL_HINT_TICK, mCallback.getUserId());
            hintPressed = false;
            DatabaseHelperSingleton.instance().setUserValue(hintPressed ? 1 : 0,
                    DatabaseHelper.COL_HINT_PRESSED, mCallback.getUserId());
            skipPressed = false;
            DatabaseHelperSingleton.instance().setUserValue(skipPressed ? 1 : 0,
                    DatabaseHelper.COL_SKIP_PRESSED, mCallback.getUserId());
            savedHint = "";
            DatabaseHelperSingleton.instance().setUserValue("",
                    DatabaseHelper.COL_HINT, mCallback.getUserId());
            isCorrect = false;
            DatabaseHelperSingleton.instance().setUserValue(isCorrect ? 1 : 0,
                    DatabaseHelper.COL_IS_CORRECT, mCallback.getUserId());
            return null;
        }
        
        protected void onProgressUpdate(Void... nothing) {
            answerText.setText("");
            answerButton.setText("LOADING");
            //imm.restartInput(answerText);
        }
        
        @Override
        protected void onPostExecute(Void nothing) {
            mCallback.next();
        }
    }
    
    @Override
    public void disableButton(boolean isRetry) {
        if (!isRetry) {
            answerButton.setBackgroundResource(R.drawable.button_disabled);
            answerButton.setTextColor(ResourcesSingleton.instance().getColor(R.color.light_gray));
            answerButton.setEnabled(false);
        }
        else {
            retryButton.setBackgroundResource(R.drawable.button_disabled);
            retryButton.setTextColor(ResourcesSingleton.instance().getColor(R.color.light_gray));
            retryButton.setEnabled(false);
        }
    }
    
    @Override
    public void enableButton(boolean isRetry) {
        if (!isRetry) {
            answerButton.setBackgroundResource(R.drawable.button);
            answerButton.setTextColor(Color.BLACK);
            answerButton.setEnabled(true);
        }
        else {
            retryButton.setBackgroundResource(R.drawable.button);
            retryButton.setTextColor(Color.BLACK);
            retryButton.setEnabled(true);
        }
    }
    
    private void indicateHint() {
        if (hintTask != null)
            hintTask.cancel(true);
        hintTask = new HintTask();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
            hintTask.execute();
        else
            hintTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
    
    private class HintTask extends AsyncTask<Void, Void, Void> {
        
        @Override
        protected Void doInBackground(Void... nothing) {
            if (isCancelled() || mCallback == null ||
            		mCallback.getQuestionAnswer(0) == null ||
            		mCallback.getUserId() == null ||
            		mCallback.getQuestionScore(0) == null)
                return null;
            StringBuilder sb = new StringBuilder(mCallback.getQuestionAnswer(0)
                    .replaceAll("\\s+", " "));
            if (isCancelled())
                return null;
            int textSize = mCallback.getQuestionAnswer(0).replaceAll("\\s+", " ")
                    .length();
            int replaceSize = textSize / 2;
            int currIndex = -1;
            int replacements = 0;
            if (isCancelled())
                return null;
            while (replacements < replaceSize && !isCancelled()) {
                currIndex = (int) (Math.random()*textSize);
                if (sb.substring(currIndex, currIndex+1)
                        .matches("[0-9a-zA-Z]") && 
                    !sb.substring(currIndex, currIndex+1).matches("[*]")) {
                    sb.replace(currIndex, currIndex+1, "*");
                    replacements++;
                }
            }
            if (isCancelled())
                return null;
            mCallback.setQuestionHint(true, 0);
            hintPressed = true;
            DatabaseHelperSingleton.instance().setUserValue(hintPressed ? 1 : 0,
                    DatabaseHelper.COL_HINT_PRESSED, mCallback.getUserId());
            if (isCancelled())
                return null;
            if (mCallback.getQuestionScore(0) != null) {
                calculateCurrentScore();
                answerTextHint = Integer.toString(currScore) + " points";
            }
            else
                answerTextHint = "";
            if (isCancelled())
                return null;
            savedHint = sb.toString();
            DatabaseHelperSingleton.instance().setUserValue(savedHint,
                    DatabaseHelper.COL_HINT, mCallback.getUserId());
            if (isCancelled())
                return null;
            publishProgress();
            return null;
        }
        
        protected void onProgressUpdate(Void... nothing) {
            answerPlace.setText(savedHint, TextView.BufferType.NORMAL);
            answerPlace.setVisibility(View.VISIBLE);
            answerText.setHint(answerTextHint);
            imm.restartInput(answerText);
        }
        
        @Override
        protected void onCancelled(Void nothing) {
        }
    }
    
    @Override
    public void resetHint() {
        savedHint = "";
        DatabaseHelperSingleton.instance().setUserValue("",
                DatabaseHelper.COL_HINT, mCallback.getUserId());
    }
    
    @Override
    public void resumeQuestion() {
    	if (mCallback != null) {
	        if (!mCallback.questionIdsEmpty() &&
	        		mCallback.getQuestionId(0) != null &&
	        		mCallback.isCorrectAnswer(mCallback.getQuestionId(0)) &&
	                !mCallback.isNewQuestion()) {
	            if (Build.VERSION.SDK_INT <
	                    Build.VERSION_CODES.HONEYCOMB)
	                new NextTask().execute();
	            else
	                new NextTask().executeOnExecutor(
	                        AsyncTask.THREAD_POOL_EXECUTOR);
	            return;
	        }
	        if (answerText != null)
	        	answerText.setVisibility(View.VISIBLE);
	        if (mCallback.getQuestion(0) != null && questionText != null) {
	            questionText.setText(mCallback.getQuestion(0));
	            questionText.setVisibility(View.VISIBLE);
	        }
	        /*
	        if (savedAnswer != null)
	            answerText.setText(savedAnswer);
	        */
	        if (mCallback.getQuestionScore(0) != null) {
	            calculateCurrentScore();
	            answerTextHint = Integer.toString(currScore) + " points";
	        }
	        else
	            answerTextHint = "";
	        if (!isCorrect) {
	            if (!hintPressed)
	                savedHint = "";
	        }
	        if (savedHint == null || savedHint.equals("")) {
	            ArrayList<String> answerStrings = new ArrayList<String>();
	            int lastSpace = -1;
	            String answer = (mCallback.getQuestionAnswer(0)
	                    .replaceAll("\\s+", " ")).replaceAll("[0-9a-zA-Z#&]", "*");
	            if (answer.length() > 36 && answer.lastIndexOf(" ") > -1) {
	                lastSpace = answer.lastIndexOf(" ");
	                if (lastSpace > 36)
	                    lastSpace = answer.substring(0, lastSpace-1)
	                            .lastIndexOf(" ");
	                answerStrings.add(answer.substring(0, lastSpace-1));
	                answerStrings.add(answer.substring(lastSpace+2,
	                        answer.length()));
	                answer = answerStrings.get(0) + "\n" + answerStrings.get(1);
	            }
	            savedHint = answer;
	        }
	        DatabaseHelperSingleton.instance().setUserValue(savedHint,
	                DatabaseHelper.COL_HINT, mCallback.getUserId());
	        if (answerPlace != null) {
		        answerPlace.setText(savedHint, TextView.BufferType.NORMAL);
		        answerPlace.setVisibility(View.VISIBLE);
	        }
	        if (answerText != null)
	        	answerText.setHint(answerTextHint);
	        imm.restartInput(answerText);
	        if (answerButton != null) {
		        answerButton.setVisibility(View.VISIBLE);
		        answerButton.setBackgroundResource(R.drawable.button);
		        answerButton.setTextColor(Color.BLACK);
	        }
	        if (retryText != null)
	        	retryText.setVisibility(View.INVISIBLE);
	        if (retryButton != null)
	        	retryButton.setVisibility(View.INVISIBLE);
	        if (upLevelButton != null) {
	        	upLevelButton.setVisibility(View.INVISIBLE);
	        }
	        if (mCallback.isNewQuestion()) {
	            answerButton.setText("NEXT");
	            answerButton.setEnabled(true);
	            if (mCallback.isCorrectAnswer(mCallback.getQuestionId(0))) {
	                answerImage.setImageResource(R.drawable.correct);
	                questionText.setTextColor(Color.GREEN);
	                answerImage.setVisibility(View.VISIBLE);
	            }
	            else {
	                questionText.setTextColor(Color.WHITE);
	                answerImage.setVisibility(View.INVISIBLE);
	            }
	            hintText.setTextColor(ResourcesSingleton.instance().getColor(R.color.light_gray));
	            hintText.setBackgroundResource(R.drawable.button_disabled);
	            skipText.setTextColor(ResourcesSingleton.instance().getColor(R.color.light_gray));
	            skipText.setBackgroundResource(R.drawable.button_disabled);
	            hintTick = 0;
	            DatabaseHelperSingleton.instance().setUserValue((int) hintTick,
	                    DatabaseHelper.COL_HINT_TICK, mCallback.getUserId());
	            skipTick = 0;
	            DatabaseHelperSingleton.instance().setUserValue((int) skipTick,
	                    DatabaseHelper.COL_SKIP_TICK, mCallback.getUserId());
	            skipPressed = true;
	            DatabaseHelperSingleton.instance().setUserValue(skipPressed ? 1 : 0,
	                    DatabaseHelper.COL_SKIP_PRESSED, mCallback.getUserId());
	            hintPressed = true;
	            DatabaseHelperSingleton.instance().setUserValue(hintPressed ? 1 : 0,
	                    DatabaseHelper.COL_HINT_PRESSED, mCallback.getUserId());
	        }
	        else {
	            answerButton.setText("ENTER");
	            answerButton.setEnabled(true);
	            questionText.setTextColor(Color.WHITE);
	            answerImage.setVisibility(View.INVISIBLE);
	            skipPressed = false;
	            DatabaseHelperSingleton.instance().setUserValue(skipPressed ? 1 : 0,
	                    DatabaseHelper.COL_SKIP_PRESSED, mCallback.getUserId());
	        }
	        hintButton.setEnabled(false);
	        hintButton.setVisibility(View.VISIBLE);
	        if (hintTimer != null)
	            hintTimer.cancel();
	        if (hintPressed) {
	            hintText.setTextColor(ResourcesSingleton.instance().getColor(R.color.light_gray));
	            hintText.setBackgroundResource(R.drawable.button_disabled);
	            hintText.setVisibility(View.VISIBLE);
	            hintTime.setVisibility(View.INVISIBLE);
	        }
	        else {
	            if (hintTick > 0) {
	                hintTimer = new HintTimer(hintTick, 500);
	                hintTimer.start();
	            }
	            else {
	                hintText.setBackgroundResource(R.drawable.button);
	                hintText.setTextColor(Color.BLACK);
	                hintText.setVisibility(View.VISIBLE);
	                hintTime.setVisibility(View.INVISIBLE);
	                hintButton.setEnabled(true);
	            }
	        }
	        if (skipTimer != null)
	            skipTimer.cancel();
	        // TODO Add Jason's serial
	        if (!ApplicationEx.getSerialsList().contains(Build.SERIAL))
	        	skipButton.setEnabled(false);
	        skipButton.setVisibility(View.VISIBLE);
	        if (skipPressed) {
	            skipText.setTextColor(ResourcesSingleton.instance().getColor(R.color.light_gray));
	            skipText.setBackgroundResource(R.drawable.button_disabled);
	            skipText.setVisibility(View.VISIBLE);
	            skipTime.setVisibility(View.INVISIBLE);
	        }
	        else {
	            if (skipTick > 0) {
	                if (hintTick == 0) {
	                    skipTimer = new SkipTimer(skipTick, 500);
	                    skipTimer.start();
	                }
	            }
	            else {
	                skipText.setBackgroundResource(R.drawable.button);
	                skipText.setTextColor(Color.BLACK);
	                skipText.setVisibility(View.VISIBLE);
	                skipTime.setVisibility(View.INVISIBLE);
	                skipButton.setEnabled(true);
	            }
	        }
	        //cameraButton.setVisibility(View.VISIBLE);
	        updateScoreText();
	        /*
	        background.resetColoredViews();
	        background.addColoredView(questionText,
	        		ResourcesSingleton.instance().getColor(R.color.background_dark));
	    	background.invalidate();
	    	*/
    	}
    	else
    		showNetworkProblem();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        if (mCallback != null) {
        	mCallback.setLoggingOut(false);
            if (ApplicationEx.hasConnection()) {
                //mCallback.saveUserScore(mCallback.getCurrentScore());
                if (!mCallback.questionIdsEmpty() &&
                		mCallback.getQuestionId(0) != null) {
                    mCallback.getNextQuestions(true,
                    		SharedPreferencesSingleton.instance().getInt(
                    				ResourcesSingleton.instance().getString(R.string.level_key),
                    				Constants.HARD));
                    //resumeQuestion();
                }
                else {
                    showNoMoreQuestions(SharedPreferencesSingleton.instance().getInt(
                    		ResourcesSingleton.instance().getString(R.string.level_key), Constants.HARD));
                    retryButton.setBackgroundResource(
                            R.drawable.button_disabled);
                    retryButton.setTextColor(
                            ResourcesSingleton.instance().getColor(R.color.light_gray));
                    retryButton.setText("CHECKING FOR QUESTIONS");
                    retryButton.setVisibility(View.VISIBLE);
                    retryButton.setEnabled(false);
                    mCallback.getNextQuestions(false,
                    		SharedPreferencesSingleton.instance().getInt(
                    				ResourcesSingleton.instance().getString(R.string.level_key),
                    				Constants.HARD));
                }
            }
            else {
                ApplicationEx.showLongToast(R.string.NoConnectionToast);
                showNetworkProblem();
            }
        }
        if (!SharedPreferencesSingleton.instance().contains(
        		ResourcesSingleton.instance().getString(R.string.menu_key))) {
            /*
	        showQuickTipMenu(quickTipLeftView, "Swipe from left for menu",
	        		Constants.QUICK_TIP_LEFT | Constants.QUICK_TIP_TOP);
	        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD)
	        	SharedPreferencesSingleton.instance().edit().putBoolean(
	        			ResourcesSingleton.instance().getString(R.string.menu_key), true).commit();
	        else
	        	SharedPreferencesSingleton.instance().edit().putBoolean(
	        			ResourcesSingleton.instance().getString(R.string.menu_key), true).apply();
			*/
        }
    }
    
    @Override
    public void onPause() {
        if (skipTimer != null)
            skipTimer.cancel();
        if (hintTimer != null)
            hintTimer.cancel();
        if (hintTask != null)
            hintTask.cancel(true);
        Editor editor = SharedPreferencesSingleton.instance().edit();
        editor.putString(ResourcesSingleton.instance().getString(R.string.scoretext_key),
        		scoreText.getText().toString());
        editor.putString(ResourcesSingleton.instance().getString(R.string.questiontext_key),
        		questionText.getText().toString());
        editor.putString(ResourcesSingleton.instance().getString(R.string.hinttext_key),
        		answerText.getHint().toString());
        editor.putString(ResourcesSingleton.instance().getString(R.string.answertext_key),
        		answerText.getText().toString());
        editor.putString(ResourcesSingleton.instance().getString(R.string.placetext_key),
        		answerPlace.getText().toString());
        editor.putInt(ResourcesSingleton.instance().getString(R.string.hinttimevis_key),
        		hintTime.getVisibility());
        editor.putInt(ResourcesSingleton.instance().getString(R.string.hinttextvis_key),
        		hintText.getVisibility());
        editor.putInt(ResourcesSingleton.instance().getString(R.string.skiptimevis_key),
        		skipTime.getVisibility());
        editor.putInt(ResourcesSingleton.instance().getString(R.string.skiptextvis_key),
        		skipText.getVisibility());
        editor.putString(ResourcesSingleton.instance().getString(R.string.hintnum_key),
        		hintTime.getText().toString());
        editor.putString(ResourcesSingleton.instance().getString(R.string.skipnum_key),
        		skipTime.getText().toString());
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD)
        	editor.commit();
        else
        	editor.apply();
        if (!stagedMap.isEmpty())
        	stageQuestions(mCallback.getUserId());
        if (!saveMap.isEmpty())
        	saveQuestionScores();
        if (!correctMap.isEmpty())
        	saveAnswers(mCallback.getUserId());
        super.onPause();
    }
    
    private class VerifyTask extends AsyncTask<String, Void, Void> {
        private String userId;
        private String questionId;
        private boolean questionHint;
        
        private VerifyTask(String userId, String questionId,
                boolean questionHint) {
            this.userId = userId;
            this.questionId = questionId;
            this.questionHint = questionHint;
        }
        
        @Override
        protected Void doInBackground(String... entry) {
            String trimmed = entry[0].trim();
            if (trimmed.equalsIgnoreCase(mCallback.getQuestionAnswer(0))) {
                mCallback.setIsNewQuestion(true);
                isCorrect = true;
                mCallback.addCorrectAnswer(questionId);
                mCallback.addCurrentScore(currScore);
                publishProgress();
                playAudio("correct");
                hintTick = 0;
                DatabaseHelperSingleton.instance().setUserValue((int) hintTick,
                        DatabaseHelper.COL_HINT_TICK, mCallback.getUserId());
                skipTick = 0;
                DatabaseHelperSingleton.instance().setUserValue((int) skipTick,
                        DatabaseHelper.COL_SKIP_TICK, mCallback.getUserId());
                hintPressed = true;
                DatabaseHelperSingleton.instance().setUserValue(hintPressed ? 1 : 0,
                        DatabaseHelper.COL_HINT_PRESSED, mCallback.getUserId());
                skipPressed = true;
                DatabaseHelperSingleton.instance().setUserValue(skipPressed ? 1 : 0,
                        DatabaseHelper.COL_SKIP_PRESSED, mCallback.getUserId());
                DatabaseHelperSingleton.instance().setUserValue(isCorrect ? 1 : 0,
                        DatabaseHelper.COL_IS_CORRECT, mCallback.getUserId());
                if (wrongTimer != null)
                    wrongTimer.cancel();
                correctMap.put(questionId, questionHint);
            }
            else {
                isCorrect = false;
                playAudio("wrong");
                publishProgress();
                DatabaseHelperSingleton.instance().setUserValue(isCorrect ? 1 : 0,
                        DatabaseHelper.COL_IS_CORRECT, mCallback.getUserId());
            }
            return null;
        }
        
        protected void onProgressUpdate(Void... nothing) {
            if (isCorrect) {
                if (mCallback.getCurrentScore() > -1 &&
                        !DatabaseHelperSingleton.instance().isAnonUser(userId)) {
                    scoreText.setText(
                            Integer.toString(mCallback.getCurrentScore()));
                    scoreText.setVisibility(View.VISIBLE);
                }
                else
                    scoreText.setVisibility(View.INVISIBLE);
                if (hintTimer != null)
                    hintTimer.cancel();
                if (skipTimer != null)
                    skipTimer.cancel();
                savedHint = mCallback.getQuestionAnswer(0);
                DatabaseHelperSingleton.instance().setUserValue(savedHint,
                        DatabaseHelper.COL_HINT, mCallback.getUserId());
                answerPlace.setText(savedHint);
                hintText.setVisibility(View.VISIBLE);
                hintTime.setVisibility(View.INVISIBLE);
                skipText.setVisibility(View.VISIBLE);
                skipTime.setVisibility(View.INVISIBLE);
                hintButton.setEnabled(false);
                // TODO Add Jason's serial
                if (!ApplicationEx.getSerialsList().contains(Build.SERIAL))
                	skipButton.setEnabled(false);
                answerImage.setImageResource(R.drawable.correct);
                answerImage.setVisibility(View.VISIBLE);
                questionText.setTextColor(Color.GREEN);
                answerText.setText("");
                //imm.restartInput(answerText);
                answerButton.setText("NEXT");
                answerButton.setEnabled(true);
                answerButton.setBackgroundResource(R.drawable.button);
                answerButton.setTextColor(Color.BLACK);
                hintText.setTextColor(ResourcesSingleton.instance().getColor(R.color.light_gray));
                hintText.setBackgroundResource(R.drawable.button_disabled);
                skipText.setTextColor(ResourcesSingleton.instance().getColor(R.color.light_gray));
                skipText.setBackgroundResource(R.drawable.button_disabled);
            }
            else {
                answerButton.setBackgroundResource(R.drawable.button);
                answerButton.setTextColor(Color.BLACK);
                answerButton.setText("ENTER");
                answerButton.setEnabled(true);
                answerImage.setImageResource(R.drawable.wrong);
                answerImage.setVisibility(View.VISIBLE);
                questionText.setTextColor(Color.RED);
                if (wrongTimer == null)
                    wrongTimer = new WrongTimer(2000, 1000);
                wrongTimer.start();
            }
        }
        
        @Override
        protected void onPostExecute(Void nothing) {
            if (isCorrect)
            	saveMap.put(mCallback.getQuestionId(0), false);
            isCorrect = false;
            DatabaseHelperSingleton.instance().setUserValue(isCorrect ? 1 : 0,
                    DatabaseHelper.COL_IS_CORRECT, mCallback.getUserId());
        }
    }
    
    private void saveAnswers(final String userId) {
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("CorrectAnswers");
        query.whereEqualTo("userId", userId);
        query.whereContainedIn("objectId", correctMap.keySet());
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> answers, ParseException e) {
                if (e != null && e.getCode() != 101) {
                    Log.e(Constants.LOG_TAG, "Error: " + e.getMessage());
                    showNetworkProblem();
                }
                else {
                	ParseObject correctAnswer;
                	for (ParseObject answer : answers) {
                		correctMap.remove(answer.getString("questionId"));
                	}
                    for (Entry<String, Boolean> answer :
                    		correctMap.entrySet()) {
                    	correctAnswer = new ParseObject("CorrectAnswers");
                    	correctAnswer.put("questionId", answer.getKey());
                        correctAnswer.put("userId", userId);
                        correctAnswer.put("hint", answer.getValue());
                        try {
                            correctAnswer.saveEventually();
                        } catch (RuntimeException exception) {}
                    }
                    correctMap.clear();
                }
            }
        });
    }

    private void saveQuestionScores() {
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Question");
        query.whereContainedIn("objectId", saveMap.keySet());
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> questions, ParseException e) {
                if (e == null) {
                    String currScore;
                    Number number;
                    boolean isSkip = false;
                    for (ParseObject question : questions) {
                    	number = question.getNumber("score");
                    	isSkip = saveMap.get(question.getObjectId());
                        if (number != null) {
                            int score = number.intValue();
                            if (isSkip) {
                                currScore = Integer.toString((int)(score/0.99));
                                if (Integer.parseInt(currScore) > 1000)
                                    currScore = "1000";
                            }
                            else {
                                currScore = Integer.toString((int)(score*0.99));
                                if (Integer.parseInt(currScore) < 100)
                                    currScore = "100";
                            }
                        }
                        else
                            currScore = "1000";
                        if (number != null || (number == null && !isSkip)) {
                            question.put("score", Integer.parseInt(currScore));
                            try {
                                question.saveEventually();
                            } catch (RuntimeException err) {}
                        }
                    }
                    saveMap.clear();
                }
                else {
                    Log.e(Constants.LOG_TAG, "Error: " + e.getMessage());
                    showNetworkProblem();
                }
            }
        });
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("hint", savedHint);
        outState.putLong("skipTick", skipTick);
        outState.putLong("hintTick", hintTick);
        outState.putBoolean("hintPressed", hintPressed);
        outState.putBoolean("skipPressed", skipPressed);
        outState.putBoolean("isCorrect", isCorrect);
        if (mCallback != null) {
	        if (!mCallback.isLoggingOut())
	            super.onSaveInstanceState(outState);
	        else
	            super.onSaveInstanceState(null);
        }
    }
    
    @SuppressLint("ResourceAsColor")
	@Override
    public void showNoMoreQuestions(int level) {
        answerImage.setVisibility(View.INVISIBLE);
        questionText.setVisibility(View.VISIBLE);
        questionText.setTextColor(Color.WHITE);
        switch (level) {
        case Constants.MEDIUM:
        	questionText.setText("Congratulations!\nYou've answered all the easy and medium questions!" +
        			"\nChange level for more questions");
        	upLevelButton.setVisibility(View.VISIBLE);
        	break;
        case Constants.EASY:
        	questionText.setText("Congratulations!\nYou've answered all the easy questions!" +
        			"\nChange level for more questions");
        	upLevelButton.setVisibility(View.VISIBLE);
        	break;
        case Constants.HARD:
    	default:
    		questionText.setText("Congratulations!\nYou've answered them all!\nCheck back often for more questions");
    		break;
        }
        /*
        background.resetColoredViews();
        background.addColoredView(questionText,
        		ResourcesSingleton.instance().getColor(R.color.background_dark));
        background.invalidate();
        */
        retryText.setVisibility(View.INVISIBLE);
        answerText.setVisibility(View.INVISIBLE);
        answerPlace.setVisibility(View.INVISIBLE);
        answerButton.setVisibility(View.INVISIBLE);
        if (hintTimer != null)
            hintTimer.cancel();
        if (skipTimer != null)
            skipTimer.cancel();
        hintButton.setVisibility(View.INVISIBLE);
        skipButton.setVisibility(View.INVISIBLE);
        retryButton.setVisibility(View.INVISIBLE);
        scoreText.setVisibility(View.VISIBLE);
        updateScoreText();
    }
    
    @Override
    public void showNetworkProblem() {
        enableButton(true);
        if (mCallback != null)
            mCallback.setNetworkProblem(true);
        try {
	        answerImage.setVisibility(View.INVISIBLE);
	        questionText.setVisibility(View.INVISIBLE);
	        /*
	        background.resetColoredViews();
	        background.invalidate();
	        */
	        retryText.setVisibility(View.VISIBLE);
	        answerText.setVisibility(View.INVISIBLE);
	        answerPlace.setVisibility(View.INVISIBLE);
	        answerButton.setVisibility(View.INVISIBLE);
	        if (hintTimer != null)
	            hintTimer.cancel();
	        if (skipTimer != null)
	            skipTimer.cancel();
	        hintButton.setVisibility(View.INVISIBLE);
	        skipButton.setVisibility(View.INVISIBLE);
	        retryButton.setText(ResourcesSingleton.instance().getString(R.string.retry));
	        retryButton.setVisibility(View.VISIBLE);
	        scoreText.setVisibility(View.INVISIBLE);
        } catch (NullPointerException e) {}
    }
    
    @Override
    public void updateScoreText() {
        if (mCallback != null && mCallback.getCurrentScore() > -1 &&
                mCallback.getDisplayName() != null) {
            scoreText.setText(Integer.toString(mCallback.getCurrentScore()));
            scoreText.setVisibility(View.VISIBLE);
        }
        else
            scoreText.setVisibility(View.INVISIBLE);
    }
    
    private void calculateCurrentScore() {
        currScore = (int)(Integer.parseInt(mCallback.getQuestionScore(0))*0.99);
        if (currScore < 100)
            currScore = 100;
        if (mCallback.getQuestionHint(0))
            currScore = currScore / 2;
    }
    
    private void stageQuestions(final String userId) {
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Stage");
        query.whereEqualTo("userId", userId);
        query.whereContainedIn("questionId", stagedMap.keySet());
        //query.whereEqualTo("questionId", questionId);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> answers, ParseException e) {
                if (e != null && e.getCode() != 101) {
                    Log.e(Constants.LOG_TAG, "Error: " + e.getMessage());
                    showNetworkProblem();
                }
                else {
                	String currQuestionId = null;
                	HashMap<String, Boolean> map = new HashMap<String, Boolean>(stagedMap);
                	stagedMap.clear();
                	for (ParseObject parseAnswer : answers) {
                		currQuestionId = parseAnswer.getString("questionId");
                		parseAnswer.put("hint", map.get(currQuestionId));
                		parseAnswer.put("skip", true);
                        try {
                        	parseAnswer.saveEventually();
                        } catch (RuntimeException exception) {}
                        map.remove(currQuestionId);
                	}
                    if (!map.isEmpty()) {
                    	ParseObject stageAnswer = new ParseObject("Stage");
                    	for (Entry<String, Boolean> stage : map.entrySet()) {
	                        stageAnswer.put("questionId", stage.getKey());
	                        stageAnswer.put("userId", userId);
	                        stageAnswer.put("hint", stage.getValue());
	                        stageAnswer.put("skip", true);
	                        try {
	                            stageAnswer.saveEventually();
	                        } catch (RuntimeException exception) {}
                    	}
                    }
                }
            }
        });
    }
    
    @Override
    public void showRetry() {
        retryButton.setBackgroundResource(
                R.drawable.button_disabled);
        retryButton.setTextColor(
                ResourcesSingleton.instance().getColor(R.color.light_gray));
        retryButton.setText("CHECKING FOR QUESTIONS");
        retryButton.setVisibility(View.VISIBLE);
        retryButton.setEnabled(false);
    }
    /*
    @Override
    public void setBackground(Bitmap newBackground) {
        if (background != null && newBackground != null) {
        	background.setBitmap(newBackground);
        	background.invalidate();
        }
    }
    */
}