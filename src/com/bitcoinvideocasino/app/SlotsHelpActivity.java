package com.bitcoinvideocasino.app;

import java.io.IOException;
import java.util.Random;


import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ImageButton;
import android.app.Dialog;
import android.widget.Button;
import android.widget.Spinner;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.view.SurfaceHolder;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.Point;
import java.util.Map;
import java.util.ArrayList;
import android.view.Window;
import android.view.WindowManager;
import de.marcreichelt.android.RealViewSwitcher;
import com.bitcoinvideocasino.lib.*;
import com.bitcoinvideocasino.R;

public class SlotsHelpActivity extends Activity {
	
    RealViewSwitcher mViewSwitcher;
    final static String TAG = "SlotsHelpActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState );
        Log.v(TAG, "SlotsHelpActivity onCreate");

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView( R.layout.activity_slots_help );

        /*
        mViewSwitcher = new RealViewSwitcher(getApplicationContext());
        int[] imageResources = new int[] { R.drawable.slt_help_howtoplay, R.drawable.slt_help_paytables1, R.drawable.slt_help_paytables2 };
        for( int i = 0; i < imageResources.length; i++ ) {
            ImageView view = new ImageView(getApplicationContext());
            view.setImageResource( imageResources[i] );
            mViewSwitcher.addView(view);
        }
        setContentView(mViewSwitcher);
        */
    }

}

