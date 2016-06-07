package com.bitcoinvideocasino.app;

import java.io.IOException;


import android.content.Context;
import android.os.Bundle;
import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.view.View.OnClickListener;
import android.view.MotionEvent;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageButton;
import android.app.Dialog;
import android.widget.Button;
import android.widget.Spinner;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.graphics.Color;

import java.util.Map;

import com.bitcoinvideocasino.lib.*;
import com.bitcoinvideocasino.R;

public class DiceActivity extends GameActivity {
	
	class DiceGameState extends GameState {
		final static public int WAIT_USER_THROW = 0;
	}
	class AutoStrategy {
		final static public int REPEAT_BET = 0;
        final static public int MARTINGALE = 1;
	}
    class AutoTarget {
        final static public int HIGH = 0;
        final static public int LOW = 1;
    }
    class ThrowHint {
        final static public int NONE = 0;
        final static public int HIGH = 1;
        final static public int LOW = 2;
    }
    class LastGameResult {
        final static public int NOTHING = 0;
        final static public int WIN = 1;
        final static public int LOSE = 2;
    }

    final static int MIN_BET = 1;
    final static int MAX_BET = 1000;

    class DirtyControls {
        boolean mPayoutEditText;
        boolean mPayoutSeekBar;
        boolean mChanceEditText;
        boolean mChanceSeekBar;
        boolean mAmountEditText;
        boolean mAmountSeekBar;
        boolean mProfitEditText;
    }
    DirtyControls mDirtyControls;

    Dice mDice;
    int mAutoStrategy;
	int mAutoSpeed;
    int mAutoTarget;

    private TextView mJackpot5Text;
    private TextView mJackpot6Text;

    private LinearLayout mLuckyNumberActual;
    private TextView mLuckyNumberDirection;
    private TextView mLuckyNumberGoal;

    private TextView[] mLuckyNumberActuals;

    private SeekBar mPayoutSeekbar;
    private EditText mPayoutEditText;
    private SeekBar mChanceSeekbar;
    private EditText mChanceEditText;
    private SeekBar mAmountSeekbar;
    private EditText mAmountEditText;
    private EditText mProfitEditText;
    private int mAmountValue;
    private double mPayoutValue;
    private double mChanceValue;
    private double mProfitValue;
    private String mTargetValue;
    private View mErrorContainer;
    private View mResultContainer;
    private TextView mErrorText;
    private ImageButton mAutoButton;

    private ImageButton mRollHighButton;
    private ImageButton mRollLowButton;
    private TextView mRollHighButtonGoal;
    private TextView mRollLowButtonGoal;

    private NetRulesetTask mNetRulesetTask;
	private NetReseedTask mNetReseedTask;
	private NetUpdateTask mNetUpdateTask;
    private NetThrowTask mNetThrowTask;
    public ShowLuckyNumberRunnable mShowLuckyNumberRunnable;

	final private String TAG = "DiceActivity";

	private int mSoundCoinPay;
	private int mSoundBoop;
	private int mSoundWin;
    private int mSoundWinJackpot;
    JSONDiceThrowResult mThrowResult;
    private JSONDiceRulesetResult mRuleset;

    // These get calculated whenn the ruleset is returned.
    private double mRulesetMaximumChance;
    private double mRulesetMinimumChance;

    private int mThrowHint;
    private int mLuckyNumberDirectionBackgroundResource;

    // This is needed to prevent a stack overflow from the controls getting updated by other controls,
    // and then updating other controls indefinitely.
    private boolean mIsInsideUpdateControls;
    private boolean mIsUserInputError;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_dice);
        
        BitcoinVideoCasino bvc = BitcoinVideoCasino.getInstance(this);
        mTimeUpdateDelay = 50;
        mCreditBTCValue = Bitcoin.stringAmountToLong("0.0001");

        mGameState = DiceGameState.WAIT_USER_THROW;
		mDice = new Dice();
		mAutoStrategy = AutoStrategy.REPEAT_BET;
		mAutoSpeed = AutoSpeed.MEDIUM;
        mAutoTarget = AutoTarget.HIGH;
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        mShowLuckyNumberRunnable = null;
        mThrowHint = ThrowHint.HIGH;
        mIsInsideUpdateControls = false;
        mIsUserInputError = false;
        mDirtyControls = new DirtyControls();

        mJackpot5Text = (TextView) findViewById( R.id.jackpot5 );
        mJackpot6Text = (TextView) findViewById( R.id.jackpot6 );

        mRollHighButton = (ImageButton) findViewById( R.id.roll_high );
        mRollLowButton = (ImageButton) findViewById( R.id.roll_low );
        mRollHighButtonGoal = (TextView) findViewById( R.id.roll_high_goal );
        mRollLowButtonGoal = (TextView) findViewById( R.id.roll_low_goal );


        mAutoButton = (ImageButton) findViewById( R.id.auto_button );

        mLuckyNumberActual = (LinearLayout) findViewById( R.id.lucky_number_actual );
        mLuckyNumberDirection = (TextView) findViewById( R.id.lucky_number_direction );
        mLuckyNumberGoal = (TextView) findViewById( R.id.lucky_number_goal );

        mLuckyNumberActuals = new TextView[7];
        mLuckyNumberActuals[0] = (TextView) findViewById( R.id.lucky_number_actual0 );
        mLuckyNumberActuals[1] = (TextView) findViewById( R.id.lucky_number_actual1 );
        mLuckyNumberActuals[2] = (TextView) findViewById( R.id.lucky_number_actual2 );
        mLuckyNumberActuals[3] = (TextView) findViewById( R.id.lucky_number_actual3 );
        mLuckyNumberActuals[4] = (TextView) findViewById( R.id.lucky_number_actual4 );
        mLuckyNumberActuals[5] = (TextView) findViewById( R.id.lucky_number_actual5 );
        mLuckyNumberActuals[6] = (TextView) findViewById( R.id.lucky_number_actual6 );

        mPayoutSeekbar = (SeekBar) findViewById( R.id.payout_seekbar );
        mPayoutEditText = (EditText) findViewById( R.id.payout_edittext );
        mChanceSeekbar = (SeekBar) findViewById( R.id.chance_seekbar );
        mChanceEditText = (EditText) findViewById( R.id.chance_edittext );
        mAmountSeekbar = (SeekBar) findViewById( R.id.amount_seekbar );
        mAmountEditText = (EditText) findViewById( R.id.amount_edittext );
        mProfitEditText = (EditText) findViewById( R.id.profit_edittext );

        mErrorContainer = (View) findViewById( R.id.error_container );
        mResultContainer = (View) findViewById( R.id.result_container );
        mErrorText = (TextView) findViewById( R.id.error_text );

        mAmountValue = 1;
        mPayoutValue = 2;
        mChanceValue = 49.5;

        mLuckyNumberDirectionBackgroundResource = R.drawable.result_box;

        mSoundCoinPay = mSoundPool.load(this,  R.raw.coinpay, 1);
		mSoundBoop = mSoundPool.load(this, R.raw.boop, 1 );
		mSoundWin = mSoundPool.load(this, R.raw.win1, 1);
        mSoundWinJackpot = mSoundPool.load(this, R.raw.slot_machine_win_19, 1);

        mPayoutSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if( mRuleset == null ) {
                    return;
                }

                // Changing one seek bar will change the other, so we don't want to keep calling onProgressChanged for each change.
                if( !fromUser ) {
                    return;
                }

                double quadraticProgress = getQuadraticEasedProgress(progress);
                mPayoutValue = mRuleset.result.minimum_payout + ( (mRuleset.result.maximum_payout - mRuleset.result.minimum_payout) * quadraticProgress);
                mPayoutValue /= 100000000;

                mDirtyControls.mPayoutEditText = true;
                handlePayoutChange();
                hideVirtualKeyboard( seekBar );

            }
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

        });
        mChanceSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mRuleset == null) {
                    return;
                }
                // Changing one seek bar will change the other, so we don't want to keep calling onProgressChanged for each change.
                if (!fromUser) {
                    return;
                }

                // TB TODO - get from ruleset!
                double minChance = 0.99;
                double maxChance = 97.0;

                double quadraticProgress = getQuadraticEasedProgress(progress);
                mChanceValue = minChance + ((maxChance - minChance) * quadraticProgress);

                mDirtyControls.mChanceEditText = true;
                handleChanceChange();
                hideVirtualKeyboard(seekBar);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }

        });
        mAmountSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress <= 0) {
                    progress = 1;
                }

                // Changing one seek bar will change the other, so we don't want to keep calling onProgressChanged for each change.
                if (!fromUser) {
                    return;
                }

                double quadraticProgress = getSlowQuadraticEasedInProgress(progress);
                mAmountValue = (int) (MIN_BET + ((MAX_BET - MIN_BET) * quadraticProgress));

                mDirtyControls.mAmountEditText = true;
                handleAmountChange();
                hideVirtualKeyboard(seekBar);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }

        });

        mRollHighButton.setOnTouchListener( new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mThrowHint = ThrowHint.HIGH;
                updateControls();
                return false;
            }
        });
        mRollLowButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mThrowHint = ThrowHint.LOW;
                updateControls();
                return false;
            }
        });






        mPayoutEditText.setCursorVisible(false);
        mPayoutEditText.addTextChangedListener( new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            public void afterTextChanged(Editable s) {
                if( mIsInsideUpdateControls ) {
                    return;
                }
                boolean isOK = false;
                if( s.length() > 0 ) {
                    try {
                        mPayoutValue = Double.parseDouble(s.toString());
                        mDirtyControls.mPayoutSeekBar = true;
                        handlePayoutChange();
                        isOK = true;
                        mPayoutEditText.setTextColor( Color.BLACK );
                    }
                    catch( NumberFormatException e ) {
                        //
                    }
                }
                if( !isOK ) {
                    mPayoutEditText.setTextColor( Color.RED );
                }
                updateControls();
            }
        });


        // TB TODO - This is causing a stack overflow bouncing back and forth between amount + profit
        mChanceEditText.setCursorVisible(false);
        mChanceEditText.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            public void afterTextChanged(Editable s) {
                if (mIsInsideUpdateControls) {
                    return;
                }

                boolean isOK = false;
                if (s.length() > 0) {
                    try {
                        mChanceValue = Double.parseDouble(s.toString());
                        mDirtyControls.mChanceSeekBar = true;
                        handleChanceChange();
                        isOK = true;
                        mChanceEditText.setTextColor(Color.BLACK);
                    } catch (NumberFormatException e) {
                        //
                    }
                }
                if (!isOK) {
                    mChanceEditText.setTextColor(Color.RED);
                }
                updateControls();
            }
        });

        mAmountEditText.setCursorVisible(false);
        mAmountEditText.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            public void afterTextChanged(Editable s) {
                if (mIsInsideUpdateControls) {
                    return;
                }
                boolean isOK = false;
                if (s.length() > 0) {
                    try {
                        mAmountValue = Integer.parseInt(s.toString());
                        mDirtyControls.mAmountSeekBar = true;
                        handleAmountChange();
                        isOK = true;
                        mAmountEditText.setTextColor(Color.BLACK);
                    } catch (NumberFormatException e) {
                        //
                    }
                }
                if (!isOK) {
                    mAmountEditText.setTextColor(Color.RED);
                }
                updateControls();
            }
        });


        mProfitEditText.setCursorVisible(false);
        mProfitEditText.addTextChangedListener( new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            public void afterTextChanged(Editable s) {
                if( mIsInsideUpdateControls ) {
                    return;
                }
                boolean isOK = false;
                if( s.length() > 0 ) {
                    try {
                        mProfitValue = Double.parseDouble(s.toString());
                        handleProfitChange();
                        isOK = true;
                        mProfitEditText.setTextColor( Color.BLACK );
                    }
                    catch( NumberFormatException e ) {
                        //
                    }
                }
                if( !isOK ) {
                    mProfitEditText.setTextColor( Color.RED );
                }
                updateControls();
            }
        });






        OnClickListener cursorClickListener = new OnClickListener() {
            public void onClick(View v) {
                //mPayoutEditText.setCursorVisible(true);
                ((EditText)v).setCursorVisible(true);
            }
        };

        mPayoutEditText.setOnClickListener(cursorClickListener);
        mChanceEditText.setOnClickListener(cursorClickListener);
        mAmountEditText.setOnClickListener(cursorClickListener);
        mProfitEditText.setOnClickListener(cursorClickListener);

        TextView.OnEditorActionListener textActionListener = new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    hideVirtualKeyboard(v);
                    v.setCursorVisible(false);
                    return true;
                }
                return false;
            }
        };
        mPayoutEditText.setOnEditorActionListener(textActionListener);
        mChanceEditText.setOnEditorActionListener(textActionListener);
        mAmountEditText.setOnEditorActionListener(textActionListener);
        mProfitEditText.setOnEditorActionListener(textActionListener);

        updateCredits( mUseFakeCredits ? bvc.mFakeIntBalance : bvc.mIntBalance );
        mMixpanel.track("dice_activity_create", null);

    }

    void hideVirtualKeyboard( View v )
    {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow( v.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN );
    }

    String prettyDouble4( double d ) {
        // return String.valueOf( Math.floor(d * 100000000) / 100000000 );
        return String.format("%.4f", d);
    }

    double getQuadraticEasedProgress( int progress ) {

        // Quadratic easing in/out, so that the edges of the seekbar move the value slower.
        // This assumes that the slider goes from 0 to 1000
        double t = progress/1000.0;
        if( t < 0.5 ) {
            return 2 * t * t;
        }
        else {
            t = 1 - t;
            return 1.0 - (2 * t * t);
        }

    }

    // We need to get the correct progress value to use when repositioning the seekbars.
    // Since the corresponding values ease in/out quadratically, we need to map back to that.
    // Pos is from 0-1
    double getLinearProgressFromQuadratic( double pos ) {
        if( pos < 0.5 ) {
            return Math.sqrt( pos / 2.0 );
        }
        else {
            return 1 - Math.sqrt( -(pos - 1) / 2 );
        }
    }

    double getSlowQuadraticEasedInProgress( int progress ) {
        double t = progress/1000.0;
        return t * t;
    }

    double getLinearProgressFromSlowQuadraticEasedIn( double pos ) {
        return Math.sqrt( pos );
    }

    void setPayoutEditText() {
        mPayoutEditText.setText( prettyDouble4(mPayoutValue) );
    }
    void setPayoutSeekbar() {
        // This division sucks
        double minPayout = mRuleset.result.minimum_payout / 100000000;
        double maxPayout = mRuleset.result.maximum_payout / 100000000;
        double linearPos = (mPayoutValue-minPayout) / (maxPayout-minPayout);
        if( linearPos > 1 ) {
            linearPos = 1;
        }
        else if( linearPos < 0 ) {
            linearPos = 0;
        }
        double quadPos = getLinearProgressFromQuadratic( linearPos );
        mPayoutSeekbar.setProgress((int) (quadPos * 1000));
    }

    void setChanceEditText() {
        mChanceEditText.setText(prettyDouble4(mChanceValue));
    }
    void setChanceSeekbar() {
        // TB TODO - get from ruleset!
        double minChance = 0.99;
        double maxChance = 97.0;
        double linearPos = (mChanceValue-minChance) / (maxChance-minChance);
        if( linearPos > 1 ) {
            linearPos = 1;
        }
        else if( linearPos < 0 ) {
            linearPos = 0;
        }
        double quadPos = getLinearProgressFromQuadratic( linearPos );
        mChanceSeekbar.setProgress((int) (quadPos * 1000));
    }
    void setAmountSeekbar() {
        double linearPos = (double)(mAmountValue-1) / (double)(MAX_BET-MIN_BET);
        if( linearPos > 1 ) {
            linearPos = 1;
        }
        else if( linearPos < 0 ) {
            linearPos = 0;
        }
        double quadPos = getLinearProgressFromSlowQuadraticEasedIn(linearPos);
        mAmountSeekbar.setProgress( (int)(quadPos * 1000));
    }

    void recalculateProfit() {
        mProfitValue = this.mPayoutValue * mAmountValue - mAmountValue;
        mDirtyControls.mProfitEditText = true;
    }
    void handlePayoutChange() {
        mChanceValue = (mRuleset.result.player_return / 1000000) / mPayoutValue;

        mDirtyControls.mChanceEditText = true;
        mDirtyControls.mChanceSeekBar = true;
        recalculateProfit();
        updateControls();
    }
    void handleChanceChange() {
        mPayoutValue = (mRuleset.result.player_return / 1000000) / mChanceValue;
        mDirtyControls.mPayoutEditText = true;
        mDirtyControls.mPayoutSeekBar = true;

        recalculateProfit();
        updateControls();
    }

    void handleAmountChange() {
        recalculateProfit();
        updateControls();
    }

    void handleProfitChange() {
        // TB TODO - Amount gets clobbered to int, which then makes the profit not guaranteed...
        mAmountValue = (int)(mProfitValue / (mPayoutValue-1.0));

        mDirtyControls.mAmountEditText = true;
        mDirtyControls.mAmountSeekBar = true;
        updateControls();
    }

    @Override
    public void onDestroy() {
    	super.onDestroy();
    }
    
	void timeUpdate() {
		super.timeUpdate();
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }
    
    @Override
    public void onResume() {
    	super.onResume();

    	final Activity that = this;
    	
    	if( mServerSeedHash == null ) {
    		// If the seed happens to have expired when he returns, that's OK because we'll get a new seed
    		// when need_seed is returned when dealing the game.
			mNetReseedTask = new NetReseedTask(this, false);
	    	mNetReseedTask.executeParallel( Long.valueOf(0) ); 
    	}
    	
		mNetUpdateTask = new NetUpdateTask(this);
    	mNetUpdateTask.executeParallel( Long.valueOf(0) );

        // TB - Kind of silly to be getting this multiple times???
        mNetRulesetTask = new NetRulesetTask(this);
        mNetRulesetTask.execute( Long.valueOf(0) );

    	timeUpdate();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK ) {
            if( mIsGameBusy || mIsWaitingForServer ) {
                showEarlyExitDialog();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onPause() {
    	super.onPause();
    	mHandler.removeCallbacks(mTimeUpdateRunnable);
    	mHandler.removeCallbacks(mCountUpRunnable);
        mHandler.removeCallbacks(mShowLuckyNumberRunnable);
		setAuto(false);
    }

	void handleNotEnoughCredits()
	{
		Toast.makeText(this, "Please deposit more credits", Toast.LENGTH_SHORT).show();
		setAuto(false); 
	}

	public void onThrow(String target) {
		if( mGameState == DiceGameState.WAIT_USER_THROW ) {
			if( !canThrow() ) {
				return;
			}
			BitcoinVideoCasino bvc = BitcoinVideoCasino.getInstance(this);

			if( bvc.mIntBalance - (mAmountValue * mCreditBTCValue) < 0 ) {
				handleNotEnoughCredits();
				return;
			}

			if( mServerSeedHash == null ) {
				// TB TODO - Get another hash? Or maybe we're waiting for it still?
				return;
			}

            mTargetValue = target;
			mNetThrowTask = new NetThrowTask(this);
	    	mNetThrowTask.executeParallel();
		}
	}
    public void onRollHigh(View button) {
        // Set the throw hint again just in case the button touch handler didn't trigger for some reason
        mThrowHint = ThrowHint.HIGH;
        onThrow("high");
    }
    public void onRollLow(View button) {
        // Set the throw hint again just in case the button touch handler didn't trigger for some reason
        mThrowHint = ThrowHint.LOW;
        onThrow("low");
    }
	private boolean canThrow() {
		if( mIsWaitingForServer || mIsGameBusy ) {
			return false;
		}

        if( mIsUserInputError ) {
            return false;
        }

		return (mGameState == DiceGameState.WAIT_USER_THROW );
	}
	private boolean canAuto() {
		return canThrow();
	}

    void setLuckyNumberDirectionBackgroundResource( int resource ) {
        // This is insane.
        // When you call setBackgroundResource, the padding info is lost. So you need to rebuild it.
        if( resource != mLuckyNumberDirectionBackgroundResource ) {
            int bottom = mLuckyNumberDirection.getPaddingBottom();
            int top = mLuckyNumberDirection.getPaddingTop();
            int right = mLuckyNumberDirection.getPaddingRight();
            int left = mLuckyNumberDirection.getPaddingLeft();
            mLuckyNumberDirection.setBackgroundResource(resource);
            mLuckyNumberDirection.setPadding(left, top, right, bottom);
            mLuckyNumberDirectionBackgroundResource = resource;
        }
    }

	public void updateControls() {
        if( mRuleset == null ) {
            return;
        }


        mIsInsideUpdateControls = true;
		if( canThrow() ) {
			//mRollHighButton.setEnabled(true);
            //mRollLowButton.setEnabled(true);
            mRollHighButton.setImageResource( R.drawable.button_roll_high );
            mRollLowButton.setImageResource( R.drawable.button_roll_low );
		}
		else {
            //mRollHighButton.setEnabled(false);
            //mRollLowButton.setEnabled(false);
            mRollHighButton.setImageResource( R.drawable.button_roll_off );
            mRollLowButton.setImageResource( R.drawable.button_roll_off );
		}
		
		if( mIsAutoOn ) {
			mAutoButton.setImageResource( R.drawable.button_auto_stop ); 
		}
		else if( canAuto() ) {
			mAutoButton.setImageResource( R.drawable.button_auto );
		}
		else {
			mAutoButton.setImageResource( R.drawable.button_draw_off );
		}
        mTextBet.setText( "BET " + mAmountValue );




        if( mDirtyControls.mPayoutEditText ) {
            setPayoutEditText();
            mDirtyControls.mPayoutEditText = false;
        }
        if( mDirtyControls.mPayoutSeekBar ) {
            setPayoutSeekbar();
            mDirtyControls.mPayoutSeekBar = false;
        }
        if( mDirtyControls.mChanceEditText) {
            setChanceEditText();
            mDirtyControls.mChanceEditText = false;
        }
        if( mDirtyControls.mChanceSeekBar) {
            setChanceSeekbar();
            mDirtyControls.mChanceSeekBar = false;
        }
        if( mDirtyControls.mAmountEditText) {
            mAmountEditText.setText( String.valueOf(mAmountValue) );
            mDirtyControls.mAmountEditText = false;
        }
        if( mDirtyControls.mAmountSeekBar) {
            setAmountSeekbar();
            mDirtyControls.mAmountSeekBar = false;
        }
        if( mDirtyControls.mProfitEditText) {
            mProfitEditText.setText( prettyDouble4(mProfitValue) );
            mDirtyControls.mProfitEditText = false;
        }





        //long intBetChance = Bitcoin.stringAmountToLong( String.format("%.8f", mChanceValue)) / 10000;
        //long intPayout = Bitcoin.stringAmountToLong( String.format("%.8f", mPayoutValue));
        long intBetChance = (long)(mChanceValue * 10000);
        long intPayout = (long)(mPayoutValue * 100000000);

        int luckyNumberDirectionBackgroundResource = R.drawable.result_box;
        if( mThrowHint == ThrowHint.NONE ) {
            // What goes here?
            if( mThrowResult != null ) {
                // Go back to showing the result of the throw!
                if( mThrowResult.target.equalsIgnoreCase("high") ) {
                    mLuckyNumberDirection.setText(">");
                }
                else {
                    mLuckyNumberDirection.setText("<");
                }

                drawLuckyNumberGoal( mThrowResult.target.equalsIgnoreCase("high") ? 999999-mThrowResult.chance : mThrowResult.chance );

                if( mThrowResult.intwinnings > 0 ) {
                    luckyNumberDirectionBackgroundResource = R.drawable.result_box_win;
                }
                drawLuckyNumberActual(mThrowResult.lucky_number, 7);

            }
            else {
                // The starting configuration
                // TB TODO - Set the correct digits
                drawLuckyNumberActual( 0, 0 );
                mLuckyNumberDirection.setText(">");
                //mLuckyNumberGoal.setText( "49.5000" );
                drawLuckyNumberGoal(495000);
            }
        }
        else if( mThrowHint == ThrowHint.HIGH ) {
            mLuckyNumberDirection.setText(">");
            //mLuckyNumberGoal.setText("99");
            //mLuckyNumberGoal.setText( mDice.getWinCutoff(true, intBetChance));
            drawLuckyNumberGoal( mDice.getWinCutoff(true, intBetChance));
            drawLuckyNumberActual( 0, 0 );
        }
        else if( mThrowHint == ThrowHint.LOW ) {
            mLuckyNumberDirection.setText("<");
            //mLuckyNumberGoal.setText("3");
            drawLuckyNumberGoal(mDice.getWinCutoff(false, intBetChance));
            drawLuckyNumberActual( 0, 0 );
        }

        setLuckyNumberDirectionBackgroundResource(luckyNumberDirectionBackgroundResource);



        mChanceEditText.setTextColor( Color.BLACK );
        mPayoutEditText.setTextColor( Color.BLACK );
        mAmountEditText.setTextColor( Color.BLACK );
        mProfitEditText.setTextColor( Color.BLACK );

        mIsUserInputError = false;
        String errorString = "";
        if( intPayout < mRuleset.result.minimum_payout ) {
            mIsUserInputError = true;
            errorString = "Chance can not be greater than " + prettyDouble4(mRulesetMaximumChance);
            mChanceEditText.setTextColor( Color.RED );
        }
        else if( intPayout > mRuleset.result.maximum_payout ) {
            mIsUserInputError = true;
            errorString = "Chance can not be smaller than " + prettyDouble4(mRulesetMinimumChance);
            mChanceEditText.setTextColor( Color.RED );
        }

        if( mAmountValue < 1 ) {
            mIsUserInputError = true;
            errorString = "Bet amount must be greater than 0";
            mAmountEditText.setTextColor( Color.RED );
        }

        // No need to check for a whole number bet amount like the JS code since mAmountValue is a whole number.

        if( mProfitValue * mCreditBTCValue > mRuleset.result.maximum_profit ) {
            mIsUserInputError = true;
            errorString = "Profit can not be bigger than " + String.valueOf(mRuleset.result.maximum_profit / mCreditBTCValue) + " credits";
            mAmountEditText.setTextColor( Color.RED );
        }


        if( mIsUserInputError ) {
            mErrorContainer.setVisibility(View.VISIBLE);
            mResultContainer.setVisibility(View.GONE);
            mErrorText.setText(errorString);
        }
        else {
            mErrorContainer.setVisibility(View.GONE);
            mResultContainer.setVisibility(View.VISIBLE);
        }

        //String s = "Roll High > " + formatLuckyNumber( mDice.getWinCutoff(true, intBetChance ));
        //mRollHighButton.setText(s);
        String s = "> " + formatLuckyNumber( mDice.getWinCutoff(true, intBetChance ));
        mRollHighButtonGoal.setText(s);
        //s = "Roll Low < " + formatLuckyNumber( mDice.getWinCutoff(false, intBetChance ));
        //mRollLowButton.setText(s);
        s = "< " + formatLuckyNumber( mDice.getWinCutoff(false, intBetChance ));
        mRollLowButtonGoal.setText(s);

        mIsInsideUpdateControls = false;
	}

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if( !mDidScaleContents ) {
            mDidScaleContents = true;
            int width = mLuckyNumberActuals[0].getWidth();

            mLuckyNumberActuals[0].getLayout();
        }
    }

	private void doAuto( int lastGameResult )
	{
		if( !mIsAutoOn ) {
			return;
		}

        // We update the value here instead of the game's onSuccess handler so that the bet amount first
        // changes right before the new bet is initiated.
        if( mAutoStrategy == AutoStrategy.MARTINGALE ) {
            if( lastGameResult == LastGameResult.WIN ) {
                mAmountValue = 1;
            }
            else if( lastGameResult == LastGameResult.LOSE ) {
                mAmountValue *= 2;
                if( mAmountValue > MAX_BET ) {
                    // TB TODO - Parameter to specify whether the amount should reset or stay at max...???
                    mAmountValue = MAX_BET;
                }

                // Check that profit is not over the max. If so, reduce the bet amount.
                recalculateProfit();
                if( mProfitValue * mCreditBTCValue > mRuleset.result.maximum_profit ) {
                    mProfitValue = mRuleset.result.maximum_profit / mCreditBTCValue;
                    handleProfitChange();
                }
            }
            mDirtyControls.mAmountEditText = true;
            mDirtyControls.mAmountSeekBar = true;
            handleAmountChange();
        }

        if( mAutoTarget == AutoTarget.HIGH ) {
            mThrowHint = ThrowHint.HIGH;
            onThrow("high");
        }
        else {
            mThrowHint = ThrowHint.LOW;
            onThrow("low");
        }
	}
	
	private void checkAuto( final int lastGameResult ) {
		if( mIsGameBusy ) {
			Log.e(TAG, "Error: checkAuto() called while game is busy.");
			return;
		}
		if( mIsWaitingForServer ) {
			Log.e(TAG, "Error: checkAuto() called while waiting for server.");
			return;
		}
		if( !mIsAutoOn ) {
			return;
		} 

		int delay = getDelayFromAutoSpeed(mAutoSpeed);
		
		// When you initially start auto mode, it should immediately jump into action.
		// It shouldn't just initially sit there for 1-2 seconds, since that seems unresponsive.
		if( mIsFirstAutoAction ) {
			delay = 0;
			mIsFirstAutoAction = false;
		} 
		
		mHandler.postDelayed(new Runnable() {
            public void run() {
                doAuto( lastGameResult );
            }
        }, delay);
	}
	public void setAuto( boolean val ) {
		mIsAutoOn = val;
		updateControls();
		if( val == true ) {
			mIsFirstAutoAction = true;
			checkAuto( LastGameResult.NOTHING );
		}
	}

    // TB TODO DICE
    /*
    public void onHelp(View button) {
        Intent intent = new Intent(this, SlotsHelpActivity.class);
        startActivity(intent);
    }
    */

	public void onAuto(View button) {


        // TB TODO DICE


		if( mIsAutoOn ) {
			setAuto( false );
			return;
		}
		
		final Dialog dialog = new Dialog(this);
		dialog.setContentView(R.layout.d_auto);
		dialog.setTitle("Autoplay Settings");

        final Spinner strategySpinner = (Spinner) dialog.findViewById(R.id.strategy_spinner);
		final Spinner speedSpinner = (Spinner) dialog.findViewById(R.id.speed_spinner);
        final Spinner targetSpinner = (Spinner) dialog.findViewById(R.id.target_spinner);

        strategySpinner.setSelection( mAutoStrategy );
		speedSpinner.setSelection( mAutoSpeed );
        targetSpinner.setSelection( mAutoTarget );

		Button playButton = (Button) dialog.findViewById(R.id.play_button);
		playButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
                mAutoStrategy = strategySpinner.getSelectedItemPosition();
				mAutoSpeed = speedSpinner.getSelectedItemPosition();
                mAutoTarget = targetSpinner.getSelectedItemPosition();

                if( mAutoStrategy == AutoStrategy.MARTINGALE ) {
                    // TB TODO - Should this reset to 1? Or remember your starting bet?
                    mAmountValue = 1;

                    mDirtyControls.mAmountEditText = true;
                    mDirtyControls.mAmountSeekBar = true;
                    handleAmountChange();
                }
				setAuto( true );
				dialog.dismiss();
			}
		});
		
		Button cancelButton = (Button) dialog.findViewById(R.id.cancel_button);
		cancelButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				dialog.dismiss();
			}
		});

		dialog.show();
	}

	String getProgressiveJackpotString( long progressiveJackpot ) {
		// The jackpot returned is in 10000ths of a credit
        if( mRuleset == null ) {
            return "XXX";
        }

        float val = (float)( progressiveJackpot/10000.0);
        return String.format("%.2f", val);
	}
	void updateProgressiveJackpot( Map<String,Integer> prog )
	{
        int jp5 = prog.get("5");
        int jp6 = prog.get("6");
        if( jp5 > 0 ) {
            mJackpot5Text.setText( "ROLL FIVE 7'S.........." + getProgressiveJackpotString(jp5) );
        }
        if( jp6 > 0 ) {
            mJackpot6Text.setText( "ROLL SIX 7'S.........." + getProgressiveJackpotString(jp6) );
        }
	}

    @Override
    boolean shouldConnectingDialogShow()
    {
        boolean val = super.shouldConnectingDialogShow();
        if( val == true ) {
            return true;
        }

        return mRuleset == null;
    }

    void drawLuckyNumberActual( long luckyNumber, int numVisibleDigits ) {

        String luckyString = "";
        if( luckyNumber < 100000 ) {
            luckyString += "0";
        }
        luckyString += Integer.toString( (int) ( luckyNumber / 10000 ) );
        luckyString += ".";
        luckyString += Long.toString( (luckyNumber % 10000) );
        while( luckyString.length() < 7 ) {
            luckyString += "0";
        }

        for( int i = 0; i < numVisibleDigits; i++ ) {
            char ch = luckyString.charAt(i);
            if( ch == '7' ) {
                mLuckyNumberActuals[i].setTextColor( Color.GREEN );
            }
            else {
                mLuckyNumberActuals[i].setTextColor( Color.WHITE );
            }
            mLuckyNumberActuals[i].setText( String.valueOf(luckyString.charAt(i)) );
        }

        for( int k = numVisibleDigits; k < 7; k++ ) {
            mLuckyNumberActuals[k].setTextColor( Color.GRAY );
            if( k == 2 ) {
                // Don't mess with the decimal
            }
            else {
                mLuckyNumberActuals[k].setText( "X" );
            }
        }
    }

    String formatLuckyNumber( long goal ) {
        return String.format("%07.4f", (goal/10000.0));

    }
    void drawLuckyNumberGoal( long goal ) {
        mLuckyNumberGoal.setText( formatLuckyNumber(goal));
    }

    class ShowLuckyNumberRunnable implements Runnable {
        // Position in the reels
        long mLuckyNumber;
        Runnable mFinishedCallback;
        int mDigitIndex;

        ShowLuckyNumberRunnable( long luckyNumber, Runnable finishedCallback ) {
            mFinishedCallback = finishedCallback;
            mLuckyNumber = luckyNumber;
            mDigitIndex = 1;
        }

        public void run() {
            // Instantly show the digits if in auto mode
            if( mIsAutoOn ) {
                mFinishedCallback.run();
                return;
            }

            drawLuckyNumberActual( mLuckyNumber, mDigitIndex );

            if( mDigitIndex == 7 ) {
                mFinishedCallback.run();
                return;
            }

            mDigitIndex += 1;
            final int delay = 50;
            mHandler.postDelayed(this, delay);
        }

    };

    class NetUpdateTask extends NetAsyncTask<Long, Void, JSONDiceUpdateResult> {
    	
    	NetUpdateTask( CommonActivity a ) { super(a); }
    	
    	public JSONDiceUpdateResult go(Long...v) throws IOException {
    		int last = 999999999;
    		int chatlast = 999999999;

    		return mBVC.diceUpdate(last, chatlast, mCreditBTCValue);
    	}
    	public void onSuccess(JSONDiceUpdateResult result) {
    		updateProgressiveJackpot( result.progressive_jackpots );
    	}
    }
    
    class NetReseedTask extends NetAsyncTask<Long, Void, JSONReseedResult> {
    	boolean mAutodeal; 
    	NetReseedTask( Activity a, boolean autodeal ) { 
    		super(a);
    		// If true, the game will try to automatically deal after getting this seed.
    		// This should happen if the server returns need_seed when the user hit's DRAW.
    		// He shouldn't have to hit Draw again after getting the seed.
    		mAutodeal = autodeal;
    	}
    	
    	public JSONReseedResult go(Long...v) throws IOException {
    		return mBVC.diceReseed();
    	}
    	public void onSuccess(JSONReseedResult result) {
    		mServerSeedHash = result.server_seed_hash;
    		updateControls();
    		checkConnectingAlert();
    		if( mConnectingDialog == null && mAutodeal ) {
    			onThrow(mTargetValue);
    		}
    	}
    }
    class NetThrowTask extends NetAsyncTask<Long, Void, JSONDiceThrowResult> {

        boolean mIsFreeSpin;
    	NetThrowTask( CommonActivity a ) {
    		super(a);
            // TB TEMP TEST - Keep this running so that if the task is interrupted (phone call, etc), that the result
            // will still be shown.
            mAllowAbort = false;

            updateCredits( (mUseFakeCredits ? mBVC.mFakeIntBalance : mBVC.mIntBalance) - (mAmountValue * mCreditBTCValue) );

            // TB TEMP TEST - There's probably a better place to put this?
            stopCountUpWins();
            updateWin( 0, false );

    		mIsWaitingForServer	= true;
    		mIsGameBusy = true;
            playSound( mSoundCoinPay );

    		// TB - Credits are now dirty (so don't update credits with whatever we get from a balance update, since it will be incorrect)
			mCreditsAreDirty = true;
    		updateControls();
    	}
    	
    	public JSONDiceThrowResult go(Long...v) throws IOException {
    		String serverSeedHash = mServerSeedHash;

            long bet = mAmountValue * mCreditBTCValue;
            long payout = (long)(mPayoutValue * 100000000);
            return mBVC.diceThrow(serverSeedHash, getClientSeed(), bet, payout, mTargetValue, mUseFakeCredits );
    	}
    	@Override
    	public void onSuccess(final JSONDiceThrowResult result) {
    		if( result.error != null ) {
                mIsGameBusy = false;
                setAuto(false);
    			if( result.error.contains("need_seed") ) {
                    mServerSeedHash = null;
    				showConnectingDialog();
					mNetReseedTask = new NetReseedTask(mActivity, true);
			    	mNetReseedTask.execute( Long.valueOf(0) ); 
    			}
    			else {
    				Log.e(TAG, "Unknown error returned by server:" + result.error);
                    handleError(result, String.format("Error from server: %s", result.error));
    			}
                updateControls();
				return;
    		}
    		mThrowResult = result;
            mServerSeedHash = result.server_seed_hash;

            mBVC.mIntBalance = result.intbalance;
            mBVC.mFakeIntBalance = result.fake_intbalance;

            updateProgressiveJackpot( result.progressive_jackpots );

    		mShowLuckyNumberRunnable = new ShowLuckyNumberRunnable( result.lucky_number, new Runnable() {

				public void run() {
                    if( result.progressive_win > 0 ) {
                        playSound( mSoundWinJackpot );
                    }
                    else if( result.intwinnings > 0 ) {
                        playSound( mSoundWin );
                    }

                    if( result.intwinnings > 0 ) {
                        long delta = mCreditBTCValue;
                        if( result.intwinnings/mCreditBTCValue >= 50 ) {
                            delta = mCreditBTCValue * 5;
                        }
                        if( mIsAutoOn ) {
                            delta = result.intwinnings;
                        }

                        startCountUpWins(result.intwinnings, (mUseFakeCredits ? result.fake_intbalance : result.intbalance) - result.intwinnings, delta );
                    }

		    		mIsGameBusy = false;

                    int lastGameResult = LastGameResult.LOSE;
                    if( result.intwinnings > 0 ) {
                        lastGameResult = LastGameResult.WIN;
                    }
                    updateControls();

                    checkAuto( lastGameResult );
				}
    		});
    		mShowLuckyNumberRunnable.run();

    	}

    	@Override
    	public void onError(final JSONDiceThrowResult result) {
    		mIsGameBusy = false;
            setAuto(false);
            updateControls();
    	}
    	@Override
    	public void onDone() {
    		mIsWaitingForServer = false;
    		
    		// TB - Credits are now clean. We can display the intbalance we get from the server again.
			mCreditsAreDirty = false;
            mThrowHint = ThrowHint.NONE;
    	}
    }
    class NetRulesetTask extends NetAsyncTask<Long, Void, JSONDiceRulesetResult> {

        NetRulesetTask( CommonActivity a ) {
            super(a);
            mIsWaitingForServer	= true;
        }

        public JSONDiceRulesetResult go(Long...v) throws IOException {
            return mBVC.diceRuleset();
        }
        @Override
        public void onSuccess(final JSONDiceRulesetResult result) {
            mRuleset = result;

            mRulesetMaximumChance = 100.0 * mRuleset.result.player_return / mRuleset.result.minimum_payout;
            mRulesetMinimumChance = 100.0 * mRuleset.result.player_return / mRuleset.result.maximum_payout;

            checkConnectingAlert();
            mDirtyControls.mPayoutSeekBar = true;
            mDirtyControls.mPayoutEditText = true;
            mDirtyControls.mChanceSeekBar = true;
            mDirtyControls.mChanceEditText = true;
            handlePayoutChange();
        }
        @Override
        public void onDone() {
            mIsWaitingForServer = false;
        }
    }


}

