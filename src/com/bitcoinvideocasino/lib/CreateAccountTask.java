package com.bitcoinvideocasino.lib;

import android.app.ProgressDialog;
import android.app.Activity;
import java.io.IOException;
import android.util.Log;
import android.widget.Toast;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class CreateAccountTask extends NetAsyncTask<Long, Void, JSONCreateAccountResult> {

	private ProgressDialog mAlert;
	final static String TAG = "CreateAccountTask";

	public CreateAccountTask(Activity a) {
		super(a);
		mAlert = ProgressDialog.show(mActivity, "", "Creating anonymous account...", true);        
	}

	public JSONCreateAccountResult go(Long... v) throws IOException {
		return mBVC.getCreateAccount();
	}

	public void onDone() {
		mAlert.cancel();
	}

	public void onSuccess(JSONCreateAccountResult result) {
		// TB TODO - This will crash if result.account_key is null for some reason.
		Log.v(TAG, "New account created!");
		Log.v(TAG, result.account_key);

		Toast.makeText(mActivity, "New account created!", Toast.LENGTH_SHORT).show();
		setAccountKeyInPreferences(mActivity, result.account_key);
	}
	
	public static void setAccountKeyInPreferences( Activity a, String key ) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(a);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("account_key", key);
		editor.putString("deposit_address", null);
		editor.putString("last_withdraw_address", null);
		editor.commit(); 
		
		BitcoinVideoCasino bvc = BitcoinVideoCasino.getInstance(a);
		bvc.mIntBalance = -1;
		bvc.mFakeIntBalance = -1;
	}
}
