package com.bitcoinvideocasino.lib;

import android.app.Activity;	

import java.util.List;
import java.util.ArrayList;
import android.os.Handler;
import android.util.Log;
import android.os.Bundle;
import com.bitcoinvideocasino.lib.*;


public class CommonActivity extends Activity {
	public Handler mHandler;
	static String TAG = "CommonActivity";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); 
        mHandler = new Handler();
    }
	
    @Override
    public void onPause() {
    	super.onPause();
		((CommonApplication)this.getApplication()).abortNetAsyncTasks();
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    }
    

}
