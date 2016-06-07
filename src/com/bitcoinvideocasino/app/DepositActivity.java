package com.bitcoinvideocasino.app;

import java.io.IOException;
import com.bitcoinvideocasino.lib.*;
import com.bitcoinvideocasino.R;


import java.util.logging.ConsoleHandler;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Button;
import android.graphics.Typeface;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.view.View;
import android.util.Log;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import java.util.List;
import android.content.pm.ResolveInfo;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Context;
import android.widget.Toast;
import android.os.Handler;
import java.lang.Runnable;
import android.os.Handler;


public class DepositActivity extends CommonActivity {
	
	TextView mBalance;
	TextView mUnconfirmedWarning;
	TextView mTitle;
	TextView mDepositAddress;
	Button mExternalApp;
	final static String TAG = "DepositActivity";
	NetBitcoinAddressTask mNetBitcoinAddressTask;
	final static int REQUEST_CODE_DEPOSIT_APP = 0;
	DepositNetBalanceTask mNetBalanceTask;
	
	boolean mWillReturnFromDeposit;
	ProgressDialog mWaitingForDepositAlert;
	
	public void updateValues()
	{

		BitcoinVideoCasino bvc = BitcoinVideoCasino.getInstance(this);
		if( bvc.mIntBalance != -1 ) {
			String btc = Bitcoin.longAmountToStringChopped(bvc.mIntBalance);
			mBalance.setText(String.format("Balance: %s BTC", btc));
		}
		else {
			mBalance.setText(String.format("BTC"));
		}
		
		if( bvc.mUnconfirmed ) {
			mUnconfirmedWarning.setVisibility(View.VISIBLE);
		}
		else {
			mUnconfirmedWarning.setVisibility(View.GONE); 
		}
	
		String address = bvc.mDepositAddress;
		if( address == null ) {
			mDepositAddress.setText("Connecting...");
		}
		else {
			mDepositAddress.setText(address);
		}
		// TB TODO - Enable/disable external app button depending on whether an address exists yet
		// TB TODO - Enable/disable external app button if no bitcoin intent handler app exists
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);  
        setContentView(R.layout.activity_deposit);
        
		mBalance = (TextView) findViewById(R.id.balance);
		mUnconfirmedWarning = (TextView) findViewById(R.id.unconfirmed_warning);
		mTitle = (TextView) findViewById(R.id.title);
		mDepositAddress = (TextView) findViewById(R.id.deposit_address);
		mExternalApp = (Button) findViewById(R.id.external_app_button);
		Typeface robotoLight = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
		Typeface robotoBold = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Bold.ttf");
		
		mNetBitcoinAddressTask	= null;
		mNetBalanceTask = null;
		mTitle.setText("Deposit");
		mTitle.setTypeface(robotoLight);
		
		// TB TODO - Should store/retrieve this address in the preferences storage, and then just
		// set it to null whenever the user account changes (indicating that we must get the deposit address)
		updateValues();
		
		// Sucky hack
		mWillReturnFromDeposit = false;
		mWaitingForDepositAlert	= null;
    }
    
	private Runnable mTimeUpdateRunnable = new Runnable() {
		public void run() {
			timeUpdate();
		} 
	}; 
    
	public void timeUpdate() {
		
		BitcoinVideoCasino bvc = BitcoinVideoCasino.getInstance(this);
		if (bvc.mAccountKey != null) {
			Log.v(TAG, bvc.mAccountKey);
			mNetBalanceTask = new DepositNetBalanceTask(this);
			mNetBalanceTask.executeParallel(Long.valueOf(0));
		}
		
		// Every 5 seconds
		final int timeUpdateDelay = 5000;
		mHandler.postDelayed( mTimeUpdateRunnable, timeUpdateDelay );
	}

    
	@Override
    public void onResume() {
    	super.onResume();
    	
    	BitcoinVideoCasino bvc = BitcoinVideoCasino.getInstance(this);
    	if( bvc.mDepositAddress == null || bvc.mDepositAddress.length() < 10 ) {
	    	mNetBitcoinAddressTask = new NetBitcoinAddressTask(this); 
			mNetBitcoinAddressTask.executeParallel(Long.valueOf(0));
    	}
    	
    	// Sucky hack since we can't rely on onActivityResult() being called after a deposit is made. :(
    	Log.v(TAG, "onResume: " + mWillReturnFromDeposit);
		if( mWillReturnFromDeposit ) {
			mWaitingForDepositAlert = ProgressDialog.show(this, "", "Checking for new deposit...\nThis usually takes just a few seconds.", true);        
			mWillReturnFromDeposit = false;
			
			// If the alert hasn't been dismissed because of a new deposit, just kill it after a while so that the user is not stuck.
			final int delay = 7000;
			mHandler.postDelayed( new Runnable() {
				public void run() {
					if( mWaitingForDepositAlert != null ) {
						Log.v(TAG, "Dismissing2");
						mWaitingForDepositAlert.dismiss();
					}
				}
			}, delay);
		}
    	
		timeUpdate();
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	Log.v(TAG, "OnPause"); 
    	mHandler.removeCallbacks(mTimeUpdateRunnable);
    }
    
    public void handleBlockchainCrash() {
    	/*
    	final Activity that = this;
		Handler handler = new Handler();
		handler.postDelayed( new Runnable() {
			public void run() {
				AlertDialog.Builder builder = new AlertDialog.Builder(that);
				builder.setMessage("Blockchain seems to have crashed. Try setting up your Blockchain account from the app before trying to deposit coins.")
				       .setCancelable(false)
				       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
				    	   public void onClick(DialogInterface dialog, int id) {
				               dialog.cancel();
				           }
				       });
				AlertDialog alert = builder.create();	        
				alert.show(); 
			}
		}, 500 ); 
		*/
    }
    
    
	// TB TODO - This is getting immediately called when startActivityForResult is called... because android:launchMode in the bitcoin app is android:launchMode="singleTask",
	// which apparently causes this. So could just run the balance checker or something...
    // So for now I'm just calling startActivity(), which doesn't cause onPause -> onResume -> onPause -> SendCoinsExternalActivity -> onResume...
    @Override
    public void onActivityResult( int requestCode, int resultCode, Intent data) {
    	if( requestCode != REQUEST_CODE_DEPOSIT_APP ) {
    		Log.e(TAG, "Got a activity request code that we didn't specify!"); 
    		return;
    	}
    	if( resultCode == RESULT_CANCELED ) {
    		Log.v(TAG, "resultCode == RESULT_CANCELED!");
	    	handleBlockchainCrash();
    		return;
    	}
		Log.v(TAG, "resultCode == " + resultCode);
		
		// TB TODO - Finish this activity so we pop back to the main screen (on successful deposit)
    }
    
    public void handleMissingExternalApp() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("No Android Bitcoin app was found on your device. Would you like to download the Blockchain app from Google Play?")
		       .setCancelable(false)
		       .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		               dialog.cancel();
		           }
		       })
		       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
		    	   public void onClick(DialogInterface dialog, int id) {
		               dialog.cancel();
		               String url = "https://play.google.com/store/apps/details?id=piuk.blockchain.android";
		               Intent intent = new Intent( Intent.ACTION_VIEW );
		               intent.setData( Uri.parse(url));
		               intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
		               startActivity(intent);
		           }
		       });
		AlertDialog alert = builder.create();	        
		alert.show();
    	
    }
    
	@SuppressLint("NewApi")
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void onDepositAddress(View button) {
		String address = BitcoinVideoCasino.getInstance(this).mDepositAddress;
		
		// http://stackoverflow.com/questions/238284/how-to-copy-text-programatically-in-my-android-app
		if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
			// TB - Old crappy way
		    android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		    clipboard.setText( address );
		} else {
			// TB - New hotness
		    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE); 
		    android.content.ClipData clip = android.content.ClipData.newPlainText("Bitcoin Address", address);
		    clipboard.setPrimaryClip(clip);
		}
		
		Toast.makeText(this, "Deposit address has been copied to your clipboard", Toast.LENGTH_SHORT).show();
	}
	
	public void onExternalApp(View button) {
		// TB TODO - Verify that an external app actually exists!!!
		// TB TODO - Get correct deposit address from service call
		String address = BitcoinVideoCasino.getInstance(this).mDepositAddress;
    	if( address == null ) {
			return;
		}
		String url = "bitcoin://" + address;
		Intent intent = new Intent( Intent.ACTION_VIEW );
		intent.setData( Uri.parse(url));
        //intent.setFlags( Intent.FLAG_ACTIVITY_SINGLE_TOP );

		try {
			// TB TODO - This will immediately call onActivityResult because android:launchMode in the bitcoin app is android:launchMode="singleTask",
			// which apparently causes this. So could just run the balance checker or something...
			// So for now just call startActivity and hack in a progress alert when you return... sucky...
			//startActivityForResult( intent, REQUEST_CODE_DEPOSIT_APP );
			startActivity( intent );

			// TB - This hack sucks, since we don't know what the user actually did in the other app. If he didn't deposit anything, we won't know!
			mWillReturnFromDeposit = true;
		}
		catch( ActivityNotFoundException e ) {
			handleMissingExternalApp(); 
		}
			
	}
	
    class NetBitcoinAddressTask extends NetAsyncTask<Long, Void, JSONBitcoinAddressResult> {
    	
    	ProgressDialog mAlert;
    	
    	NetBitcoinAddressTask( CommonActivity a ) { 
    		super(a); 
			Log.v(TAG, "NetBitcoinAddressTask go!"); 
			mAlert = ProgressDialog.show(a, "", "Retrieving deposit address...", true);        
    	}
    	public void onDone() {
    		mAlert.cancel();
    	}
    	
    	public JSONBitcoinAddressResult go(Long...v) throws IOException {
			Log.v(TAG, "deposit check go!"); 
    		return mBVC.getBitcoinAddress();
    	}
    	public void onSuccess(JSONBitcoinAddressResult result) {
			Log.v(TAG, "deposit check success!"); 
			// TB TODO - Error checking!
    		mBVC.mDepositAddress = result.address;

            // TB - Don't remember the deposit address, so we don't run into the same problem of people
            // being stuck with an old address that is no longer valid.
            /*
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mActivity);
			SharedPreferences.Editor editor = settings.edit();
			editor.putString("deposit_address", result.address);
			editor.commit();
			*/
		
			updateValues();
    	}
    	public void onError(JSONBitcoinAddressResult r) {
    		mShowDialogOnError = false;
			GameActivity.handleCriticalConnectionError(mActivity);
    	}
    }
    
	class DepositNetBalanceTask extends NetBalanceTask {

		DepositNetBalanceTask(CommonActivity a) {
			super(a);
		}

		public void onSuccess(JSONBalanceResult result) {
			
			if( mWaitingForDepositAlert	!= null ) {
				if( result.notify_transaction != null ) {
					// Ditch the progress dialog so that the new deposit box can appear instead.
					Log.v(TAG, "Dismissing!");
					mWaitingForDepositAlert.dismiss();
				}
			}
			
			super.onSuccess(result);
			updateValues(); 
		}
		
		@Override
   		public void onDone() {
			// TB TODO - Kill waiting for depositalert!
		}
		
		@Override
		public void onUserConfirmNewBalance()
		{
			// Get out of the deposit screen now that the deposit is done...
			mActivity.finish();
		}
	}
}
