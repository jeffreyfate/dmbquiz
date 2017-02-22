package com.jeffthefate.dmbquiz.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.UUID;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.CheckedTextView;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessException;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.BackendlessDataQuery;
import com.backendless.persistence.QueryOptions;
import com.backendless.persistence.local.UserTokenStorageFactory;
import com.facebook.CallbackManager;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.jeffthefate.dmbquiz.ApplicationEx;
import com.jeffthefate.dmbquiz.ApplicationEx.DatabaseHelperSingleton;
import com.jeffthefate.dmbquiz.ApplicationEx.FileCacheSingleton;
import com.jeffthefate.dmbquiz.ApplicationEx.ResourcesSingleton;
import com.jeffthefate.dmbquiz.ApplicationEx.SharedPreferencesSingleton;
import com.jeffthefate.dmbquiz.Constants;
import com.jeffthefate.dmbquiz.DatabaseHelper;
import com.jeffthefate.dmbquiz.ImageViewEx;
import com.jeffthefate.dmbquiz.OnButtonListener;
import com.jeffthefate.dmbquiz.Question;
import com.jeffthefate.dmbquiz.R;
import com.jeffthefate.dmbquiz.SavedInstance;
import com.jeffthefate.dmbquiz.SetInfo;
import com.jeffthefate.dmbquiz.Setlist;
import com.jeffthefate.dmbquiz.SetlistAdapter;
import com.jeffthefate.dmbquiz.Venue;
import com.jeffthefate.dmbquiz.fragment.FragmentBase;
import com.jeffthefate.dmbquiz.fragment.FragmentFaq;
import com.jeffthefate.dmbquiz.fragment.FragmentInfo;
import com.jeffthefate.dmbquiz.fragment.FragmentLeaders;
import com.jeffthefate.dmbquiz.fragment.FragmentLoad;
import com.jeffthefate.dmbquiz.fragment.FragmentLogin;
import com.jeffthefate.dmbquiz.fragment.FragmentNameDialog;
import com.jeffthefate.dmbquiz.fragment.FragmentPager;
import com.jeffthefate.dmbquiz.fragment.FragmentRetained;
import com.jeffthefate.dmbquiz.fragment.FragmentScoreDialog;
import com.jeffthefate.dmbquiz.fragment.FragmentSetlist;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.OnClosedListener;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

/**
 // UserTokenStorageFactory is available in the com.backendless.persistence.local package

 String userToken = UserTokenStorageFactory.instance().getStorage().get();

 if( userToken != null && !userToken.equals( "" ) )
 {  // user login is available, skip the login activity/login form }
 */
public class ActivityMain extends SlidingFragmentActivity implements
		OnButtonListener {

    private static final String TAG_RETAINED_FRAGMENT = "FragmentRetained";
	
	private SavedInstance savedInstance;

	private FragmentManager fMan;
	private FragmentBase currFrag;
    private FragmentRetained retainedFrag;

	private ArrayList<Integer> fieldsList;

	private GetStatsTask getStatsTask;
	private GetScoreTask getScoreTask;
	private GetNextQuestionsTask getNextQuestionsTask;
	private GetStageTask getStageTask;
	private ParseTask parseTask;

	private Bundle leadersBundle;

	private UserTask userTask;
    private LoginTask loginTask;

	private boolean facebookLogin = false;

	public interface UiCallback {
		void showNetworkProblem();

		void showLoading(String message);

		void showNoMoreQuestions(int level);

		void resumeQuestion();

		void updateScoreText();

		void resetHint();

		void disableButton(boolean isRetry);

		void enableButton(boolean isRetry);

		void setDisplayName(String displayName);

		Drawable getBackground();

		// public void setBackground(Bitmap background);
		void showRetry();

		int getPage();

		void setPage(int page);

		void setBackground(Context context, Bitmap newBackground);

		void showResizedSetlist();

		void hideResizedSetlist();

		void setSetlistText(String setlistText, boolean canRefresh);

		void setSetlistStampVisible(boolean isVisible);

		void updateSetlistMap(TreeMap<String, TreeMap<String, String>> setlistMap);
	}

	private NotificationManager nManager;

	private ConnectionReceiver connReceiver;

	private RelativeLayout switchButton;
	// protected RelativeLayout levelButton;
	protected ImageViewEx levelImage;
	protected TextView levelText;
	private CheckedTextView soundsText;
	private CheckedTextView notificationsText;
	private RelativeLayout notificationSoundButton;
	private TextView notificationSoundText;
	private ImageViewEx notificationSoundImage;
	private RelativeLayout notificationAlbumButton;
	private TextView notificationAlbumText;
	private ImageViewEx notificationAlbumImage;

	private boolean goToSetlist = false;

	private Menu mMenu;

	private ScreenshotTask screenshotTask;
	private LogoutWaitTask logoutWaitTask;
	private SetBackgroundTask setBackgroundTask;
	private SetBackgroundWaitTask setBackgroundWaitTask;

	private Tracker tracker;
	
	private SetlistReceiver setlistReceiver;

    private CallbackManager callbackManager;

    private int usableHeight;

	/**
	 * Activity lifecycle methods
	 */
	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		/*
		 * StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
		 * .detectAll() .penaltyDialog() .penaltyLog() .permitDiskReads()
		 * .permitDiskWrites() .build()); StrictMode.setVmPolicy(new
		 * StrictMode.VmPolicy.Builder() .detectAll() .penaltyLog() .build());
		 */
		super.onCreate(savedInstanceState);
		/*
		 * Display display = getWindowManager().getDefaultDisplay(); if
		 * (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR2) { width =
		 * display.getWidth(); height = display.getHeight(); } else { Point size
		 * = new Point(); display.getSize(size); width = size.x; height =
		 * size.y; }
		 */
		setContentView(R.layout.main);

		Field[] fields = R.drawable.class.getFields();
		fieldsList = new ArrayList<>();
		for (Field field : fields) {
			if (field.getName().contains("splash")) {
				try {
					fieldsList.add(field.getInt(null));
				} catch (IllegalArgumentException|IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
		setlistReceiver = new SetlistReceiver();
		/*
		 * TEST Parse.initialize(this,
		 * "6pJz1oVHAwZ7tfOuvHfQCRz6AVKZzg1itFVfzx2q",
		 * "2ocGkdBygVyNStd8gFQQgrDyxxZJCXt3K1GbRpMD");
		 */
		/*
		 * Parse.initialize(this, "ImI8mt1EM3NhZNRqYZOyQpNSwlfsswW73mHsZV3R",
		 * "hpTbnpuJ34zAFLnpOAXjH583rZGiYQVBWWvuXsTo");
		 */
		// PushService.subscribe(this, "", ActivityMain.class);
		// TODO Add these to update to the setlist channel and remove old
		// PushService.unsubscribe(app, "");
		/**
		PushService.subscribe(ApplicationEx.getApp(), "setlist",
				ActivityMain.class);
		// TODO TESTING ONLY
		PushService.subscribe(ApplicationEx.getApp(), "test",
				ActivityMain.class);
		 */
		// PushService.setDefaultPushCallback(this, ActivityMain.class);
		fMan = getSupportFragmentManager();
        retainedFrag = (FragmentRetained) fMan.findFragmentByTag(TAG_RETAINED_FRAGMENT);
		if (retainedFrag != null) {
			savedInstance = retainedFrag.getSavedInstance();
            if (savedInstance == null) {
                savedInstance = new SavedInstance();
            }
            DatabaseHelperSingleton.instance(getApplicationContext()).setUserValue(
                    savedInstance.isNetworkProblem() ? 1 : 0, DatabaseHelper.COL_NETWORK_PROBLEM,
                    savedInstance.getUserId());
		} else {
            retainedFrag = new FragmentRetained();
            fMan.beginTransaction().add(retainedFrag, TAG_RETAINED_FRAGMENT).commit();
			savedInstance = new SavedInstance();
			savedInstance.setUserId(DatabaseHelperSingleton.instance(getApplicationContext()).getUserId());
			if (!savedInstance.getUserId().isEmpty()) {
				getPersistedData(savedInstance.getUserId());
			}
		}
		if (savedInstance.getUserId().isEmpty()) {
			savedInstance.setUserId(DatabaseHelperSingleton.instance(getApplicationContext()).getUserId());
			if (!savedInstance.getUserId().isEmpty()) {
				getUserData(savedInstance.getUserId());
			}
		}
		if (!savedInstance.getUserId().isEmpty()) {
            if (loginTask != null) {
                loginTask.cancel(true);
            }
            loginTask = new LoginTask();
            loginTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			if (savedInstance.getPortBackground() == null) {
                savedInstance.setPortBackground(
                        DatabaseHelperSingleton.instance(getApplicationContext()).getPortBackground(
                                savedInstance.getUserId()));
            }
			if (savedInstance.getLandBackground() == null) {
                savedInstance.setLandBackground(
                        DatabaseHelperSingleton.instance(getApplicationContext()).getLandBackground(
                                savedInstance.getUserId()));
            }
		}
		nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		connReceiver = new ConnectionReceiver();
		getSlidingMenu().setShadowWidthRes(R.dimen.shadow_width);
		getSlidingMenu().setFadeDegree(0.35f);
		if (!SharedPreferencesSingleton.instance(getApplicationContext()).contains(
				ResourcesSingleton.instance(getApplicationContext()).getString(R.string.notification_key))) {
			SharedPreferencesSingleton.putBoolean(R.string.notification_key, true);
		}
		if (!SharedPreferencesSingleton.instance(getApplicationContext()).contains(
				ResourcesSingleton.instance(getApplicationContext()).getString(R.string.level_key))) {
			SharedPreferencesSingleton.putInt(R.string.level_key, Constants.HARD);
		}
		if (!SharedPreferencesSingleton.instance(getApplicationContext()).contains(
				ResourcesSingleton.instance(getApplicationContext()).getString(R.string.notificationsound_key))) {
			SharedPreferencesSingleton.putInt(R.string.notificationsound_key, 0);
		}
		if (!SharedPreferencesSingleton.instance(getApplicationContext()).contains(
				ResourcesSingleton.instance(getApplicationContext()).getString(R.string.notificationtype_key))) {
			SharedPreferencesSingleton.putInt(R.string.notificationtype_key, 1);
		}
		refreshSlidingMenu();
		getWindow().setBackgroundDrawable(null);

        callbackManager = CallbackManager.Factory.create();

		tracker = GoogleAnalytics.getInstance(this).newTracker(R.xml.global_tracker);
	}

	private void getPersistedData(String userId) {
		savedInstance.setLoggedIn(DatabaseHelperSingleton.instance(getApplicationContext()).getUserIntValue(
				DatabaseHelper.COL_LOGGED_IN, userId) == 1);
		savedInstance.setLogging(DatabaseHelperSingleton.instance(getApplicationContext()).getUserIntValue(
				DatabaseHelper.COL_LOGGING, userId) == 1);
		savedInstance.setInLoad(DatabaseHelperSingleton.instance(getApplicationContext()).getUserIntValue(
				DatabaseHelper.COL_IN_LOAD, userId) == 1);
		savedInstance.setInStats(DatabaseHelperSingleton.instance(getApplicationContext()).getUserIntValue(
				DatabaseHelper.COL_IN_STATS, userId) == 1);
		savedInstance.setInInfo(DatabaseHelperSingleton.instance(getApplicationContext()).getUserIntValue(
				DatabaseHelper.COL_IN_INFO, userId) == 1);
		savedInstance.setInFaq(DatabaseHelperSingleton.instance(getApplicationContext()).getUserIntValue(
				DatabaseHelper.COL_IN_FAQ, userId) == 1);
		savedInstance.setInSetlist(DatabaseHelperSingleton.instance(getApplicationContext()).getUserIntValue(
				DatabaseHelper.COL_IN_SETLIST, userId) == 1);
		savedInstance.setInChooser(DatabaseHelperSingleton.instance(getApplicationContext()).getUserIntValue(
				DatabaseHelper.COL_IN_CHOOSER, userId) == 1);
		getUserData(userId);
		savedInstance.setNetworkProblem(DatabaseHelperSingleton.instance(getApplicationContext()).getUserIntValue(
				DatabaseHelper.COL_NETWORK_PROBLEM, userId) == 1);
	}

	private void getUserData(String userId) {
        Log.i(Constants.LOG_TAG, "getUserData: " + userId);
		savedInstance.setPortBackground(
                DatabaseHelperSingleton.instance(getApplicationContext()).getPortBackground(userId));
		savedInstance.setLandBackground(
				DatabaseHelperSingleton.instance(getApplicationContext()).getLandBackground(userId));
		savedInstance.setNewQuestion(DatabaseHelperSingleton.instance(getApplicationContext()).getUserIntValue(
				DatabaseHelper.COL_NEW_QUESTION, userId) == 1);
		savedInstance.setDisplayName(DatabaseHelperSingleton.instance(getApplicationContext()).getUserStringValue(
				DatabaseHelper.COL_DISPLAY_NAME, userId));
		savedInstance.setCurrScore(DatabaseHelperSingleton.instance(getApplicationContext()).getUserIntValue(
				DatabaseHelper.COL_SCORE, userId));
		savedInstance.setQuestionHints(ApplicationEx.getStringArrayPref(getApplicationContext(),
                ResourcesSingleton.instance(getApplicationContext()).getString(R.string.questionhints_key)));
		if (savedInstance.getQuestionHints() == null) {
            savedInstance.setQuestionHints(new ArrayList<String>(Constants.CACHED_QUESTIONS));
        }
		savedInstance.setQuestionSkips(ApplicationEx.getStringArrayPref(getApplicationContext(),
                ResourcesSingleton.instance(getApplicationContext()).getString(R.string.questionskips_key)));
		if (savedInstance.getQuestionSkips() == null) {
            savedInstance.setQuestionSkips(new ArrayList<String>(Constants.CACHED_QUESTIONS));
        }
		savedInstance.setQuestionIds(ApplicationEx.getStringArrayPref(getApplicationContext(),
				ResourcesSingleton.instance(getApplicationContext()).getString(R.string.questionids_key)));
		if (savedInstance.getQuestionIds() == null) {
            savedInstance.setQuestionIds(new ArrayList<String>(Constants.CACHED_QUESTIONS));
        }
		if (savedInstance.getQuestionIds().isEmpty()) {
			String questionId = DatabaseHelperSingleton.instance(getApplicationContext()).getCurrQuestionId(userId);
			boolean questionHint = DatabaseHelperSingleton.instance(getApplicationContext())
                    .getCurrQuestionHint(userId);
			if (questionId != null) {
				addQuestionId(questionId);
				addQuestionHint(questionHint);
				addQuestionSkip(false);
			}
			questionId = DatabaseHelperSingleton.instance(getApplicationContext()).getNextQuestionId(userId);
			questionHint = DatabaseHelperSingleton.instance(getApplicationContext()).getNextQuestionHint(userId);
			if (questionId != null) {
				addQuestionId(questionId);
				addQuestionHint(questionHint);
				addQuestionSkip(false);
			}
			questionId = DatabaseHelperSingleton.instance(getApplicationContext()).getThirdQuestionId(userId);
			questionHint = DatabaseHelperSingleton.instance(getApplicationContext()).getThirdQuestionHint(userId);
			if (questionId != null) {
				addQuestionId(questionId);
				addQuestionHint(questionHint);
				addQuestionSkip(false);
			}
		}
		savedInstance.setQuestions(ApplicationEx.getStringArrayPref(getApplicationContext(),
				ResourcesSingleton.instance(getApplicationContext()).getString(R.string.questions_key)));
		if (savedInstance.getQuestions() == null) {
            savedInstance.setQuestions(new ArrayList<String>(Constants.CACHED_QUESTIONS));
        }
		if (savedInstance.getQuestions().isEmpty()) {
			String question = DatabaseHelperSingleton.instance(getApplicationContext()).getCurrQuestionQuestion(userId);
			if (question != null) {
                addQuestion(question);
            }
			question = DatabaseHelperSingleton.instance(getApplicationContext()).getNextQuestionQuestion(userId);
			if (question != null) {
                addQuestion(question);
            }
			question = DatabaseHelperSingleton.instance(getApplicationContext()).getThirdQuestionQuestion(userId);
			if (question != null) {
                addQuestion(question);
            }
		}
		savedInstance.setQuestionAnswers(ApplicationEx.getStringArrayPref(getApplicationContext(),
				ResourcesSingleton.instance(getApplicationContext()).getString(R.string.questionanswers_key)));
		if (savedInstance.getQuestionAnswers() == null) {
            savedInstance.setQuestionAnswers(new ArrayList<String>(Constants.CACHED_QUESTIONS));
        }
		if (savedInstance.getQuestionAnswers().isEmpty()) {
			String questionAnswer = DatabaseHelperSingleton.instance(getApplicationContext())
                    .getCurrQuestionAnswer(userId);
			if (questionAnswer != null) {
                addQuestionAnswer(questionAnswer);
            }
			questionAnswer = DatabaseHelperSingleton.instance(getApplicationContext()).getNextQuestionAnswer(userId);
			if (questionAnswer != null) {
                addQuestionAnswer(questionAnswer);
            }
			questionAnswer = DatabaseHelperSingleton.instance(getApplicationContext()).getThirdQuestionAnswer(userId);
			if (questionAnswer != null) {
                addQuestionAnswer(questionAnswer);
            }
		}
		savedInstance.setQuestionCategories(ApplicationEx.getStringArrayPref(getApplicationContext(),
				ResourcesSingleton.instance(getApplicationContext()).getString(R.string.questioncategories_key)));
		if (savedInstance.getQuestionCategories() == null) {
            savedInstance.setQuestionCategories(new ArrayList<String>(Constants.CACHED_QUESTIONS));
        }
		if (savedInstance.getQuestionCategories().isEmpty()) {
			String questionCategory = DatabaseHelperSingleton.instance(getApplicationContext())
                    .getCurrQuestionCategory(userId);
			if (questionCategory != null) {
                addQuestionCategory(questionCategory);
            }
			questionCategory = DatabaseHelperSingleton.instance(getApplicationContext())
                    .getNextQuestionCategory(userId);
			if (questionCategory != null) {
                addQuestionCategory(questionCategory);
            }
			questionCategory = DatabaseHelperSingleton.instance(getApplicationContext())
					.getThirdQuestionCategory(userId);
			if (questionCategory != null) {
                addQuestionCategory(questionCategory);
            }
		}
		savedInstance.setQuestionScores(ApplicationEx.getStringArrayPref(getApplicationContext(),
				ResourcesSingleton.instance(getApplicationContext()).getString(R.string.questionscores_key)));
		if (savedInstance.getQuestionScores() == null) {
            savedInstance.setQuestionScores(new ArrayList<String>(Constants.CACHED_QUESTIONS));
        }
		if (savedInstance.getQuestionScores().isEmpty()) {
			String questionScore = DatabaseHelperSingleton.instance(getApplicationContext())
					.getCurrQuestionScore(userId);
			if (questionScore != null) {
                addQuestionScore(questionScore);
            }
			questionScore = DatabaseHelperSingleton.instance(getApplicationContext()).getNextQuestionScore(userId);
			if (questionScore != null) {
                addQuestionScore(questionScore);
            }
			questionScore = DatabaseHelperSingleton.instance(getApplicationContext()).getThirdQuestionScore(userId);
			if (questionScore != null) {
                addQuestionScore(questionScore);
            }
		}
		savedInstance.setCorrectAnswers(ApplicationEx.getStringArrayPref(getApplicationContext(),
				ResourcesSingleton.instance(getApplicationContext()).getString(R.string.correct_key)));
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (intent.getBooleanExtra("setlist", false)) {
            tracker.setScreenName("ActivityMain/SetlistNotificationClicked");
			tracker.send(new HitBuilders.ScreenViewBuilder().build());
			goToSetlist = true;
		} else {
            goToSetlist = false;
        }
	}

	@Override
	public void onResume() {
		super.onResume();
		if (parseTask != null) {
			parseTask.cancel(true);
		}
		if (getScoreTask != null) {
			getScoreTask.cancel(true);
		}
		if (!savedInstance.getUserId().isEmpty()) {
            if (loginTask != null) {
                loginTask.cancel(true);
            }
            loginTask = new LoginTask();
            loginTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
        // TODO Figure out how to make displayName for Facebook
        /**
		if (user != null) {
			getFacebookDisplayName(user);
		}
         */
		if (!savedInstance.isLogging()) {
			if (savedInstance.getUserId().isEmpty()) {
				if (!savedInstance.isLoggedIn()) {
					logOut();
				} else {
					checkUser();
				}
			} else {
				if (savedInstance.getCorrectAnswers() == null) {
					savedInstance.setLogging(true);
					showLogin();
					if (!DatabaseHelperSingleton.instance(getApplicationContext()).isAnonUser(
							savedInstance.getUserId())) {
						getScore(false, true, savedInstance.getUserId(), false);
					} else {
						savedInstance.setCorrectAnswers(
								new ArrayList<String>());
						if (savedInstance.getQuestionIds().size() >= 1
								&& savedInstance.getQuestionIds().get(0) !=
									null) {
							try {
								goToQuiz();
							} catch (IllegalStateException exception) {
                                // TODO
							}
						} else {
							getNextQuestions(
									false,
									SharedPreferencesSingleton
											.instance(getApplicationContext())
											.getInt(ResourcesSingleton
													.instance(getApplicationContext()).getString(
															R.string.level_key),
													Constants.HARD));
						}
					}
				} else {
                    showLoggedInFragment();
                }
			}
		} else {
			if (!savedInstance.getUserId().isEmpty()) {
				setupUser(savedInstance.isNewUser());
			} else if (!facebookLogin) {
				checkUser();
			}
		}
		if (savedInstance.isInInfo()) {
			onInfoPressed();
		}
		if (savedInstance.isInFaq()) {
			onFaqPressed();
		}
		if (savedInstance.getSelectedSet() == null) {
			savedInstance.setSelectedSet((SetInfo) FileCacheSingleton.instance(getApplicationContext())
					.readSerializableFromFile(Constants.SELECTED_SET_FILE));
			savedInstance.setSetlistMap(new TreeMap<String, TreeMap<String, String>>(new YearComparator()));
			TreeMap<String, TreeMap<String, String>> tempMap = (TreeMap<String,
					TreeMap<String, String>>) FileCacheSingleton.instance(getApplicationContext())
					.readSerializableFromFile(Constants.SETLIST_MAP_FILE);
			if (tempMap != null) {
				TreeMap<String, String> tempChild;
				for (Entry<String, TreeMap<String, String>> entry : tempMap.entrySet()) {
					tempChild = new TreeMap<>(new SetlistComparator());
					tempChild.putAll(entry.getValue());
					savedInstance.getSetlistMap().put(entry.getKey(), tempChild);
				}
			}
		}
		ApplicationEx.setActive();
		nManager.cancel(Constants.NOTIFICATION_NEW_QUESTIONS);
		getApplicationContext().registerReceiver(connReceiver, new IntentFilter(Constants.ACTION_CONNECTION));
		IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.ACTION_UPDATE_SETLIST);
        intentFilter.addAction(Constants.ACTION_NEW_SONG);
		getApplicationContext().registerReceiver(setlistReceiver, intentFilter);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		switch (newConfig.orientation) {
		case Configuration.ORIENTATION_LANDSCAPE:
            tracker.send(new HitBuilders.EventBuilder()
                    .setCategory(Constants.CATEGORY_HARDWARE)
                    .setAction(Constants.ACTION_ROTATE)
                    .setLabel("landscape")
                    .setValue(1L)
                    .build());
			getSlidingMenu().setBehindOffsetRes(R.dimen.menu_width_land);
			break;
		case Configuration.ORIENTATION_PORTRAIT:
            tracker.send(new HitBuilders.EventBuilder()
                    .setCategory(Constants.CATEGORY_HARDWARE)
                    .setAction(Constants.ACTION_ROTATE)
                    .setLabel("portrait")
                    .setValue(1L)
                    .build());
			getSlidingMenu().setBehindOffsetRes(R.dimen.menu_width_port);
			break;
		}
	}
	
	private class SetlistReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
			new ReceiveTask(context, intent).executeOnExecutor(
					AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }
    
    private class ReceiveTask extends AsyncTask<Void, Void, Void> {
    	private Context context;
    	private Intent intent;
    	private WakeLock wakeLock;
    	
    	private ReceiveTask(Context context, Intent intent) {
    		this.context = context;
    		this.intent = intent;
    	}
    	
        @Override
        protected Void doInBackground(Void... nothing) {
        	PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, Constants.SETLIST_WAKE_LOCK);
            wakeLock.acquire();
            return null;
        }
        
        protected void onProgressUpdate(Void... nothing) {
        }
        
        @Override
        protected void onCancelled(Void nothing) {
        }
        
        @Override
        protected void onPostExecute(Void nothing) {
            if (currFrag != null) {
                if ((!intent.hasExtra("success") || intent.getBooleanExtra("success", false))) {
                    Log.i(Constants.LOG_TAG, "FragmentBase ReceiveTask");
                    currFrag.updateSetText();
                } else {
                    currFrag.showNetworkProblem();
                }
            }
        	wakeLock.release();
        }
    }

	private void showLogin(/* boolean loggingIn */) {
		try {
			FragmentLogin fLogin = new FragmentLogin();
			currFrag = fLogin;
			FragmentTransaction ft = fMan.beginTransaction();
			if (currFrag != null && currFrag instanceof FragmentPager && currFrag.isVisible()) {
                ((FragmentPager) currFrag).removeChildren(ft);
            }
			ft.replace(android.R.id.content, fLogin, "fLogin").commitAllowingStateLoss();
			fMan.executePendingTransactions();
			getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
			getSlidingMenu().setTouchModeBehind(SlidingMenu.TOUCHMODE_NONE);
		} catch (IllegalStateException e) {
			// TODO
		}
	}

	private void showLoggedInFragment() {
		// fetchDisplayName();
		if (Backendless.UserService.CurrentUser() != null && savedInstance.getDisplayName() == null) {
			savedInstance.setDisplayName((String) Backendless.UserService.CurrentUser().getProperty("displayName"));
			DatabaseHelperSingleton.instance(getApplicationContext()).setUserValue(savedInstance.getDisplayName(),
					DatabaseHelper.COL_DISPLAY_NAME, savedInstance.getUserId());
		}
		if (savedInstance.isInStats()) {
			showLeaders();
		}
		else if (savedInstance.isInLoad()) {
			onStatsPressed();
		}
		/*
		 * else if (inSetlist) showSetlist(false);
		 */
		else {
			savedInstance.setLogging(false);
			DatabaseHelperSingleton.instance(getApplicationContext()).setUserValue(savedInstance.isLogging() ? 1 : 0,
					DatabaseHelper.COL_LOGGING, savedInstance.getUserId());
			savedInstance.setLoggedIn(true);
			DatabaseHelperSingleton.instance(getApplicationContext()).setUserValue(savedInstance.isLoggedIn() ? 1 : 0,
					DatabaseHelper.COL_LOGGED_IN, savedInstance.getUserId());
			showQuiz();
		}
	}

	private void fetchDisplayName() {
		if (savedInstance.getUserId().isEmpty()) {
			return;
		}
        // TODO Where is stayLoggedIn set?
        Backendless.UserService.findById(Backendless.UserService.loggedInUser(),
                new AsyncCallback<BackendlessUser>() {
            @Override
            public void handleResponse(BackendlessUser currentUser) {
                if (currentUser != null) {
                    savedInstance.setDisplayName((String) currentUser.getProperty("displayName"));
                    Backendless.UserService.setCurrentUser(currentUser);
                }
                if (getStatsTask != null) {
                    getStatsTask.cancel(true);
                }
                getStatsTask = new GetStatsTask();
                getStatsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                // TODO
            }
        });
	}

	private void showLeaders() {
		try {
			FragmentLeaders fLeaders = new FragmentLeaders();
			currFrag = fLeaders;
			fMan.beginTransaction()
					.replace(android.R.id.content, fLeaders, "fLeaders")
					.commitAllowingStateLoss();
			fMan.executePendingTransactions();
			getSlidingMenu()
					.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
			refreshSlidingMenu();
		} catch (IllegalStateException e) {
            // TODO
		}
	}

	private void checkUser() {
        if (!savedInstance.getUserId().isEmpty()) {
            getUserData(savedInstance.getUserId());
		}
		if (!savedInstance.isLoggedIn() && !savedInstance.isLogging() && savedInstance.getUserId().isEmpty()) {
            logOut();
        } else {
            setupUser(savedInstance.isNewUser());
        }
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onBackPressed() {
		if (!savedInstance.isInStats() && !savedInstance.isInLoad() &&
				!savedInstance.isLogging() && !savedInstance.isInInfo() &&
				!savedInstance.isInFaq()) {
            moveTaskToBack(true);
        } else {
			if (savedInstance.isInLoad()) {
				if (getStatsTask != null)
					getStatsTask.cancel(true);
				showQuiz();
				savedInstance.setInLoad(false);
				DatabaseHelperSingleton.instance(getApplicationContext()).setUserValue(
						savedInstance.isInLoad() ? 1 : 0,
						DatabaseHelper.COL_IN_LOAD, savedInstance.getUserId());
			} else if (savedInstance.isInFaq()) {
				showLeaders();
				savedInstance.setInFaq(false);
				DatabaseHelperSingleton.instance(getApplicationContext()).setUserValue(
						savedInstance.isInFaq() ? 1 : 0,
						DatabaseHelper.COL_IN_FAQ, savedInstance.getUserId());
			} else if (savedInstance.isInStats()) {
				savedInstance.setInStats(false);
				DatabaseHelperSingleton.instance(getApplicationContext()).setUserValue(
						savedInstance.isInStats() ? 1 : 0,
								DatabaseHelper.COL_IN_STATS,
								savedInstance.getUserId());
				showQuiz();
			} else if (savedInstance.isInInfo()) {
				showSplash();
				savedInstance.setInInfo(false);
				DatabaseHelperSingleton.instance(getApplicationContext()).setUserValue(
						savedInstance.isInInfo() ? 1 : 0,
						DatabaseHelper.COL_IN_INFO, savedInstance.getUserId());
			} else if (savedInstance.isLogging()) {
                if (loginTask != null) {
                    loginTask.cancel(true);
                }
				if (userTask != null) {
                    userTask.cancel(true);
                }
				if (getScoreTask != null) {
                    getScoreTask.cancel(true);
                }
				logOut();
			}
		}
	}

	private void showQuiz() {
		savedInstance.setNewUser(false);
		try {
			FragmentPager fQuiz = new FragmentPager();
			currFrag = fQuiz;
			FragmentTransaction ft = fMan.beginTransaction();
			/*
			 * if (fromStats) ft.setCustomAnimations(R.anim.slide_in_top,
			 * R.anim.slide_out_top); else if (fromSetlist)
			 * ft.setCustomAnimations(R.anim.slide_in_left,
			 * R.anim.slide_out_right);
			 */
			ft.replace(android.R.id.content, fQuiz, "fQuiz").commitAllowingStateLoss();
			fMan.executePendingTransactions();
			refreshSlidingMenu();
		} catch (IllegalStateException e) {
            // TODO
		}
	}

	private void showSplash() {
		Log.i(Constants.LOG_TAG, "ActivityMain showSplash");
		try {
			FragmentPager fSplash = new FragmentPager();
			currFrag = fSplash;
			FragmentTransaction ft = fMan.beginTransaction();
			/*
			 * if (fromInfo) ft.setCustomAnimations(R.anim.slide_in_bottom,
			 * R.anim.slide_out_bottom); else if (fromSetlist)
			 * ft.setCustomAnimations(R.anim.slide_in_left,
			 * R.anim.slide_out_right);
			 */
			ft.replace(android.R.id.content, fSplash, "fSplash").commitAllowingStateLoss();
			fMan.executePendingTransactions();
			refreshSlidingMenu();
		} catch (IllegalStateException e) {
            // TODO
		}
	}

	@Override
	public void onPause() {
		try {
			getApplicationContext().unregisterReceiver(connReceiver);
			getApplicationContext().unregisterReceiver(setlistReceiver);
		} catch (IllegalArgumentException e) {
            // TODO
        }
		if (getStageTask != null) {
			getStageTask.cancel(true);
		}
		if (getNextQuestionsTask != null) {
			getNextQuestionsTask.cancel(true);
		}
        if (loginTask != null) {
            loginTask.cancel(true);
        }
		if (userTask != null) {
			userTask.cancel(true);
		}
		if (getStatsTask != null) {
			getStatsTask.cancel(true);
		}
		if (!savedInstance.isLogging() && !savedInstance.getUserId().isEmpty()
				&& !DatabaseHelperSingleton.instance(getApplicationContext()).isAnonUser(
						savedInstance.getUserId()))
			getScore(false, false, savedInstance.getUserId(), false);
		ApplicationEx.setInactive();
		DatabaseHelperSingleton.instance(getApplicationContext()).setCheckCount(
				savedInstance.getUserId(), true);
		FileCacheSingleton.instance(getApplicationContext()).saveSerializableToFile(
				Constants.SELECTED_SET_FILE, savedInstance.getSelectedSet());
		FileCacheSingleton.instance(getApplicationContext()).saveSerializableToFile(
				Constants.SETLIST_MAP_FILE, savedInstance.getSetlistMap());
        if (isFinishing()) {
            fMan.beginTransaction().remove(retainedFrag).commit();
        }
		super.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	private void getScore(boolean show, boolean restore, String userId, boolean newUser) {
		if (getScoreTask != null) {
            getScoreTask.cancel(true);
        }
		getScoreTask = new GetScoreTask(show, restore, userId, newUser);
        getScoreTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private class GetScoreTask extends AsyncTask<Void, Void, Void> {
		private boolean show;
		private boolean restore;
		private String userId;
		private boolean newUser;
		private ArrayList<String> tempAnswers;
		private Integer correctScore = 0;

		private GetScoreTask(boolean show, boolean restore, String userId, boolean newUser) {
			this.show = show;
			this.restore = restore;
			this.userId = userId;
			this.newUser = newUser;
		}

		@Override
		protected void onPreExecute() {
			if (tempAnswers != null) {
                tempAnswers.clear();
            } else {
                tempAnswers = new ArrayList<>();
            }
			if (!newUser && currFrag != null) {
                currFrag.showLoading("Calculating score...");
            }
		}

		@Override
		protected Void doInBackground(Void... nothing) {
			// Find correct answers for current user
            List<BackendlessUser> userList = getUserRelations(userId, "correct", "hint");
            if (userList != null && !userList.isEmpty()) {
                BackendlessUser user = userList.get(0);
                Object[] corrects = (Object[]) user.getProperty("correct");
                Object[] hints = (Object[]) user.getProperty("hint");
                Integer tempScore;
                ArrayList<String> hintIds = new ArrayList<>(hints.length);
                for (Object hint : hints) {
                    hintIds.add(((HashMap<String, Object>) hint).get("objectId").toString());
                }
                for (Object correct : corrects) {
                    tempScore = (Integer) ((HashMap<String, Object>) correct).get("score");
                    if (tempScore == null) {
                        tempScore = 1000;
                    }
                    if (hintIds.contains(((HashMap<String, Object>) correct).get("objectId").toString())) {
                        tempScore = tempScore / 2;
                    }
                    correctScore += tempScore;
                    tempAnswers.add(((HashMap<String, Object>) correct).get("objectId").toString());
                    if (isCancelled()) {
                        return null;
                    }
                }
            }
			// TODO Revisit when adding levels back
			/*
			 * ParseQuery scoreQuery = new ParseQuery("Question"); try {
			 * scoreQuery.orderByAscending("score"); lowest =
			 * scoreQuery.getFirst().getInt("score");
			 * ApplicationEx.addParseQuery();
			 * scoreQuery.orderByDescending("score"); highest =
			 * scoreQuery.getFirst().getInt("score"); if (highest == 0) highest
			 * = 1000; int easy = ((highest-lowest) / 3) + lowest; int med =
			 * ((easy-lowest) * 2) + lowest;
			 * DatabaseHelperSingleton.instance().setUserValue(easy,
			 * DatabaseHelper.COL_EASY, userId);
			 * DatabaseHelperSingleton.instance().setUserValue(med,
			 * DatabaseHelper.COL_MEDIUM, userId); } catch (ParseException e) {
			 * Log.e(Constants.LOG_TAG, "Error: " + e.getMessage()); if
			 * (userTask != null) userTask.cancel(true); if (getScoreTask !=
			 * null) getScoreTask.cancel(true); if (show && currFrag != null)
			 * currFrag.showNetworkProblem(); }
			 */
			if (!isCancelled()) {
				savedInstance.setCurrScore(correctScore);
                correctScore = 0;
				saveUserScore(savedInstance.getCurrScore());
				savedInstance.setCorrectAnswers(new ArrayList<>(tempAnswers));
				ApplicationEx.setStringArrayPref(getApplicationContext(),
                        ResourcesSingleton.instance(getApplicationContext()).getString(R.string.correct_key),
						savedInstance.getCorrectAnswers());
				publishProgress();
			}
            correctScore = 0;
			return null;
		}

		@Override
		protected void onCancelled(Void nothing) {
			correctScore = 0;
		}

		protected void onProgressUpdate(Void... nothing) {
			if (show) {
				if (savedInstance.getQuestionIds().size() >= 1 &&
						savedInstance.getQuestionIds().get(0) != null) {
					try {
						goToQuiz();
					} catch (IllegalStateException exception) {
                        // TODO
					}
				} else {
					getNextQuestions(false,
							SharedPreferencesSingleton.instance(getApplicationContext()).getInt(
                                    ResourcesSingleton.instance(getApplicationContext()).getString(R.string.level_key),
											Constants.HARD));
				}
			}
			if (restore) {
                showLoggedInFragment();
            }
		}

		@Override
		protected void onPostExecute(Void nothing) {
		}
	}

    private List<BackendlessUser> getUserRelations(String userId, String... relations) {
        QueryOptions queryOptions = new QueryOptions();
        for (String relation : relations) {
            queryOptions.addRelated(relation);
        }
        queryOptions.setPageSize(1);
        queryOptions.setOffset(0);
        BackendlessDataQuery query = new BackendlessDataQuery();
        query.setQueryOptions(queryOptions);
        query.setWhereClause("objectId = '" + userId + "'");
        try {
            return Backendless.Data.of(BackendlessUser.class).find(query).getData();
        } catch (BackendlessException e) {
            Log.e(Constants.LOG_TAG, "Error: " + e.getMessage());
            if (currFrag != null) {
                currFrag.showNetworkProblem();
            }
            return null;
        }
    }

	private void goToQuiz() {
		savedInstance.setLogging(false);
		DatabaseHelperSingleton.instance(getApplicationContext()).setUserValue(
				savedInstance.isLogging() ? 1 : 0,
				DatabaseHelper.COL_LOGGING, savedInstance.getUserId());
		savedInstance.setLoggedIn(true);
		DatabaseHelperSingleton.instance(getApplicationContext()).setUserValue(
				savedInstance.isLoggedIn() ? 1 : 0,
				DatabaseHelper.COL_LOGGED_IN, savedInstance.getUserId());
		showQuiz();
	}

	@Override
	public void onDestroy() {
		if (setBackgroundTask != null)
			setBackgroundTask.cancel(true);
		if (setBackgroundWaitTask != null)
			setBackgroundWaitTask.cancel(true);
		if (getScoreTask != null)
			getScoreTask.cancel(true);
		retainedFrag.setSavedInstance(savedInstance);
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_setlist, menu);
		mMenu = menu;
		refreshMenu();
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
            tracker.send(new HitBuilders.EventBuilder()
                    .setCategory(Constants.CATEGORY_ACTION_BAR)
                    .setAction(Constants.ACTION_BUTTON_PRESS)
                    .setLabel("home")
                    .setValue(1L)
                    .build());
			if (getSlidingMenu().isMenuShowing()) {
                tracker.send(new HitBuilders.EventBuilder()
                        .setCategory(Constants.CATEGORY_MENU)
                        .setAction(Constants.ACTION_MENU_CLOSE)
                        .setLabel("hardwareMenu")
                        .setValue(1L)
                        .build());
			} else {
                tracker.send(new HitBuilders.EventBuilder()
                        .setCategory(Constants.CATEGORY_MENU)
                        .setAction(Constants.ACTION_MENU_OPEN)
                        .setLabel("hardwareMenu")
                        .setValue(1L)
                        .build());
			}
			if (currFrag != null) {
				if (currFrag instanceof FragmentPager) {
					if (currFrag.getPage() == 1)
						currFrag.setPage(0);
					else
						toggle();
				} else
					toggle();
			}
			return true;
		case R.id.ShareMenu:
            tracker.send(new HitBuilders.EventBuilder()
                    .setCategory(Constants.CATEGORY_ACTION_BAR)
                    .setAction(Constants.ACTION_BUTTON_PRESS)
                    .setLabel("share")
                    .setValue(1L)
                    .build());
			ApplicationEx.showShortToast("Capturing screen");
			if (screenshotTask != null) {
                screenshotTask.cancel(true);
            }
			screenshotTask = new ScreenshotTask(true);
            screenshotTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			return true;
		case R.id.SetlistMenu:
            tracker.send(new HitBuilders.EventBuilder()
                    .setCategory(Constants.CATEGORY_ACTION_BAR)
                    .setAction(Constants.ACTION_BUTTON_PRESS)
                    .setLabel("setlist")
                    .setValue(1L)
                    .build());
			if (currFrag instanceof FragmentPager) {
				currFrag.setPage(1);
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private class ScreenshotTask extends AsyncTask<Void, Void, Void> {
		private boolean isSetlist = false;

		private ScreenshotTask(boolean isSetlist) {
			this.isSetlist = isSetlist;
		}

		@Override
		protected Void doInBackground(Void... nothing) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
                // TODO
			}
			return null;
		}

		@Override
		protected void onCancelled(Void nothing) {
		}

		@Override
		protected void onPostExecute(Void nothing) {
			shareScreenshot(isSetlist);
		}
	}

	private class LogoutWaitTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... nothing) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
                // TODO
			}
			DatabaseHelperSingleton.instance(getApplicationContext()).setOffset(0, getUserId());
			setLoggingOut(true);
			clearQuestionIds();
			clearQuestions();
			clearQuestionAnswers();
			clearQuestionCategories();
			clearQuestionScores();
			clearQuestionHints();
			clearQuestionSkips();
			updatePersistedLists();
			return null;
		}

		@Override
		protected void onCancelled(Void nothing) {
		}

		@Override
		protected void onPostExecute(Void nothing) {
			showLogin();
			// getSlidingMenu().showContent();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
            tracker.send(new HitBuilders.EventBuilder()
                    .setCategory(Constants.CATEGORY_HARDWARE)
                    .setAction(Constants.ACTION_BUTTON_PRESS)
                    .setLabel("menu")
                    .setValue(1L)
                    .build());
			if (getSlidingMenu().isMenuShowing()) {
                tracker.send(new HitBuilders.EventBuilder()
                        .setCategory(Constants.CATEGORY_MENU)
                        .setAction(Constants.ACTION_MENU_CLOSE)
                        .setLabel("hardwareMenu")
                        .setValue(1L)
                        .build());
			} else {
                tracker.send(new HitBuilders.EventBuilder()
                        .setCategory(Constants.CATEGORY_MENU)
                        .setAction(Constants.ACTION_MENU_OPEN)
                        .setLabel("hardwareMenu")
                        .setValue(1L)
                        .build());
			}
			if (currFrag != null) {
				if (currFrag instanceof FragmentPager) {
					if (currFrag.getPage() == 1)
						currFrag.setPage(0);
					else
						toggle();
				} else
					toggle();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * Sliding menu methods and associated methods/classes
	 */
	private void refreshSlidingMenu() {
		if (!savedInstance.isLoggedIn()) {
			setBehindContentView(R.layout.menu_splash);
			switch (ResourcesSingleton.instance(getApplicationContext()).getConfiguration().orientation) {
			case Configuration.ORIENTATION_LANDSCAPE:
				getSlidingMenu().setBehindOffsetRes(R.dimen.menu_width_land);
				break;
			default:
				getSlidingMenu().setBehindOffsetRes(R.dimen.menu_width_port);
				break;
			}
            RelativeLayout infoButton = (RelativeLayout) getSlidingMenu().findViewById(R.id.InfoButton);
			infoButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
                    tracker.send(new HitBuilders.EventBuilder()
                            .setCategory(Constants.CATEGORY_MENU)
                            .setAction(Constants.ACTION_BUTTON_PRESS)
                            .setLabel("splashInfo")
                            .setValue(1L)
                            .build());
					if (getSlidingMenu().isMenuShowing()) {
						getSlidingMenu().setOnClosedListener(
								new OnClosedListener() {
									@Override
									public void onClosed() {
										getSlidingMenu().setOnClosedListener(null);
										Thread infoThread = new Thread() {
											public void run() {
                                                try {
													Thread.sleep(500);
												} catch (InterruptedException e) {
                                                    // TODO
												}
												onInfoPressed();
											}
										};
										infoThread.start();
									}
								});
						getSlidingMenu().showContent();
					}
				}
			});
			/*
			 * switchButton = (RelativeLayout)
			 * getSlidingMenu().findViewById(R.id.SwitchButton);
			 * switchButton.setOnClickListener(new OnClickListener() {
			 *
			 * @Override public void onClick(View arg0) {
			 * switchButton.setEnabled(false); if
			 * (getSlidingMenu().isMenuShowing()) {
			 * getSlidingMenu().setOnClosedListener(new OnClosedListener() {
			 *
			 * @Override public void onClosed() {
			 * getSlidingMenu().setOnClosedListener(null);
			 * setBackground(getSplashBackground(), true, "splash"); } });
			 * getSlidingMenu().showContent(); } } });
			 * switchButton.setEnabled(true);
			 */
            RelativeLayout exitButton = (RelativeLayout) getSlidingMenu().findViewById(
					R.id.ExitButton);
			exitButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
                    tracker.send(new HitBuilders.EventBuilder()
                            .setCategory(Constants.CATEGORY_MENU)
                            .setAction(Constants.ACTION_BUTTON_PRESS)
                            .setLabel("splashExit")
                            .setValue(1L)
                            .build());
					if (getSlidingMenu().isMenuShowing())
						moveTaskToBack(true);
				}
			});
			notificationSoundButton = (RelativeLayout) getSlidingMenu()
					.findViewById(R.id.NotificationSoundsButton);
			notificationSoundImage = (ImageViewEx) getSlidingMenu()
					.findViewById(R.id.NotificationSoundsImage);
			notificationSoundText = (TextView) getSlidingMenu().findViewById(
					R.id.NotificationSoundsText);
			int soundSetting = SharedPreferencesSingleton.instance(getApplicationContext()).getInt(
					ResourcesSingleton.instance(getApplicationContext()).getString(
							R.string.notificationsound_key), 0);
			notificationSoundImage.setImageLevel(soundSetting);
			notificationAlbumButton = (RelativeLayout) getSlidingMenu()
					.findViewById(R.id.NotificationTypeButton);
			notificationAlbumImage = (ImageViewEx) getSlidingMenu()
					.findViewById(R.id.NotificationTypeImage);
			notificationAlbumText = (TextView) getSlidingMenu().findViewById(
					R.id.NotificationTypeText);
			switch (soundSetting) {
			case 0:
				notificationSoundText.setText(R.string.NotificationBothTitle);
				notificationAlbumButton.setEnabled(true);
				notificationAlbumImage.setEnabled(true);
				notificationAlbumText.setTextColor(ResourcesSingleton
						.instance(getApplicationContext()).getColor(android.R.color.white));
				notificationAlbumText.setEnabled(true);
				break;
			case 1:
				notificationSoundText.setText(R.string.NotificationSoundsTitle);
				notificationAlbumButton.setEnabled(true);
				notificationAlbumImage.setEnabled(true);
				notificationAlbumText.setTextColor(ResourcesSingleton
						.instance(getApplicationContext()).getColor(android.R.color.white));
				notificationAlbumText.setEnabled(true);
				break;
			case 2:
				notificationSoundText
						.setText(R.string.NotificationVibrateTitle);
				notificationAlbumButton.setEnabled(false);
				notificationAlbumImage.setEnabled(false);
				notificationAlbumText.setTextColor(ResourcesSingleton
						.instance(getApplicationContext()).getColor(R.color.dark_gray));
				notificationAlbumText.setEnabled(false);
				break;
			case 3:
				notificationSoundText.setText(R.string.NotificationSilentTitle);
				notificationAlbumButton.setEnabled(false);
				notificationAlbumImage.setEnabled(false);
				notificationAlbumText.setTextColor(ResourcesSingleton
						.instance(getApplicationContext()).getColor(R.color.dark_gray));
				notificationAlbumText.setEnabled(false);
				break;
			}
			notificationSoundButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					if (getSlidingMenu().isMenuShowing()) {
						int soundSetting = SharedPreferencesSingleton
								.instance(getApplicationContext())
								.getInt(ResourcesSingleton.instance(getApplicationContext())
										.getString(
												R.string.notificationsound_key),
										0);
						switch (soundSetting) {
						case 0:
							notificationSoundText
									.setText(R.string.NotificationSilentTitle);
							notificationAlbumButton.setEnabled(false);
							notificationAlbumImage.setEnabled(false);
							notificationAlbumText
									.setTextColor(ResourcesSingleton.instance(getApplicationContext())
											.getColor(R.color.dark_gray));
							notificationAlbumText.setEnabled(false);
							soundSetting = 3;
							break;
						case 1:
							notificationSoundText
									.setText(R.string.NotificationBothTitle);
							notificationAlbumButton.setEnabled(true);
							notificationAlbumImage.setEnabled(true);
							notificationAlbumText
									.setTextColor(ResourcesSingleton.instance(getApplicationContext())
											.getColor(android.R.color.white));
							notificationAlbumText.setEnabled(true);
							soundSetting = 0;
							break;
						case 2:
							notificationSoundText
									.setText(R.string.NotificationSoundsTitle);
							notificationAlbumButton.setEnabled(true);
							notificationAlbumImage.setEnabled(true);
							notificationAlbumText
									.setTextColor(ResourcesSingleton.instance(getApplicationContext())
											.getColor(android.R.color.white));
							notificationAlbumText.setEnabled(true);
							soundSetting = 1;
							break;
						case 3:
							notificationSoundText
									.setText(R.string.NotificationVibrateTitle);
							notificationAlbumButton.setEnabled(false);
							notificationAlbumImage.setEnabled(false);
							notificationAlbumText
									.setTextColor(ResourcesSingleton.instance(getApplicationContext())
											.getColor(R.color.dark_gray));
							notificationAlbumText.setEnabled(false);
							soundSetting = 2;
							break;
						}
						notificationSoundImage.setImageLevel(soundSetting);
						SharedPreferencesSingleton.putInt(
								R.string.notificationsound_key, soundSetting);
                        tracker.send(new HitBuilders.EventBuilder()
                                .setCategory(Constants.CATEGORY_MENU)
                                .setAction(Constants.ACTION_BUTTON_PRESS)
                                .setLabel("splashNotificationSound")
                                .setValue((long) soundSetting)
                                .build());
					}
				}
			});
			int typeSetting = SharedPreferencesSingleton.instance(getApplicationContext()).getInt(
					ResourcesSingleton.instance(getApplicationContext()).getString(
							R.string.notificationtype_key), 1);
			notificationAlbumImage.setImageLevel(typeSetting);
			switch (typeSetting) {
			case 0:
				notificationAlbumText
						.setText(R.string.NotificationTypeStandardTitle);
				break;
			case 1:
				notificationAlbumText
						.setText(R.string.NotificationTypeAlbumTitle);
				break;
			/*
			 * case 2:
			 * notificationAlbumText.setText(R.string.NotificationTypeSongTitle
			 * ); break;
			 */
			}
			notificationAlbumButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					if (getSlidingMenu().isMenuShowing()) {
						int typeSetting = SharedPreferencesSingleton.instance(getApplicationContext())
								.getInt(ResourcesSingleton.instance(getApplicationContext()).getString(
												R.string.notificationtype_key), 1);
						switch (typeSetting) {
						case 0:
							notificationAlbumText
									.setText(
										R.string.NotificationTypeAlbumTitle);
							typeSetting = 1;
							break;
						case 1:
							notificationAlbumText
									.setText(
										R.string.NotificationTypeStandardTitle);
							typeSetting = 0;
							/*
							 * if (DatabaseHelperSingleton.instance()
							 * .getNotificatationsToDownload() != null)
							 * showDownloadDialog();
							 */
							break;
						/*
						 * case 2: notificationAlbumText.setText(R.string.
						 * NotificationTypeStandardTitle); typeSetting = 0;
						 * break;
						 */
						}
						notificationAlbumImage.setImageLevel(typeSetting);
						SharedPreferencesSingleton.putInt(
								R.string.notificationtype_key, typeSetting);
                        tracker.send(new HitBuilders.EventBuilder()
                                .setCategory(Constants.CATEGORY_MENU)
                                .setAction(Constants.ACTION_BUTTON_PRESS)
                                .setLabel("splashNotificationType")
                                .setValue((long) typeSetting)
                                .build());
					}
				}
			});
            RelativeLayout notificationsButton = (RelativeLayout) getSlidingMenu()
					.findViewById(R.id.NotificationsButton);
			notificationsText = (CheckedTextView) getSlidingMenu()
					.findViewById(R.id.NotificationsText);
			notificationsText.setChecked(SharedPreferencesSingleton.instance(getApplicationContext())
					.getBoolean(
							ResourcesSingleton.instance(getApplicationContext()).getString(
									R.string.notification_key), true));
			if (notificationSoundButton != null
					&& notificationAlbumButton != null) {
				if (!notificationsText.isChecked()) {
					notificationSoundButton.setEnabled(false);
					notificationSoundImage.setEnabled(false);
					notificationSoundText.setTextColor(ResourcesSingleton
							.instance(getApplicationContext()).getColor(R.color.dark_gray));
					notificationAlbumButton.setEnabled(false);
					notificationAlbumImage.setEnabled(false);
					notificationAlbumText.setTextColor(ResourcesSingleton
							.instance(getApplicationContext()).getColor(R.color.dark_gray));
					notificationAlbumText.setEnabled(false);
				} else {
					notificationSoundButton.setEnabled(true);
					notificationSoundImage.setEnabled(true);
					notificationSoundText.setTextColor(ResourcesSingleton
							.instance(getApplicationContext()).getColor(android.R.color.white));
					switch (SharedPreferencesSingleton.instance(getApplicationContext()).getInt(
							ResourcesSingleton.instance(getApplicationContext()).getString(
									R.string.notificationsound_key), 0)) {
					case 0:
					case 1:
						notificationAlbumButton.setEnabled(true);
						notificationAlbumImage.setEnabled(true);
						notificationAlbumText.setTextColor(ResourcesSingleton
								.instance(getApplicationContext()).getColor(android.R.color.white));
						notificationAlbumText.setEnabled(true);
						break;
					case 2:
					case 3:
						notificationAlbumButton.setEnabled(false);
						notificationAlbumImage.setEnabled(false);
						notificationAlbumText.setTextColor(ResourcesSingleton
								.instance(getApplicationContext()).getColor(R.color.dark_gray));
						notificationAlbumText.setEnabled(false);
						break;
					}
				}
			}
			notificationsButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					if (getSlidingMenu().isMenuShowing()) {
						notificationsText.toggle();
						if (notificationSoundButton != null
								&& notificationAlbumButton != null) {
							if (!notificationsText.isChecked()) {
								notificationSoundButton.setEnabled(false);
								notificationSoundImage.setEnabled(false);
								notificationSoundText
										.setTextColor(ResourcesSingleton
												.instance(getApplicationContext()).getColor(
														R.color.dark_gray));
								notificationAlbumButton.setEnabled(false);
								notificationAlbumImage.setEnabled(false);
								notificationAlbumText
										.setTextColor(ResourcesSingleton
												.instance(getApplicationContext()).getColor(
														R.color.dark_gray));
								notificationAlbumText.setEnabled(false);
							} else {
								notificationSoundButton.setEnabled(true);
								notificationSoundImage.setEnabled(true);
								notificationSoundText.setTextColor(ResourcesSingleton.instance(
                                        getApplicationContext()).getColor(android.R.color.white));
								switch (SharedPreferencesSingleton.instance(getApplicationContext())
										.getInt(ResourcesSingleton.instance(getApplicationContext())
												.getString(R.string.notificationsound_key), 0)) {
								case 0:
								case 1:
									notificationAlbumButton.setEnabled(true);
									notificationAlbumImage.setEnabled(true);
									notificationAlbumText
											.setTextColor(ResourcesSingleton
													.instance(getApplicationContext())
													.getColor(
														android.R.color.white));
									notificationAlbumText.setEnabled(true);
									break;
								case 2:
								case 3:
									notificationAlbumButton.setEnabled(false);
									notificationAlbumImage.setEnabled(false);
									notificationAlbumText
											.setTextColor(ResourcesSingleton
													.instance(getApplicationContext()).getColor(
															R.color.dark_gray));
									notificationAlbumText.setEnabled(false);
									break;
								}
							}
						}
						SharedPreferencesSingleton.putBoolean(
								R.string.notification_key,
								notificationsText.isChecked());
                        tracker.send(new HitBuilders.EventBuilder()
                                .setCategory(Constants.CATEGORY_MENU)
                                .setAction(Constants.ACTION_BUTTON_PRESS)
                                .setLabel("splashNotificationToggle")
                                .setValue(notificationsText.isChecked() ? 1L : 0L)
                                .build());
					}
				}
			});
		} else if (!savedInstance.isInStats()) {
			setBehindContentView(R.layout.menu_quiz);
			switch (ResourcesSingleton.instance(getApplicationContext()).getConfiguration().orientation) {
			case Configuration.ORIENTATION_LANDSCAPE:
				getSlidingMenu().setBehindOffsetRes(R.dimen.menu_width_land);
				break;
			default:
				getSlidingMenu().setBehindOffsetRes(R.dimen.menu_width_port);
				break;
			}
            /**
            RelativeLayout statsButton = (RelativeLayout) getSlidingMenu().findViewById(R.id.StatsButton);
			if (DatabaseHelperSingleton.instance(getApplicationContext()).isAnonUser(
					savedInstance.getUserId())) {
				statsButton.setVisibility(View.GONE);
			} else {
				statsButton.setVisibility(View.VISIBLE);
				statsButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
                        tracker.send(new HitBuilders.EventBuilder()
                                .setCategory(Constants.CATEGORY_MENU)
                                .setAction(Constants.ACTION_BUTTON_PRESS)
                                .setLabel("quizStats")
                                .setValue(1L)
                                .build());
						openStats();
					}
				});
			}
             */
			/*
			 * switchButton = (RelativeLayout)
			 * getSlidingMenu().findViewById(R.id.SwitchButton);
			 * switchButton.setOnClickListener(new OnClickListener() {
			 *
			 * @Override public void onClick(View arg0) {
			 * switchButton.setEnabled(false); if
			 * (getSlidingMenu().isMenuShowing()) {
			 * getSlidingMenu().setOnClosedListener(new OnClosedListener() {
			 *
			 * @Override public void onClosed() {
			 * getSlidingMenu().setOnClosedListener(null);
			 * setBackground(getQuizBackground(), true, "quiz"); } });
			 * getSlidingMenu().showContent(); } } });
			 * switchButton.setEnabled(true);
			 */
            /**
            RelativeLayout reportButton = (RelativeLayout) getSlidingMenu().findViewById(
					R.id.ReportButton);
			reportButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
                    tracker.send(new HitBuilders.EventBuilder()
                            .setCategory(Constants.CATEGORY_MENU)
                            .setAction(Constants.ACTION_BUTTON_PRESS)
                            .setLabel("quizReport")
                            .setValue(1L)
                            .build());
					if (getSlidingMenu().isMenuShowing()) {
						getSlidingMenu().setOnClosedListener(
								new OnClosedListener() {
									@Override
									public void onClosed() {
										getSlidingMenu().setOnClosedListener(
												null);
										Thread shareThread = new Thread() {
											public void run() {
												try {
													Thread.sleep(1000);
												} catch (InterruptedException e) {
                                                    // TODO
                                                }
												ApplicationEx.reportQuestion(getQuestionId(0),
														getQuestion(0), getQuestionAnswer(0),
														getQuestionScore(0));
											}
										};
										shareThread.start();
									}
								});
						getSlidingMenu().showContent();
					}
				}
			});
            RelativeLayout shareButton = (RelativeLayout) getSlidingMenu().findViewById(
					R.id.ShareButton);
			shareButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
                    tracker.send(new HitBuilders.EventBuilder()
                            .setCategory(Constants.CATEGORY_MENU)
                            .setAction(Constants.ACTION_BUTTON_PRESS)
                            .setLabel("quizShare")
                            .setValue(1L)
                            .build());
					if (getSlidingMenu().isMenuShowing()) {
						ApplicationEx.showShortToast("Capturing screen");
						getSlidingMenu().setOnClosedListener(
								new OnClosedListener() {
									@Override
									public void onClosed() {
										getSlidingMenu().setOnClosedListener(
												null);
										if (screenshotTask != null)
											screenshotTask.cancel(true);
										screenshotTask = new ScreenshotTask(false);
                                        screenshotTask.executeOnExecutor(
                                                AsyncTask.THREAD_POOL_EXECUTOR);
									}
								});
						getSlidingMenu().showContent();
					}
				}
			});
            RelativeLayout logoutButton = (RelativeLayout) getSlidingMenu().findViewById(
					R.id.LogoutButton);
            TextView logoutText = (TextView) findViewById(R.id.LogoutText);
            RelativeLayout nameButton = (RelativeLayout) getSlidingMenu().findViewById(
					R.id.NameButton);
			nameButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					if (getSlidingMenu().isMenuShowing())
						showNameDialog();
				}
			});
			if (getUserId() != null) {
				if (DatabaseHelperSingleton.instance(getApplicationContext()).hasUser(getUserId())
						&& !DatabaseHelperSingleton.instance(getApplicationContext()).isAnonUser(
								getUserId())) {
					if (getDisplayName() != null)
						logoutText.setText("Logout (" + getDisplayName() + ")");
					statsButton.setVisibility(View.VISIBLE);
					nameButton.setVisibility(View.VISIBLE);
				} else {
					statsButton.setVisibility(View.GONE);
					nameButton.setVisibility(View.GONE);
				}
			} else {
				statsButton.setVisibility(View.GONE);
				nameButton.setVisibility(View.GONE);
			}
			logoutButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
                    tracker.send(new HitBuilders.EventBuilder()
                            .setCategory(Constants.CATEGORY_MENU)
                            .setAction(Constants.ACTION_BUTTON_PRESS)
                            .setLabel("quizLogout")
                            .setValue(1L)
                            .build());
					if (getSlidingMenu().isMenuShowing()) {
						getSlidingMenu().setOnClosedListener(
								new OnClosedListener() {
									@Override
									public void onClosed() {
										getSlidingMenu().setOnClosedListener(null);
										if (logoutWaitTask != null) {
                                            logoutWaitTask.cancel(true);
                                        }
										logoutWaitTask = new LogoutWaitTask();
                                        logoutWaitTask.executeOnExecutor(
                                                AsyncTask.THREAD_POOL_EXECUTOR);
									}
								});
						getSlidingMenu().showContent();
					}
				}
			});
             */
            RelativeLayout exitButton = (RelativeLayout) getSlidingMenu().findViewById(
					R.id.ExitButton);
			exitButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
                    tracker.send(new HitBuilders.EventBuilder()
                            .setCategory(Constants.CATEGORY_MENU)
                            .setAction(Constants.ACTION_BUTTON_PRESS)
                            .setLabel("quizExit")
                            .setValue(1L)
                            .build());
					if (getSlidingMenu().isMenuShowing()) {
						moveTaskToBack(true);
					}
				}
			});
            /**
			levelButton = (RelativeLayout) getSlidingMenu().findViewById(R.id.LevelButton);
			levelImage = (ImageViewEx) getSlidingMenu().findViewById(R.id.LevelImage);
			levelText = (TextView) getSlidingMenu().findViewById(R.id.LevelText);
			switch (SharedPreferencesSingleton.instance(getApplicationContext())
					.getInt(ResourcesSingleton.instance(getApplicationContext()).getString(
							R.string.level_key), Constants.HARD)) {
			case Constants.EASY:
				levelText.setText(ResourcesSingleton.instance(getApplicationContext()).getString(
						R.string.LevelTitle) + " (Easy)");
				levelImage.setImageResource(R.drawable.ic_level_easy_inverse);
				break;
			case Constants.MEDIUM:
				levelText.setText(ResourcesSingleton.instance(getApplicationContext()).getString(
						R.string.LevelTitle) + " (Medium)");
				levelImage.setImageResource(R.drawable.ic_level_med_inverse);
				break;
			case Constants.HARD:
				levelText.setText(ResourcesSingleton.instance(getApplicationContext()).getString(
						R.string.LevelTitle) + " (Hard)");
				levelImage.setImageResource(R.drawable.ic_level_hard_inverse);
				break;
			}
			levelButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					if (getSlidingMenu().isMenuShowing()) {
						switch (SharedPreferencesSingleton.instance(getApplicationContext()).getInt(
								ResourcesSingleton.instance(getApplicationContext()).getString(
										R.string.level_key), Constants.HARD)) {
						case Constants.EASY:
							SharedPreferencesSingleton.putInt(
									R.string.level_key, Constants.MEDIUM);
                            levelText.setText(String.format(ResourcesSingleton.instance(getApplicationContext())
                                    .getString(R.string.LevelTitle), "(Medium)"));
							levelImage.setImageResource(R.drawable.ic_level_med_inverse);
							break;
						case Constants.MEDIUM:
							SharedPreferencesSingleton.putInt(R.string.level_key, Constants.HARD);
                            levelText.setText(String.format(ResourcesSingleton.instance(getApplicationContext())
                                    .getString(R.string.LevelTitle), "(Hard)"));
							levelImage.setImageResource(R.drawable.ic_level_hard_inverse);
							break;
						case Constants.HARD:
							SharedPreferencesSingleton.putInt(R.string.level_key, Constants.EASY);
                            levelText.setText(String.format(ResourcesSingleton.instance(getApplicationContext())
                                    .getString(R.string.LevelTitle), "(Easy)"));
							levelImage.setImageResource(R.drawable.ic_level_easy_inverse);
							break;
						}
						if (getQuestion(0) == null) {
							getNextQuestions(false, SharedPreferencesSingleton.instance(
                                    getApplicationContext()).getInt(ResourcesSingleton.instance(
                                    getApplicationContext()).getString(R.string.level_key),
                                    Constants.HARD));
							if (currFrag != null) {
								currFrag.showRetry();
							}
						}
					}
				}
			});
             */
            /**
            RelativeLayout soundsButton = (RelativeLayout) getSlidingMenu().findViewById(
					R.id.SoundsButton);
			soundsText = (CheckedTextView) getSlidingMenu().findViewById(
					R.id.SoundsText);
			soundsText.setChecked(SharedPreferencesSingleton.instance(getApplicationContext())
					.getBoolean(
							ResourcesSingleton.instance(getApplicationContext()).getString(
									R.string.sound_key), true));
			soundsButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					if (getSlidingMenu().isMenuShowing()) {
						soundsText.toggle();
						SharedPreferencesSingleton.toggleBoolean(
								R.string.sound_key, true);
                        tracker.send(new HitBuilders.EventBuilder()
                                .setCategory(Constants.CATEGORY_MENU)
                                .setAction(Constants.ACTION_BUTTON_PRESS)
                                .setLabel("quizSounds")
                                .setValue(soundsText.isChecked() ? 1L : 0L)
                                .build());
					}
				}
			});
             */
			notificationSoundButton = (RelativeLayout) getSlidingMenu()
					.findViewById(R.id.NotificationSoundsButton);
			notificationSoundImage = (ImageViewEx) getSlidingMenu()
					.findViewById(R.id.NotificationSoundsImage);
			notificationSoundText = (TextView) getSlidingMenu().findViewById(
					R.id.NotificationSoundsText);
			int soundSetting = SharedPreferencesSingleton.instance(getApplicationContext()).getInt(
					ResourcesSingleton.instance(getApplicationContext()).getString(
							R.string.notificationsound_key), 0);
			notificationSoundImage.setImageLevel(soundSetting);
			notificationAlbumButton = (RelativeLayout) getSlidingMenu()
					.findViewById(R.id.NotificationTypeButton);
			notificationAlbumImage = (ImageViewEx) getSlidingMenu()
					.findViewById(R.id.NotificationTypeImage);
			notificationAlbumText = (TextView) getSlidingMenu().findViewById(
					R.id.NotificationTypeText);
			switch (soundSetting) {
			case 0:
				notificationSoundText.setText(R.string.NotificationBothTitle);
				notificationAlbumButton.setEnabled(true);
				notificationAlbumImage.setEnabled(true);
				notificationAlbumText.setTextColor(ResourcesSingleton
						.instance(getApplicationContext()).getColor(android.R.color.white));
				notificationAlbumText.setEnabled(true);
				break;
			case 1:
				notificationSoundText.setText(R.string.NotificationSoundsTitle);
				notificationAlbumButton.setEnabled(true);
				notificationAlbumImage.setEnabled(true);
				notificationAlbumText.setTextColor(ResourcesSingleton
						.instance(getApplicationContext()).getColor(android.R.color.white));
				notificationAlbumText.setEnabled(true);
				break;
			case 2:
				notificationSoundText
						.setText(R.string.NotificationVibrateTitle);
				notificationAlbumButton.setEnabled(false);
				notificationAlbumImage.setEnabled(false);
				notificationAlbumText.setTextColor(ResourcesSingleton
						.instance(getApplicationContext()).getColor(R.color.dark_gray));
				notificationAlbumText.setEnabled(false);
				break;
			case 3:
				notificationSoundText.setText(R.string.NotificationSilentTitle);
				notificationAlbumButton.setEnabled(false);
				notificationAlbumImage.setEnabled(false);
				notificationAlbumText.setTextColor(ResourcesSingleton
						.instance(getApplicationContext()).getColor(R.color.dark_gray));
				notificationAlbumText.setEnabled(false);
				break;
			}
			notificationSoundButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					if (getSlidingMenu().isMenuShowing()) {
						int soundSetting = SharedPreferencesSingleton
								.instance(getApplicationContext())
								.getInt(ResourcesSingleton.instance(getApplicationContext())
										.getString(
												R.string.notificationsound_key),
										0);
						switch (soundSetting) {
						case 0:
							notificationSoundText
									.setText(R.string.NotificationSilentTitle);
							notificationAlbumButton.setEnabled(false);
							notificationAlbumImage.setEnabled(false);
							notificationAlbumText
									.setTextColor(ResourcesSingleton.instance(getApplicationContext())
											.getColor(R.color.dark_gray));
							notificationAlbumText.setEnabled(false);
							soundSetting = 3;
							break;
						case 1:
							notificationSoundText
									.setText(R.string.NotificationBothTitle);
							notificationAlbumButton.setEnabled(true);
							notificationAlbumImage.setEnabled(true);
							notificationAlbumText
									.setTextColor(ResourcesSingleton.instance(getApplicationContext())
											.getColor(android.R.color.white));
							notificationAlbumText.setEnabled(true);
							soundSetting = 0;
							break;
						case 2:
							notificationSoundText
									.setText(R.string.NotificationSoundsTitle);
							notificationAlbumButton.setEnabled(true);
							notificationAlbumImage.setEnabled(true);
							notificationAlbumText
									.setTextColor(ResourcesSingleton.instance(getApplicationContext())
											.getColor(android.R.color.white));
							notificationAlbumText.setEnabled(true);
							soundSetting = 1;
							break;
						case 3:
							notificationSoundText
									.setText(R.string.NotificationVibrateTitle);
							notificationAlbumButton.setEnabled(false);
							notificationAlbumImage.setEnabled(false);
							notificationAlbumText
									.setTextColor(ResourcesSingleton.instance(getApplicationContext())
											.getColor(R.color.dark_gray));
							notificationAlbumText.setEnabled(false);
							soundSetting = 2;
							break;
						}
						notificationSoundImage.setImageLevel(soundSetting);
						SharedPreferencesSingleton.putInt(
								R.string.notificationsound_key, soundSetting);
                        tracker.send(new HitBuilders.EventBuilder()
                                .setCategory(Constants.CATEGORY_MENU)
                                .setAction(Constants.ACTION_BUTTON_PRESS)
                                .setLabel("quizNotificationSound")
                                .setValue((long) soundSetting)
                                .build());
					}
				}
			});
			int typeSetting = SharedPreferencesSingleton.instance(getApplicationContext()).getInt(
					ResourcesSingleton.instance(getApplicationContext()).getString(
							R.string.notificationtype_key), 1);
			notificationAlbumImage.setImageLevel(typeSetting);
			switch (typeSetting) {
			case 0:
				notificationAlbumText
						.setText(R.string.NotificationTypeStandardTitle);
				break;
			case 1:
				notificationAlbumText
						.setText(R.string.NotificationTypeAlbumTitle);
				break;
			/*
			 * case 2:
			 * notificationAlbumText.setText(R.string.NotificationTypeSongTitle
			 * ); break;
			 */
			}
			notificationAlbumButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					if (getSlidingMenu().isMenuShowing()) {
						int typeSetting = SharedPreferencesSingleton.instance(getApplicationContext())
								.getInt(ResourcesSingleton.instance(getApplicationContext())
										.getString(R.string.notificationtype_key), 1);
						switch (typeSetting) {
						case 0:
							notificationAlbumText
									.setText(
										R.string.NotificationTypeAlbumTitle);
							typeSetting = 1;
							break;
						case 1:
							notificationAlbumText
									.setText(
										R.string.NotificationTypeStandardTitle);
							typeSetting = 0;
							/*
							 * if (DatabaseHelperSingleton.instance()
							 * .getNotificatationsToDownload() != null)
							 * showDownloadDialog();
							 */
							break;
						/*
						 * case 2: notificationAlbumText.setText(R.string.
						 * NotificationTypeStandardTitle); typeSetting = 0;
						 * break;
						 */
						}
						notificationAlbumImage.setImageLevel(typeSetting);
						SharedPreferencesSingleton.putInt(
								R.string.notificationtype_key, typeSetting);
                        tracker.send(new HitBuilders.EventBuilder()
                                .setCategory(Constants.CATEGORY_MENU)
                                .setAction(Constants.ACTION_BUTTON_PRESS)
                                .setLabel("quizNotificationType")
                                .setValue((long) typeSetting)
                                .build());
					}
				}
			});
            RelativeLayout notificationsButton = (RelativeLayout) getSlidingMenu()
					.findViewById(R.id.NotificationsButton);
			notificationsText = (CheckedTextView) getSlidingMenu()
					.findViewById(R.id.NotificationsText);
			notificationsText.setChecked(SharedPreferencesSingleton.instance(getApplicationContext())
					.getBoolean(ResourcesSingleton.instance(getApplicationContext()).getString(
									R.string.notification_key), true));
			if (notificationSoundButton != null
					&& notificationAlbumButton != null) {
				if (!notificationsText.isChecked()) {
					notificationSoundButton.setEnabled(false);
					notificationSoundImage.setEnabled(false);
					notificationSoundText.setTextColor(ResourcesSingleton
							.instance(getApplicationContext()).getColor(R.color.dark_gray));
					notificationAlbumButton.setEnabled(false);
					notificationAlbumImage.setEnabled(false);
					notificationAlbumText.setTextColor(ResourcesSingleton
							.instance(getApplicationContext()).getColor(R.color.dark_gray));
					notificationAlbumText.setEnabled(false);
				} else {
					notificationSoundButton.setEnabled(true);
					notificationSoundImage.setEnabled(true);
					notificationSoundText.setTextColor(ResourcesSingleton
							.instance(getApplicationContext()).getColor(android.R.color.white));
					switch (SharedPreferencesSingleton.instance(getApplicationContext()).getInt(
							ResourcesSingleton.instance(getApplicationContext()).getString(
									R.string.notificationsound_key), 0)) {
					case 0:
					case 1:
						notificationAlbumButton.setEnabled(true);
						notificationAlbumImage.setEnabled(true);
						notificationAlbumText.setTextColor(ResourcesSingleton
								.instance(getApplicationContext()).getColor(android.R.color.white));
						notificationAlbumText.setEnabled(true);
						break;
					case 2:
					case 3:
						notificationAlbumButton.setEnabled(false);
						notificationAlbumImage.setEnabled(false);
						notificationAlbumText.setTextColor(ResourcesSingleton
								.instance(getApplicationContext()).getColor(R.color.dark_gray));
						notificationAlbumText.setEnabled(false);
						break;
					}
				}
			}
			notificationsButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					if (getSlidingMenu().isMenuShowing()) {
						notificationsText.toggle();
						if (notificationSoundButton != null
								&& notificationAlbumButton != null) {
							if (!notificationsText.isChecked()) {
								notificationSoundButton.setEnabled(false);
								notificationSoundImage.setEnabled(false);
								notificationSoundText
										.setTextColor(ResourcesSingleton
												.instance(getApplicationContext()).getColor(
														R.color.dark_gray));
								notificationAlbumButton.setEnabled(false);
								notificationAlbumImage.setEnabled(false);
								notificationAlbumText
										.setTextColor(ResourcesSingleton
												.instance(getApplicationContext()).getColor(
														R.color.dark_gray));
								notificationAlbumText.setEnabled(false);
							} else {
								notificationSoundButton.setEnabled(true);
								notificationSoundImage.setEnabled(true);
								notificationSoundText
										.setTextColor(ResourcesSingleton
												.instance(getApplicationContext()).getColor(
														android.R.color.white));
								switch (SharedPreferencesSingleton.instance(getApplicationContext())
										.getInt(ResourcesSingleton
												.instance(getApplicationContext()).getString(
												R.string.notificationsound_key), 0)) {
								case 0:
								case 1:
									notificationAlbumButton.setEnabled(true);
									notificationAlbumImage.setEnabled(true);
									notificationAlbumText
											.setTextColor(ResourcesSingleton
													.instance(getApplicationContext())
													.getColor(
														android.R.color.white));
									notificationAlbumText.setEnabled(true);
									break;
								case 2:
								case 3:
									notificationAlbumButton.setEnabled(false);
									notificationAlbumImage.setEnabled(false);
									notificationAlbumText
											.setTextColor(ResourcesSingleton
													.instance(getApplicationContext()).getColor(
															R.color.dark_gray));
									notificationAlbumText.setEnabled(false);
									break;
								}
							}
						}
						SharedPreferencesSingleton.putBoolean(
								R.string.notification_key,
								notificationsText.isChecked());
                        tracker.send(new HitBuilders.EventBuilder()
                                .setCategory(Constants.CATEGORY_MENU)
                                .setAction(Constants.ACTION_BUTTON_PRESS)
                                .setLabel("quizNotificationToggle")
                                .setValue(notificationsText.isChecked() ? 1L : 0L)
                                .build());
					}
				}
			});
		} else if (savedInstance.isInStats()) {
			setBehindContentView(R.layout.menu_leaders);
			switch (ResourcesSingleton.instance(getApplicationContext()).getConfiguration()
					.orientation) {
			case Configuration.ORIENTATION_LANDSCAPE:
				getSlidingMenu().setBehindOffsetRes(R.dimen.menu_width_land);
				break;
			default:
				getSlidingMenu().setBehindOffsetRes(R.dimen.menu_width_port);
				break;
			}
            /**
            RelativeLayout shareButton = (RelativeLayout) getSlidingMenu().findViewById(
					R.id.ShareButton);
			shareButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
                    tracker.send(new HitBuilders.EventBuilder()
                            .setCategory(Constants.CATEGORY_MENU)
                            .setAction(Constants.ACTION_BUTTON_PRESS)
                            .setLabel("statsShare")
                            .setValue(1L)
                            .build());
					if (getSlidingMenu().isMenuShowing()) {
						ApplicationEx.showShortToast("Capturing screen");
						getSlidingMenu().setOnClosedListener(
								new OnClosedListener() {
									@Override
									public void onClosed() {
										getSlidingMenu().setOnClosedListener(
												null);
										if (screenshotTask != null)
											screenshotTask.cancel(true);
										screenshotTask = new ScreenshotTask(false);
                                        screenshotTask.executeOnExecutor(
                                                AsyncTask.THREAD_POOL_EXECUTOR);
									}
								});
						getSlidingMenu().showContent();
					}
				}
			});
            RelativeLayout nameButton = (RelativeLayout) getSlidingMenu().findViewById(
					R.id.NameButton);
			nameButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
                    tracker.send(new HitBuilders.EventBuilder()
                            .setCategory(Constants.CATEGORY_MENU)
                            .setAction(Constants.ACTION_BUTTON_PRESS)
                            .setLabel("statsName")
                            .setValue(1L)
                            .build());
					if (getSlidingMenu().isMenuShowing())
						showNameDialog();
				}
			});
             */
            RelativeLayout infoButton = (RelativeLayout) getSlidingMenu().findViewById(
					R.id.InfoButton);
			infoButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
                    tracker.send(new HitBuilders.EventBuilder()
                            .setCategory(Constants.CATEGORY_MENU)
                            .setAction(Constants.ACTION_BUTTON_PRESS)
                            .setLabel("statsInfo")
                            .setValue(1L)
                            .build());
					if (getSlidingMenu().isMenuShowing()) {
						getSlidingMenu().setOnClosedListener(
								new OnClosedListener() {
									@Override
									public void onClosed() {
										getSlidingMenu().setOnClosedListener(
												null);
										Thread infoThread = new Thread() {
											public void run() {
												try {
													Thread.sleep(500);
												} catch (InterruptedException e) {
                                                    // TODO
												}
												onFaqPressed();
											}
										};
										infoThread.start();
									}
								});
						getSlidingMenu().showContent();
					}
				}
			});
            RelativeLayout exitButton = (RelativeLayout) getSlidingMenu().findViewById(
					R.id.ExitButton);
			exitButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
                    tracker.send(new HitBuilders.EventBuilder()
                            .setCategory(Constants.CATEGORY_MENU)
                            .setAction(Constants.ACTION_BUTTON_PRESS)
                            .setLabel("statsExit")
                            .setValue(1L)
                            .build());
					if (getSlidingMenu().isMenuShowing()) {
						getSlidingMenu().setOnClosedListener(
								new OnClosedListener() {
									@Override
									public void onClosed() {
										getSlidingMenu().setOnClosedListener(
												null);
										savedInstance.setInStats(false);
										DatabaseHelperSingleton
												.instance(getApplicationContext())
												.setUserValue(
													savedInstance.isInStats() ?
															1 : 0,
													DatabaseHelper.COL_IN_STATS,
													savedInstance.getUserId());
										showQuiz();
									}
								});
						getSlidingMenu().showContent();
					}
				}
			});
			/*
			 * switchButton = (RelativeLayout)
			 * getSlidingMenu().findViewById(R.id.SwitchButton);
			 * switchButton.setOnClickListener(new OnClickListener() {
			 *
			 * @Override public void onClick(View arg0) {
			 * switchButton.setEnabled(false); if
			 * (getSlidingMenu().isMenuShowing()) {
			 * getSlidingMenu().setOnClosedListener(new OnClosedListener() {
			 *
			 * @Override public void onClosed() {
			 * getSlidingMenu().setOnClosedListener(null);
			 * setBackground(getLeadersBackground(), true, "leaders"); } });
			 * getSlidingMenu().showContent(); } } });
			 * switchButton.setEnabled(true);
			 */
		}
		/*
		 * tipsButton = (RelativeLayout)
		 * getSlidingMenu().findViewById(R.id.QuickTipsButton); tipsText =
		 * (CheckedTextView) getSlidingMenu().findViewById(R.id.QuickTipsText);
		 * tipsText.setChecked(SharedPreferencesSingleton.instance().getBoolean(
		 * res.getString(R.string.quicktip_key), false));
		 * tipsButton.setOnClickListener(new OnClickListener() {
		 *
		 * @Override public void onClick(View arg0) { if
		 * (getSlidingMenu().isMenuShowing()) { tipsText.toggle();
		 * SharedPreferencesSingleton.toggleBoolean( R.string.quicktip_key,
		 * true); } } });
		 */
        RelativeLayout followButton = (RelativeLayout) getSlidingMenu().findViewById(
				R.id.FollowButton);
		followButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (!savedInstance.isLoggedIn())
                tracker.send(new HitBuilders.EventBuilder()
                        .setCategory(Constants.CATEGORY_MENU)
                        .setAction(Constants.ACTION_BUTTON_PRESS)
                        .setLabel("splashFollow")
                        .setValue(1L)
                        .build());
				else if (!savedInstance.isInStats())
                tracker.send(new HitBuilders.EventBuilder()
                        .setCategory(Constants.CATEGORY_MENU)
                        .setAction(Constants.ACTION_BUTTON_PRESS)
                        .setLabel("quizFollow")
                        .setValue(1L)
                        .build());
				else if (savedInstance.isInStats())
                    tracker.send(new HitBuilders.EventBuilder()
                            .setCategory(Constants.CATEGORY_MENU)
                            .setAction(Constants.ACTION_BUTTON_PRESS)
                            .setLabel("statsFollow")
                            .setValue(1L)
                            .build());
				if (getSlidingMenu().isMenuShowing())
					startActivity(getOpenTwitterIntent());
			}
		});
        RelativeLayout likeButton = (RelativeLayout) getSlidingMenu().findViewById(
				R.id.LikeButton);
		likeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (!savedInstance.isLoggedIn())
                tracker.send(new HitBuilders.EventBuilder()
                        .setCategory(Constants.CATEGORY_MENU)
                        .setAction(Constants.ACTION_BUTTON_PRESS)
                        .setLabel("splashLike")
                        .setValue(1L)
                        .build());
				else if (!savedInstance.isInStats())
                    tracker.send(new HitBuilders.EventBuilder()
                            .setCategory(Constants.CATEGORY_MENU)
                            .setAction(Constants.ACTION_BUTTON_PRESS)
                            .setLabel("quizLike")
                            .setValue(1L)
                            .build());
				else if (savedInstance.isInStats())
                    tracker.send(new HitBuilders.EventBuilder()
                            .setCategory(Constants.CATEGORY_MENU)
                            .setAction(Constants.ACTION_BUTTON_PRESS)
                            .setLabel("statsLike")
                            .setValue(1L)
                            .build());
				if (getSlidingMenu().isMenuShowing())
					startActivity(getOpenFacebookIntent());
			}
		});
		/*
		 * quickTipView = (ViewGroup) getLayoutInflater().inflate(
		 * R.layout.quicktip_menu, (ViewGroup)
		 * findViewById(R.id.ToolTipLayout)); quickTipMenuView = (ViewGroup)
		 * getLayoutInflater().inflate( R.layout.quicktip_menu, (ViewGroup)
		 * findViewById(R.id.ToolTipLayout)); quickTipLeftView = (ViewGroup)
		 * getLayoutInflater().inflate( R.layout.quicktip_left, (ViewGroup)
		 * findViewById(R.id.ToolTipLayout));
		 */
		switchButton = (RelativeLayout) getSlidingMenu().findViewById(
				R.id.SwitchButton);
		switchButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (!savedInstance.isLoggedIn())
                tracker.send(new HitBuilders.EventBuilder()
                        .setCategory(Constants.CATEGORY_MENU)
                        .setAction(Constants.ACTION_BUTTON_PRESS)
                        .setLabel("splashSwitch")
                        .setValue(1L)
                        .build());
				else if (!savedInstance.isInStats())
                tracker.send(new HitBuilders.EventBuilder()
                        .setCategory(Constants.CATEGORY_MENU)
                        .setAction(Constants.ACTION_BUTTON_PRESS)
                        .setLabel("quizSwitch")
                        .setValue(1L)
                        .build());
				else if (savedInstance.isInStats())
                tracker.send(new HitBuilders.EventBuilder()
                        .setCategory(Constants.CATEGORY_MENU)
                        .setAction(Constants.ACTION_BUTTON_PRESS)
                        .setLabel("statsSwitch")
                        .setValue(1L)
                        .build());
				switchButton.setEnabled(false);
				if (getSlidingMenu().isMenuShowing()) {
					getSlidingMenu().setOnClosedListener(
							new OnClosedListener() {
								@Override
								public void onClosed() {
									getSlidingMenu().setOnClosedListener(null);
									setBackground(getBackground(), true,
											"load");
								}
							});
					getSlidingMenu().showContent();
				}
			}
		});
		switchButton.setEnabled(true);
        RelativeLayout versionLayout = (RelativeLayout) getSlidingMenu().findViewById(
				R.id.VersionLayout);
        TextView versionText = (TextView) getSlidingMenu().findViewById(
				R.id.VersionNumber);
		try {
			versionText.setText(getPackageManager().getPackageInfo(
					getPackageName(), 0).versionName);
			versionLayout.setVisibility(View.VISIBLE);
		} catch (Exception e) {
			versionLayout.setVisibility(View.GONE);
		}
		if (savedInstance.isInChooser() || savedInstance.isInSetlist()) {
			getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
		} else {
			getSlidingMenu()
					.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		}
	}

	private class ParseTask extends AsyncTask<Void, Void, Void> {
		ExpandableListView setlistListView;
		ProgressBar setlistProgress;

		private ParseTask(ExpandableListView setlistListView,
				ProgressBar setlistProgress) {
			this.setlistListView = setlistListView;
			this.setlistProgress = setlistProgress;
		}

		protected void onProgressUpdate(Void... nothing) {
			setlistListView.setVisibility(View.INVISIBLE);
			setlistProgress.setVisibility(View.VISIBLE);
		}

		@Override
		protected Void doInBackground(Void... nothing) {
			if (savedInstance.getSetlistMap() == null) {
				savedInstance.setSetlistMap(
                        new TreeMap<String, TreeMap<String, String>>(new YearComparator()));
			}
			if (savedInstance.getSetlistMap().isEmpty()) {
				publishProgress();
			}
			// Get a count of how many setlists there are
            BackendlessDataQuery countQuery = new BackendlessDataQuery();
            QueryOptions queryOptions = new QueryOptions();
            queryOptions.setPageSize(1);
            countQuery.setQueryOptions(queryOptions);
            BackendlessCollection<Setlist> setlists =
                    Backendless.Persistence.of(Setlist.class).find(countQuery);
            int setlistCount = setlists.getTotalObjects();
            int setlistMapCount = 0;
            for (Entry<String, TreeMap<String, String>> entry :
                    savedInstance.getSetlistMap().entrySet()) {
                setlistMapCount += entry.getValue().size();
            }
            if (setlistCount == setlistMapCount) {
                // If same number, get the latest and compare
                BackendlessDataQuery latestQuery = new BackendlessDataQuery();
                queryOptions = new QueryOptions();
                queryOptions.setPageSize(1);
                queryOptions.addSortByOption("setDate DESC");
                latestQuery.setQueryOptions(queryOptions);
                setlists = Backendless.Persistence.of(Setlist.class).find();
                getLatestSetInfo().setSetlist(setlists.getCurrentPage().get(0).getSet());
                return null;
            }
            // Otherwise get them all
            BackendlessDataQuery setlistsQuery = new BackendlessDataQuery();
            queryOptions = new QueryOptions();
            queryOptions.addSortByOption("setDate DESC");
            queryOptions.addRelated("venue");
            setlistsQuery.setQueryOptions(queryOptions);
            setlists = Backendless.Persistence.of(Setlist.class).find(setlistsQuery);
            ArrayList<Setlist> setlistList = new ArrayList<>(0);
            while (setlists.getCurrentPage().size() > 0) {
                setlistList.addAll(setlists.getCurrentPage());
                setlists = setlists.nextPage();
            }
            publishProgress();
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            String setDate;
            String year;
            Venue venue;
            StringBuilder sb;
            String setlist;
            TreeMap<String, String> tempMap;
            savedInstance.setSetlistMap(
                    new TreeMap<String, TreeMap<String, String>>(new YearComparator()));
            for (Setlist set : setlistList) {
                setDate = (String) DateFormat.format(dateFormat.toLocalizedPattern(),
                        convertToUtc(set.getSetDate()));
                year = setDate.substring(0, 4);
                setlist = set.getSet();
                venue = set.getVenue();
                sb = new StringBuilder();
                sb.append(setDate);
                sb.append(" - ");
                if (venue != null) {
                    sb.append(venue.getName());
                    sb.append(" - ");
                    sb.append(venue.getCity());
                }
                if (savedInstance.getSetlistMap().containsKey(year)) {
                    tempMap = savedInstance.getSetlistMap().get(year);
                    if (!tempMap.containsKey(sb.toString())) {
                        tempMap.put(sb.toString(), setlist);
                    }
                    savedInstance.getSetlistMap().put(year, tempMap);
                } else {
                    tempMap = new TreeMap<>(new SetlistComparator());
                    tempMap.put(sb.toString(), setlist);
                    savedInstance.getSetlistMap().put(year, tempMap);
                }
            }
            lookForSavedSetInfo();
            return null;
		}

		@Override
		protected void onCancelled(Void nothing) {
		}

		@Override
		protected void onPostExecute(Void nothing) {
			showSetlistChooser(setlistListView, setlistProgress);
		}
	}

	private void lookForSavedSetInfo() {
		SetInfo selectedSetInfo = savedInstance.getSelectedSet();
		if (selectedSetInfo == null) {
			selectedSetInfo = new SetInfo();
		}
		if (selectedSetInfo.getSetDate() == null) {
			selectedSetInfo.setSetDate(SharedPreferencesSingleton.instance(getApplicationContext())
				.getString(ResourcesSingleton.instance(getApplicationContext()).getString(R.string.set_date_key), ""));
		}
		if (selectedSetInfo.getSetVenue() == null) {
			selectedSetInfo.setSetVenue(SharedPreferencesSingleton.instance(getApplicationContext())
					.getString(ResourcesSingleton.instance(getApplicationContext()).getString(R.string.setvenue_key),
                            ""));
		}
		if (selectedSetInfo.getSetCity() == null) {
			selectedSetInfo.setSetCity(SharedPreferencesSingleton.instance(getApplicationContext())
					.getString(ResourcesSingleton.instance(getApplicationContext()).getString(R.string.setcity_key),
                            ""));
		}
		if (selectedSetInfo.getSetlist() == null) {
			selectedSetInfo.setSetlist(SharedPreferencesSingleton.instance(getApplicationContext())
					.getString(ResourcesSingleton.instance(getApplicationContext()).getString(R.string.setlist_key),
                            ""));
		}
		if (!selectedSetInfo.getSetDate().isEmpty() && selectedSetInfo.getSetDate().length() >= 4) {
			TreeMap<String, String> tempMap = savedInstance.getSetlistMap().get(
                    selectedSetInfo.getSetDate().substring(0, 4));
			if (tempMap != null) {
				boolean found = false;
				for (Entry<String, String> temp : tempMap.entrySet()) {
					found = temp.getKey().contains(selectedSetInfo.getSetDate());
				}
				if (!found) {
					selectedSetInfo.setKey(createSetlistKey(selectedSetInfo));
					tempMap.put(selectedSetInfo.getKey(), selectedSetInfo.getSetlist());
				}
			}
		}
		if (selectedSetInfo.getKey() == null) {
			selectedSetInfo.setKey(savedInstance.getSetlistMap().firstEntry().getValue().firstKey());
		}
		Log.i(Constants.LOG_TAG, "lookForSavedSetInfo: " + selectedSetInfo);
		savedInstance.setSelectedSet(selectedSetInfo);
	}

	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd",
			Locale.getDefault());

	private Date convertToUtc(Date date) {
		Calendar mbCal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		mbCal.setTimeInMillis(date.getTime());
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, mbCal.get(Calendar.YEAR));
		cal.set(Calendar.MONTH, mbCal.get(Calendar.MONTH));
		cal.set(Calendar.DAY_OF_MONTH, mbCal.get(Calendar.DAY_OF_MONTH));
		cal.set(Calendar.HOUR_OF_DAY, mbCal.get(Calendar.HOUR_OF_DAY));
		cal.set(Calendar.MINUTE, mbCal.get(Calendar.MINUTE));
		cal.set(Calendar.SECOND, mbCal.get(Calendar.SECOND));
		cal.set(Calendar.MILLISECOND, mbCal.get(Calendar.MILLISECOND));
		return cal.getTime();
	}

	@SuppressLint("NewApi")
	protected void openStats() {
		if (getSlidingMenu().isMenuShowing()) {
			getSlidingMenu().setOnClosedListener(new OnClosedListener() {
				@Override
				public void onClosed() {
					getSlidingMenu().setOnClosedListener(null);
					onStatsPressed();
				}
			});
			getSlidingMenu().showContent();
			if (!SharedPreferencesSingleton.instance(getApplicationContext())
					.contains(
							ResourcesSingleton.instance(getApplicationContext()).getString(
									R.string.stats_key))
					|| SharedPreferencesSingleton.instance(getApplicationContext()).getBoolean(
							ResourcesSingleton.instance(getApplicationContext()).getString(
									R.string.quicktip_key), false)) {
				SharedPreferencesSingleton.putBoolean(R.string.stats_key, true);
				/*
				 * showQuickTipMenu(quickTipMenuView,
				 * "Touch your score to enter Stats & Standings",
				 * Constants.QUICK_TIP_TOP);
				 */
			}
		}
	}

	private Intent getOpenFacebookIntent() {
        Intent fbIntent = new Intent(Intent.ACTION_VIEW);
		try {
            int versionCode = getApplicationContext().getPackageManager().getPackageInfo("com.facebook.katana", 0)
                    .versionCode;
            if (versionCode >= 3002850) {
                fbIntent.setData(Uri.parse("fb://facewebmodal/f?href=https://www.facebook.com/DMBTrivia"));
            } else {
                fbIntent.setData(Uri.parse("fb://page/DMBTrivia"));
            }

		} catch (Exception e) {
            fbIntent.setData(Uri.parse("https://www.facebook.com/DMBTrivia"));
		}
        return fbIntent;
	}

	private Intent getOpenTwitterIntent() {
		Uri uri = Uri.parse("http://www.twitter.com/dmbtrivia");
		return new Intent(Intent.ACTION_VIEW, uri);
	}

	/*
	 * private class SetlistBackgroundWaitTask extends AsyncTask<Void, Void,
	 * Void> { private String name; private ImageViewEx background;
	 *
	 * private SetlistBackgroundWaitTask(String name, ImageViewEx background) {
	 * this.name = name; this.background = background; }
	 *
	 * @Override protected Void doInBackground(Void... nothing) { if
	 * (isCancelled()) return null; try { Thread.sleep(1000); } catch
	 * (InterruptedException e) {} return null; }
	 *
	 * @Override protected void onCancelled(Void nothing) { }
	 *
	 * @Override protected void onPostExecute(Void nothing) { if
	 * (!isCancelled()) setlistBackground(name, background); } }
	 */

	/*
	 * @Override public String getSplashBackground() { return splashBackground;
	 * }
	 *
	 * @Override public String getQuizBackground() { return quizBackground; }
	 *
	 * @Override public String getLeadersBackground() { return
	 * leadersBackground; }
	 *
	 * @Override public void loadSetlist() { showSetlist(true); }
	 *
	 * private void showSetlist(boolean animate) { try { FragmentSetlist
	 * fSetlist = new FragmentSetlist(); FragmentTransaction ft =
	 * fMan.beginTransaction(); if (animate)
	 * ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
	 * ft.replace(android.R.id.content, fSetlist, "fSetlist")
	 * .commitAllowingStateLoss(); fMan.executePendingTransactions(); currFrag =
	 * fSetlist; setBackground(currentBackground, false); inSetlist = true;
	 * DatabaseHelperSingleton.instance().setUserValue(inSetlist ? 1 : 0,
	 * DatabaseHelper.COL_IN_SETLIST, userId); invalidateOptionsMenu(); } catch
	 * (IllegalStateException e) {} }
	 */
	/**
	 * Broadcast receiver classes
	 */
	private class ConnectionReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction()
					.equalsIgnoreCase(Constants.ACTION_CONNECTION)) {
				if (intent.hasExtra("hasConnection")) {
					if (!intent.getBooleanExtra("hasConnection", false)) {
                        currFrag.showNetworkProblem();
                    } else {
                        ApplicationEx.getSetlist(getApplicationContext());
                    }
				}
			}
		}
	}

	/**
	 * OnButtonListener implemented methods and associated methods/classes
	 */
	@SuppressLint("NewApi")
	@Override
	public boolean setBackground(final String name, final boolean showNew,
			final String screen) {
		if (!showNew) {
			if (name == null) {
				return false;
			}
			if (setBackgroundTask != null) {
				setBackgroundTask.cancel(true);
			}
			int resourceId = ResourcesSingleton.instance(getApplicationContext()).getIdentifier(name,
					"drawable", getPackageName());
			try {
				if (name.equals("setlist")) {
					ApplicationEx.setSetlistBitmap(getApplicationContext(), getBitmap(resourceId));
					if (currFrag != null) {
						currFrag.setBackground(getApplicationContext(),
								ApplicationEx.getSetlistBitmap(getApplicationContext()));
					}
				} else if (!(currFrag instanceof FragmentSetlist)) {
					if (fieldsList.indexOf(resourceId) >= 0) {
						ApplicationEx
								.setBackgroundBitmap(getApplicationContext(), getBitmap(resourceId));
					} else {
						ApplicationEx
								.setBackgroundBitmap(getApplicationContext(), getBitmap(
										R.drawable.splash4));
					}
					if (currFrag != null) {
						Log.w(Constants.LOG_TAG,
								"setting non setlist background on " + screen);
						currFrag.setBackground(getApplicationContext(), ApplicationEx
								.getBackgroundBitmap(getApplicationContext()));
					}
				} else {
					return false;
				}
			} catch (RuntimeException err) {
				Log.e(Constants.LOG_TAG, "Failed to set background!", err);
				if (setBackgroundWaitTask != null)
					setBackgroundWaitTask.cancel(true);
				setBackgroundWaitTask = new SetBackgroundWaitTask(name, false, screen);
                setBackgroundWaitTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			} catch (OutOfMemoryError memErr) {
				ApplicationEx.showShortToast("Error setting background");
				/*
				 * if (setBackgroundWaitTask != null)
				 * setBackgroundWaitTask.cancel(true); setBackgroundWaitTask =
				 * new SetBackgroundWaitTask(name, showNew, screen); if
				 * (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
				 * setBackgroundWaitTask.execute(); else
				 * setBackgroundWaitTask.executeOnExecutor
				 * (AsyncTask.THREAD_POOL_EXECUTOR);
				 */
			}
		} else {
			if (setBackgroundTask != null)
				setBackgroundTask.cancel(true);
			setBackgroundTask = new SetBackgroundTask(name, true, screen);
            setBackgroundTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
		return true;
	}

	private class SetBackgroundTask extends AsyncTask<Void, Void, Void> {
		private String name;
		private boolean showNew;
		private String screen;

		private int currentId;
		private int resourceId;

		private SetBackgroundTask(String name, boolean showNew, String screen) {
			this.name = name;
			this.showNew = showNew;
			this.screen = screen;
		}

		@Override
		protected Void doInBackground(Void... nothing) {
			if (isCancelled())
				return null;
			if (name == null)
				name = "splash4";
			resourceId = ResourcesSingleton.instance(getApplicationContext()).getIdentifier(name,
					"drawable", getPackageName());
			if (isCancelled())
				return null;
			if (showNew) {
				int rawIndex = fieldsList.indexOf(resourceId);
				if (rawIndex < 0)
					rawIndex = fieldsList.indexOf(R.drawable.splash4);
				rawIndex++;
				if (rawIndex >= fieldsList.size())
					rawIndex = 0;
				currentId = fieldsList.get(rawIndex);
				if (isCancelled())
					return null;
				switch (ResourcesSingleton.instance(getApplicationContext()).getConfiguration()
						.orientation) {
				case Configuration.ORIENTATION_PORTRAIT:
					savedInstance.setPortBackground(
							ResourcesSingleton.instance(getApplicationContext())
							.getResourceEntryName(currentId));
					DatabaseHelperSingleton.instance(getApplicationContext()).setPortBackground(
							savedInstance.getUserId(),
							savedInstance.getPortBackground());
					break;
				case Configuration.ORIENTATION_LANDSCAPE:
					savedInstance.setLandBackground(
							ResourcesSingleton.instance(getApplicationContext())
							.getResourceEntryName(currentId));
					DatabaseHelperSingleton.instance(getApplicationContext()).setLandBackground(
							savedInstance.getUserId(),
							savedInstance.getLandBackground());
					break;
				default:
					savedInstance.setPortBackground(
							ResourcesSingleton.instance(getApplicationContext())
							.getResourceEntryName(currentId));
					DatabaseHelperSingleton.instance(getApplicationContext()).setPortBackground(
							savedInstance.getUserId(),
							savedInstance.getPortBackground());
					break;
				}
				if (isCancelled())
					return null;
				/*
				 * Log.d(Constants.LOG_TAG, "SWITCH: " + currentBackground); if
				 * (screen.equals("splash")) {
				 * DatabaseHelperSingleton.instance()
				 * .setSplashBackground(userId, currentBackground);
				 * splashBackground = currentBackground; } else if
				 * (screen.equals("quiz")) {
				 * DatabaseHelperSingleton.instance().setQuizBackground(userId,
				 * currentBackground); quizBackground = currentBackground; }
				 * else if (screen.equals("leaders")) {
				 * DatabaseHelperSingleton.instance
				 * ().setLeadersBackground(userId, currentBackground);
				 * leadersBackground = currentBackground; }
				 */
				if (isCancelled())
					return null;
				if (currentId != resourceId) {
					try {
						ApplicationEx.setBackgroundBitmap(getApplicationContext(), getBitmap(currentId));
						if (isCancelled()) {
                            return null;
                        }
						/*
						 * if (ApplicationEx.getBackgroundDrawable() != null &&
						 * ApplicationEx.getBackgroundDrawable() instanceof
						 * TransitionDrawable) { oldBitmapDrawable =
						 * (BitmapDrawableEx)(
						 * ((TransitionDrawable)ApplicationEx
						 * .getBackgroundDrawable()) .getDrawable(1)); } else if
						 * (ApplicationEx.getBackgroundDrawable() != null)
						 * oldBitmapDrawable = new BitmapDrawableEx(res,
						 * ((BitmapDrawable
						 * )ApplicationEx.getBackgroundDrawable()).getBitmap());
						 * if (ApplicationEx.getBackgroundDrawable() != null)
						 * oldBitmapDrawable = new BitmapDrawableEx(res,
						 * ((BitmapDrawable
						 * )ApplicationEx.getBackgroundDrawable()).getBitmap());
						 * if (ApplicationEx.getBackgroundDrawable() != null) {
						 * arrayDrawable[0] = oldBitmapDrawable;
						 * arrayDrawable[1] = tempDrawable; }
						 */
					} catch (OutOfMemoryError memErr) {
						if (isCancelled())
							return null;
						ApplicationEx
								.showShortToast("Error switching backgrounds");
						// setBackground(currentBackground, showNew, screen);
					} catch (Resources.NotFoundException e) {
						if (isCancelled())
							return null;
						switch (ResourcesSingleton.instance(getApplicationContext())
								.getConfiguration().orientation) {
						case Configuration.ORIENTATION_PORTRAIT:
							setBackground(savedInstance.getPortBackground(),
									showNew, screen);
							break;
						case Configuration.ORIENTATION_LANDSCAPE:
							setBackground(savedInstance.getLandBackground(),
									showNew, screen);
							break;
						default:
							setBackground(savedInstance.getPortBackground(),
									showNew, screen);
							break;
						}
					}
				} else {
					if (isCancelled())
						return null;
					switch (ResourcesSingleton.instance(getApplicationContext()).getConfiguration()
							.orientation) {
					case Configuration.ORIENTATION_PORTRAIT:
						setBackground(savedInstance.getPortBackground(),
								showNew, screen);
						break;
					case Configuration.ORIENTATION_LANDSCAPE:
						setBackground(savedInstance.getLandBackground(),
								showNew, screen);
						break;
					default:
						setBackground(savedInstance.getPortBackground(),
								showNew, screen);
						break;
					}
				}
			} else {
				if (isCancelled())
					return null;
				switch (ResourcesSingleton.instance(getApplicationContext()).getConfiguration()
						.orientation) {
				case Configuration.ORIENTATION_PORTRAIT:
					savedInstance.setPortBackground(
							ResourcesSingleton.instance(getApplicationContext())
							.getResourceEntryName(currentId));
					DatabaseHelperSingleton.instance(getApplicationContext()).setPortBackground(
							savedInstance.getUserId(),
							savedInstance.getPortBackground());
					break;
				case Configuration.ORIENTATION_LANDSCAPE:
					savedInstance.setLandBackground(
							ResourcesSingleton.instance(getApplicationContext())
							.getResourceEntryName(currentId));
					DatabaseHelperSingleton.instance(getApplicationContext()).setLandBackground(
							savedInstance.getUserId(),
							savedInstance.getLandBackground());
					break;
				default:
					savedInstance.setPortBackground(
							ResourcesSingleton.instance(getApplicationContext())
							.getResourceEntryName(currentId));
					DatabaseHelperSingleton.instance(getApplicationContext()).setPortBackground(
							savedInstance.getUserId(),
							savedInstance.getPortBackground());
					break;
				}
				if (isCancelled())
					return null;
				/*
				 * if (screen.equals("setlist")) { if (isCancelled()) return
				 * null;
				 * ApplicationEx.setSetlistDrawable(getDrawable(resourceId)); }
				 * else {
				 */
				if (isCancelled())
					return null;
				if (fieldsList.indexOf(resourceId) >= 0)
					ApplicationEx.setBackgroundBitmap(getApplicationContext(), getBitmap(resourceId));
				else
					ApplicationEx.setBackgroundBitmap(getApplicationContext(), getBitmap(R.drawable.splash4));
				// }
			}
			return null;
		}

		@Override
		protected void onCancelled(Void nothing) {
		}

		@Override
		protected void onPostExecute(Void nothing) {
			if (currFrag != null) {
				if (showNew) {
					/*
					 * if (currentId != resourceId && arrayDrawable[0] != null
					 * && arrayDrawable[1] != null) {
					 * ApplicationEx.setBackgroundDrawable( new
					 * TransitionDrawable(arrayDrawable));
					 * ((TransitionDrawable)ApplicationEx
					 * .getBackgroundDrawable()) .setCrossFadeEnabled(true); try
					 * {
					 * currFrag.setBackground(ApplicationEx.getBackgroundDrawable
					 * ()); } catch (IllegalArgumentException e) {
					 * Log.e(Constants.LOG_TAG, "Failed to set background!", e);
					 * } catch (NullPointerException e) {}
					 * //background.setImageDrawable(transitionDrawable);
					 * ((TransitionDrawable
					 * )ApplicationEx.getBackgroundDrawable())
					 * .startTransition(500); } else { try {
					 * currFrag.setBackground
					 * (((BitmapDrawable)tempDrawable).getBitmap()); } catch
					 * (NullPointerException e) {} }
					 */
					try {
						getWindow().setBackgroundDrawable(null);
						currFrag.setBackground(getApplicationContext(), ApplicationEx
								.getBackgroundBitmap(getApplicationContext()));
					} catch (NullPointerException e) {
                        // TODO
					}
					// background.setImageDrawable(tempDrawable);
				} else {
					try {
						getWindow().setBackgroundDrawable(null);
						currFrag.setBackground(getApplicationContext(), ApplicationEx
								.getBackgroundBitmap(getApplicationContext()));
					} catch (IllegalArgumentException e) {
						Log.e(Constants.LOG_TAG,
								"Failed to set background!", e);
					} catch (NullPointerException|OutOfMemoryError err) {
						ApplicationEx
								.showShortToast("Error setting background");
						/*
						 * Thread backgroundThread = new Thread() { public void
						 * run() { try { Thread.sleep(500); } catch
						 * (InterruptedException e) {} Log.i(Constants.LOG_TAG,
						 * "SetBackgroundTask OOM"); setBackground(name,
						 * showNew, screen); } }; backgroundThread.start();
						 */
					}
				}
			}
			// ApplicationEx.setBackgroundBitmap(((BitmapDrawable)currFrag.getBackground()).getBitmap());
			switchButton.setEnabled(true);
		}
	}

	private class SetBackgroundWaitTask extends AsyncTask<Void, Void, Void> {
		private String name;
		private boolean showNew;
		private String screen;

		private SetBackgroundWaitTask(String name, boolean showNew,
				String screen) {
			this.name = name;
			this.showNew = showNew;
			this.screen = screen;
		}

		@Override
		protected Void doInBackground(Void... nothing) {
			if (isCancelled())
				return null;
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
                // TODO
			}
			return null;
		}

		@Override
		protected void onCancelled(Void nothing) {
		}

		@Override
		protected void onPostExecute(Void nothing) {
			if (!isCancelled()) {
				setBackground(name, showNew, screen);
			}
		}
	}

	@Override
	public String getBackground() {
		switch (ResourcesSingleton.instance(getApplicationContext()).getConfiguration().orientation) {
		case Configuration.ORIENTATION_PORTRAIT:
			return savedInstance.getPortBackground();
		case Configuration.ORIENTATION_LANDSCAPE:
			return savedInstance.getLandBackground();
		default:
			return savedInstance.getPortBackground();
		}
	}

	@Override
	public void setlistBackground(final String name,
			final ImageViewEx background) {
		if (name == null) {
			return;
		}
		int resourceId = ResourcesSingleton.instance(getApplicationContext()).getIdentifier(name,
				"drawable", getPackageName());
		if (background != null) {
			try {
				ApplicationEx.setSetlistBitmap(getApplicationContext(), getBitmap(resourceId));
				background.setImageDrawable(null);
				BitmapDrawable bitmapDrawable = new BitmapDrawable(
						ResourcesSingleton.instance(getApplicationContext()),
						ApplicationEx.getSetlistBitmap(getApplicationContext()));
				bitmapDrawable.setColorFilter(new PorterDuffColorFilter(
						ResourcesSingleton.instance(getApplicationContext()).getColor(
								R.color.background_light),
						PorterDuff.Mode.SRC_ATOP));
				background.setImageDrawable(bitmapDrawable);
				// background.setImageBitmap(ApplicationEx.getSetlistBitmap());
			} catch (OutOfMemoryError memErr) {
				ApplicationEx.showShortToast("Error setting setlist");
				/*
				 * if (setlistBackgroundWaitTask != null)
				 * setlistBackgroundWaitTask.cancel(true);
				 * setlistBackgroundWaitTask = new
				 * SetlistBackgroundWaitTask(name, background); if
				 * (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
				 * setlistBackgroundWaitTask.execute(); else
				 * setlistBackgroundWaitTask
				 * .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				 */
			}
		}
		/*
		 * if (setlistBackgroundTask != null)
		 * setlistBackgroundTask.cancel(true); setlistBackgroundTask = new
		 * SetBackgroundTask(name, false, "setlist", background); if
		 * (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
		 * setlistBackgroundTask.execute(); else
		 * setlistBackgroundTask.executeOnExecutor
		 * (AsyncTask.THREAD_POOL_EXECUTOR);
		 */
	}

	@Override
	public void onLoginPressed(int loginType, String user, String pass) {
		savedInstance.setLogging(true);
		showLogin();
		switch (loginType) {
		case FragmentBase.LOGIN_FACEBOOK:
			facebookLogin();
			break;
		case FragmentBase.LOGIN_TWITTER:
			try {
				twitterLogin();
			} catch (IllegalArgumentException e) {
                // TODO
			}
			break;
		case FragmentBase.LOGIN_ANON:
			anonymousLogin();
			break;
		case FragmentBase.LOGIN_EMAIL:
			emailLogin(user, pass);
			break;
		case FragmentBase.SIGNUP_EMAIL:
			emailSignup(user, pass);
			break;
		}
		DatabaseHelperSingleton.instance(getApplicationContext()).setUserValue(
				savedInstance.isLogging() ? 1 : 0,
				DatabaseHelper.COL_LOGGING, savedInstance.getUserId());
	}

    // TODO Easy login to stay logged in?
	private void facebookLogin() {
		facebookLogin = true;
        Backendless.UserService.loginWithFacebookSdk(this, callbackManager,
                new AsyncCallback<BackendlessUser>() {
            @Override
            public void handleResponse(BackendlessUser loggedInUser) {
                if (loggedInUser == null) {
                    ApplicationEx.showLongToast("Login failed, try again");
                    logOut();
                } else {
                    // TODO Fix new user state - only used to show messages in the UI
                    // savedInstance.setNewUser(loggedInUser.isNew());
                    savedInstance.setNewUser(false);
                    savedInstance.setUserId(loggedInUser.getUserId());
                    if (loginTask != null) {
                        loginTask.cancel(true);
                    }
                    loginTask = new LoginTask();
                    loginTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    if (!DatabaseHelperSingleton.instance(getApplicationContext()).hasUser(savedInstance.getUserId())) {
                        DatabaseHelperSingleton.instance(getApplicationContext()).addUser(loggedInUser, "Facebook");
                    } else {
                        DatabaseHelperSingleton.instance(getApplicationContext()).setOffset(1, loggedInUser.getUserId());
                    }
                    // TODO Figure out how to make displayName for Facebook
                    // getFacebookDisplayName(loggedInUser);
                    if (savedInstance.isLogging()) {
                        setupUser(savedInstance.isNewUser());
                    }
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                // TODO More granular error handling
                ApplicationEx.showLongToast("Login failed, try again");
                logOut();
            }
        });
	}
    // TODO Figure out how to make displayName for Facebook
    /**
	private void getFacebookDisplayName(final BackendlessUser user) {
		if (user.getProperty("displayName") == null) {
			Session session = ParseFacebookUtils.getSession();
			boolean error = false;
			if (session != null && session.getState().isOpened()) {
				do {
					try {
						Request.executeMeRequestAsync(session, new GraphUserCallback() {
									@Override
									public void onCompleted(GraphUser graphUser,
                                                            Response response) {
										if (graphUser != null && response != null
                                                && response.getError() == null) {
											savedInstance.setDisplayName(graphUser.getFirstName() +
													" " + graphUser.getLastName().substring(0, 1)
													+ ".");
                                            user.setProperty("displayName",
                                                    savedInstance.getDisplayName());
                                            Backendless.UserService.update(user, new AsyncCallback<BackendlessUser>() {
                                                @Override
                                                public void handleResponse(BackendlessUser response) {
                                                    // TODO
                                                }

                                                @Override
                                                public void handleFault(BackendlessFault fault) {
                                                    // TODO
                                                }
                                            });
											DatabaseHelperSingleton.instance().setUserValue(
															savedInstance.getDisplayName(),
															DatabaseHelper.COL_DISPLAY_NAME,
															savedInstance.getUserId());
										}
									}
								});
						error = false;
					} catch (IllegalStateException e) {
						error = true;
					}
				} while (error);
			}
		}
	}
     */

    // TODO Easy login to stay logged in?
	private void twitterLogin() {
        Map<String, String> twitterFieldsMappings = new HashMap<>();
        twitterFieldsMappings.put( "name", "twitter_name" );
        Backendless.UserService.loginWithTwitter(this, twitterFieldsMappings,
                new AsyncCallback<BackendlessUser>() {
            @Override
            public void handleResponse(BackendlessUser loggedInUser) {
                if (loggedInUser == null) {
                    ApplicationEx.showLongToast("Login failed, try again");
                    logOut();
                } else {
                    // TODO Fix new user state - only used to show messages in the UI
                    // savedInstance.setNewUser(loggedInUser.isNew());
                    savedInstance.setNewUser(false);
                    savedInstance.setUserId(loggedInUser.getUserId());
                    if (loginTask != null) {
                        loginTask.cancel(true);
                    }
                    loginTask = new LoginTask();
                    loginTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    // TODO Update displayName
                    /**
                    if (loggedInUser.getProperty("displayName") == null) {
                        loggedInUser.setProperty("displayName", "@"
                                + ParseTwitterUtils.getTwitter().getScreenName());
                        Backendless.UserService.update(loggedInUser,
                                new AsyncCallback<BackendlessUser>() {
                            @Override
                            public void handleResponse(BackendlessUser response) {
                                // TODO
                            }

                            @Override
                            public void handleFault(BackendlessFault fault) {
                                // TODO
                            }
                        });
                    }
                     */
                    if (!DatabaseHelperSingleton.instance(getApplicationContext()).hasUser(savedInstance.getUserId())) {
                        DatabaseHelperSingleton.instance(getApplicationContext()).addUser(loggedInUser, "Twitter");
                    } else {
                        DatabaseHelperSingleton.instance(getApplicationContext()).setOffset(1, loggedInUser.getUserId());
                    }
                    if (savedInstance.isLogging()) {
                        setupUser(savedInstance.isNewUser());
                    }
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                // TODO More granular error handling
                ApplicationEx.showLongToast("Login failed, try again");
                logOut();
            }
        });
	}

	private void anonymousLogin() {
        final BackendlessUser anonymousUser = new BackendlessUser();
        anonymousUser.setProperty("username", UUID.randomUUID().toString());
        anonymousUser.setProperty("password", "changeme");
        Backendless.UserService.register(anonymousUser, new AsyncCallback<BackendlessUser>() {
            @Override
            public void handleResponse(BackendlessUser newUser) {
                ApplicationEx.showLongToast("registered anonymous user");
                Backendless.UserService.login((String) anonymousUser.getProperty("username"),
                        (String) anonymousUser.getProperty("password"),
                    new AsyncCallback<BackendlessUser>() {
                        @Override
                        public void handleResponse(BackendlessUser loggedInUser) {
                            ApplicationEx.showLongToast("logged in anonymous user");
                            savedInstance.setNewUser(true);
                            savedInstance.setUserId(loggedInUser.getUserId());
                            if (loginTask != null) {
                                loginTask.cancel(true);
                            }
                            loginTask = new LoginTask();
                            loginTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            if (!DatabaseHelperSingleton.instance(getApplicationContext()).hasUser(savedInstance.getUserId())) {
                                DatabaseHelperSingleton.instance(getApplicationContext()).addUser(loggedInUser, "Anonymous");
                            } else {
                                DatabaseHelperSingleton.instance(getApplicationContext()).setOffset(1, savedInstance.getUserId());
                            }
                            if (savedInstance.isLogging()) {
                                setupUser(savedInstance.isNewUser());
                            }
                        }

                        @Override
                        public void handleFault(BackendlessFault fault) {
                            // TODO Failed!
                        }
                    },
                true);
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                // TODO More granular error handling
                ApplicationEx.showLongToast("Login failed, try again\n" + fault.getMessage());
                logOut();
            }
        });
    }

	private void emailSignup(String username, String password) {
        final BackendlessUser emailUser = new BackendlessUser();
        emailUser.setProperty("username", username);
        emailUser.setProperty("password", password);
        emailUser.setProperty("displayName", username.substring(0, username.indexOf("@") + 1));
        Backendless.UserService.register(emailUser, new AsyncCallback<BackendlessUser>() {
            @Override
            public void handleResponse(BackendlessUser newUser) {
                savedInstance.setNewUser(true);
                savedInstance.setUserId(newUser.getUserId());
                if (loginTask != null) {
                    loginTask.cancel(true);
                }
                loginTask = new LoginTask();
                loginTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                if (!DatabaseHelperSingleton.instance(getApplicationContext()).hasUser(savedInstance.getUserId())) {
                    DatabaseHelperSingleton.instance(getApplicationContext()).addUser(newUser, "Email");
                } else {
                    DatabaseHelperSingleton.instance(getApplicationContext()).setOffset(1, savedInstance.getUserId());
                }
                if (savedInstance.isLogging()) {
                    setupUser(savedInstance.isNewUser());
                }
                Backendless.UserService.login((String) emailUser.getProperty("username"),
                        (String) emailUser.getProperty("password"),
                    new AsyncCallback<BackendlessUser>() {
                        @Override
                        public void handleResponse(BackendlessUser response) {
                            // TODO Logged in
                        }

                        @Override
                        public void handleFault(BackendlessFault fault) {
                            // TODO Failed!
                        }
                    },
                true);
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                // TODO More granular error handling
                ApplicationEx.showLongToast("Sign up failed, try again");
                logOut();
            }
        });
	}

	private void emailLogin(final String username, String password) {
        Backendless.UserService.login(username, password, new AsyncCallback<BackendlessUser>() {
            @Override
            public void handleResponse(BackendlessUser newUser) {
                savedInstance.setNewUser(true);
                savedInstance.setUserId(newUser.getUserId());
                if (!DatabaseHelperSingleton.instance(getApplicationContext()).hasUser(savedInstance.getUserId())) {
                    DatabaseHelperSingleton.instance(getApplicationContext()).addUser(newUser, "Email");
                } else {
                    DatabaseHelperSingleton.instance(getApplicationContext()).setOffset(1, savedInstance.getUserId());
                }
                if (savedInstance.isLogging()) {
                    setupUser(savedInstance.isNewUser());
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                // TODO More granular error handling
                ApplicationEx.showLongToast("Login failed, try again");
                logOut();
            }
        }, true);
	}

	@Override
	public void onInfoPressed(/* boolean fresh */) {
		try {
			savedInstance.setInInfo(true);
			DatabaseHelperSingleton.instance(getApplicationContext()).setUserValue(
					savedInstance.isInInfo() ? 1 : 0,
					DatabaseHelper.COL_IN_INFO, savedInstance.getUserId());
			FragmentInfo fInfo = new FragmentInfo();
			currFrag = fInfo;
			FragmentTransaction ft = fMan.beginTransaction();
			/*
			 * if (!fresh) ft.setCustomAnimations(R.anim.slide_in_top,
			 * R.anim.slide_out_top);
			 */
			ft.replace(android.R.id.content, fInfo, "fInfo").commitAllowingStateLoss();
			fMan.executePendingTransactions();
			getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
			getSlidingMenu().setTouchModeBehind(SlidingMenu.TOUCHMODE_NONE);
		} catch (IllegalStateException e) {
            // TODO
		}
	}

	@Override
	public void onStatsPressed() {
		savedInstance.setInLoad(true);
		DatabaseHelperSingleton.instance(getApplicationContext()).setUserValue(
				savedInstance.isInLoad() ? 1 : 0,
				DatabaseHelper.COL_IN_LOAD, savedInstance.getUserId());
		showLoad();
		fetchDisplayName();
	}

	private void showLoad() {
		try {
			FragmentLoad fLoad = new FragmentLoad();
			currFrag = fLoad;
			FragmentTransaction ft = fMan.beginTransaction();
			if (currFrag != null && currFrag instanceof FragmentPager
					&& currFrag.isVisible())
				((FragmentPager) currFrag).removeChildren(ft);
			/*
			 * ft.setCustomAnimations(R.anim.slide_in_bottom,
			 * R.anim.slide_out_bottom);
			 */
			ft.replace(android.R.id.content, fLoad, "fLoad").commitAllowingStateLoss();
			fMan.executePendingTransactions();
			getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
			getSlidingMenu().setTouchModeBehind(SlidingMenu.TOUCHMODE_NONE);
		} catch (IllegalStateException e) {
            // TODO
		}
	}

	@Override
	public void onFaqPressed(/* boolean fresh */) {
		try {
			savedInstance.setInFaq(true);
			DatabaseHelperSingleton.instance(getApplicationContext()).setUserValue(
					savedInstance.isInFaq() ? 1 : 0,
					DatabaseHelper.COL_IN_FAQ, savedInstance.getUserId());
			FragmentFaq fFaq = new FragmentFaq();
			currFrag = fFaq;
			FragmentTransaction ft = fMan.beginTransaction();
			/*
			 * if (!fresh) ft.setCustomAnimations(R.anim.slide_in_top,
			 * R.anim.slide_out_top);
			 */
			ft.replace(android.R.id.content, fFaq, "fFaq").commitAllowingStateLoss();
			fMan.executePendingTransactions();
			getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
			getSlidingMenu().setTouchModeBehind(SlidingMenu.TOUCHMODE_NONE);
		} catch (IllegalStateException e) {
            // TODO
		}
	}

	private class GetStatsTask extends AsyncTask<Void, Void, Void> {
		BackendlessException error;
		int hints;

		private GetStatsTask() {}

		@Override
		protected void onPreExecute() {
			currFrag.showLoading("Downloading standings...");
		}

		@Override
		protected Void doInBackground(Void... nothing) {
			// TODO Use already known values for correct answers and hints
			leadersBundle = new Bundle();
			leadersBundle.putString("userId", savedInstance.getUserId());
			leadersBundle.putString("userName", savedInstance.getDisplayName());
			leadersBundle.putString("userScore", Integer.toString(savedInstance.getCurrScore()));
			try {
				leadersBundle.putString("userAnswers",
                        Integer.toString(savedInstance.getCorrectAnswers().size()));
			} catch (NullPointerException e) {
				leadersBundle.putString("userAnswers", "Error!");
			}
			if (isCancelled()) {
                return null;
            }
			DatabaseHelperSingleton.instance(getApplicationContext()).clearLeaders();
			ArrayList<String> devList = new ArrayList<>();
			devList.add("unPF5wRxnK");
			devList.add("LuzjEBVnC8");
			devList.add("8aLb2I0fQA");
			devList.add("krEPKBuzFN");
			devList.add("k5VoRhL5BQ");
			devList.add("9LvKnpSEqu");
			if (isCancelled()) {
                return null;
            }
            // TODO
            // Get all hint relations of user
            List<BackendlessUser> userList = getUserRelations(savedInstance.getUserId(), "hint");
            if (userList != null && !userList.isEmpty()) {
                BackendlessUser user = userList.get(0);
                Object[] hints = (Object[]) user.getProperty("hint");
                leadersBundle.putString("userHints", Integer.toString(hints.length));
            }
			if (isCancelled()) {
                return null;
            }
            // TODO Same but for Backendless
            BackendlessDataQuery questionQuery = new BackendlessDataQuery();
            QueryOptions queryOptions = new QueryOptions();
            queryOptions.addSortByOption("created DESC");
            queryOptions.setPageSize(1);
            questionQuery.setQueryOptions(queryOptions);
            Question latestQuestion = Backendless.Persistence.of(Question.class).find(questionQuery).getCurrentPage().get(0);
            Date questionDate = latestQuestion.getCreated();
            Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
            cal.setTime(questionDate);
            cal.setTimeZone(TimeZone.getDefault());
            String lastQuestionDate = DateFormat.getDateFormat(getApplicationContext()).format(cal.getTime());
            leadersBundle.putString("lastQuestion", lastQuestionDate);
            // TODO Get all users, sorted by highest score first, get position
            String whereClause = "displayName is not null AND objectId not in (";
            for (String dev : devList) {
                whereClause += "'";
                whereClause += dev;
                whereClause += "',";
            }
            whereClause = whereClause.substring(0, whereClause.lastIndexOf(","));
            whereClause += ")";
            BackendlessDataQuery userQuery = new BackendlessDataQuery();
            userQuery.setWhereClause(whereClause);
            queryOptions = new QueryOptions();
            queryOptions.addSortByOption("score DESC");
            userQuery.setQueryOptions(queryOptions);
            BackendlessCollection<BackendlessUser> userCollection =
                    Backendless.Persistence.of(BackendlessUser.class).find(userQuery);
            ArrayList<BackendlessUser> rankList = new ArrayList<>(0);
            while (!userCollection.getCurrentPage().isEmpty()) {
                rankList.addAll(userCollection.getCurrentPage());
                userCollection = userCollection.nextPage();
            }
            int rank = -1;
            for (int i = 0; i < rankList.size(); i++) {
                if ((rankList.get(i).getUserId()).equals(savedInstance.getUserId())) {
                    rank = ++i;
                    break;
                }
            }
            if (rank > -1) {
                leadersBundle.putString("userRank", (rank < 10 ? "0" : "") + Integer.toString(rank));
            }
			if (!leadersBundle.containsKey("userRank")) {
                leadersBundle.putString("userRank", "");
            }
			getLeaders(rankList);
			if (!isCancelled()) {
				savedInstance.setInStats(true);
				DatabaseHelperSingleton.instance(getApplicationContext()).setUserValue(
						savedInstance.isInStats() ? 1 : 0,
								DatabaseHelper.COL_IN_STATS,
								savedInstance.getUserId());
				savedInstance.setInLoad(false);
				DatabaseHelperSingleton.instance(getApplicationContext()).setUserValue(
						savedInstance.isInLoad() ? 1 : 0,
						DatabaseHelper.COL_IN_LOAD, savedInstance.getUserId());
			}
			return null;
		}

		protected void onProgressUpdate(Void... nothing) {
			if (error != null) {
                Log.e(Constants.LOG_TAG, "Error getting stats: " + error.getMessage());
            }
			currFrag.showNetworkProblem();
			if (getStatsTask != null) {
                getStatsTask.cancel(true);
            }
		}

		@Override
		protected void onCancelled(Void nothing) {
		}

		@Override
		protected void onPostExecute(Void nothing) {
			showLeaders();
		}

		private void getLeaders(List<BackendlessUser> leaders) {
			ArrayList<String> rankList = new ArrayList<>(0);
			ArrayList<String> userList = new ArrayList<>(0);
			ArrayList<String> scoreList = new ArrayList<>(0);
			ArrayList<String> userIdList = new ArrayList<>(0);
			int tempInt;
			int limit = 0;
			for (BackendlessUser leader : leaders) {
				if (isCancelled()) {
                    return;
                }
				if (limit >= 50) {
                    break;
                }
				userIdList.add(leader.getUserId());
				if (limit + 1 < 10) {
                    rankList.add("0" + Integer.toString(limit + 1));
                } else {
                    rankList.add(Integer.toString(limit + 1));
                }
				userList.add((String) leader.getProperty("displayName"));
                try {
                    tempInt = Integer.parseInt((String) leader.getProperty("score"));
                } catch (NumberFormatException e) {
                    tempInt = 0;
                }
				scoreList.add(Integer.toString(tempInt));
				if (leader.getUserId().equals(savedInstance.getUserId()))
					leadersBundle.putString("userScore", Integer.toString(tempInt));
				limit++;
				DatabaseHelperSingleton.instance(getApplicationContext()).addLeader(
						savedInstance.getUserId(),
						rankList.get(rankList.size() - 1),
						userList.get(userList.size() - 1),
						scoreList.get(scoreList.size() - 1),
						userIdList.get(userIdList.size() - 1));
			}
			leadersBundle.putStringArrayList("rank", rankList);
			leadersBundle.putStringArrayList("user", userList);
			leadersBundle.putStringArrayList("score", scoreList);
			leadersBundle.putStringArrayList("userIdList", userIdList);
		}
	}

	@Override
	public Bundle getLeadersState() {
		return leadersBundle;
	}

	@SuppressLint("NewApi")
	@Override
	public void setupUser(boolean newUser) {
		if (savedInstance.getUserId().isEmpty() && !savedInstance.isLogging()) {
            ApplicationEx.showLongToast("showing splash");
			showSplash();
			return;
		}
		if (!savedInstance.getUserId().isEmpty()) {
            ApplicationEx.showLongToast("getting user data");
            getUserData(savedInstance.getUserId());
            if (loginTask != null) {
                loginTask.cancel(true);
            }
            loginTask = new LoginTask();
            loginTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
		if (fMan.findFragmentByTag("fLogin") == null) {
            ApplicationEx.showLongToast("showing login");
            showLogin();
        }
		if (userTask != null) {
            userTask.cancel(true);
        }
		userTask = new UserTask(newUser);
        userTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private class UserTask extends AsyncTask<Void, Void, Void> {
		private boolean newUser;

		private UserTask(boolean newUser) {
			this.newUser = newUser;
		}

		@Override
		protected void onPreExecute() {
			if (newUser && currFrag != null) {
                currFrag.showLoading("Creating account...");
            }
		}

		@Override
		protected Void doInBackground(Void... nothing) {
			if (savedInstance.getUserId().isEmpty()) {
                ApplicationEx.showLongToast("userId is null!");
				publishProgress();
				return null;
			}
			if (getScoreTask != null)
				getScoreTask.cancel(true);
			if (DatabaseHelperSingleton.instance(getApplicationContext()).isAnonUser(
					savedInstance.getUserId())) {
				if (savedInstance.getCorrectAnswers() != null)
					savedInstance.getCorrectAnswers().clear();
				else
					savedInstance.setCorrectAnswers(new ArrayList<String>());
				getNextQuestions(
						false,
						SharedPreferencesSingleton.instance(getApplicationContext()).getInt(
								ResourcesSingleton.instance(getApplicationContext()).getString(
										R.string.level_key), Constants.HARD));
			} else {
                publishProgress();
            }
			if (savedInstance.getDisplayName() == null && !isCancelled()) {
				savedInstance.setDisplayName((String) Backendless.UserService.CurrentUser().getProperty("displayName"));
				DatabaseHelperSingleton.instance(getApplicationContext()).setUserValue(
						savedInstance.getDisplayName(),
						DatabaseHelper.COL_DISPLAY_NAME,
						savedInstance.getUserId());
			}
			return null;
		}

		protected void onProgressUpdate(Void... nothing) {
            Log.i(Constants.LOG_TAG, savedInstance.getUserId());
			if (savedInstance.getUserId().isEmpty()) {
				logOut();
				ApplicationEx.showLongToast("Login failed, try again");
			} else if (!DatabaseHelperSingleton.instance(getApplicationContext()).isAnonUser(
                    savedInstance.getUserId())) {
                getScore(true, false, savedInstance.getUserId(), newUser);
            }
		}

		@Override
		protected void onCancelled(Void nothing) {
		}

		@Override
		protected void onPostExecute(Void nothing) {
            Log.i(Constants.LOG_TAG, "onPostExecute");
			if (!savedInstance.getUserId().isEmpty()) {
				switch (ResourcesSingleton.instance(getApplicationContext()).getConfiguration()
						.orientation) {
				case Configuration.ORIENTATION_PORTRAIT:
					setBackground(DatabaseHelperSingleton.instance(getApplicationContext())
							.getPortBackground(savedInstance.getUserId()),
							false, "quiz");
					break;
				case Configuration.ORIENTATION_LANDSCAPE:
					setBackground(DatabaseHelperSingleton.instance(getApplicationContext())
							.getLandBackground(savedInstance.getUserId()),
							false, "quiz");
					break;
				default:
					setBackground(DatabaseHelperSingleton.instance(getApplicationContext())
							.getPortBackground(savedInstance.getUserId()),
							false, "quiz");
					break;
				}
			}
		}
	}

	@Override
	public void logOut(/* boolean force */) {
        new LogOutTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private class LogOutTask extends AsyncTask<Void, Void, Void> {
		// boolean force;

		private LogOutTask(/* boolean force */) {
			// this.force = force;
		}

		/*
		 * @Override protected void onPreExecute() { showLogin(false); }
		 */
		@Override
		protected Void doInBackground(Void... nothing) {
			savedInstance.setLoggedIn(false);
			SharedPreferencesSingleton.putString(R.string.scoretext_key, "");
			SharedPreferencesSingleton.putString(R.string.questiontext_key, "");
			SharedPreferencesSingleton.putString(R.string.hinttext_key, "");
			SharedPreferencesSingleton.putString(R.string.answertext_key, "");
			SharedPreferencesSingleton.putString(R.string.placetext_key, "");
			SharedPreferencesSingleton.putInt(R.string.hinttimevis_key, View.VISIBLE);
			SharedPreferencesSingleton.putInt(R.string.hinttextvis_key, View.INVISIBLE);
			SharedPreferencesSingleton.putInt(R.string.skiptimevis_key, View.VISIBLE);
			SharedPreferencesSingleton.putInt(R.string.skiptextvis_key, View.INVISIBLE);
			SharedPreferencesSingleton.putString(R.string.hintnum_key, "");
			SharedPreferencesSingleton.putString(R.string.skipnum_key, "");
            if (loginTask != null) {
                loginTask.cancel(true);
            }
			if (userTask != null) {
                userTask.cancel(true);
            }
			if (getScoreTask != null) {
                getScoreTask.cancel(true);
            }
			if (getNextQuestionsTask != null) {
                getNextQuestionsTask.cancel(true);
            }
			if (getStageTask != null) {
                getStageTask.cancel(true);
            }
			if (getStatsTask != null) {
                getStatsTask.cancel(true);
            }
			if (!savedInstance.getUserId().isEmpty()) {
				DatabaseHelperSingleton.instance(getApplicationContext()).setUserValue(
						savedInstance.isLogging() ? 1 : 0,
								DatabaseHelper.COL_LOGGING,
								savedInstance.getUserId());
				DatabaseHelperSingleton.instance(getApplicationContext()).setUserValue(
						savedInstance.isLoggedIn() ? 1 : 0,
								DatabaseHelper.COL_LOGGED_IN,
								savedInstance.getUserId());
				DatabaseHelperSingleton.instance(getApplicationContext()).setOffset(0,
						savedInstance.getUserId());
			}
			Backendless.UserService.logout();
			savedInstance.setUserId("");
			savedInstance.setDisplayName(null);
			if (savedInstance.getCorrectAnswers() != null) {
				savedInstance.getCorrectAnswers().clear();
				savedInstance.setCorrectAnswers(null);
				ApplicationEx.setStringArrayPref(getApplicationContext(),
                        ResourcesSingleton.instance(getApplicationContext())
						.getString(R.string.correct_key), savedInstance.getCorrectAnswers());
			}
			return null;
		}

		@Override
		protected void onCancelled(Void nothing) {
		}

		protected void onProgressUpdate(Void... nothing) {
		}

		@Override
		protected void onPostExecute(Void nothing) {
            Log.i(Constants.LOG_TAG, "logouttask onPostExecute");
			savedInstance.setLogging(false);
			setLoggingOut(false);
			if (!savedInstance.isInInfo()) {
                showSplash();
            } else {
                onInfoPressed();
            }
		}
	}

	@Override
	public void showScoreDialog() {
		DialogFragment newFragment = new FragmentScoreDialog();
		newFragment.show(getSupportFragmentManager(), "dScore");
	}

	@Override
	public void showNameDialog() {
		DialogFragment newFragment = new FragmentNameDialog();
		newFragment.show(getSupportFragmentManager(), "dName");
	}

	/*
	 * public void showDownloadDialog() { DialogFragment newFragment = new
	 * FragmentDownloadDialog(); newFragment.show(getSupportFragmentManager(),
	 * "dDownload"); }
	 */
	@Override
	public void next() {
		if (!savedInstance.getQuestionIds().isEmpty()) {
            new BackgroundTask(savedInstance.getQuestionIds().get(0))
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
		savedInstance.setNewQuestion(false);
		DatabaseHelperSingleton.instance(getApplicationContext()).setUserValue(
				savedInstance.isNewQuestion() ? 1 : 0,
				DatabaseHelper.COL_NEW_QUESTION, savedInstance.getUserId());
		getNextQuestions(
				false,
				SharedPreferencesSingleton.instance(getApplicationContext()).getInt(
                        ResourcesSingleton.instance(getApplicationContext()).getString(
                                R.string.level_key), Constants.HARD));
	}

	private class BackgroundTask extends AsyncTask<Void, Void, Void> {
		private String questionId;

		private BackgroundTask(String questionId) {
			this.questionId = questionId;
		}

		@Override
		protected Void doInBackground(Void... nothing) {
			if (isCancelled())
				return null;
			if (questionId != null && savedInstance.getCorrectAnswers() != null
					&& savedInstance.getCorrectAnswers().contains(questionId)) {
				if (savedInstance.getCorrectAnswers() != null &&
						savedInstance.getCorrectAnswers().size() % 20 == 0) {
					if (getBackground() == null) {
						if (!savedInstance.getUserId().isEmpty()) {
							if (isCancelled())
								return null;
							savedInstance.setPortBackground(
									DatabaseHelperSingleton.instance(getApplicationContext())
									.getPortBackground(
											savedInstance.getUserId()));
							savedInstance.setLandBackground(
									DatabaseHelperSingleton.instance(getApplicationContext())
									.getLandBackground(
											savedInstance.getUserId()));
							if (getBackground() == null) {
								switch (ResourcesSingleton.instance(getApplicationContext())
										.getConfiguration().orientation) {
								case Configuration.ORIENTATION_PORTRAIT:
									savedInstance.setPortBackground("splash4");
									DatabaseHelperSingleton.instance(getApplicationContext())
											.setPortBackground(
													savedInstance.getUserId(),
													savedInstance
														.getPortBackground());
									break;
								case Configuration.ORIENTATION_LANDSCAPE:
									savedInstance.setLandBackground("splash4");
									DatabaseHelperSingleton.instance(getApplicationContext())
											.setLandBackground(
													savedInstance.getUserId(),
													savedInstance
														.getLandBackground());
									break;
								default:
									savedInstance.setPortBackground("splash4");
									DatabaseHelperSingleton.instance(getApplicationContext())
											.setPortBackground(
													savedInstance.getUserId(),
													savedInstance
														.getPortBackground());
									break;
								}
							}
						} else {
							switch (ResourcesSingleton.instance(getApplicationContext())
									.getConfiguration().orientation) {
							case Configuration.ORIENTATION_PORTRAIT:
								savedInstance.setPortBackground("splash4");
								break;
							case Configuration.ORIENTATION_LANDSCAPE:
								savedInstance.setLandBackground("splash4");
								break;
							default:
								savedInstance.setPortBackground("splash4");
								break;
							}
						}
					}
					publishProgress();
				}
			}
			return null;
		}

		protected void onProgressUpdate(Void... nothing) {
			setBackground(getBackground(), true, "quiz");
		}

		@Override
		protected void onPostExecute(Void nothing) {

		}
	}

	@Override
	public boolean questionIdsEmpty() {
		return savedInstance.getQuestionIds().isEmpty();
	}

	@Override
	public String getQuestionId(int index) {
		if (savedInstance.getQuestionIds().isEmpty()) {
			return null;
		}
		return savedInstance.getQuestionIds().get(index);
	}

	@Override
	public void addQuestionId(String questionId) {
		savedInstance.getQuestionIds().add(questionId);
	}

	@Override
	public void clearQuestionIds() {
		savedInstance.getQuestionIds().clear();
	}

	@Override
	public String getQuestion(int index) {
		return savedInstance.getQuestions().get(index);
	}

	@Override
	public void addQuestion(String question) {
		savedInstance.getQuestions().add(question);
	}

	@Override
	public void clearQuestions() {
		savedInstance.getQuestions().clear();
	}

	@Override
	public String getQuestionAnswer(int index) {
		return savedInstance.getQuestionAnswers().get(index);
	}

	@Override
	public void addQuestionAnswer(String correctAnswer) {
		savedInstance.getQuestionAnswers().add(correctAnswer);
	}

	@Override
	public void clearQuestionAnswers() {
		savedInstance.getQuestionAnswers().clear();
	}

	@Override
	public String getQuestionScore(int index) {
		return savedInstance.getQuestionScores().get(index);
	}

	@Override
	public void addQuestionScore(String questionScore) {
		savedInstance.getQuestionScores().add(questionScore);
	}

	@Override
	public void clearQuestionScores() {
		savedInstance.getQuestionScores().clear();
	}

	@Override
	public String getQuestionCategory(int index) {
		return savedInstance.getQuestionCategories().get(index);
	}

	@Override
	public void addQuestionCategory(String questionCategory) {
		savedInstance.getQuestionCategories().add(questionCategory);
	}

	@Override
	public void clearQuestionCategories() {
		savedInstance.getQuestionCategories().clear();
	}

	@Override
	public boolean getQuestionHint(int index) {
		return Boolean.parseBoolean(
				savedInstance.getQuestionHints().get(index));
	}

	@Override
	public void setQuestionHint(boolean questionHint, int index) {
		if (savedInstance.getQuestionHints().size() > index)
			savedInstance.getQuestionHints().set(index,
					Boolean.toString(questionHint));
	}

	@Override
	public void addQuestionHint(boolean questionHint) {
		savedInstance.getQuestionHints().add(Boolean.toString(questionHint));
	}

	@Override
	public void clearQuestionHints() {
		savedInstance.getQuestionHints().clear();
	}

	private void addQuestionSkip(boolean questionSkip) {
		savedInstance.getQuestionSkips().add(Boolean.toString(questionSkip));
	}

	@Override
	public boolean getQuestionSkip(int index) {
		return Boolean.parseBoolean(
				savedInstance.getQuestionSkips().get(index));
	}

	@Override
	public void clearQuestionSkips() {
		savedInstance.getQuestionSkips().clear();
	}

	@Override
	public void getNextQuestions(boolean force, int level) {
		if (getNextQuestionsTask != null) {
            getNextQuestionsTask.cancel(true);
        }
		// TODO Use different levels: easy, hard and default
		getNextQuestionsTask = new GetNextQuestionsTask(force, Constants.HARD);
        getNextQuestionsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private class GetNextQuestionsTask extends AsyncTask<Void, Void, Void> {
		List<Question> questionList;
		BackendlessException error;
		boolean questionNull = false;
		ArrayList<String> stageList;
		boolean force;
		boolean resumed = false;
		private ArrayList<String> tempQuestions;
		private int level;

		private GetNextQuestionsTask(boolean force, int level) {
			this.force = force;
			this.level = level;
		}

		@Override
		protected Void doInBackground(Void... nothing) {
			/*
			 * if (((getQuestionId(1) == null && getQuestionId(2) != null) ||
			 * correctAnswers != null &&
			 * correctAnswers.contains(getQuestionId(1))) && !isCancelled() &&
			 * !force) { updateIds(); if (questionIds.size() <=
			 * Constants.CACHED_LIMIT) getNextQuestions(force, level); } else
			 */if (!isCancelled()) {
				if (!force && !questionIdsEmpty()) {
                    updateIds();
                }
				publishProgress();
				if (savedInstance.getQuestionIds().size() > Constants.CACHED_LIMIT) {
					updatePersistedLists();
					return null;
				}
				try {
                    BackendlessDataQuery questionQuery = new BackendlessDataQuery();
                    QueryOptions queryOptions = new QueryOptions();
                    queryOptions.addSortByOption("created DESC");
					if (tempQuestions == null) {
                        tempQuestions = new ArrayList<>();
                    } else {
                        tempQuestions.clear();
                    }
					if (savedInstance.getCorrectAnswers() != null) {
                        tempQuestions.addAll(savedInstance.getCorrectAnswers());
                    }
					tempQuestions.addAll(savedInstance.getQuestionIds());
                    String whereClause = "";
                    if (!tempQuestions.isEmpty()) {
                        whereClause = buildExclusionWhereClause(tempQuestions) + " AND ";
                    }
					if (level == Constants.HARD) {
                        whereClause += "(score IS NULL OR score <= 1000)";
					} else {
                        int threshold = 1000;
						if (level == Constants.EASY) {
							threshold = DatabaseHelperSingleton.instance(getApplicationContext()).getUserIntValue(
                                    DatabaseHelper.COL_EASY, savedInstance.getUserId());
							if (threshold <= 0) {
								threshold = 600;
							}
						} else if (level == Constants.MEDIUM) {
							threshold = DatabaseHelperSingleton.instance(getApplicationContext()).getUserIntValue(
                                    DatabaseHelper.COL_MEDIUM, savedInstance.getUserId());
							if (threshold <= 0) {
								threshold = 800;
							}
						}
                        whereClause += "score <= " + threshold;
					}
                    questionQuery.setWhereClause(whereClause);
					/*
					 * if (checkCount || count < 0) { count = query.count();
					 * ApplicationEx.addParseQuery(); checkCount = false;
					 * DatabaseHelperSingleton.instance().setCheckCount(userId,
					 * checkCount); }
					 */
					if (/* count > 0 && */!isCancelled()) {
						stageList = new ArrayList<>();
						/*
						 * int skip = (int) (Math.random()*count); int limit =
						 * Constants.CACHED_QUESTIONS - questionIds.size();
						 * query.setLimit(limit); if (query.getLimit() >
						 * (count-skip)) skip = count-query.getLimit();
						 * query.setSkip(skip);
						 */
                        BackendlessCollection<Question> questionCollection =
                                Backendless.Persistence.of(Question.class).find(questionQuery);
						questionList = questionCollection.getCurrentPage();
                        if (!questionList.isEmpty() && !isCancelled()) {
                            String questionId;
                            int index;
                            Integer score;
                            if (questionList.size() <= (Constants.CACHED_QUESTIONS - savedInstance.getQuestionIds().size())) {
                                for (Question question : questionList) {
                                    questionId = question.getObjectId();
                                    addQuestionId(questionId);
                                    stageList.add(questionId);
                                    addQuestion(question.getQuestion());
                                    addQuestionAnswer(question.getAnswer());
                                    addQuestionCategory(question.getCategory());
                                    score = question.getScore();
                                    addQuestionScore(Integer.toString(score == null ? 1011 : score));
                                    addQuestionHint(false);
                                    savedInstance.getQuestionSkips().add(Boolean.toString(false));
                                }
                            } else {
                                Question question;
                                do {
                                    index = (int) (Math.random() * questionList.size());
                                    question = questionList.get(index);
                                    addQuestionId(question.getObjectId());
                                    stageList.add(question.getObjectId());
                                    addQuestion(question.getQuestion());
                                    addQuestionAnswer(question.getAnswer());
                                    addQuestionCategory(question.getCategory());
                                    score = question.getScore();
                                    addQuestionScore(Integer.toString(score == null ? 1011 : score));
                                    addQuestionHint(false);
                                    savedInstance.getQuestionSkips().add(Boolean.toString(false));
                                }
                                while (savedInstance.getQuestionIds().size() < Constants.CACHED_QUESTIONS);
                            }
                            getStage(savedInstance.getUserId(), stageList, resumed);
                            if (!isCancelled()) {
                                updatePersistedLists();
                            }
                        }
					}
				} catch (OutOfMemoryError memErr) {
					Log.e(Constants.LOG_TAG, "Error: " + memErr.getMessage());
					if (getNextQuestionsTask != null) {
                        getNextQuestionsTask.cancel(true);
                    }
					getNextQuestions(force, level);
				} catch (BackendlessException e) {
					error = e;
				}
			}
			updatePersistedLists();
			return null;
		}

		@Override
		protected void onCancelled(Void nothing) {
		}

		protected void onProgressUpdate(Void... nothing) {
			if (!questionIdsEmpty() && savedInstance.getQuestionIds().get(0) !=
					null && !isCancelled()) {
				if (!savedInstance.isLoggedIn()) {
					try {
						goToQuiz();
					} catch (IllegalStateException exception) {
                        // TODO
					}
				} else if (currFrag != null) {
					currFrag.resumeQuestion();
					resumed = true;
				}
			} else if (questionIdsEmpty() ||
					savedInstance.getQuestionIds().get(0) == null)
				questionNull = true;
		}

		@Override
		protected void onPostExecute(Void nothing) {
			if (error != null && !isCancelled()) {
				Log.e(Constants.LOG_TAG, "Error: " + error.getMessage());
                if (loginTask != null) {
                    loginTask.cancel(true);
                }
				if (userTask != null)
					userTask.cancel(true);
				currFrag.showNetworkProblem();
			} else if (!isCancelled() && questionNull) {
				if (!savedInstance.isLoggedIn()) {
					try {
						goToQuiz();
					} catch (IllegalStateException exception) {
                        // TODO
					}
				} else {
					if (questionIdsEmpty() ||
							savedInstance.getQuestionIds().get(0) == null)
						currFrag.showNoMoreQuestions(SharedPreferencesSingleton
								.instance(getApplicationContext()).getInt(
										ResourcesSingleton.instance(getApplicationContext())
												.getString(R.string.level_key),
										Constants.HARD));
					else if (!resumed)
						currFrag.resumeQuestion();
				}
			}
		}

		private void updateIds() {
			savedInstance.getQuestionIds().remove(0);
			savedInstance.getQuestions().remove(0);
			savedInstance.getQuestionAnswers().remove(0);
			savedInstance.getQuestionCategories().remove(0);
			savedInstance.getQuestionScores().remove(0);
			savedInstance.getQuestionHints().remove(0);
			savedInstance.getQuestionSkips().remove(0);
		}

        private String buildExclusionWhereClause(ArrayList<String> objectIds) {
            String whereClause = "objectId NOT IN (";
            for (String objectId : objectIds) {
                whereClause += "'";
                whereClause += objectId;
                whereClause += "',";
            }
            whereClause = whereClause.substring(0, whereClause.lastIndexOf(","));
            whereClause += ")";
            return whereClause;
        }

	}

	private void updatePersistedLists() {
		ApplicationEx.setStringArrayPref(getApplicationContext(),
                ResourcesSingleton.instance(getApplicationContext())
				.getString(R.string.questionids_key),
				savedInstance.getQuestionIds());
		ApplicationEx.setStringArrayPref(getApplicationContext(),
                ResourcesSingleton.instance(getApplicationContext())
				.getString(R.string.questions_key),
				savedInstance.getQuestions());
		ApplicationEx.setStringArrayPref(getApplicationContext(),
                ResourcesSingleton.instance(getApplicationContext())
				.getString(R.string.questionanswers_key),
				savedInstance.getQuestionAnswers());
		ApplicationEx.setStringArrayPref(getApplicationContext(),
						ResourcesSingleton.instance(getApplicationContext()).getString(
								R.string.questioncategories_key),
						savedInstance.getQuestionCategories());
		ApplicationEx.setStringArrayPref(getApplicationContext(),
                ResourcesSingleton.instance(getApplicationContext())
				.getString(R.string.questionscores_key),
				savedInstance.getQuestionScores());
		ApplicationEx.setStringArrayPref(getApplicationContext(),
                ResourcesSingleton.instance(getApplicationContext())
				.getString(R.string.questionhints_key),
				savedInstance.getQuestionHints());
		ApplicationEx.setStringArrayPref(getApplicationContext(),
                ResourcesSingleton.instance(getApplicationContext())
				.getString(R.string.questionskips_key),
				savedInstance.getQuestionSkips());
	}

	private void getStage(String userId, ArrayList<String> questionIds, boolean resumed) {
		if (getStageTask != null) {
            getStageTask.cancel(true);
        }
		getStageTask = new GetStageTask(userId, questionIds, resumed);
        getStageTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private class GetStageTask extends AsyncTask<Void, Void, Void> {
		private String userId;
		ArrayList<String> ids;
		private boolean resumed = false;
		private boolean progressed = false;

		private GetStageTask(String userId, ArrayList<String> ids, boolean resumed) {
			this.userId = userId;
			this.ids = ids;
			this.resumed = resumed;
		}

		@Override
		protected Void doInBackground(Void... nothing) {
            List<BackendlessUser> userList = getUserRelations(userId, "hint", "skip");
            if (userList != null && !userList.isEmpty()) {
                BackendlessUser user = userList.get(0);
				Object[] hints = (Object[]) user.getProperty("hint");
				Object[] skips = (Object[]) user.getProperty("skip");
                int index;
                // TODO Make this more efficient by finding each of these in the pile of hints or skips
                for (int i = 0; i < ids.size(); i++) {
                    index = savedInstance.getQuestionIds().indexOf(ids.get(i));
                    if (index >= 0) {
                        savedInstance.getQuestionHints().set(index, Boolean.toString(false));
                        savedInstance.getQuestionSkips().set(index, Boolean.toString(false));
                        if (!progressed) {
                            publishProgress();
                            progressed = true;
                        }
                    }
                }
                for (Object hint : hints) {
                    index = savedInstance.getQuestionIds().indexOf(((HashMap<String, Object>) hint).get("objectId").toString());
                    if (index >= 0) {
                        savedInstance.getQuestionHints().set(index, Boolean.toString(true));
                    }
                    if (!progressed) {
                        publishProgress();
                        progressed = true;
                    }
                }
                for (Object skip : skips) {
                    index = savedInstance.getQuestionIds().indexOf(((HashMap<String, Object>) skip).get("objectId").toString());
                    if (index >= 0) {
                        savedInstance.getQuestionSkips().set(index, Boolean.toString(true));
                    }
                    if (!progressed) {
                        publishProgress();
                        progressed = true;
                    }
                }
            }
			return null;
		}

		protected void onProgressUpdate(Void... nothing) {
			if (!savedInstance.isLoggedIn()) {
				try {
					goToQuiz();
				} catch (IllegalStateException exception) {
                    // TODO
				}
			} else if (!resumed) {
                currFrag.resumeQuestion();
            }
		}

		@Override
		protected void onPostExecute(Void nothing) {}
	}

	@Override
	public boolean isNewQuestion() {
		return savedInstance.isNewQuestion();
	}

	@Override
	public void setIsNewQuestion(boolean isNewQuestion) {
		savedInstance.setNewQuestion(isNewQuestion);
		DatabaseHelperSingleton.instance(getApplicationContext()).setUserValue(
				savedInstance.isNewQuestion() ? 1 : 0,
				DatabaseHelper.COL_NEW_QUESTION, savedInstance.getUserId());
	}

	@Override
	public String getUserId() {
		if (savedInstance == null) {
			return "";
		} else {
			return savedInstance.getUserId();
		}
	}

	@Override
	public void setDisplayName(String displayName) {
		currFrag.setDisplayName(displayName);
		this.savedInstance.setDisplayName(displayName);
		if (savedInstance.getUserId() != null) {
            // TODO Get user
			Backendless.UserService.CurrentUser().setProperty("displayName", displayName);
            Backendless.UserService.update(Backendless.UserService.CurrentUser(), new AsyncCallback<BackendlessUser>() {
                @Override
                public void handleResponse(BackendlessUser response) {
                    // TODO
                }

                @Override
                public void handleFault(BackendlessFault fault) {
                    // TODO
                }
            });
		}
		if (!savedInstance.getUserId().isEmpty()) {
            DatabaseHelperSingleton.instance(getApplicationContext()).setUserValue(displayName,
                    DatabaseHelper.COL_DISPLAY_NAME, savedInstance.getUserId());
        }
	}

	@Override
	public String getDisplayName() {
		return savedInstance.getDisplayName();
	}

	@Override
	public int getCurrentScore() {
		return savedInstance.getCurrScore();
	}

	@Override
	public void addCurrentScore(int addValue) {
		savedInstance.setCurrScore(savedInstance.getCurrScore() + addValue);
	}

	@Override
	public void saveUserScore(final Integer currTemp) {
        if (Backendless.UserService.CurrentUser() == null) {
            if (loginTask != null) {
                loginTask.cancel(true);
            }
            Backendless.UserService.setCurrentUser(Backendless.UserService.findById(getUserId()));
        }
        Backendless.UserService.CurrentUser().setProperty("score", currTemp);
        Backendless.UserService.update(Backendless.UserService.CurrentUser(), new AsyncCallback<BackendlessUser>() {
            @Override
            public void handleResponse(BackendlessUser response) {
                // TODO
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                // TODO
            }
        });
		DatabaseHelperSingleton.instance(getApplicationContext()).setScore(currTemp,
				savedInstance.getUserId());
	}

	@Override
	public void shareScreenshot(boolean isSetlist) {
		if (isSetlist && currFrag != null)
			currFrag.showResizedSetlist();
		String path = takeScreenshot();
		if (isSetlist && currFrag != null)
			currFrag.hideResizedSetlist();
		if (path == null)
			return;
		Intent share = new Intent(Intent.ACTION_SEND);
		share.setType("image/jpeg");

		share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(path)));

		PackageManager packageManager = getPackageManager();
		List<ResolveInfo> activities = packageManager.queryIntentActivities(
				share, 0);
		boolean isIntentSafe = activities.size() > 0;
		if (isIntentSafe)
			startActivity(Intent.createChooser(share, "Share screenshot with"));
	}

	private String takeScreenshot() {
		// Make file name and create file
		Calendar cal = Calendar.getInstance(TimeZone.getDefault(),
				Locale.getDefault());
		int calendarMonth = cal.get(Calendar.MONTH) + 1;
		int calendarDate = cal.get(Calendar.DATE);
		int calendarHour = cal.get(Calendar.HOUR_OF_DAY);
		int calendarMinute = cal.get(Calendar.MINUTE);
		int calendarSecond = cal.get(Calendar.SECOND);
		String fileName = cal.get(Calendar.YEAR)
				+ "_"
				+ (calendarMonth < 10 ? ("0" + calendarMonth) : calendarMonth)
				+ "_"
				+ (calendarDate < 10 ? ("0" + calendarDate) : calendarDate)
				+ "_"
				+ (calendarHour < 10 ? ("0" + calendarHour) : calendarHour)
				+ "_"
				+ (calendarMinute < 10 ? ("0" + calendarMinute)
						: calendarMinute)
				+ "_"
				+ (calendarSecond < 10 ? ("0" + calendarSecond)
						: calendarSecond) + ".jpg";
		String path = ApplicationEx.cacheLocation + Constants.SCREENS_LOCATION
				+ fileName;
		// Capture the contents of the screen
		Bitmap bitmap = null;
		try {
			View rootView = getWindow().getDecorView();
			int totalHeight = rootView.getHeight();
			int totalWidth = rootView.getWidth();
			bitmap = Bitmap.createBitmap(totalWidth, totalHeight,
					Bitmap.Config.ARGB_8888);
			Canvas c = new Canvas(bitmap);
			rootView.layout(0, 0, totalWidth, totalHeight);
			rootView.draw(c);
			/*
			 * rootView.buildDrawingCache(); bitmap =
			 * Bitmap.createBitmap(rootView.getDrawingCache());
			 * rootView.destroyDrawingCache(); bitmap =
			 * Bitmap.createScaledBitmap(bitmap, rootView.getWidth() / 2,
			 * rootView.getHeight() / 2, true);
			 * rootView.setDrawingCacheEnabled(false);
			 */
		} catch (OutOfMemoryError e) {
			ApplicationEx.showLongToast("Oops, try again");
			e.printStackTrace();
			return null;
		} catch (NullPointerException e) {
			ApplicationEx.showLongToast("Oops, try again");
			e.printStackTrace();
			return null;
		}
		FileOutputStream fout;
		File imageFile = new File(path);

		try {
			fout = new FileOutputStream(imageFile);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fout);
			fout.flush();
			fout.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		bitmap.recycle();
		return path;
	}

	@Override
	public boolean getNetworkProblem() {
		return savedInstance.isNetworkProblem();
	}

	@Override
	public void setNetworkProblem(boolean networkProblem) {
		savedInstance.setNetworkProblem(networkProblem);
	}

	@Override
	public void addCorrectAnswer(String correctId) {
		if (savedInstance.getCorrectAnswers() == null)
			savedInstance.setCorrectAnswers(new ArrayList<String>());
		savedInstance.getCorrectAnswers().add(correctId);
		// count--;
		ApplicationEx.setStringArrayPref(getApplicationContext(),
                ResourcesSingleton.instance(getApplicationContext()).getString(R.string.correct_key),
				savedInstance.getCorrectAnswers());
	}

	@Override
	public boolean isCorrectAnswer(String correctId) {
        return savedInstance.getCorrectAnswers() != null && savedInstance.getCorrectAnswers().contains(correctId);
	}

	@Override
	public void setUserName(String userName) {
		if (leadersBundle != null && userName != null) {
            leadersBundle.putString("userName", userName);
        }
	}

	@Override
	public boolean isNewUser() {
		return savedInstance.isNewUser();
	}

	@Override
	public void resetPassword(String username) {
        Log.i(Constants.LOG_TAG, "resetting password for " + username);
        Backendless.UserService.restorePassword(username, new AsyncCallback<Void>() {
            @Override
            public void handleResponse(Void response) {
                ApplicationEx.showLongToast("Password reset email has been sent");
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.i(Constants.LOG_TAG, fault.toString());
                ApplicationEx.showLongToast("An error occurred, try again");
            }
        });
	}

	/*
	 * @Override public int getWidth() { return width; }
	 * 
	 * @Override public int getHeight() { return height; }
	 */
	@Override
	public boolean isLoggingOut() {
		return savedInstance.isLoggingOut();
	}

	@Override
	public void setLoggingOut(boolean loggingOut) {
		savedInstance.setLoggingOut(loggingOut);
	}

	@Override
	public void setHomeAsUp(boolean homeAsUp) {
		if (getActionBar() != null) {
			getActionBar().setDisplayHomeAsUpEnabled(homeAsUp);
			if (!homeAsUp) {
                getActionBar().setHomeButtonEnabled(false);
            }
		}
	}

	/*
	 * @Override public void showQuickTip(View view, String message) { if
	 * (SharedPreferencesSingleton.instance().getBoolean(
	 * ResourcesSingleton.instance().getString(R.string.quicktip_key), false))
	 * CheatSheet.setup(view, message); }
	 * 
	 * @Override public void showQuickTipMenu(ViewGroup view, String message,
	 * int location) { if (SharedPreferencesSingleton.instance().getBoolean(
	 * ResourcesSingleton.instance().getString(R.string.quicktip_key), false))
	 * CheatSheetMenu.setup(view, message, getWidth(), getHeight(), location); }
	 */
	@Override
	public SlidingMenu slidingMenu() {
		return getSlidingMenu();
	}

	@Override
	public void refreshMenu() {
		if (mMenu != null) {
            MenuItem shareItem = mMenu.findItem(R.id.ShareMenu);
            MenuItem setlistItem = mMenu.findItem(R.id.SetlistMenu);
			if (savedInstance.isInSetlist()) {
				if (shareItem != null) {
					shareItem.setVisible(true);
				}
				if (setlistItem != null) {
					setlistItem.setVisible(false);
				}
			} else if (!savedInstance.isInStats() && !savedInstance.isInInfo()
					&& !savedInstance.isInFaq() && !savedInstance.isInLoad() &&
					!savedInstance.isLogging()) {
				if (shareItem != null) {
					shareItem.setVisible(false);
				}
				if (setlistItem != null) {
					setlistItem.setVisible(true);
				}
			} else {
				if (shareItem != null) {
					shareItem.setVisible(false);
				}
				if (setlistItem != null) {
					setlistItem.setVisible(false);
				}
			}
			// TODO Add case for chooser, including a button to go back to
			// setlist?
			onPrepareOptionsMenu(mMenu);
		}
	}

	@Override
	public boolean getGoToSetlist() {
		if (goToSetlist) {
			nManager.cancel(Constants.NOTIFICATION_NEW_SONG);
		}
		return goToSetlist;
	}

	@Override
	public void setGoToSetlist(boolean goToSetlist) {
		this.goToSetlist = goToSetlist;
	}

	@Override
	public void setInSetlist(boolean inSetlist) {
		if (savedInstance.isInSetlist() == inSetlist) {
			return;
		}
		savedInstance.setInSetlist(inSetlist);
		DatabaseHelperSingleton.instance(getApplicationContext()).setUserValue(inSetlist ? 1 : 0,
				DatabaseHelper.COL_IN_SETLIST, savedInstance.getUserId());
		refreshMenu();
		refreshSlidingMenu();
	}

	@Override
	public boolean getInSetlist() {
		if (savedInstance.isInSetlist()) {
			nManager.cancel(Constants.NOTIFICATION_NEW_SONG);
		}
		return savedInstance.isInSetlist();
	}

    @Override
    public void setInChooser(boolean inChooser) {
        if (savedInstance.isInChooser() == inChooser) {
            return;
        }
        savedInstance.setInChooser(inChooser);
        DatabaseHelperSingleton.instance(getApplicationContext()).setUserValue(inChooser ? 1 : 0,
                DatabaseHelper.COL_IN_CHOOSER, savedInstance.getUserId());
        refreshMenu();
        refreshSlidingMenu();
    }

    @Override
    public boolean getInChooser() {
        return savedInstance.isInChooser();
    }

	@Override
	public Bitmap getBitmap(int resId) throws OutOfMemoryError {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPurgeable = true;
		options.inInputShareable = true;
		options.inTempStorage = new byte[16 * 1024];
		options.inSampleSize = 2;
        options.inMutable = true;
		return BitmapFactory.decodeResource(ResourcesSingleton.instance(getApplicationContext()),
				resId, options);
	}

	@Override
	public void setCurrFrag(FragmentBase currFrag) {
		this.currFrag = currFrag;
	}

	@Override
	public FragmentBase getCurrFrag() {
		return currFrag;
	}

	@Override
	public void updateLevel() {
		if (levelText != null && levelImage != null) {
			switch (SharedPreferencesSingleton.instance(getApplicationContext())
					.getInt(ResourcesSingleton.instance(getApplicationContext()).getString(
							R.string.level_key), Constants.HARD)) {
			case Constants.EASY:
                levelText.setText(String.format(ResourcesSingleton.instance(getApplicationContext())
                        .getString(R.string.LevelTitle), "(Easy)"));
				levelImage.setImageResource(R.drawable.ic_level_easy_inverse);
				break;
			case Constants.MEDIUM:
                levelText.setText(String.format(ResourcesSingleton.instance(getApplicationContext())
                        .getString(R.string.LevelTitle), "(Medium)"));
				levelImage.setImageResource(R.drawable.ic_level_med_inverse);
				break;
			case Constants.HARD:
                levelText.setText(String.format(ResourcesSingleton.instance(getApplicationContext())
                        .getString(R.string.LevelTitle), "(Hard)"));
				levelImage.setImageResource(R.drawable.ic_level_hard_inverse);
				break;
			}
			if (getQuestion(0) == null) {
				getNextQuestions(
						false,
						SharedPreferencesSingleton.instance(getApplicationContext()).getInt(
								ResourcesSingleton.instance(getApplicationContext()).getString(
										R.string.level_key), Constants.HARD));
				if (currFrag != null) {
					currFrag.showRetry();
				}
			}
		}
	}

	public static class YearComparator implements Comparator<String>,
			Serializable {
		
		private static final long serialVersionUID = 5030935796787982439L;

		@Override
		public int compare(String left, String right) {
			int lhs = Integer.parseInt(left);
			int rhs = Integer.parseInt(right);
			if (lhs > rhs) {
				return -1;
			} else if (lhs == rhs) {
				return 0;
			} else {
				return 1;
			}
		}
	}

	public static class SetlistComparator implements Comparator<String>,
			Serializable{
		
		private static final long serialVersionUID = 8353202092989559280L;
		
		Date leftDate;
		Date rightDate;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd",
				Locale.getDefault());

		@Override
		public int compare(String left, String right) {
			try {
				leftDate = dateFormat.parse(left.substring(0, 10));
				rightDate = dateFormat.parse(right.substring(0, 10));
			} catch (Exception e) {
				return 0;
			}
			if (leftDate.after(rightDate)) {
				return -1;
			} else if (rightDate.after(leftDate)) {
				return -1;
			} else {
				return 0;
			}
		}
	}

	@Override
	public void updateSetlistMap(String setKey, String setlist) {
		if (savedInstance.getSetlistMap() == null) {
			savedInstance.setSetlistMap((TreeMap<String,
					TreeMap<String, String>>) FileCacheSingleton.instance(getApplicationContext())
					.readSerializableFromFile(Constants.SETLIST_MAP_FILE));
			if (savedInstance.getSetlistMap() == null) {
				return;
			}
		}
		TreeMap<String, String> tempMap;
		for (Entry<String, TreeMap<String, String>> setEntry :
				savedInstance.getSetlistMap().entrySet()) {
			if (setEntry.getKey().startsWith(setKey.substring(0, 4))) {
				tempMap = setEntry.getValue();
				if (!tempMap.containsKey(setKey)) {
					tempMap.put(setKey, setlist);
				}
			}
		}
	}

	@Override
	public TreeMap<String, TreeMap<String, String>> getSetlistMap() {
		return savedInstance.getSetlistMap();
	}

	@Override
	public void checkSetlistMap(ExpandableListView setlistListView,
			ProgressBar setlistProgress) {
		if (savedInstance.getSetlistMap() == null ||
				savedInstance.getSetlistMap().isEmpty()) {
			if (ApplicationEx.hasConnection()) {
				parseTask = new ParseTask(setlistListView, setlistProgress);
                parseTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			} else {
				ApplicationEx.showLongToast(getApplicationContext(), R.string.NoConnectionToast);
			}
		} else {
			showSetlistChooser(setlistListView, setlistProgress);
		}
	}

	private void showSetlistChooser(ExpandableListView setlistListView,
			ProgressBar setlistProgress) {
		setlistProgress.setVisibility(View.INVISIBLE);
		if (setlistListView != null) {
			SetlistAdapter setlistAdapter = new SetlistAdapter(
                    getApplicationContext(), savedInstance.getSetlistMap());
			setlistListView.setAdapter(setlistAdapter);
			if (!SharedPreferencesSingleton.instance(getApplicationContext()).contains(
					ResourcesSingleton.instance(getApplicationContext()).getString(
							R.string.selected_group_key))
					|| !SharedPreferencesSingleton.instance(getApplicationContext()).contains(
							ResourcesSingleton.instance(getApplicationContext()).getString(
									R.string.selected_group_key))) {
				SharedPreferencesSingleton.putInt(R.string.selected_group_key, 0);
				SharedPreferencesSingleton.putInt(R.string.selected_child_key, 0);
			}
			setlistListView.setVisibility(View.VISIBLE);
		}
	}
	
	private String createSetlistKey(SetInfo setInfo) {
        return setInfo.getSetDate() + " - " + setInfo.getSetVenue() + " - " + setInfo.getSetCity();
	}
	
	@Override
	public void readSetlistInfoFromDatabase() {
		if (savedInstance.getLatestSet() == null) {
			savedInstance.setLatestSet(new SetInfo());
		}
    	savedInstance.getLatestSet().setSetlist(
    			SharedPreferencesSingleton.instance(getApplicationContext()).getString(
        		ResourcesSingleton.instance(getApplicationContext()).getString(R.string.setlist_key),
        		""));
    	savedInstance.getLatestSet().setSetStamp(
    			SharedPreferencesSingleton.instance(getApplicationContext()).getString(
        		ResourcesSingleton.instance(getApplicationContext()).getString(R.string.setstamp_key),
        		""));
    	savedInstance.getLatestSet().setArchive(
    			SharedPreferencesSingleton.instance(getApplicationContext()).getBoolean(
        		ResourcesSingleton.instance(getApplicationContext()).getString(R.string.archive_key),
        		false));
        // TODO Update setlistMap with latest one if doesn't exist
    	savedInstance.getLatestSet().setSetDate(
    			SharedPreferencesSingleton.instance(getApplicationContext()).getString(
        		ResourcesSingleton.instance(getApplicationContext()).getString(R.string.set_date_key),
        		""));
    	savedInstance.getLatestSet().setSetVenue(
    			SharedPreferencesSingleton.instance(getApplicationContext()).getString(
        		ResourcesSingleton.instance(getApplicationContext()).getString(R.string.setvenue_key),
        		""));
    	savedInstance.getLatestSet().setSetCity(
    			SharedPreferencesSingleton.instance(getApplicationContext()).getString(
        		ResourcesSingleton.instance(getApplicationContext()).getString(R.string.setcity_key),
        		""));
    	if (savedInstance.getLatestSet().getKey() == null ||
    			StringUtils.isBlank(savedInstance.getLatestSet().getKey())) {
    		savedInstance.getLatestSet().setKey(createSetlistKey(
    				savedInstance.getLatestSet()));
    	}
        if (!savedInstance.getLatestSet().getKey().isEmpty() &&
        		!savedInstance.getLatestSet().getSetlist().isEmpty()) {
        	updateSetlistMap(savedInstance.getLatestSet().getKey(),
        			savedInstance.getLatestSet().getSetlist());
        }
        Log.i(Constants.LOG_TAG, "readSetlistInfoFromDatabase: " + savedInstance.getLatestSet());
        savedInstance.setSelectedSet(savedInstance.getLatestSet());
    }
	
	@Override
	public SetInfo getLatestSetInfo() {
		return savedInstance.getLatestSet();
	}
	
	@Override
	public void setLatestKey(String key) {
		if (savedInstance.getLatestSet() != null) {
			savedInstance.getLatestSet().setKey(key);
		}
		else {
			SetInfo latestSet = new SetInfo();
			latestSet.setKey(key);
			savedInstance.setLatestSet(latestSet);
		}
	}
	
	@Override
	public String getLatestKey() {
		if (savedInstance.getLatestSet() != null) {
			return savedInstance.getLatestSet().getKey();
		}
		else {
			return null;
		}
	}

	@Override
	public String getLatestSetlist() {
		if (savedInstance.getLatestSet() != null) {
			return savedInstance.getLatestSet().getSetlist();
		}
		else {
			return null;
		}
	}

	@Override
	public String getLatestSetStamp() {
		if (savedInstance.getLatestSet() != null) {
			return savedInstance.getLatestSet().getSetStamp();
		}
		else {
			return null;
		}
	}
	
	@Override
	public SetInfo getSelectedSetInfo() {
		return savedInstance.getSelectedSet();
	}
	
	@Override
	public void setSelectedSetInfo(SetInfo setInfo) {
		savedInstance.setSelectedSet(setInfo);
	}

    private class LoginTask extends AsyncTask<Void, Void, Void> {

        private LoginTask() {}

        @Override
        protected Void doInBackground(Void... nothing) {
            if (savedInstance.isLoggedIn()) {
                if (!savedInstance.getUserId().isEmpty()) {
                    getUserData(savedInstance.getUserId());
                    Backendless.UserService.setCurrentUser(Backendless.UserService.findById(getUserId()));
                }
            }
            return null;
        }

    }

}