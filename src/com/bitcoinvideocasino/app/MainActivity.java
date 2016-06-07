package com.bitcoinvideocasino.app;

import java.io.IOException;


import java.util.logging.ConsoleHandler;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Button;
import android.graphics.Typeface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.util.Log;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.preference.PreferenceManager;
import android.widget.Toast;
import android.widget.ImageButton;
import android.os.Handler;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import android.content.Intent;
import com.bitcoinvideocasino.lib.*;
import com.bitcoinvideocasino.R;

public class MainActivity extends CommonActivity {

	//TextView mTitle;
	TextView mBalance;
    TextView mTestLocalWarning;
	ImageButton mVideoPoker;
	ImageButton mBlackjack;
    ImageButton mSlots;
	Button mDeposit;
	Button mSettings;
	Button mCashOut;
    Button mShare;
	MainNetBalanceTask mNetBalanceTask;
	MainCreateAccountTask mCreateAccountTask;
	NetAndroidAppVersionTask mAndroidAppVersionTask;
	Handler mHandler;
	MixpanelAPI mMixpanel;
	final static String TAG = "MainActivity";
	long mLastNetBalanceCheck;
	boolean mBlinkOn;
	final static int BLINK_DELAY = 500;
	final static String SETTING_ANDROID_APP_VERSION_CHECK = "android_app_version_check";
	Typeface mRobotoLight;
	Typeface mRobotoBold;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main);

		// TB TEMP TEST - For now always reset the preferences (true) so that we
		// can get first time players working.
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		// TB - These below lines reset everything to defaults... good for
		// testing first time players
		/*
		 * PreferenceManager.getDefaultSharedPreferences(this).edit().clear().commit
		 * (); PreferenceManager.setDefaultValues(this, R.xml.preferences,
		 * true);
		 */

		//mTitle = (TextView) findViewById(R.id.title);
		mBalance = (TextView) findViewById(R.id.balance);
        mTestLocalWarning = (TextView) findViewById(R.id.test_local_warning);
		mVideoPoker = (ImageButton) findViewById(R.id.videopoker_button);
		mBlackjack = (ImageButton) findViewById(R.id.blackjack_button);
        mSlots = (ImageButton) findViewById(R.id.slots_button);
		mDeposit = (Button) findViewById(R.id.deposit_button);
		mCashOut = (Button) findViewById(R.id.cashout_button);
        mShare = (Button) findViewById(R.id.share_button);
		mSettings = (Button) findViewById(R.id.settings_button);
		mRobotoLight = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
		mRobotoBold = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Bold.ttf");

		mBalance.setTypeface(mRobotoBold);
		mDeposit.setTypeface(mRobotoLight);
		mCashOut.setTypeface(mRobotoLight);
		mSettings.setTypeface(mRobotoLight);
        mShare.setTypeface(mRobotoLight);

		mNetBalanceTask = null;
		mCreateAccountTask = null;
		mAndroidAppVersionTask = null;
        mHandler = new Handler();
        mMixpanel = MixpanelAPI.getInstance(this, "f2d42ccd048fde33f91428a79a0e933e");
		mMixpanel.track("main_activity_create", null);
		mLastNetBalanceCheck = 0;
		mBlinkOn = false;

        if( BitcoinVideoCasino.RUN_ENVIRONMENT == BitcoinVideoCasino.RunEnvironment.PRODUCTION ) {
            mTestLocalWarning.setVisibility(View.GONE);
        }
	}
	
	@Override
	protected void onDestroy() {
	    mMixpanel.flush();
	    super.onDestroy();
	}
	
	private Runnable mTimeUpdateRunnable = new Runnable() {
		public void run() {
			timeUpdate();
		} 
	};
	
	public void timeUpdate() {
		//Log.v(TAG, "timeUpdate!");
		
		mBlinkOn = !mBlinkOn;
		
		BitcoinVideoCasino bvc = BitcoinVideoCasino.getInstance(this);
		long now = System.currentTimeMillis();
		if( now - mLastNetBalanceCheck > 5000 ) {
			mLastNetBalanceCheck = now;
			if (bvc.mAccountKey != null) {
				mNetBalanceTask = new MainNetBalanceTask(this);
				mNetBalanceTask.execute(Long.valueOf(0));
			}
		}
		
		if( bvc.mIntBalance == 0 && mBlinkOn ) {
			mDeposit.setTypeface( mRobotoBold );
		}
		else {
			mDeposit.setTypeface( mRobotoLight ); 
		} 
		
		mHandler.postDelayed( mTimeUpdateRunnable, BLINK_DELAY );
	}

	public void updateValues() {

		BitcoinVideoCasino bvc = BitcoinVideoCasino.getInstance(this);

		if( bvc.mIntBalance != -1 ) {
			String btc = Bitcoin.longAmountToStringChopped(bvc.mIntBalance);
			mBalance.setText(String.format("Balance: %s BTC", btc));
		}
		else {
			mBalance.setText(String.format("Connecting..."));
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		updateValues();
        
		BitcoinVideoCasino bvc = BitcoinVideoCasino.getInstance(this);
		if (bvc.mAccountKey == null) {
			Log.v(TAG, "account key is null, creating a new account!");
			mCreateAccountTask = new MainCreateAccountTask(this);
			mCreateAccountTask.execute(Long.valueOf(0));
		}
		else {
			// TB TODO - Is there some better kind of storage I should be using for something like this?
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
			long lastCheck = sharedPref.getLong(SETTING_ANDROID_APP_VERSION_CHECK, 0); 
			long now = System.currentTimeMillis()/1000;
			// Check every week
			long APP_CHECK_DELAY = 60 * 60 * 24 * 7;
			if( now - lastCheck > APP_CHECK_DELAY ) { 
				mAndroidAppVersionTask = new NetAndroidAppVersionTask(this);
				mAndroidAppVersionTask.execute(Long.valueOf(0));
			}
		}
		
		// Bitcoin.test();
		timeUpdate();
	}
    @Override
    public void onPause() {
    	super.onPause();
    	mHandler.removeCallbacks(mTimeUpdateRunnable);
    }
    
	public void onShare(View button) {
		mMixpanel.track("share_button_pushed", null);
		
		// TB TODO - Show a dialog explaining that you will get a referral bonus.
		// Might be tricky to enforce the user zapping the QR code on the Android page. Otherwise no referral bonus... Hmmm
		// TB TODO - Add on referral code to the URL???
		
		Intent intent = new Intent( Intent.ACTION_SEND );
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_SUBJECT, "Come play Bitcoin Video Casino");
		intent.putExtra(Intent.EXTRA_TEXT, "Check out the Bitcoin Video Casino Android app. https://bitcoinvideocasino.com/android");
		startActivity(Intent.createChooser(intent,"Share Bitcoin Video Casino with friends"));
		
		// TB TODO - Might be cool to include an image as well, instead of just text
		// http://stackoverflow.com/questions/10271993/android-action-send-put-extra-stream-from-res-drawable-folder-causes-crash
		/*
		intent.setType("image/*");
		intent.putExtra(Intent.EXTRA_SUBJECT, "Some subject");
		intent.putExtra(Intent.EXTRA_TEXT, "Some message");
		startActivity(Intent.createChooser(intent,"Share with friends"));
		*/
	}

	public void onVideoPoker(View button) {
		Intent intent = new Intent(this, VideoPokerActivity.class);
		intent.putExtra(GameActivity.KEY_USE_FAKE_CREDITS, false);
		startActivity(intent);
	}

    public void onSlots(View button) {
        Intent intent = new Intent(this, SlotsActivity.class);
		intent.putExtra(GameActivity.KEY_USE_FAKE_CREDITS, false);
        startActivity(intent);
    }
    public void onDice(View button) {
        Intent intent = new Intent(this, DiceActivity.class);
		intent.putExtra(GameActivity.KEY_USE_FAKE_CREDITS, false);
        startActivity(intent);
    }

	public void onBlackjack(View button) {
		Intent intent = new Intent(this, BlackjackActivity.class);
		intent.putExtra(GameActivity.KEY_USE_FAKE_CREDITS, false);
		startActivity(intent);
	}

	public void onCashOut(View button) {
		Intent intent = new Intent(this, CashOutActivity.class);
		startActivity(intent);
	}

	public void onDeposit(View button) {
		Intent intent = new Intent(this, DepositActivity.class);
		startActivity(intent);
	}

	public void onSettings(View button) {
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
	}

	class MainCreateAccountTask extends CreateAccountTask {
		
		MainCreateAccountTask(CommonActivity a) {
			super(a);
		}
		
		@Override
		public void onSuccess(JSONCreateAccountResult result) {
			super.onSuccess(result);
			updateValues();
			
			mMixpanel.track("create_account", null);
		}
	}

	class MainNetBalanceTask extends NetBalanceTask {

		MainNetBalanceTask(CommonActivity a) {
			super(a);
		}

		public void onSuccess(JSONBalanceResult result) {
			super.onSuccess(result);
			updateValues();
		}
	}
	
    class NetAndroidAppVersionTask extends NetAsyncTask<Long, Void, JSONAndroidAppVersionResult> {
    	
    	NetAndroidAppVersionTask( CommonActivity a ) { super(a); }
    	
    	public JSONAndroidAppVersionResult go(Long...v) throws IOException {
    		mShowDialogOnError = false;
    		return mBVC.getAndroidAppVersion();
    	}
    	void showNewVersionDialog( String oldVersion, String newVersion )
    	{
			AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
			String message = String.format("A new version of Bitcoin Video Casino is now available!\nYour version: %s\nNew version: %s\n\nDo you want to download it?", oldVersion, newVersion);
			builder.setMessage(message)
			       .setCancelable(false)
			       .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			               dialog.cancel();
			           }
			       })
			       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			               dialog.cancel();
			               BitcoinVideoCasino bvc = BitcoinVideoCasino.getInstance(mActivity);
			               String url = "https://bitcoinvideocasino.com/android?account_key=" + bvc.mAccountKey;
			               Intent intent = new Intent( Intent.ACTION_VIEW );
			               intent.setData( Uri.parse(url));
			               startActivity(intent);
			           }
			       });
			AlertDialog alert = builder.create();	        
			alert.show();
    	}
    	public void onSuccess(JSONAndroidAppVersionResult result) {
    		Log.v(TAG, "This is version: " + CommonApplication.APPLICATION_VERSION);
    		Log.v(TAG, "Most recent version is: " + result.version);
    		
    		if( !CommonApplication.APPLICATION_VERSION.contentEquals(result.version)) {
				// Don't ask again for a while 
				SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mActivity);
				long now = System.currentTimeMillis()/1000;
				SharedPreferences.Editor editor = sharedPref.edit();
				editor.putLong(SETTING_ANDROID_APP_VERSION_CHECK, now);
				editor.commit(); 
	    		
				showNewVersionDialog( CommonApplication.APPLICATION_VERSION, result.version); 
    		}
    	}
    }

}
