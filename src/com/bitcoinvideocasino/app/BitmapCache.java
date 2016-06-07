package com.bitcoinvideocasino.app;

import java.util.HashMap;
import android.util.Log;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class BitmapCache {
	HashMap<Integer, Bitmap> mLookup;
	Activity mActivity;
	final static String TAG = "BitmapCache";
	public BitmapCache( Activity a ) {
		// TB TODO - Use SparseArray for better performance?
		mLookup = new HashMap<Integer, Bitmap>();
		mActivity = a;
	}
	
	public void addBitmap( int resID ) {
		
		if( mLookup.containsKey(resID) ) {
			Log.v(TAG, "Skipping addBitmap because " + String.valueOf(resID) + " is already here." );
		}
		// TB TODO - Consider using SoftReference?
		// TB TODO - Out of memory errors?
		Bitmap b = BitmapFactory.decodeResource( mActivity.getResources(), resID );
		//mCardImage.setImageBitmap(b);
		mLookup.put( resID, b );
	}
	/*
	public void addBitmap( String name ) {
		//int resID = mActivity.getResources().getIdentifier("card_"+cardName, "drawable", mActivity.getPackageName());
		int resID = mActivity.getResources().getIdentifier(name, "drawable", mActivity.getPackageName());
	} 
	*/
	
	public Bitmap getBitmap( int resID ) {
		return mLookup.get( resID );
	}
	
	public void clear() {
		// TB TODO - Necessary to call recycle?
		// Does not seem to be necessary.
		/*
		for( HashMap.Entry<Integer, Bitmap> entry : mLookup.entrySet()) {
			entry.getValue().recycle();
		}		
		*/
		mLookup.clear();
	}
}
