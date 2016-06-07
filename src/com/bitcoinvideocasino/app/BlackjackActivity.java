
package com.bitcoinvideocasino.app;

import java.io.IOException;

import java.util.HashMap;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.Bitmap;
import android.view.ViewGroup;
import android.graphics.Typeface;
import android.widget.LinearLayout;
import android.util.TypedValue;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.Toast;
import android.widget.ImageButton;
import android.app.ActivityManager.MemoryInfo;
import android.app.ActivityManager;
import java.util.List;
import java.util.ArrayList;

import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuff;
import android.graphics.ColorFilter;

import com.bitcoinvideocasino.lib.*;
import com.bitcoinvideocasino.R;

import android.app.Dialog;
import android.widget.Spinner;
import android.widget.Button;
import android.widget.EditText;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuff;
import android.graphics.ColorFilter;
import android.text.TextWatcher;
import android.text.Editable;
import android.view.View.OnTouchListener;
import android.view.MotionEvent;
import android.widget.TextView.OnEditorActionListener;
import android.view.inputmethod.EditorInfo;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;

public class BlackjackActivity extends GameActivity {
	
	class BlackjackGameState extends GameState {
		final static public int WAIT_USER_DEAL = 0; 
		final static public int WAIT_USER_COMMAND = 1;
	}
	class Who {
		final static public int PLAYER = 0;
		final static public int DEALER = 1;
	}
	class AutoInsurance {
		final static public int NEVER = 0;
		final static public int SOMETIMES = 1;
		final static public int ALWAYS = 2;
	}
	final int WIN_SOUND_DELAY = 150;
	
	private NetReseedTask mNetReseedTask;
	private NetRulesetTask mNetRulesetTask;
	final private String TAG = "BlackjackActivity";
	
	//int mNextHand;
	private ImageButton mDealButton;
	private ImageButton mHitButton;
	private ImageButton mStandButton;
	private ImageButton mInsuranceButton;
	private ImageButton mSplitButton;
	private ImageButton mDoubleButton;
	//private ImageButton mInsuranceButton;
	private ImageButton mAutoButton;
	private NetCommandTask mNetCommandTask;
	private TextView mBetText;
	private EditText mBetCreditsInput;
	private TextView mCreditConversionHint;
	private TextView mMaxBetHint;
	private ImageView mInsuranceWin;
	private ImageView mInsuranceLose;
	
	private long mBet;
	private String mGameID;
	int mAutoSpeed;
	int mAutoInsurance;
	List<Integer> mActions;
	private JSONBlackjackCommandResult mDealResult;
	private JSONBlackjackRulesetResult mRuleset;
	
	private HandGroup[] mHandGroups;
	
	private int mSoundCardDeal;
	private int mSoundCoinPay;
	private int mSoundBoop;
	private int mSoundWinBlackjack;
	private int mSoundWin;
	private int mSoundWinProgressive;
	private int mSoundCanSplit;
	
	int mTotalCardsDealt;
	
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_blackjack);
        
        BitcoinVideoCasino bvc = BitcoinVideoCasino.getInstance(this); 
        
		mDealButton = (ImageButton) findViewById( R.id.deal_button );
		mSplitButton = (ImageButton) findViewById( R.id.split_button );
		mDoubleButton = (ImageButton) findViewById( R.id.double_button );
		mInsuranceButton = (ImageButton) findViewById( R.id.insurance_button );
		mHitButton = (ImageButton) findViewById( R.id.hit_button );
		mStandButton = (ImageButton) findViewById( R.id.stand_button );
		mAutoButton = (ImageButton) findViewById( R.id.auto_button );
		mBetCreditsInput = (EditText) findViewById( R.id.bet_credits_input );
		mBetText = (TextView) findViewById( R.id.bet_text );
		mCreditConversionHint = (TextView) findViewById( R.id.credit_conversion_hint );
		mMaxBetHint = (TextView) findViewById( R.id.max_bet_hint );
		mInsuranceWin = (ImageView) findViewById( R.id.insurance_win );
		mInsuranceLose = (ImageView) findViewById( R.id.insurance_lose );
		
		// TB TODO - Remove unused sounds!
        mSoundCardDeal = mSoundPool.load(this,  R.raw.carddeal, 1);
        mSoundCoinPay = mSoundPool.load(this,  R.raw.coinpay, 1);
		mSoundBoop = mSoundPool.load(this, R.raw.boop, 1 );
		mSoundWinBlackjack = mSoundPool.load(this, R.raw.slot_machine_win_22, 1);
		mSoundWin = mSoundPool.load(this, R.raw.win1, 1);
		mSoundWinProgressive = mSoundPool.load(this, R.raw.slot_machine_win_19, 1);
		mSoundCanSplit = mSoundPool.load(this, R.raw.slot_machine_bet_10, 1);
		
		mRuleset = null;
        mCreditBTCValue = Bitcoin.stringAmountToLong("0.0001");
		mBet = Bitcoin.stringAmountToLong("0.0001");
		mHandGroups	= new HandGroup[2];
		mHandGroups[ Who.PLAYER ] = new HandGroup( R.id.player_hands_holder );
		mHandGroups[ Who.DEALER ] = new HandGroup( R.id.dealer_hands_holder );
		mAutoSpeed = AutoSpeed.MEDIUM;
		mAutoInsurance = AutoInsurance.SOMETIMES;
		mActions = new ArrayList<Integer>();
        mShowDecimalCredits = true;
		
		addCardBitmapsToCache();
		
        mBetCreditsInput.setCursorVisible(false);
		mBetCreditsInput.addTextChangedListener( new TextWatcher() {

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			public void afterTextChanged(Editable s) {
				boolean isOK = false;
				if( s.length() > 0 ) {
					try {
						long bet = mCreditBTCValue * Integer.parseInt(s.toString());
						if( isBetAmountOK(bet) ) {
							isOK = true;
							mBetCreditsInput.setTextColor( Color.BLACK );
						}
						// Always set the bet value even if it's bad, so that the edittext and textarea are always consistent.
						mBetText.setText( "BET " + s ); 
						mBet = bet;
					}
					catch( NumberFormatException e ) {
						//
					}
				}
				if( !isOK ) {
					mBetCreditsInput.setTextColor( Color.RED ); 
				}
				updateControls();
			}
		});
		
		mBetCreditsInput.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
		        mBetCreditsInput.setCursorVisible(true);
				mCreditConversionHint.setVisibility(View.VISIBLE);
				mMaxBetHint.setVisibility(View.VISIBLE); 
		    }
		});
		mBetCreditsInput.setOnEditorActionListener(new OnEditorActionListener() {
		    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		        if (actionId == EditorInfo.IME_ACTION_DONE) {
		            // hide virtual keyboard
		            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		            //imm.hideSoftInputFromWindow(m_txtSearchText.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
		            imm.hideSoftInputFromWindow(mBetCreditsInput.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
			        mBetCreditsInput.setCursorVisible(false);
		            return true;
		        }
		        return false;
		    }

		});

		updateCredits( mUseFakeCredits ? bvc.mFakeIntBalance : bvc.mIntBalance );

		mMixpanel.track("blackjack_activity_create", null);
        updateControls();
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	mBitmapCache.clear();
    } 
    
    private boolean isBetAmountOK( long bet ) {
    	if( bet <= 0 ) {
    		return false;
    	}
    	if( mRuleset == null ) {
    		return false;
    	}
    	if( bet > mRuleset.result.maximum_bet ) {
    		return false;
    	}
    	if( bet % mRuleset.result.bet_resolution != 0 ) {
    		return false;
    	}
    	
		return true;
    }
    
    private void resetToIntroCards() {
    	resetGame();
	    mHandGroups[Who.DEALER].addCard(0, "back");
	    mHandGroups[Who.DEALER].addCard(0, "back");
	    mHandGroups[Who.PLAYER].addCard(0, "back");
	    mHandGroups[Who.PLAYER].addCard(0, "back"); 
	    /* 
	    mHandGroups[Who.PLAYER].addHand();
	    mHandGroups[Who.PLAYER].addCard(1, "back"); 
	    mHandGroups[Who.PLAYER].addHand();
	    mHandGroups[Who.PLAYER].addCard(2, "back"); 
	    mHandGroups[Who.PLAYER].addHand();
	    mHandGroups[Who.PLAYER].addCard(3, "back"); 
	    */
    }
    
    private void resetGame() {
    	mHandGroups[Who.DEALER].reset();
	    mHandGroups[Who.DEALER].addHand();
    	mHandGroups[Who.PLAYER].reset();
	    mHandGroups[Who.PLAYER].addHand();
	    mTotalCardsDealt = 0;
	    //mNextHand = 0;
        mGameState = BlackjackGameState.WAIT_USER_DEAL;
        mActions.clear();
        mDealResult = null;
        
        // TB TODO - Kind of sloppy that updateControls can't keey track of the state properly.
		mInsuranceLose.setVisibility(View.INVISIBLE); 
		mInsuranceWin.setVisibility(View.INVISIBLE); 
		mInsuranceButton.setVisibility(View.INVISIBLE); 
    }
    
    @Override
	void timeUpdate() {
		super.timeUpdate();
		if( canDeal() ) {
			mDealButton.setImageResource( mBlinkOn ? R.drawable.button_draw_bright : R.drawable.button_draw );
		}
	}
    
    @Override
    public void onResume() {
    	super.onResume();
    	final Activity that = this;
    	
		mNetReseedTask = new NetReseedTask(this, false);
    	mNetReseedTask.execute( Long.valueOf(0) ); 
    	
    	// TB - Kind of silly to be getting this multiple times???
		mNetRulesetTask = new NetRulesetTask(this);
    	mNetRulesetTask.execute( Long.valueOf(0) ); 
    	
    	timeUpdate();
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	mHandler.removeCallbacks(mTimeUpdateRunnable);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK ) {
            if( mIsGameBusy || mIsWaitingForServer || mGameState == BlackjackGameState.WAIT_USER_COMMAND ) {
                showEarlyExitDialog();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
    	super.onWindowFocusChanged(hasFocus);
    	if( !mDidScaleContents ) {
		    scaleContents(mContents, mContainer);
    		resetToIntroCards();
    		mDidScaleContents = true;
    	}
    }
    boolean areHandsRemaining() {
    	// return mNextHand < mHandGroups[Who.PLAYER].mHands.size(); 
    	return mDealResult.next_hand < mHandGroups[Who.PLAYER].mHands.size();
    }
    
	boolean canDeal() {
		if( mIsWaitingForServer || mIsGameBusy ) {
			return false;
		}
		if( mGameState != BlackjackGameState.WAIT_USER_DEAL ) {
			return false;
		}
		
		if( !isBetAmountOK(mBet) ) {
			return false;
		}
		
		return true;
	}
	boolean canDouble() {
		if( mIsWaitingForServer || mIsGameBusy ) {
			return false;
		}
		if( mGameState != BlackjackGameState.WAIT_USER_COMMAND ) {
			return false;
		}
		if( !areHandsRemaining() ) {
			return false;
		}
		List<String> cards = mHandGroups[Who.PLAYER].mHands.get( mDealResult.next_hand ).mCards;
		if( Blackjack.is_21(cards) ) {
			return false;
		}
		if( cards.size() != 2 ) {
			return false;
		}
		return true; 
	}
	boolean canHit() {
		if( mIsWaitingForServer || mIsGameBusy ) {
			return false;
		}
		if( mGameState != BlackjackGameState.WAIT_USER_COMMAND ) {
			return false;
		}
		if( !areHandsRemaining() ) {
			return false;
		}
		List<String> cards = mHandGroups[Who.PLAYER].mHands.get( mDealResult.next_hand ).mCards;
		if( Blackjack.is_21(cards) ) {
			return false;
		}
		if( !mRuleset.result.can_hit_split_aces ) {
			if( mHandGroups[Who.PLAYER].mHands.size() >= 2 && Blackjack.get_card_rank_number(cards.get(0)) == 1 ) {
				return false;
			}
		}
		// TB TODO - If you have split aces, then you can't hit (if that rule is set)
		// We still need to wait for user input since he may want to split further on aces.
		return true; 
	}
	boolean canStand() {
		if( mIsWaitingForServer || mIsGameBusy ) {
			return false;
		}
		if( mGameState != BlackjackGameState.WAIT_USER_COMMAND ) {
			return false;
		}
		// Don't check areHandsRemaining(), since we could have blackjack and the dealer shows an ace, in which case 
		// the user must either stand or choose insurance.
		return true; 
	}
	boolean canSplit() {
		if( mIsWaitingForServer || mIsGameBusy ) {
			return false;
		}
		if( mGameState != BlackjackGameState.WAIT_USER_COMMAND ) {
			return false;
		}
		if( !areHandsRemaining() ) {
			return false;
		}
		if( mDealResult.next_hand == mHandGroups[Who.PLAYER].mHands.size() ) {
			Log.e("ARGH","ABOUT TO CRASH!!!");
		}
		List<String> cards = mHandGroups[Who.PLAYER].mHands.get( mDealResult.next_hand ).mCards;
		if( cards.size() == 2 && 
				Blackjack.get_card_rank_number(cards.get(0)) == Blackjack.get_card_rank_number(cards.get(1)) && 
				mHandGroups[Who.PLAYER].mHands.size() <= mRuleset.result.max_split_count ) { 
			return true; 
		}
		return false;
	}
	boolean canInsurance() {
		if( mIsWaitingForServer || mIsGameBusy ) {
			return false;
		}
		if( mGameState != BlackjackGameState.WAIT_USER_COMMAND ) {
			return false;
		}
		if( !areHandsRemaining() ) {
			return false;
		}
		if( mActions.size() != 0 ) {
			return false;
		}
		List<String> cards = mHandGroups[Who.DEALER].mHands.get( 0 ).mCards;
        if( Blackjack.get_card_rank_number(cards.get(1)) == 1 && mTotalCardsDealt == 4 ) {
        	return true;
        } 
		return false;
	}
	
	void tryNetCommandTask( int command ) {
    		
		long cost = getCommandCost(command);
		if( cost > 0 ) {
			BitcoinVideoCasino bvc = BitcoinVideoCasino.getInstance(this);
			if( bvc.mIntBalance - cost < 0 ) {
				handleNotEnoughCredits();
				return;
			} 
		}
		mNetCommandTask = new NetCommandTask(this, command);
    	mNetCommandTask.execute(); 
	}
	
	public void onDouble(View button) {
		if( !canDouble() ) {
			return;
		}
		tryNetCommandTask( Blackjack.Command.DOUBLE );
	}
	public void onSplit(View button) {
		if( !canSplit() ) {
			return;
		}
		tryNetCommandTask( Blackjack.Command.SPLIT );
	}
	public void onStand(View button) {
		if( !canStand() ) {
			return;
		}
		tryNetCommandTask( Blackjack.Command.STAND );
	}
	public void onHit(View button) {
		if( !canHit() ) {
			return;
		}
		tryNetCommandTask( Blackjack.Command.HIT );
	}
	public void onInsurance(View button) {
		if( !canInsurance() ) {
			return;
		}
		playSound(mSoundBoop);
		tryNetCommandTask( Blackjack.Command.INSURANCE );
	}
	void handleNotEnoughCredits()
	{
		Toast.makeText(this, "Please deposit more credits", Toast.LENGTH_SHORT).show();
		setAuto(false); 
	} 
	public void onDeal(View button) {
		if( !canDeal() ) {
			return;
		}
		tryNetCommandTask( Blackjack.Command.DEAL );
		
		mCreditConversionHint.setVisibility(View.INVISIBLE);
		mMaxBetHint.setVisibility(View.INVISIBLE);
	}
	public void setAuto( boolean val ) {
		mIsAutoOn = val;
		updateControls();
		
		if( val == true ) {
			mIsFirstAutoAction = true;
			checkAuto();
		}
	}
	public void onAuto(View button) {
		
		if( mIsAutoOn ) {
			setAuto( false );
			return;
		}
		
		final Dialog dialog = new Dialog(this);
		dialog.setContentView(R.layout.bj_auto);
		dialog.setTitle("Autoplay Settings");

		final Spinner speedSpinner = (Spinner) dialog.findViewById(R.id.speed_spinner);
		final Spinner insuranceSpinner = (Spinner) dialog.findViewById(R.id.insurance_spinner);
		
		speedSpinner.setSelection( mAutoSpeed );
		insuranceSpinner.setSelection( mAutoInsurance );
		
		Button playButton = (Button) dialog.findViewById(R.id.play_button);
		playButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mAutoSpeed = speedSpinner.getSelectedItemPosition();
				mAutoInsurance = insuranceSpinner.getSelectedItemPosition();
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
	private boolean canAuto() {
		if( mIsWaitingForServer || mIsGameBusy ) {
			return false;
		} 
		return true;
	}
	public void updateControls() {
		
		mDealButton.setImageResource( canDeal() ? R.drawable.button_draw : R.drawable.button_draw_off );
		mSplitButton.setImageResource( canSplit() ? R.drawable.button_split : R.drawable.button_split_off );
		mDoubleButton.setImageResource( canDouble() ? R.drawable.button_bjdouble : R.drawable.button_bjdouble_off );
		mHitButton.setImageResource( canHit() ? R.drawable.button_hit : R.drawable.button_hit_off );
		mStandButton.setImageResource( canStand() ? R.drawable.button_stand : R.drawable.button_stand_off );
		
		if( canInsurance() ) {
			// mInsuranceButton.setImageResource( canInsurance() ? R.drawable.button_insurance : R.drawable.button_insurance_off ); 
			 mInsuranceButton.setVisibility( View.VISIBLE );
			 mInsuranceButton.setImageResource( R.drawable.button_insurance );
		}
		/*
		else {
			 mInsuranceButton.setVisibility( View.INVISIBLE );
			 mInsuranceButton.setImageResource( R.drawable.button_insurance ); 
		}
		*/
		
		// We can't just check canDeal() since that also verifies bet size...
		// we need to enable the input even if the credit size is bad.
		if( mGameState == BlackjackGameState.WAIT_USER_DEAL && !mIsGameBusy && !mIsWaitingForServer ) {
			//mBetCreditsInput.setFocusable(true);
			mBetCreditsInput.setEnabled( true );
		}
		else {
			mBetCreditsInput.setEnabled( false ); 
			mBetCreditsInput.clearFocus();
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
	}
	
	private void doAuto()
	{
		if( !mIsAutoOn ) {
			return;
		} 
		
		switch( mGameState ) {
		case BlackjackGameState.WAIT_USER_DEAL:
			onDeal(null);
			break;
		case BlackjackGameState.WAIT_USER_COMMAND:
			// mCards is sometimes a list with 0 size???
			if( mHandGroups[Who.DEALER].mHands.get(0).mCards.size() < 2 ) {
				Log.e(TAG, "Gonna crash");
			}
			String dealerShows = mHandGroups[Who.DEALER].mHands.get(0).mCards.get(1);
			List<List<String>> playerHands = new ArrayList<List<String>>();
			for( int i = 0; i < mHandGroups[Who.PLAYER].mHands.size(); i++ ) {
				/*
				List<String> hand = new ArrayList<String>();
				mHandGroups[Who.Player].mHands.get(i).
				hand
				*/
				playerHands.add(mHandGroups[Who.PLAYER].mHands.get(i).mCards); 
			}
			// TB TODO 
			double takeInsuranceFreq = 0;
			if( mAutoInsurance == AutoInsurance.SOMETIMES ) {
				takeInsuranceFreq = 0.5;
			}
			else if( mAutoInsurance == AutoInsurance.ALWAYS ) {
				takeInsuranceFreq = 1.0;
			}
			long progressiveBet = 0;
			
			BitcoinVideoCasino bvc = BitcoinVideoCasino.getInstance(this);
			int command = Blackjack.player_action(dealerShows, playerHands, mDealResult.next_hand, mActions, takeInsuranceFreq, mRuleset.result.max_split_count, progressiveBet, mBet, bvc.mIntBalance );
			switch( command ) {
			case Blackjack.Command.HIT:
				if( !canHit() ) {
					throw new RuntimeException("Blackjack.player_action() recommending a invalid hit."); 
				}
				onHit(null);
				break;
			case Blackjack.Command.STAND:
				if( !canStand() ) {
					throw new RuntimeException("Blackjack.player_action() recommending a invalid stand."); 
				}
				onStand(null);
				break;
			case Blackjack.Command.DOUBLE:
				if( !canDouble() ) {
					throw new RuntimeException("Blackjack.player_action() recommending a invalid double."); 
				}
				onDouble(null);
				break;
			case Blackjack.Command.SPLIT:
				if( !canSplit() ) {
					throw new RuntimeException("Blackjack.player_action() recommending a invalid split."); 
				}
				onSplit(null);
				break;
			case Blackjack.Command.INSURANCE:
				if( !canInsurance() ) {
					throw new RuntimeException("Blackjack.player_action() recommending a invalid insurance."); 
				}
				onInsurance(null);
				break;
			default:
				throw new RuntimeException("Blackjack.player_action() recommending a invalid command.");
			} 
			break;
		}
	}
	
	private void checkAuto() {
		if( mIsWaitingForServer ) {
			Log.v(TAG, "Error: checkAuto() called while waiting for server.");
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
				doAuto();
	        }
	    }, delay);
	}
	
	class Hand {
		ViewGroup mHolder;
		ViewGroup mCountHolder;
		ViewGroup mCardHolder;
		List<String> mCards;
		private boolean mIsDone;
		int mScore;
		Hand() {
			// holder >> another_holder >> [ card_holder + count_holder ]
    		mHolder= (ViewGroup) getLayoutInflater().inflate( R.layout.bj_hand, null );
    		mCountHolder= (ViewGroup) mHolder.findViewById( R.id.count_holder );
    		mCardHolder= (ViewGroup) mHolder.findViewById( R.id.card_holder ); 
    		mCards = new ArrayList<String>();
    		mIsDone = false;
    		mScore = 0;
		}
		void end() {
			if( mIsDone ) {
				Log.v(TAG, "ERROR: Trying to finalize the same hand more than once!");
			}
			mIsDone = true;
			updateCount();
		}
		void drawResultText( Activity a, int res ) {
			ImageView text = new ImageView(a);
			text.setImageResource( res );
			text.setBackgroundColor( Color.argb(255,0,61,16) );
    		FrameLayout.LayoutParams layout = new FrameLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    		layout.gravity = Gravity.CENTER;
    		// We want to center over the card, so we need to go down a little bit from the center of the 
    		// Can't use the mCardHolder, since the width is not big enough to 
    		layout.topMargin = mCountHolder.getHeight() / 2;
    		mHolder.addView(text, layout);
		}
		
		private void updateCount() {
			mCountHolder.removeAllViews();
			// TB TODO - Aces are 1 or 11. Can just keep track of how many aces we've seen, and then check X and X+10 if we've seen an ace.
			// TB TODO - Iterating thru all cards every time is retarded
			boolean hasAce = false;
			int val = 0;
			boolean showNothing = false;
			for( int i = 0; i < mCards.size(); i++ ) {
			    int rank = Blackjack.get_card_rank_number(mCards.get(i));
			    if( rank == 0 ) {
			    	// Dealer will initially be showing a face down card, in which case we should just show no number.
			    	showNothing = true;
			    }	
			    if( rank == 1 ) {
			    	hasAce = true;
			    }
				val += rank;
			}
			if( val == 0 || showNothing ) {
				// Adding a blank image on the top prevents the bottom card from filling that area with a bigger card image.
				addImageToViewGroup( R.drawable.letter_blank, mCountHolder, null );
				return;
			}
			if( hasAce ) {
				if( val + 10 == 21 ) {
					// Don't show 2 alternatives if you clearly have 21
					val = 21;
				}
				if( mIsDone && val + 10 <= 21 ) {
					val = val + 10;
				} 
			}
			mScore = val;
			addNumberToViewGroup( val, mCountHolder ); 
			
			if( !mIsDone && hasAce && val + 10 <= 21 ) {
		    	LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
		    	int margin = (int) TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());
		    	layout.setMargins( margin, 0, margin, 0 );
				addImageToViewGroup( R.drawable.letter_or, mCountHolder, layout ); 
		    	
				addNumberToViewGroup( val + 10, mCountHolder ); 
			}
			
		}
		public void addCard( String card ) {
    		ImageView view = (ImageView) getLayoutInflater().inflate( R.layout.bj_card, null );
    		view.setImageBitmap( mBitmapCache.getBitmap(getCardResourceFromCard(card)));
    		
    		//FrameLayout.LayoutParams layout = new FrameLayout.LayoutParams(view.getLayoutParams());
    		//FrameLayout.LayoutParams layout = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    		FrameLayout.LayoutParams layout = new FrameLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    		//layout.setMargins( mCards.size() * 40, 0, 0, 0);
    		
    		mCardHolder.addView(view, layout);
    		
    		mCards.add(card);
    		//scaleContents(view, mCardHolder);
			updateCount();
		}
		public void removeCards() {
			mCountHolder.removeAllViews();
			mCardHolder.removeAllViews();
			mCards.clear();
		}
		private void setCardsColorFilter( ColorFilter cf ) {
			for( int i = 0; i < mCardHolder.getChildCount(); i++) {
				ImageView img = (ImageView) mCardHolder.getChildAt(i);
				img.setColorFilter(cf);
			} 
		}
		public void setGray( boolean gray ) {
			ColorFilter cf = null;
			if( gray ) { 
				cf = new PorterDuffColorFilter(Color.argb(255, 128, 128, 128), Mode.MULTIPLY);
			}
			setCardsColorFilter(cf);
		}
	}
	
	class HandGroup {
		ViewGroup mHolder;
		List<Hand> mHands;
		
		HandGroup( int resource ) {
			mHands = new ArrayList<Hand>();
			mHolder = (ViewGroup) findViewById( resource );	
		}
		void addHand()
		{
			// TB TODO
			Hand hand = new Hand();
			
			mHolder.addView(hand.mHolder);
    		//scaleContents(hand.mHolder, mHolder);
			mHands.add( hand );
			
			
			// Resize the main hand containers so that they fill up the entire space
			// [           1 hand           ]
			// [  2 hands    ] [ 2  hands   ]
			// etc
			// TB TODO - Not sure why a linearlayout with weight of 0 does not do this properly when items are dynamically added...
			for( int i = 0; i < mHands.size(); i++ ) {
				ViewGroup handHolder = mHands.get(i).mHolder;
				FrameLayout.LayoutParams layout = (FrameLayout.LayoutParams) handHolder.getLayoutParams();
				layout.width = mContents.getWidth() / mHands.size();
				layout.leftMargin = i * (mContents.getWidth() / mHands.size());
				handHolder.setLayoutParams(layout);
				//int bgColor = i == 0 ? Color.rgb(0,255,0) : Color.rgb(255,255,255);
				//handHolder.setBackgroundColor( bgColor );
			}
		}
		void addCard( int hand_index, String card )
		{
			Hand currentHand = mHands.get(hand_index);
			currentHand.addCard(card);
			//mHolder.recomputeViewAttributes(null);
			
			double nextCardMarginSpacing = 0.3;
			int biggestHand = 0;
			for( int i = 0; i< mHands.size(); i++ ) {
				if( mHands.get(i).mCards.size() > biggestHand ) {
					biggestHand = mHands.get(i).mCards.size();
				}
			}
			int handHolderWidth = mContents.getWidth() / mHands.size();
			int cardWidth = 0;
			for( int i = 5; i < 20; i++ ) {
				int tryWidth = mContents.getWidth() / i;
				// See if the cards at that size will fit into a hand holder.
				if( tryWidth + (biggestHand-1) * (tryWidth * nextCardMarginSpacing) < handHolderWidth - 10 ) {
					// Also the resulting card height must fit
					final double cardWidthToHeight = 1.4516129;
					if( tryWidth * cardWidthToHeight < mHandGroups[Who.PLAYER].mHolder.getHeight() ) {
						cardWidth = tryWidth;
						break;
					}
				}
			}
			
			for( int h = 0; h < mHands.size(); h++ ) {
				Hand hand = mHands.get(h);
				ViewGroup handHolder = hand.mHolder;
				for( int i = 0; i < hand.mCardHolder.getChildCount(); i++) {
					ImageView c = (ImageView)hand.mCardHolder.getChildAt(i);
					FrameLayout.LayoutParams p = (FrameLayout.LayoutParams) c.getLayoutParams();
					p.width = cardWidth;
		    		p.setMargins( i * (int)(cardWidth * nextCardMarginSpacing), 0, 0, 0);
					c.setLayoutParams(p);
					//c.getDrawable().setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);
				}
				
				// Expand the gray area to fit
				// TB TODO - Move to makeGray function
				/*
				LinearLayout.LayoutParams grayerLayout = (LinearLayout.LayoutParams) hand.mCardHolderGrayer.getLayoutParams();
				grayerLayout.width = (int)( cardWidth + ( (hand.mCardHolder.getChildCount()-1) * cardWidth * nextCardMarginSpacing));
				hand.mCardHolderGrayer.setLayoutParams(grayerLayout);
				*/
				
				//hand.setGray(true);
			}
			
		}
		void reset()
		{
			mHands.clear();
			mHolder.removeAllViews();
		}
		/*
		void makeAllHandsGray() {
    		for( int i = 0; i < mHandGroups[Who.PLAYER].mHands.size(); i++ ) {
	    		mHands.get(i).setGray(true);
    		}
		}
		*/
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
    		return mBVC.blackjackReseed();
    	}
    	public void onSuccess(JSONReseedResult result) {
    		mServerSeedHash = result.server_seed_hash;
    		updateControls();
    		checkConnectingAlert();
    		if( mConnectingDialog == null && mAutodeal ) {
    			onDeal(null);
    		}
    	}
    }
    
	class ShowCardsRunnable implements Runnable {
		String[] mCards;
    	int mCardIndex;
    	int mHandIndex;
    	Runnable mFinishedCallback;
    	int mCommand;
    	ShowCardsRunnable( int command, String[] cards, int handIndex, Runnable finishedCallback ) {
    		mCardIndex = 0;
    		mFinishedCallback = finishedCallback;
    		mCards = cards;
    		mCommand = command;
    		mHandIndex = handIndex;
    		
    	}
    	
    	public void run() {
    		// TB TODO - Correct hand and playerDone values!
    		boolean playerDone = mHandIndex == mHandGroups[Who.PLAYER].mHands.size();
    		int handGroup = 0;
    		if( mTotalCardsDealt == 0 || mTotalCardsDealt == 2 ) {
    			handGroup = Who.PLAYER; 
    		}
    		else if( mTotalCardsDealt == 1 || mTotalCardsDealt == 3 ) {
    			handGroup = Who.DEALER; 
    		}
    		else {
    			handGroup = playerDone ? Who.DEALER : Who.PLAYER;
    		}
    		if( mCommand == Blackjack.Command.PLAYOUT_DEALER ) {
    			handGroup = Who.DEALER;
    		}
    		
    		for( int i = 0; i < mHandGroups[Who.PLAYER].mHands.size(); i++ ) {
	    		mHandGroups[Who.PLAYER].mHands.get(i).setGray(true);
    		}
    		if( !playerDone && mCommand != Blackjack.Command.PLAYOUT_DEALER ) {
	    		mHandGroups[Who.PLAYER].mHands.get(mHandIndex).setGray(false); 
    		}
			
			if( mCardIndex == mCards.length ) {
				if( mFinishedCallback != null ) {
					mFinishedCallback.run();
				}
				return;
			}
			
			// TB TODO - Fix this!!!
			// If you do a split, and then a double, it fucks up and puts two cards and hand #1 instead of one one each...
			Log.v("Deal player card", "mHandIndex=" + Integer.toString(mHandIndex));
    		int hand = 0;
			if( handGroup == Who.PLAYER ) {
				hand = mHandIndex;
			}
			mHandGroups[handGroup].addCard( hand, mCards[mCardIndex] );
			playSound( mSoundCardDeal );
			
			mCardIndex++;
			mTotalCardsDealt++;
			
			if( handGroup == Who.PLAYER ) {
				boolean goToNextHand = false;
				Hand currentHand = mHandGroups[handGroup].mHands.get(hand);
				if( Blackjack.is_21(currentHand.mCards)) {
					goToNextHand=true;
				}
				if( Blackjack.is_bust(currentHand.mCards)) {
					goToNextHand=true;
				}
				
				if( mCommand == Blackjack.Command.SPLIT ) {
					if( Blackjack.get_card_rank_number(currentHand.mCards.get(0)) == 1 ) {
						
						if( Blackjack.get_card_rank_number(currentHand.mCards.get(1)) == 1 ) {
						
							if( !mRuleset.result.can_resplit_aces ) {
								goToNextHand=true;
							}
						}
						if( !mRuleset.result.can_hit_split_aces ) {
							goToNextHand=true;
						}
					}
				}
				if( currentHand.mCards.size() == 3 && mActions.get( mActions.size() - 1 ) == Blackjack.Command.DOUBLE ) {
					goToNextHand=true;
				}
				if( goToNextHand ) {
					currentHand.end();
					mHandIndex++;
				}
			}
			
			int delay = 100;
			mHandler.postDelayed(this, delay);
    	}
	};
	
	@Override
    boolean shouldConnectingDialogShow()
    {
		boolean val = super.shouldConnectingDialogShow();
		if( val == true ) {
			return true;
		}
		
		return mRuleset == null;
    }
	
	
	void drawResultOfAllHands( final JSONBlackjackCommandResult result ) {
		
		HandGroup playerGroup = mHandGroups[Who.PLAYER];
		String[] splitEval = result.game_eval.split(",");
		for( int i = 0; i < playerGroup.mHands.size(); i++ ) {
			// TB TODO WIN/LOSE/BUST/BLACKACK
			// game_eval = comma separated, L,W, etc
			// result.game_eval.split
			String eval = splitEval[i];
			Log.v(TAG, "Eval:" + eval);
			
			int resource = 0;
			boolean makeGray = false;
			if( eval.contains("P") ) {
				resource = R.drawable.letter_push;
			}
			else if( eval.contains("BJ") ) {
				resource = R.drawable.letter_blackjack; 
			}
			else if( eval.contains("B") ) {
				resource = R.drawable.letter_bust;
				makeGray = true;
			}
			else if( eval.contains("W") ) {
				resource = R.drawable.letter_win; 
			}
			else {
				makeGray = true;
			}
			playerGroup.mHands.get(i).drawResultText(this, resource);
			playerGroup.mHands.get(i).setGray(makeGray);
		}
		
		int dealerScore = mHandGroups[Who.DEALER].mHands.get(0).mScore;
		if( dealerScore > 21 ) {
			mHandGroups[Who.DEALER].mHands.get(0).drawResultText( this, R.drawable.letter_bust );
		}
	} 
	
	
	
	void finishGame( final JSONBlackjackCommandResult result ) {
		mServerSeedHash = result.server_seed_hash; 
		final Activity that = this;
		
		Runnable localDone = new Runnable() {
			public void run() {
				// TB TODO - First do all this local_done() code when the dealer's hand has been shown.
				mHandGroups[Who.DEALER].mHands.get(0).end();
				long winnings = 0;
				for( int i = 0; i < result.prizes.length; i++ ) {
					winnings += result.prizes[i];
				}
				winnings += result.progressive_win;
				drawResultOfAllHands( result );
				
				if( winnings > 0 ) {
					mHandler.postDelayed( new Runnable() {
						public void run() {
							if( result.progressive_win > 0 ) {
								playSound(mSoundWinProgressive);
							}
							else {
								playSound(mSoundWin); 
							}
						}
					}, WIN_SOUND_DELAY );
				}
				
				// Counter updates at 20 fps, so /10 = 1 second of counting for a standard win (since you get back double your bet)
				// Winning doubles + multiple hands will increase this time
				long delta = (mBet / mCreditBTCValue) / 10;
				if( delta <= 0 ) {
					delta = 1;
				}
				delta *= mCreditBTCValue;
                if( mIsAutoOn ) {
                    delta = winnings;
                }
				mIsGameBusy = false;
		        startCountUpWins(winnings, (mUseFakeCredits ? result.fake_intbalance : result.intbalance) - winnings, delta);
		        
		        // TB TODO - Reduce bet size if no more credists?
		        
		        // Set the int balance in bvc so that checkAuto doesn't think you have 0 credits when it's still counting up to more
		        // (this would result in the game saying you should deposit more credits, despite you actually having a non-zero balance)
		        // Since startCountUpWins sets the credits to dirt, it won't mess with the balance display.
		        BitcoinVideoCasino bvc = BitcoinVideoCasino.getInstance(that);
	    		bvc.mIntBalance = result.intbalance;
				bvc.mFakeIntBalance = result.fake_intbalance;
				updateControls();
				checkAuto();
			} 
		};
		
		boolean playoutDealer = true;
		List<String> dealerHand = new ArrayList<String>();
		for( int i = 0; i < result.dealer_hand.length; i++ ) {
			dealerHand.add(result.dealer_hand[i]);
		}
		
        if( Blackjack.is_blackjack(dealerHand) && mHandGroups[Who.PLAYER].mHands.size() == 1 && Blackjack.is_blackjack( mHandGroups[Who.PLAYER].mHands.get(0).mCards )) {
        	// TB TODO - What is the point of this first condition? The second 
            playoutDealer = false;
        } 
        else if( mHandGroups[Who.PLAYER].mHands.size() == 1 && Blackjack.is_blackjack( mHandGroups[Who.PLAYER].mHands.get(0).mCards )) {
            playoutDealer = false;
        } 
        else {
            boolean allBust = true;
            for( int pi = 0; pi < mHandGroups[Who.PLAYER].mHands.size(); pi++ ) {
                if(!Blackjack.is_bust(mHandGroups[Who.PLAYER].mHands.get(pi).mCards)) {
                    allBust = false;
                    break;
                }
            }
            if(allBust) {
                playoutDealer = false;
            }
        }
		
		mHandGroups[Who.DEALER].reset();
		mHandGroups[Who.DEALER].addHand();
		if( playoutDealer ) {
			ShowCardsRunnable showCards = new ShowCardsRunnable(Blackjack.Command.PLAYOUT_DEALER, result.dealer_hand, 0, localDone);
			showCards.run();
		}
		else {
			mHandGroups[Who.DEALER].addCard( 0, result.dealer_hand[0] );
			mHandGroups[Who.DEALER].addCard( 0, result.dealer_hand[1] );
			// Don't need to deal anything. Can just hit up the callback immediately.
			localDone.run();
		}
		
		
	}
	
    void handleStandardResult( final int command, final int handIndex, final JSONBlackjackCommandResult result ) {
    	final Activity that = this;
    	String[] cards = new String[0];
    	if( result.cards != null ) {
			for( int i = 0; i < result.cards.length; i++ ) {
	    		Log.v(TAG, "Got card: " + result.cards[i] );
			}
			
			cards = result.cards;
			if( command == Blackjack.Command.DEAL ) {
				// Mix in the dealer cards for the deal
				cards = new String[] { result.cards[0], "back", result.cards[1], result.dealer_shows };
			}
    	}
		
		ShowCardsRunnable showCards = new ShowCardsRunnable(command, cards, handIndex, new Runnable() {
			public void run() {
				if( command == Blackjack.Command.INSURANCE ) {
					mInsuranceButton.setVisibility(View.INVISIBLE);
				}
				
				if( result.finished ) {
					mGameState = BlackjackGameState.WAIT_USER_DEAL;
					finishGame(result);
					
					if( command == Blackjack.Command.INSURANCE ) {
						mInsuranceWin.setVisibility(View.VISIBLE);
					}
				}
				else { 
					// finishGame may play out the dealer, so we need to wait for that before doing controls/auto/etc
					mIsGameBusy = false;
					updateControls(); 
					checkAuto();
					
					if( command == Blackjack.Command.INSURANCE ) {
						mInsuranceLose.setVisibility(View.VISIBLE);
					}
					
					// TB - intbalance seems to only get on /deal, and if the game is finished
					// We want to count up the wins on finished games, so don't set credits if finished
					if( command == Blackjack.Command.DEAL && !result.finished ) {
						BitcoinVideoCasino bvc = BitcoinVideoCasino.getInstance(that);
			    		bvc.mIntBalance = result.intbalance;
						bvc.mFakeIntBalance = result.fake_intbalance;
			    		updateCredits( mUseFakeCredits ? result.fake_intbalance : result.intbalance );
					}
				}
				
				if( handIndex < mHandGroups[Who.PLAYER].mHands.size() ) {
					Hand currentHand = mHandGroups[Who.PLAYER].mHands.get(handIndex);
					if( Blackjack.is_blackjack(currentHand.mCards)) {
						playSound(mSoundWinBlackjack);
					}
					if( canSplit() ) {
						playSound(mSoundCanSplit); 
					} 
				}
				
			}
		});
		showCards.run();
		
    }
    
    long getCommandCost( int command ) {
		long cost = 0;
		if( command == Blackjack.Command.DEAL || command == Blackjack.Command.DOUBLE || command == Blackjack.Command.SPLIT ) {
			cost = mBet;
		}
		else if( command == Blackjack.Command.INSURANCE ) {
			cost = mBet / 2;
		} 
		return cost;
    }
    
    
    class NetCommandTask extends NetAsyncTask<Integer, Void, JSONBlackjackCommandResult> {
    	
    	int mCommand;
    	int mHandIndex;
		    
    	NetCommandTask( CommonActivity a, int command ) { 
    		super(a);
			// TB TEMP TEST - Keep this running so that if the task is interrupted (phone call, etc), that the result
			// will still be shown.
			mAllowAbort = false;

    		long cost = getCommandCost(command);
    		if( cost > 0 ) {
				mCreditsAreDirty = true;
	    		updateCredits( (mUseFakeCredits ? mBVC.mFakeIntBalance : mBVC.mIntBalance) - cost );
    		}
    		
    		mCommand = command;
    		mIsWaitingForServer	= true;
    		mIsGameBusy = true;
    		mActions.add( command );
    		mHandIndex = mDealResult == null ? 0 : mDealResult.next_hand;
    		
    		// TB TODO - This is sloppy
			mInsuranceLose.setVisibility(View.INVISIBLE); 
			mInsuranceWin.setVisibility(View.INVISIBLE); 
    		
    		if( mCommand == Blackjack.Command.STAND ) {
				mHandGroups[Who.PLAYER].mHands.get(mHandIndex).end();
    			mHandIndex++;
    		}
    		
    		if( mCommand == Blackjack.Command.SPLIT ) {
    			HandGroup playerGroup = mHandGroups[Who.PLAYER];
    			String splitCard0 = playerGroup.mHands.get(mHandIndex).mCards.get(0);
    			String splitCard1 = playerGroup.mHands.get(mHandIndex).mCards.get(1);
    			playerGroup.mHands.get( mHandIndex ).removeCards();
    			playerGroup.addCard( mHandIndex, splitCard0 );
    			mHandGroups[Who.PLAYER].addHand();
    			mHandGroups[Who.PLAYER].addCard( playerGroup.mHands.size()-1, splitCard1 );
    			
	    		for( int i = 0; i < mHandGroups[Who.PLAYER].mHands.size(); i++ ) {
		    		mHandGroups[Who.PLAYER].mHands.get(i).setGray(true);
	    		}
	    		mHandGroups[Who.PLAYER].mHands.get(mHandIndex).setGray(false); 
    		}
    		
    		if( mCommand == Blackjack.Command.DEAL ) {
                stopCountUpWins();
			    resetGame();
				playSound( mSoundCoinPay ); 
				updateWin( 0, false ); 
    		}
    		
    		updateControls();
    		if( mCommand == Blackjack.Command.INSURANCE ) {
				mInsuranceButton.setImageResource( R.drawable.button_insurance_off ); 
    		}
    		else {
			    mInsuranceButton.setVisibility(View.INVISIBLE);
    		}
    	}
    	
    	public JSONBlackjackCommandResult go(Integer...v) throws IOException {
    		final String[] commandLookup = { "blackjack/deal", "blackjack/hit", "blackjack/stand", "blackjack/double", "blackjack/split", "blackjack/insurance" };
    		if( mCommand == Blackjack.Command.DEAL ) {
	    		long progressiveBet = 0;
	    		return mBVC.blackjackDeal(mBet, progressiveBet, mServerSeedHash, getClientSeed(), mUseFakeCredits);
    		}
    		else {
	    		return mBVC.blackjackCommand( commandLookup[mCommand], mGameID, mDealResult.next_hand ); 
    		}
    		
    	}
    	@Override
    	public void onError(final JSONBlackjackCommandResult result) {
            setAuto(false);
    		mIsGameBusy = false;
            updateControls();
    	}
    	@Override
    	public void onSuccess(final JSONBlackjackCommandResult result) {
    		if( result.error != null ) {
                mIsGameBusy = false;
                setAuto(false);
    			if( result.error.contains("need_seed") ) {
    				mServerSeedHash = null;
    				showConnectingDialog();
					mNetReseedTask = new NetReseedTask(mActivity, true);
			    	mNetReseedTask.execute( Long.valueOf(0) ); 
			    	resetGame();
    			}
    			else {
    				Log.e(TAG, "Unknown error returned by server:" + result.error);
                    handleError(result, String.format("Error from server: %s", result.error) );
    			}
                updateControls();
				return;
    		}
    		mDealResult = result;
	        mGameState = BlackjackGameState.WAIT_USER_COMMAND;
	        
    		if( mCommand == Blackjack.Command.DEAL ) {
	    		mGameID = result.game_id;
    		}
			handleStandardResult(mCommand, mHandIndex, result);
    		
    	}
    	
    	@Override
    	public void onDone() {
    		mIsWaitingForServer = false;
			mCreditsAreDirty = false;
    	}
    	
    }
    
    class NetRulesetTask extends NetAsyncTask<Long, Void, JSONBlackjackRulesetResult> {
		    
    	NetRulesetTask( CommonActivity a ) { 
    		super(a); 
    		mIsWaitingForServer	= true;
    	}
    	
    	public JSONBlackjackRulesetResult go(Long...v) throws IOException {
    		return mBVC.blackjackRuleset();
    	}
    	@Override
    	public void onSuccess(final JSONBlackjackRulesetResult result) {
    		mRuleset = result;
    		updateControls();
            checkConnectingAlert();
    	}
    	@Override
    	public void onDone() {
    		mIsWaitingForServer = false;
    	}
    }
    
    
}

