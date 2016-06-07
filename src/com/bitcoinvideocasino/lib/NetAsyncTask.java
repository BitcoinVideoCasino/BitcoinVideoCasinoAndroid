package com.bitcoinvideocasino.lib;

import java.io.IOException;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.app.Activity;
import android.os.Build;

abstract public class NetAsyncTask<Params, Progress, Result extends JSONBaseResult> extends AsyncTask<Params, Progress, Result> {
    	
	public BitcoinVideoCasino mBVC;
	public Activity mActivity;
    public Context mContext;
	private Exception mCaughtException;
	protected boolean mShowDialogOnError;
	private boolean mAborted;
	static final String TAG = "NetAsyncTask";
    protected boolean mAllowAbort;
	
	// If an error dialog is already up, then don't show more.
	// Otherwise you may get an endless stream of dialog boxes that keep popping up.
	static boolean mIsErrorDialogVisible = false;
	
	private NetAsyncTask() {
		// You should use NetAsyncTask(activity) instead 
	}

	public NetAsyncTask( Activity activity ) {
        mContext = activity;
	    mActivity = activity;
		mCaughtException = null;
		mShowDialogOnError = true;
		mAborted = false;
        mBVC = BitcoinVideoCasino.getInstance(mContext);
        mAllowAbort = true;
	}
    // TB TODO - This is retarded... Is there no way to make the service stuff play nice?
    public NetAsyncTask( Activity activity, Context ctx ) {
        mContext = ctx;
        mActivity = activity;
        mCaughtException = null;
        mShowDialogOnError = true;
        mAborted = false;
        mBVC = BitcoinVideoCasino.getInstance(mContext);
        mAllowAbort = true;
    }

	public void abort() {
        // TB TEST - If the activity is interrupted during a game task (phone call, etc) and then the user returns, the result
        // will never get shown. Perhaps it's OK for game tasks to continue updating the screen even after the activity is paused?
        // For now, just slots pull is doing this.
        if( mAllowAbort ) {
            mAborted = true;
            this.cancel(true);
        }
	}
	
	// Need this or only one network call will go out at a time (which can be laggy)
	// In later versions of the API execute() is apparently serial (only one background thread).
	// For network tasks that need a fast response, use this. Otherwise the user may be stuck waiting
	// for some boring net task like balance check or update before his network command will go out.
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void executeParallel(Params... p) {
    	//mNetBitcoinAddressTask.execute( Long.valueOf(0) ); 
    	if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB) {
	    	//super.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Long.valueOf(0));
	    	super.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, p );
    	}
		else {
	    	super.execute( p );
    	} 
	}
    protected void handleError(Result result, String message)
	{
		onError(result);
		
    	// Some network errors (balance check) are not important enough to show a dialog box for.
		if( !mShowDialogOnError ) {
			return;
		}

        if( mActivity != null ) {
            if( !NetAsyncTask.mIsErrorDialogVisible ) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                builder.setMessage(message)
                       .setCancelable(false)
                       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                           public void onClick(DialogInterface dialog, int id) {
                               mIsErrorDialogVisible = false;
                               dialog.cancel();
                           }
                       });
                AlertDialog alert = builder.create();
                mIsErrorDialogVisible = true;
                alert.show();
            }
        }

	}
    	
	// Called in background thread
	// There is no need to check the result for general errors or to check for network exceptions...
	abstract public Result go(Params...v) throws IOException;
    	
	// Result is guaranteed to be successful if you get here.
	// This will never get called if there's an error.
	abstract public void onSuccess(Result r);
	public void onError(Result r) { }
	
	// Always called when the background task is finished, right before onSuccess()
	public void onDone() { }
    	
	// TB - This is now final because you should implement go() and done() as the callbacks, not doInBackground() and onPostExecute().
	final  protected Result doInBackground(Params...v) {
        if( mActivity != null ) {
            ((CommonApplication)mActivity.getApplication()).addNetAsyncTask(this);
        }
		Result result = null;
        try {
        	result = go(v);
        }
        catch( Exception ex ) {
        	// TB TODO - Are there any exceptions that we should just let slide? Before we were only catching IOException... why was that?
	    	mCaughtException = ex;
			ex.printStackTrace(); 
        }
        return result;
	}
	
	final  protected void onPostExecute(Result result) {
    	if( mAborted ) {
    		return;
    	}
    	
		// Give implementing class a callback that always is called, even if there was an error.
        if( mActivity != null ) {
            ((CommonApplication)mActivity.getApplication()).removeNetAsyncTask(this);
        }
		onDone();
		
		// Check result validity
		if( mCaughtException != null ) {
			// TB TODO - Probably eventually need to differentiate between different kinds of exceptions.
			handleError(result, "Unable to connect. Please check your internet connection."); 
		} 
		else if( result == null ) { 
			// TB TODO - What else could cause result to be null?
			handleError(result, "Unable to get result from server."); 
		}
        else if( result.status != null && result.status.equalsIgnoreCase("error") ) {
            if( result.message.equalsIgnoreCase("invalid_account_key") ) {
                handleError(result, "This account has been disabled. Please create a new account from the settings menu, or contact us at admin@bitcoinvideocasino.com to restore your account. We are very sorry for the inconvenience." );
            }
            else {
                handleError(result, "Unable to get result from server. Please try again. Message is: " + result.message);
            }
        }
        // TB - Can not handle all errors here, since messages like need_seed must be handled by the game itself!
        /*
        else if( result.error != null ) {
            handleError(result, String.format("Error from server: %s", result.error) );
        }
        */
		// Maybe server return codes should just be checked in NetTask's onSuccess, while onError
		// is used for network screwing up and things like that
		/*
		else if( !result.isSuccess() ) {
			// TB TODO - Should all non-success errors be handled here? Or should that still be up to each implementation?
			// Maybe makes more sense to just have all errors in here?
			// showErrorDialog( result.message + " (result code " + result.result_code + ")" );
			handleError(result, "result.isSuccess() returned false"); 
		}
		*/
		else {
			onSuccess(result);
		}
	}
    	
}
    
