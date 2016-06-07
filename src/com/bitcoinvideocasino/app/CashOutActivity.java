
package com.bitcoinvideocasino.app;

import java.io.IOException;


import java.util.logging.ConsoleHandler;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Button;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Typeface;
import android.content.Intent;
import android.view.View;
import android.util.Log;
import com.google.zxing.integration.android.IntentResult;
import com.google.zxing.integration.android.IntentIntegrator;
import android.widget.Toast;
import android.widget.EditText;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuff;
import android.graphics.ColorFilter;
import com.bitcoinvideocasino.lib.*;
import com.bitcoinvideocasino.R;


public class CashOutActivity extends CommonActivity {
	
	TextView mTitle;
	TextView mBalance;
	TextView mUnconfirmedWarning;
	EditText mWithdrawAddress;
	EditText mAmount;
	Button mCashOut;
	Button mScanQRCode;
	NetWithdrawTask mNetWithdrawTask;
	CashOutNetBalanceTask mCashOutNetBalanceTask;
	
	final static String TAG = "CashOutActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);  
        setContentView(R.layout.activity_cashout);
        
		mTitle = (TextView) findViewById(R.id.title);
		mBalance = (TextView) findViewById(R.id.balance);
		mUnconfirmedWarning = (TextView) findViewById(R.id.unconfirmed_warning);
		mWithdrawAddress = (EditText) findViewById(R.id.withdraw_address);
		mAmount = (EditText) findViewById(R.id.withdraw_amount);
		mCashOut = (Button) findViewById(R.id.cashout_button);
		mScanQRCode = (Button) findViewById(R.id.scan_qr_code_button);
		Typeface robotoLight = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
		Typeface robotoBold = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Bold.ttf");

		mTitle.setText("Cash Out");
		mTitle.setTypeface(robotoLight);
		
		BitcoinVideoCasino bvc = BitcoinVideoCasino.getInstance(this);
		if( bvc.mLastWithdrawAddress != null ) {
			mWithdrawAddress.setText(bvc.mLastWithdrawAddress);
		}
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	
		mCashOutNetBalanceTask = new CashOutNetBalanceTask(this); 
    	mCashOutNetBalanceTask.execute( Long.valueOf(0) ); 
    	updateValues();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    public void onEverything(View button) {
    	// TB TODO - Disable/enable the amount area
    	// Put in the full amount of the user's balance!
    	CheckBox checkbox = (CheckBox)button;
    	if( checkbox.isChecked() ) {
    		BitcoinVideoCasino bvc = BitcoinVideoCasino.getInstance(this);
    		mAmount.setEnabled(false);
    		mAmount.setText( Bitcoin.longAmountToString(bvc.mIntBalance) );
    		// Gray background
    		mAmount.getBackground().setColorFilter( new PorterDuffColorFilter( R.color.edittext_disabled, Mode.MULTIPLY)); 
    	}
    	else {
    		mAmount.setEnabled(true); 
    		mAmount.setText("");
    		mAmount.getBackground().setColorFilter( null );
    	}
    }
    public void onScanQRCode(View button) {
       	IntentIntegrator integrator = new IntentIntegrator(this);
       	integrator.initiateScan(IntentIntegrator.QR_CODE_TYPES);
    }
    
	public void onCashOut(View button) {
		// TB TODO - Do it!
		// TB TODO - Sanity check values!
		// TB TODO - Don't let the use press the button more than once!
		
		mNetWithdrawTask = new NetWithdrawTask(this); 
    	mNetWithdrawTask.execute( Long.valueOf(0) ); 
	}
	
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		Log.v("onActivityResult", "yo");
		//Activity.RESULT_OK;
		IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
		boolean success = false;
		if (result != null) {
			String contents = result.getContents();
			if (contents != null) {
    			Log.v("SCAN_RESULT", contents);
				
				String address = contents; 
				int header = contents.indexOf("bitcoin:");
				if( header >= 0 ) {
					address = contents.substring(header + "bitcoin:".length() );
				}
				
				//showDialog(R.string.result_succeeded, result.toString());
    			mWithdrawAddress.setText( address );
				success = true;
			} 
			else {
				//showDialog(R.string.result_failed, getString(R.string.result_failed_why));
			}
		}
		
		if( !success ) {
			Toast.makeText(this, "Error scanning QR code", Toast.LENGTH_SHORT).show();
		}
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
		
		if( bvc.mUnconfirmed ) {
			mUnconfirmedWarning.setVisibility(View.VISIBLE);
		}
		else {
			mUnconfirmedWarning.setVisibility(View.GONE); 
		}
	}

	
    class NetWithdrawTask extends NetAsyncTask<Long, Void, JSONWithdrawResult> {
    	//AlertDialog mAlert;
    	String mStringAmount;
    	String mStringAddress;
    	long mIntAmount;
		ProgressDialog mWaitingProgressDialog;
    	
    	NetWithdrawTask( CommonActivity a ) { 
    		super(a); 
			mStringAmount = mAmount.getText().toString();
			mStringAddress = mWithdrawAddress.getText().toString();
    		mIntAmount = Bitcoin.stringAmountToLong( mStringAmount );
			
			mWaitingProgressDialog = ProgressDialog.show(a, "", "Withdrawing Bitcoins...", true);        
    	}
    	public void onDone() {
    		//mAlert.dismiss();
    		mWaitingProgressDialog.dismiss();
    	}
    	
    	public JSONWithdrawResult go(Long...v) throws IOException {
    		/*
    		float floatAmount = Float.parseFloat(mAmount.getText().toString());
    		// TB TODO - Use Bitcoin class!
    		int intAmount = (int)(floatAmount * 100000000);
    		*/
    		Log.v("Withdraw", mStringAmount );
    		Log.v("Withdraw", Long.toString(mIntAmount) );
    		return mBVC.getWithdraw( mStringAddress, mIntAmount );
    	}
    	public void onSuccess(JSONWithdrawResult result) {
    		if( result.result == true ) {
    			BitcoinVideoCasino bvc = BitcoinVideoCasino.getInstance(mActivity);
    			// TB - Can not use result.intamount since it does not include the fee that we charge
    			// TB TODO - We really should just get rid of that fee entirely.
    			// bvc.mIntBalance -= result.intamount;
    			bvc.mIntBalance -= mIntAmount;
    			updateValues();
    			
	    		//Toast.makeText(mActivity, "Success!", Toast.LENGTH_SHORT).show(); 
				AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
				builder.setMessage( String.format("%s BTC was sent to:\n%s", mStringAmount, mStringAddress))
					   .setTitle("Success")
				       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				               // Remember this address for next time!
							   SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mActivity);
							   SharedPreferences.Editor editor = settings.edit();
							   editor.putString("last_withdraw_address", mStringAddress);
							   editor.commit(); 
				        	   
				        	   dialog.cancel();
				        	   mActivity.finish();
				           }
				       });
				AlertDialog alert = builder.create();	        
				alert.show(); 
    		}
    		else {
    			String prettyReason = result.reason;
    			if( result.reason.contains("pending_transactions") ) {
    				prettyReason = "One or more of your previous deposits is still not confirmed. Deposit confirmations usually take 10-20 minutes. Please try again later.";
    			}
    			else if( result.reason.contains("bad_address") ) {
    				prettyReason = "Invalid destination address. Please check the Bitcoin address and try again.";
    			}
    			else if( result.reason.contains("balance") || result.reason.contains("amount_too_small") ) {
    				prettyReason = "Invalid withdrawal amount. Please check the amount and try again.";
    			}
    			// TB TODO - Check for reason == pending_transactions and display something nicer
				AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
				builder.setMessage("Reason: " + prettyReason)
					   .setTitle("Unable to cash out")
				       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				        	   dialog.cancel();
				           }
				       });
				AlertDialog alert = builder.create();	        
				alert.show(); 
    		}
    		
    		// Update balance JIC it got inconsistent somehow
			mCashOutNetBalanceTask = new CashOutNetBalanceTask(mActivity); 
	    	mCashOutNetBalanceTask.execute( Long.valueOf(0) ); 
    	}
    	
    }
    
    class CashOutNetBalanceTask extends NetBalanceTask {
    	
		CashOutNetBalanceTask( Activity a ) { super(a); }
	    	
		public void onSuccess(JSONBalanceResult result) {
			super.onSuccess(result); 
			updateValues();
		}
    }
	
	
}
