package com.bitcoinvideocasino.app;

import java.util.logging.ConsoleHandler;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
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
import android.view.animation.Animation;
import android.view.animation.AlphaAnimation;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.AnimationSet;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import com.bitcoinvideocasino.lib.*;
import com.bitcoinvideocasino.R;

public class IntroActivity extends CommonActivity {

	ImageView mTitleImage;
	boolean mStartedNewIntent;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_intro);
		mTitleImage = (ImageView) findViewById(R.id.title_image);
		mStartedNewIntent = false;
	}
	
	@Override
	protected void onDestroy() {
	    super.onDestroy();
	}
	
	private void animate(final ImageView imageView, final int images[], final int imageIndex) {

		//imageView <-- The View which displays the images
		//images[] <-- Holds R references to the images to display
		//imageIndex <-- index of the first image to show in images[] 
		//forever <-- If equals true then after the last image it starts all over again with the first image resulting in an infinite loop. You have been warned.

		int fadeInDuration = 500; // Configure time values here
		int timeBetween = 2000;
		int fadeOutDuration = 500;
		
		imageView.setVisibility(View.INVISIBLE);    //Visible or invisible by default - this will apply when the animation ends
		imageView.setImageResource(images[imageIndex]);
		
		Animation fadeIn = new AlphaAnimation(0, 1);
		fadeIn.setInterpolator(new DecelerateInterpolator()); // add this
		fadeIn.setDuration(fadeInDuration);
		
		Animation fadeOut = new AlphaAnimation(1, 0);
		fadeOut.setInterpolator(new AccelerateInterpolator()); // and this
		fadeOut.setStartOffset(fadeInDuration + timeBetween);
		fadeOut.setDuration(fadeOutDuration);
		
		AnimationSet animation = new AnimationSet(false); // change to false
		animation.addAnimation(fadeIn);
		animation.addAnimation(fadeOut);
		animation.setRepeatCount(1);
		imageView.setAnimation(animation);
		
		animation.setAnimationListener(new AnimationListener() {
		    public void onAnimationEnd(Animation animation) {
		    	/*
		        if (images.length - 1 > imageIndex) {
		            animate(imageView, images, imageIndex + 1,forever); //Calls itself until it gets to the end of the array
				}
				else {
				    if (forever == true){
					    animate(imageView, images, 0,forever);  //Calls itself to start the animation all over again in a loop if forever = true
			        }
			    }
			    */
		    }
			public void onAnimationRepeat(Animation animation) {
			    // TODO Auto-generated method stub
			}
			public void onAnimationStart(Animation animation) {
			    // TODO Auto-generated method stub
			}
		});
	}
	
	public void onContainer(View button) {
		startMainActivity();
	}
	
	private void startMainActivity() {
		if( !mStartedNewIntent ) {
			Intent intent = new Intent(this, MainActivity.class);
			startActivity(intent);		
		}
	}

	private void fadeIn() {
		final View view = findViewById(R.id.container);

        Animation a = AnimationUtils.loadAnimation( this, android.R.anim.fade_in );
        a.setDuration(500);
        a.setAnimationListener(new AnimationListener() {

            public void onAnimationEnd(Animation animation) {
            	view.setVisibility(View.VISIBLE);
				mHandler.postDelayed( new Runnable() {
					public void run() {
						fadeOut();
					}	
				}, 1500);
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationStart(Animation animation) {
            }

        });
        view.startAnimation(a);		
	}
	
	private void fadeOut() {
		final View view = findViewById(R.id.container);

        Animation a = AnimationUtils.loadAnimation( this, android.R.anim.fade_out );
        a.setDuration(500);
        a.setAnimationListener(new AnimationListener() {

            public void onAnimationEnd(Animation animation) {
            	view.setVisibility(View.INVISIBLE);
            	startMainActivity();
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationStart(Animation animation) {
            }

        });
        view.startAnimation(a);		
	}
	@Override
	public void onResume() {
		super.onResume();
		mHandler.postDelayed( new Runnable() {
			public void run() {
				fadeIn();
			}	
		}, 1000);
		//int[] images = { R.drawable.ref_logo };
		//animate(mTitleImage, images, 0);
		
	}
	
    @Override
    public void onPause() {
    	super.onPause();
    }
    

}
