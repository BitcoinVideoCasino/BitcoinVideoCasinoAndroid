package com.bitcoinvideocasino.lib;

import java.io.IOException;
import java.util.logging.ConsoleHandler;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
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
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class NetBalanceTask extends NetAsyncTask<Long, Void, JSONBalanceResult> {
    	
	public NetBalanceTask( Activity a ) { super(a); }
    	
	public JSONBalanceResult go(Long...v) throws IOException {
		return mBVC.getBalance();
	}
	public void onUserConfirmNewBalance() {
		// Deposit activity uses this to pop the user back to the main screen
	}
	public void onSuccess(JSONBalanceResult result) {
		
		//Log.v("NetBalanceTask", "Success!");
		
		mBVC.mIntBalance = result.intbalance;
		mBVC.mFakeIntBalance = result.fake_intbalance;
		mBVC.mUnconfirmed = result.unconfirmed;
		
		if( result.notify_transaction != null ) {
			String s = "Received " + result.notify_transaction.amount + " BTC\n\n";
			s += "Transaction ID: " + result.notify_transaction.txid;	
			AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
			builder.setMessage( s )
				   .setTitle("New Deposit")
			       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			                dialog.cancel();
			                onUserConfirmNewBalance();
			           }
			       });
			AlertDialog alert = builder.create();	        
			alert.show(); 
		}
	}
}
