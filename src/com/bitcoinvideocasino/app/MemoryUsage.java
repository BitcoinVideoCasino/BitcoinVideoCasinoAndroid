package com.bitcoinvideocasino.app;

import android.app.Activity;
import android.app.ActivityManager.MemoryInfo;
import android.app.ActivityManager;

public class MemoryUsage {
	static public long getFreeMemory(Activity a) {
		MemoryInfo mi = new MemoryInfo();
		ActivityManager activityManager = (ActivityManager) a.getSystemService(Activity.ACTIVITY_SERVICE);
		activityManager.getMemoryInfo(mi);
		long availableMegs = mi.availMem / 1048576L;
		return availableMegs;
	}
}

