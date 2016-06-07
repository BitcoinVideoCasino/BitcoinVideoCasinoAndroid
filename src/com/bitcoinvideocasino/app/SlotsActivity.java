package com.bitcoinvideocasino.app;

import java.io.IOException;
import java.util.Random;


import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
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
import android.graphics.RectF;
import android.graphics.Point;
import java.util.Map;
import java.util.ArrayList;
import android.content.Intent;
import android.graphics.Path;
import com.bitcoinvideocasino.lib.*;
import com.bitcoinvideocasino.R;

class Win {
    int mLineID;
    int mNumSymbols;
    int mSymbolID;
    int mPrize;
}
class LineDescription {
    int mColor;
    int mStartingY;
    Point[] mDeltas;
    public LineDescription( String col, int startingY, Point[] deltas ) {
        mColor = Color.parseColor(col);
        mStartingY = startingY;
        mDeltas = deltas;
    }
}

public class SlotsActivity extends GameActivity {
	
	class SlotsGameState extends GameState {
		final static public int WAIT_USER_PULL = 0;
	}
	class AutoMode {
		final static public int STANDARD = 0;
	}

    int mLines;
	long mProgressiveJackpot;
	int mAutoMode;
	int mAutoSpeed;

    LineDescription[] mLineDescriptions;

    private NetRulesetTask mNetRulesetTask;
	private NetReseedTask mNetReseedTask;
	private NetPullTask mNetPullTask;
	private NetUpdateTask mNetUpdateTask;
	final private String TAG = "SlotsActivity";

	private JSONSlotsPullResult mPullResult;
    private Win[] mWins;
    private int mFreeSpinsLeft;
    private JSONSlotsRulesetResult mRuleset;
	private SymbolCol[] mSymbolCols;
	private ImageButton mPullButton;
	private ImageButton mAutoButton;
    private ImageButton mLinesButton;
    private SurfaceView mSurfaceHolder;
    private TextView mLinesText;
    private TextView mLineWinPays;
    private TextView mWinSummary;
    private TextView mPlayTwentyLines;
    private TextView mJackpotText;
    private FrameLayout mWinInfoBox;
	private int mSoundCoinPay;
	private int mSoundBoop;
	private int mSoundWin;
    private int mSoundWinScatter;
    private int mSoundFreeSpin;
	private Slots mSlots;
    public ShowReelsRunnable mShowReelsRunnable;

    public static final int NUM_ROWS = 3;
    public static final int NUM_COLS = 5;
    public static final int SYMBOL_HEIGHT = 114;
    public static final int SYMBOL_WIDTH = 114;
    public static final int NUM_SYMBOLS = 10;
    public static final int MAX_LINES = 20;
    public static final int NUM_SPINNING_SYMBOLS = 32;
    public static final int SCATTER_SYMBOL = 0;
    public static final int SCATTERS_FOR_PRIZE = 2;
    public static final int COLUMN_DIVIDER_WIDTH = 2;
    public static final int WIDGET_WIDTH = 31;
    public static final int WIDGET_HEIGHT = 24;
    final private String SL_SETTING_CREDIT_BTC_VALUE = "sl_credit_btc_value";

    private static final int WIN_REVEAL_STATE_SHOW_ALL = 0;
    private static final int WIN_REVEAL_STATE_SHOW_SCATTERS = 1;
    private static final int WIN_REVEAL_STATE_SHOW_INDIVIDUAL_LINES = 2;
    private static final int WIN_REVEAL_STATE_DONE = 3;
    private int mWinRevealState;
    private int mTimeSinceLineBlink;
    private int mNumLineBlinks;
    private int mCurrentWinningLine;
    private boolean mLineBlinkOn;
    public int mBackgroundColor;

    static public Point mWidget0TopLeft;
    static public Point mWidget0BottomRight;
    static public Point mWidget1TopLeft;
    static public Point mWidget1BottomRight;
    Widget[] mWidgets;

    Bitmap mOffscreenBitmap;
    Canvas mOffscreenCanvas;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_slots);

        Log.v(TAG, "Starting free memory: " + String.valueOf(MemoryUsage.getFreeMemory(this)));
        
        BitcoinVideoCasino bvc = BitcoinVideoCasino.getInstance(this);
        mTimeUpdateDelay = 50;
        mGameState = SlotsGameState.WAIT_USER_PULL;
		mSlots = new Slots();
		mProgressiveJackpot = -1;
		mAutoMode = AutoMode.STANDARD;
		mAutoSpeed = AutoSpeed.MEDIUM;
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        mLines = 1;
        mWins = null;
        mFreeSpinsLeft = 0;
        mWinRevealState = SlotsActivity.WIN_REVEAL_STATE_DONE;
        mBackgroundColor = getResources().getColor(R.color.s_bg);
        mShowReelsRunnable = null;

        // This size fits #game_holder -- the widget boxes and the symbols.
        // +4 to account for the top and bottom 2 pixel borders
        mOffscreenBitmap = Bitmap.createBitmap(640,342+4, Bitmap.Config.ARGB_8888);
        mOffscreenCanvas = new Canvas(mOffscreenBitmap);

        // Starting value (0.001 BTC) gets set in GameActivity::onCreate()
        mCreditBTCValue = sharedPref.getLong(SL_SETTING_CREDIT_BTC_VALUE, mCreditBTCValue);
        updateBTCButton(mCreditBTCValue);

		mPullButton = (ImageButton) findViewById( R.id.pull_button );
		mAutoButton = (ImageButton) findViewById( R.id.auto_button );
        mLinesButton = (ImageButton) findViewById( R.id.lines_button );
        mLinesText = (TextView) findViewById( R.id.lines );
        mLineWinPays = (TextView) findViewById( R.id.line_win_pays );
        mWinSummary = (TextView) findViewById( R.id.win_summary );
        mPlayTwentyLines = (TextView) findViewById( R.id.play_twenty_lines );
        mWinInfoBox = (FrameLayout) findViewById( R.id.win_info_box );
        mJackpotText = (TextView) findViewById( R.id.jackpot );

        mSurfaceHolder = (SurfaceView) findViewById( R.id.surface_holder );

        mLinesButton.setOnTouchListener( new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN){
                if( !canChangeLines() ) {
                    return true;
                }
                // Log.v(TAG, String.format("%d,%d    --  %f,%f", mLinesButton.getWidth(), mLinesButton.getHeight(), event.getX(), event.getY()) );
                int centerX = mLinesButton.getWidth() / 2;
                if( event.getX() > centerX ) {
                    mLines += 1;
                    if( mLines > SlotsActivity.MAX_LINES ) {
                        mLines = SlotsActivity.MAX_LINES;
                    }
                }
                else {
                    mLines -= 1;
                    if( mLines < 1 ) {
                        mLines = 1;
                    }
                }

                handleLinesChanged();
            }
            return true;
        }
        });

        mSoundCoinPay = mSoundPool.load(this,  R.raw.coinpay, 1);
		mSoundBoop = mSoundPool.load(this, R.raw.boop, 1 );
		mSoundWin = mSoundPool.load(this, R.raw.win1, 1);
        mSoundWinScatter = mSoundPool.load(this, R.raw.slot_machine_win_22, 1);
        mSoundFreeSpin = mSoundPool.load(this, R.raw.slot_machine_bet_10, 1);

        mBitmapCache.addBitmap( R.drawable.s_symbols0_spinning );
        mBitmapCache.addBitmap( R.drawable.s_symbols0 );

        mSymbolCols = new SymbolCol[NUM_COLS];
        mSymbolCols[0] = new SymbolCol(this, mBitmapCache, 0);
        mSymbolCols[1] = new SymbolCol(this, mBitmapCache, 1);
        mSymbolCols[2] = new SymbolCol(this, mBitmapCache, 2);
        mSymbolCols[3] = new SymbolCol(this, mBitmapCache, 3);
        mSymbolCols[4] = new SymbolCol(this, mBitmapCache, 4);
		mMixpanel.track("slots_activity_create", null);

        mWidget0TopLeft = new Point( 0, 0 );
        mWidget0BottomRight = new Point( mWidget0TopLeft.x + SlotsActivity.WIDGET_WIDTH, mWidget0TopLeft.y + SlotsActivity.SYMBOL_HEIGHT * SlotsActivity.NUM_ROWS );
        mWidget1TopLeft = new Point( mWidget0BottomRight.x + SlotsActivity.SYMBOL_WIDTH * SlotsActivity.NUM_COLS + SlotsActivity.COLUMN_DIVIDER_WIDTH * (SlotsActivity.NUM_COLS-1), mWidget0TopLeft.y );
        mWidget1BottomRight = new Point( mWidget1TopLeft.x + SlotsActivity.WIDGET_WIDTH, mWidget0BottomRight.y );

        buildLineDescriptions();

        mWidgets = new Widget[SlotsActivity.MAX_LINES];
        mWidgets[0] = new Widget(0,161,true,mLineDescriptions[0].mColor);
        mWidgets[1] = new Widget(1,44,true,mLineDescriptions[1].mColor);
        mWidgets[2] = new Widget(2,278,true,mLineDescriptions[2].mColor);
        mWidgets[3] = new Widget(3,0,true,mLineDescriptions[3].mColor);
        mWidgets[4] = new Widget(4,322,true,mLineDescriptions[4].mColor);
        mWidgets[5] = new Widget(5,66,true,mLineDescriptions[5].mColor);
        mWidgets[6] = new Widget(6,256,true,mLineDescriptions[6].mColor);
        mWidgets[7] = new Widget(7,183,true,mLineDescriptions[7].mColor);
        mWidgets[8] = new Widget(8,139,true,mLineDescriptions[8].mColor);
        mWidgets[9] = new Widget(9,22,true,mLineDescriptions[9].mColor);
        mWidgets[10] = new Widget(10,300,true,mLineDescriptions[10].mColor);
        mWidgets[11] = new Widget(11,256,false,mLineDescriptions[11].mColor);
        mWidgets[12] = new Widget(12,83,false,mLineDescriptions[12].mColor);
        mWidgets[13] = new Widget(13,322,false,mLineDescriptions[13].mColor);
        mWidgets[14] = new Widget(14,0,false,mLineDescriptions[14].mColor);
        mWidgets[15] = new Widget(15,150,false,mLineDescriptions[15].mColor);
        mWidgets[16] = new Widget(16,172,false,mLineDescriptions[16].mColor);
        mWidgets[17] = new Widget(17,278,false,mLineDescriptions[17].mColor);
        mWidgets[18] = new Widget(18,61,false,mLineDescriptions[18].mColor);
        mWidgets[19] = new Widget(19,300,false,mLineDescriptions[19].mColor);
        mWidgets[0].mIsOn = true;

        mSurfaceHolder.getHolder().addCallback( new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                // TB - Is this where the intro stuff should go???
                clearCanvas(mOffscreenCanvas);
                drawAllSymbols(mOffscreenCanvas);
                blitOffscreenBitmap();



                // TB TEMP TEST!
                /*
                Widget w0 = new Widget( 0, 10, true );
                Widget w1 = new Widget( 1, 100, true );
                Widget w2 = new Widget( 0, 100, false );
                Widget w3 = new Widget( 0, 200, false );
                w0.draw(mOffscreenCanvas, true);
                w1.draw(mOffscreenCanvas, true);
                w2.draw(mOffscreenCanvas, true);
                w3.draw(mOffscreenCanvas, true);
                blitOffscreenBitmap();
                */
            }
            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }
        });

        updateControls();
        
        Log.v(TAG, "Ending free memory: " + String.valueOf(MemoryUsage.getFreeMemory(this)));
        updateCredits( mUseFakeCredits ? bvc.mFakeIntBalance : bvc.mIntBalance );
    }

    void drawPlayingLines()
    {
        for( int i = 0; i < mLines; i++ ) {
            drawLine(mOffscreenCanvas, i);
        }
    }
    void handleLinesChanged() {
        updateControls();
        mWinRevealState = WIN_REVEAL_STATE_DONE;

        for( int i = 0; i < mWidgets.length; i++ ) {
            mWidgets[i].mIsOn = i+1 <= mLines;
            mWidgets[i].mIsWin = false;
        }

        clearCanvas( mOffscreenCanvas );
        drawAllSymbols(mOffscreenCanvas );
        drawPermanentCanvasElements(mOffscreenCanvas);
        drawPlayingLines();
        blitOffscreenBitmap();
    }
    void verifyLineDescriptions()
    {
        for( int i = 0; i < SlotsActivity.MAX_LINES; i++ ) {
            LineDescription line = mLineDescriptions[i];

            int xsize = 0;
            for( int seg = 0; seg < line.mDeltas.length; seg++ ) {
                xsize += line.mDeltas[seg].x;
                if( Math.abs(line.mDeltas[seg].x) != Math.abs(line.mDeltas[seg].y) && line.mDeltas[seg].y != 0 ) {
                    Log.e(TAG, "Line #" + i + " has a segment that is not horz or 45 degs.");
                }
            }

            if( xsize != 582 ) {
                Log.e(TAG, "Line #" + i + " xsize is not 582. It is " + xsize);
            }
        }
    }
    void buildLineDescriptions() {
        mLineDescriptions = new LineDescription[MAX_LINES];
        int left_x = 0;
        int right_x = 582;
        int row0_middle_y = 55;
        int row1_middle_y = 171;
        int row2_middle_y = 286;
        int box0_middle_x = 90;
        int box1_middle_x = 200;
        int box2_middle_x = 310;
        int box3_middle_x = 420;
        int box4_middle_x = 550;

        /*
        ("#dc4408", ((left_x,row1_middle_y), (right_x,0))),
        ("#403c88", ((left_x,row0_middle_y-1), (right_x,0))),
        ("#004884", ((left_x,row2_middle_y+2), (right_x,0))),
        ("#fc9898", ((left_x,row0_middle_y-45), (15,0), (280, 280), (270,-270), (17,0))),
        ("#a468a4", ((left_x,row2_middle_y+46), (13,0), (290, -290), (260,260), (19,0))),
        */
        mLineDescriptions[0] = new LineDescription( "#dc4408", row1_middle_y, new Point[] { new Point(right_x,0) }  );
        mLineDescriptions[1] = new LineDescription( "#403c88", row0_middle_y-1, new Point[] { new Point(right_x,0) }  );
        mLineDescriptions[2] = new LineDescription( "#004884", row2_middle_y+2, new Point[] { new Point(right_x,0) }  );
        mLineDescriptions[3] = new LineDescription( "#fc9898", row0_middle_y-45, new Point[] { new Point(15,0), new Point(280,280), new Point(270,-270), new Point(17,0) }  );
        mLineDescriptions[4] = new LineDescription( "#a468a4", row2_middle_y+46, new Point[] { new Point(13,0), new Point(290,-290), new Point(260,260), new Point(19,0) }  );
        /*
        # 5
        ("#f4ac00", ((left_x,row0_middle_y+21), (195,0), (180, 180), (207,0))),
        ("#80005c", ((left_x,row2_middle_y-20), (205,0), (230, -230), (147,0))),
        ("#489430", ((left_x,row1_middle_y+22), (35,0), (170,-170), (220, 220), (120,-120), (37,0))),
        ("#fce000", ((left_x,row1_middle_y-22), (39,0), (170,170), (220, -220), (120,120), (33,0))),
        ("#c8d4d8", ((left_x,row0_middle_y-23), (50,0), (170,170), (225, 0), (93,93), (44,0))),
        */
        mLineDescriptions[5] = new LineDescription( "#f4ac00", row0_middle_y+21, new Point[] { new Point(195,0),new Point(180,180),new Point(207,0) }  );
        mLineDescriptions[6] = new LineDescription( "#80005c", row2_middle_y-20, new Point[] { new Point(205,0),new Point(230,-230),new Point(147,0) }  );
        mLineDescriptions[7] = new LineDescription( "#489430", row1_middle_y+22, new Point[] { new Point(35,0),new Point(170,-170),new Point(220,220),new Point(120,-120),new Point(37,0)} );
        mLineDescriptions[8] = new LineDescription( "#fce000", row1_middle_y-22, new Point[] { new Point(39,0),new Point(170,170),new Point(220,-220),new Point(120,120),new Point(33,0)} );
        mLineDescriptions[9] = new LineDescription( "#c8d4d8", row0_middle_y-23, new Point[] { new Point(50,0),new Point(170,170),new Point(225,0),new Point(93,93),new Point(44,0)}  );
        /*
        # 10
        ("#6cc044", ((left_x,row2_middle_y+24), (55,0), (170,-170), (210, 0), (95,-95), (52,0))),
        ("#bca464", ((left_x,row1_middle_y-29), (35,0), (116,-116), (156, 0), (240,240), (35,0))),
        ("#24c0fc", ((left_x,row1_middle_y+32), (35,0), (120,120), (120, 0), (230,-230), (77,0))),
        ("#a43884", ((left_x,row1_middle_y-14), (199,0), (90,-90), (265,265), (28,0))),
        ("#94341c", ((left_x,row1_middle_y-41), (205,0), (110,110), (230,-230), (37,0))),
        */
        mLineDescriptions[10] = new LineDescription( "#6cc0ff", row2_middle_y+24, new Point[] { new Point(55,0),new Point(170,-170),new Point(210,0),new Point(95,-95),new Point(52,0) }  );
        mLineDescriptions[11] = new LineDescription( "#bca464", row1_middle_y-29, new Point[] { new Point(35,0),new Point(116,-116),new Point(156,0),new Point(240,240),new Point(35,0) }  );
        mLineDescriptions[12] = new LineDescription( "#24c0fc", row1_middle_y+32, new Point[] { new Point(35,0),new Point(120,120),new Point(120,0),new Point(230,-230),new Point(77,0)}  );
        mLineDescriptions[13] = new LineDescription( "#a43884", row1_middle_y-14, new Point[] { new Point(199,0),new Point(90,-90),new Point(265,265),new Point(28,0)}  );
        mLineDescriptions[14] = new LineDescription( "#94341c", row1_middle_y-41, new Point[] { new Point(205,0),new Point(110,110),new Point(230,-230),new Point(37,0)}  );
        /*
        # 15
        ("#548890", ((left_x,row0_middle_y+5), (195,0), (220,220), (120,-120), (47,0))),
        ("#d04c4c", ((left_x,row2_middle_y+16), (205,0), (230,-230), (110,110), (37,0))),
        ("#2894d8", ((left_x,row1_middle_y+27), (45,0), (150,-150), (240, 240), (147,0))),
        ("#64bc7c", ((left_x,row1_middle_y-35), (40,0), (170,170), (235, -235), (137,0))),
        ("#641084", ((left_x,row0_middle_y-10), (255,0), (265,265), (62,0))),
        */
        mLineDescriptions[15] = new LineDescription( "#548890", row0_middle_y+5, new Point[] { new Point(195,0),new Point(220,220),new Point(120,-120),new Point(47,0) }  );
        mLineDescriptions[16] = new LineDescription( "#d04c4c", row2_middle_y+16, new Point[] { new Point(205,0),new Point(230,-230),new Point(110,110),new Point(37,0)}  );
        mLineDescriptions[17] = new LineDescription( "#2894d8", row1_middle_y+27, new Point[] { new Point(45,0),new Point(150,-150),new Point(240,240),new Point(147,0)}  );
        mLineDescriptions[18] = new LineDescription( "#64bc7c", row1_middle_y-35, new Point[] { new Point(40,0),new Point(170,170),new Point(235,-235),new Point(137,0)}  );
        mLineDescriptions[19] = new LineDescription( "#641084", row0_middle_y-10, new Point[] { new Point(255,0),new Point(265,265),new Point(62,0)}  );

        verifyLineDescriptions();
    }

    @Override
    public void onDestroy() {
    	super.onDestroy();
    	mBitmapCache.clear();
    }
    
	void timeUpdate() {
		super.timeUpdate();
		if( canPull() ) {
			if( mBlinkOn ) {
				mPullButton.setImageResource(R.drawable.button_spin_bright);
			}
			else {
				mPullButton.setImageResource(R.drawable.button_spin);
			}
		}

        if( mIsGameBusy ) {
            clearCanvas( mOffscreenCanvas );
            for( SymbolCol col : mSymbolCols ) {
                if( col.mIsSpinning ) {
                    col.drawSpinning(mOffscreenCanvas);
                }
                else {
                    col.drawSymbols( mOffscreenCanvas );
                }
            }
            blitOffscreenBitmap();
        }

        if( !mIsGameBusy && !mIsWaitingForServer ) {
            if( mWinRevealState != WIN_REVEAL_STATE_DONE && mPullResult != null && (mPullResult.intwinnings > 0 || mPullResult.num_scatters >= SCATTERS_FOR_PRIZE )) {
                mTimeSinceLineBlink += mTimeUpdateDelay;

                clearCanvas( mOffscreenCanvas );
                drawAllSymbols(mOffscreenCanvas);
                String lineWinPays = "";
                if( mWinRevealState == WIN_REVEAL_STATE_SHOW_ALL ) {
                    for( Widget widget : mWidgets ) {
                        boolean widgetIsWin = false;
                        for( Win win : mWins ) {
                            if( win.mLineID == widget.mID ) {
                                widgetIsWin = true;
                            }
                        }
                        widget.mIsWin = widgetIsWin;
                    }
                    drawLines(mOffscreenCanvas,mWins);
                    if( mPullResult.num_scatters >= SCATTERS_FOR_PRIZE ) {
                        drawWinningScatters(mOffscreenCanvas);
                    }

                    if( mTimeSinceLineBlink > 1000 ) {
                        setRevealState( mPullResult.num_scatters >= SCATTERS_FOR_PRIZE ? WIN_REVEAL_STATE_SHOW_SCATTERS : WIN_REVEAL_STATE_SHOW_INDIVIDUAL_LINES );
                    }

                }
                else if( mWinRevealState == WIN_REVEAL_STATE_SHOW_SCATTERS ) {
                    for( Widget widget : mWidgets ) {
                        widget.mIsWin = false;
                    }

                    if( mPullResult.num_scatters < SCATTERS_FOR_PRIZE ) {
                        setRevealState(WIN_REVEAL_STATE_SHOW_INDIVIDUAL_LINES);
                    }
                    else {
                        int lineBlinkDelay = 200;
                        if( mLineBlinkOn ) {
                            lineBlinkDelay = 400;
                            drawWinningScatters(mOffscreenCanvas);
                            // TB TODO - Verify that this number is correct!!!
                            lineWinPays = String.format("SCATTER WIN PAYS %d", mPullResult.bonus_multiplier);
                        }
                        if( mTimeSinceLineBlink >= lineBlinkDelay ) {
                            mLineBlinkOn = !mLineBlinkOn;
                            mTimeSinceLineBlink = 0;
                            mNumLineBlinks++;

                            if( mNumLineBlinks > 5 && mWins.length > 0 ) {
                                setRevealState( WIN_REVEAL_STATE_SHOW_INDIVIDUAL_LINES );
                            }
                        }
                    }
                }
                else if( mWinRevealState == WIN_REVEAL_STATE_SHOW_INDIVIDUAL_LINES ) {
                    // TB TODO - Stuff with mLineWinPays
                    int lineBlinkDelay = 200;
                    if( mLineBlinkOn ) {
                        lineBlinkDelay = 400;
                        drawLine( mOffscreenCanvas, mWins[mCurrentWinningLine].mLineID );
                        drawLineWinningBox( mOffscreenCanvas, mWins[mCurrentWinningLine] );
                        lineWinPays = String.format("LINE WIN PAYS %d", mWins[mCurrentWinningLine].mPrize);
                    }
                    for( Widget widget : mWidgets ) {
                        widget.mIsWin = widget.mID == mWins[mCurrentWinningLine].mLineID;
                    }
                    if( mTimeSinceLineBlink >= lineBlinkDelay ) {
                        mLineBlinkOn = !mLineBlinkOn;
                        mTimeSinceLineBlink = 0;
                        mNumLineBlinks++;
                        if( mNumLineBlinks > 5 ) {
                            mCurrentWinningLine++;
                            if( mCurrentWinningLine == mWins.length ) {
                                if( mPullResult.num_scatters < SCATTERS_FOR_PRIZE ) {
                                    mCurrentWinningLine = 0;
                                }
                                else {
                                    setRevealState( WIN_REVEAL_STATE_SHOW_SCATTERS );
                                }
                            }
                            mNumLineBlinks = 0;
                        }
                    }
                }
                mLineWinPays.setText( lineWinPays );
                blitOffscreenBitmap();
            }
        }
        // TB TODO - Blink lines and scatters...
	}

    public void setRevealState( int state ) {
        mTimeSinceLineBlink = 0;
        mNumLineBlinks = 0;
        mCurrentWinningLine = 0;
        mLineBlinkOn = true;
        mWinRevealState = state;
        for( Widget widget : mWidgets ) {
            widget.mIsWin = false;
        }
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

    boolean canChangeLines() {
        if( mIsWaitingForServer || mIsGameBusy ) {
            return false;
        }
        if( mFreeSpinsLeft > 0 ) {
            return false;
        }
        return true;
    }
    public void onBetMax(View button) {
        if( !canChangeLines() ) {
            return;
        }
        mLines = SlotsActivity.MAX_LINES;
        handleLinesChanged();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK ) {
            if( mIsGameBusy || mIsWaitingForServer || mFreeSpinsLeft > 0 ) {
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

        // TB TEMP TEST - If the mShowReelsRunnable is stopped (Activity stopped), then if you were in the middle of showning the reels,
        // then it will never progress to showing the final result. But if you are no longer in the Activity, what will happen?
        // mHandler.removeCallbacks(mShowReelsRunnable);
		setAuto( false );
    }

    boolean canCreditBTC() {
        return canChangeLines();
    }

    public void onCreditBTC(View button) {
        if( !canCreditBTC() ) {
            return;
        }

        CreditBTCItem [] items = new CreditBTCItem[] {
                new CreditBTCItem("1 CREDIT = 0.01 BTC    ", "Win over 100 BTC!", Bitcoin.stringAmountToLong("0.01")),
                new CreditBTCItem("1 CREDIT = 0.005 BTC    ", "Win over 50 BTC!", Bitcoin.stringAmountToLong("0.005")),
                new CreditBTCItem("1 CREDIT = 0.001 BTC    ", "Win over 10 BTC!", Bitcoin.stringAmountToLong("0.001")),
                new CreditBTCItem("1 CREDIT = 0.0001 BTC   ", "Win over 1 BTC!", Bitcoin.stringAmountToLong("0.0001")) };
        showCreditBTCDialog( SL_SETTING_CREDIT_BTC_VALUE, items );
    }

    @Override
    public void handleCreditBTCChanged() {

        // Gotta reset the jackpot until we get the new value
        mProgressiveJackpot = -1;
        updateProgressiveJackpot(mProgressiveJackpot);

        // Get the progressive jackpot
        mNetUpdateTask = new NetUpdateTask(this);
        mNetUpdateTask.execute( Long.valueOf(0) );
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
    	super.onWindowFocusChanged(hasFocus);
        /*
    	if( !mDidScaleContents ) {
		    scaleContents(mContents, mContainer);
		    mDidScaleContents = true;
    	}
    	// Need to post this so that the UI elements are correctly sized from scaleContents.
    	// Otherwise all the size getting commands will return the old values...
		mHandler.post(new Runnable() {
			public void run() {
				constructPayouts();
	        }
	    });
	    */
    }
    void drawLineWinningBox( Canvas canvas, Win win ) {
        for( int i = 0; i < win.mNumSymbols; i++ ) {
            SymbolCol col = mSymbolCols[i];
            int row = mRuleset.result.lines[ win.mLineID ][i];
            col.drawWinningSymbol( canvas, row, mLineDescriptions[win.mLineID].mColor );
        }
    }
    void drawWinningScatters( Canvas canvas ) {
        // mPullResult.num_scatters;
        for( int col = 0; col < SlotsActivity.NUM_COLS; col++ ) {
            mSymbolCols[col].drawWinningScatters( canvas );
        }
    }

    void drawLine( Canvas canvas, int lineID )
    {
        final int LINE_STROKE_WIDTH = 6;
        Paint paint = new Paint();
        paint.setStrokeWidth( LINE_STROKE_WIDTH );

        LineDescription line = mLineDescriptions[ lineID ];
        Point pt = new Point( SlotsActivity.WIDGET_WIDTH-2, line.mStartingY );
        paint.setColor( line.mColor );

        /*
        Path path = new Path();
        path.moveTo( SlotsActivity.WIDGET_WIDTH-2, line.mStartingY );
        */

        for( int seg = 0; seg < line.mDeltas.length; seg++ ) {
            Point delta = line.mDeltas[seg];
            Point next = new Point( pt.x + delta.x, pt.y + delta.y );
            canvas.drawLine( pt.x, pt.y, next.x, next.y, paint );
            pt = next;

            /*
            path.rLineTo( delta.x, delta.y );
            */
        }

        /*
        canvas.drawPath(path,paint);
        */

    }
    void drawLines( Canvas canvas, int[] linesToDraw )
    {
        //for( int i = 0; i < linesToDraw.length; i++ ) {
        for( int i : linesToDraw ) {
            drawLine( canvas, linesToDraw[i] );
        }
    }
    void drawLines( Canvas canvas, Win[] wins ) {
        for( Win win : wins ) {
            drawLine( canvas, win.mLineID );
        }
    }

	void handleNotEnoughCredits()
	{
		Toast.makeText(this, "Please deposit more credits", Toast.LENGTH_SHORT).show();
		setAuto(false); 
	}

	public void onPull(View button) {
		if( mGameState == SlotsGameState.WAIT_USER_PULL ) {
			if( !canPull() ) {
				return;
			}
			BitcoinVideoCasino bvc = BitcoinVideoCasino.getInstance(this);

            if ( (mUseFakeCredits ? bvc.mFakeIntBalance : bvc.mIntBalance) - mCreditBTCValue < 0) {
                handleNotEnoughCredits();
                return;
            }

			if( mServerSeedHash == null ) {
				// TB TODO - Get another hash? Or maybe we're waiting for it still?
				return;
			}
			
			mNetPullTask = new NetPullTask(this);
	    	mNetPullTask.executeParallel();
		}
	}
	private boolean canPull() {
		if( mIsWaitingForServer || mIsGameBusy ) {
			return false;
		}
		// TB TODO - Should this also check if you have enough credits?
		// If so, then canAuto() and canPaytable() need to not depend on canDeal().
		return (mGameState == SlotsGameState.WAIT_USER_PULL );
	}
	private boolean canAuto() {
		return canPull();
	}
	public void updateControls() {
		
		if( canPull() ) {
			mPullButton.setImageResource(R.drawable.button_spin);
		}
		else {
			mPullButton.setImageResource(R.drawable.button_draw_off);
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

        mLinesText.setText( String.format("%d",mLines) );
        mTextBet.setText( "BET " + mLines );
	}
	
	
	private void doAuto()
	{
		if( !mIsAutoOn ) {
			return;
		}

        onPull(null);
	}
	
	private void checkAuto() {
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
				doAuto();
	        }
	    }, delay);
	}
	public void setAuto( boolean val ) {
		mIsAutoOn = val;
		updateControls();
		if( val == true ) {
			mIsFirstAutoAction = true;
			checkAuto();
		}
	}
    public void onHelp(View button) {
        Intent intent = new Intent(this, SlotsHelpActivity.class);
        startActivity(intent);
    }
	public void onAuto(View button) {
		if( mIsAutoOn ) {
			setAuto( false );
			return;
		}
		
		final Dialog dialog = new Dialog(this);
		dialog.setContentView(R.layout.s_auto);
		dialog.setTitle("Autoplay Settings");

		final Spinner speedSpinner = (Spinner) dialog.findViewById(R.id.speed_spinner);

		speedSpinner.setSelection( mAutoSpeed );

		Button playButton = (Button) dialog.findViewById(R.id.play_button);
		playButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mAutoSpeed = speedSpinner.getSelectedItemPosition();
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
        /*
		float val = mPoker.get_hand_prize_amount(5, bestHand) + (float)( progressiveJackpot/10000.0);
		return String.format("%.2f", val);
		*/
        if( mRuleset == null ) {
            return "10000";
        }

        // TB TODO - Need to get the proper prize from the rules, instead of hard coding it.
        //long base = mRuleset.result.paytable.get
        long base = 10000;

        float val = (float)( progressiveJackpot/10000.0);
        return String.format("%.2f", base + val);
	}
	void updateProgressiveJackpot( long progressiveJackpot )
	{
        mJackpotText.setText( getProgressiveJackpotString(progressiveJackpot) );
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

    void drawPostBlit( Canvas liveCanvas, float startX, float startY, float scaleX, float scaleY )
    {
        for( Widget widget : mWidgets ) {
            widget.drawPostBlit(liveCanvas, startX, startY, scaleX, scaleY);
        }
    }

    void blitOffscreenBitmap()
    {
        // TB TODO - This is ghetto, since there's no guarantee you painted anything to the buffer
        if( mSurfaceHolder == null || mOffscreenBitmap == null ) {
            return;
        }

        // TB TODO - This is all pretty ghetto
        Canvas canvas = null;
        try {
            canvas = mSurfaceHolder.getHolder().lockCanvas();
        }
        catch( NullPointerException e ) {
            return;
        }
        if( canvas == null ) {
            return;
        }

        // The background color
        Paint bg = new Paint();
        bg.setColor( mBackgroundColor );
        canvas.drawPaint(bg);


        // TB TODO - This is ghetto, since there's no guarantee you painted anything to the buffer
        if( canvas == null ) {
            return;
        }
        // TB TODO - Get correct size + don't create a new Paint every time!
        //canvas.drawBitmap(mOffscreenBitmap, 0, 0, new Paint() );

        // Also need to center it...
        float sourceAspectRatio = mOffscreenBitmap.getWidth() / mOffscreenBitmap.getHeight();
        float targetAspectRatio = mSurfaceHolder.getWidth() / mSurfaceHolder.getHeight();
        int destWidth = mSurfaceHolder.getWidth();
        int destHeight = mSurfaceHolder.getHeight();
        if( targetAspectRatio > sourceAspectRatio ) {
            destWidth = (int)(mSurfaceHolder.getHeight() * targetAspectRatio);
        }
        else {
            destHeight = (int)(mSurfaceHolder.getWidth() / targetAspectRatio);
        }

        int startX = (mSurfaceHolder.getWidth() - destWidth) / 2;
        int startY = (mSurfaceHolder.getHeight() - destHeight) / 2;

        Rect src = new Rect( 0, 0, mOffscreenBitmap.getWidth(), mOffscreenBitmap.getHeight() );
        // TB TODO -  Should ensure correct proportion and all that jazz
        //Rect dst = new Rect( 0, 0, mSurfaceHolder.getWidth(), mSurfaceHolder.getHeight() );
        Rect dst = new Rect( startX, startY, destWidth+startX, destHeight+startY);
        canvas.drawBitmap( mOffscreenBitmap, src, dst, new Paint() );
        float scaleX = (float)destWidth / mOffscreenBitmap.getWidth();
        float scaleY = (float)destHeight / mOffscreenBitmap.getHeight();
        drawPostBlit(canvas,startX,startY,scaleX,scaleY);
        mSurfaceHolder.getHolder().unlockCanvasAndPost(canvas);

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mWinInfoBox.getLayoutParams();
        params.width = destWidth;
        mWinInfoBox.setLayoutParams(params);
    }

    void startAllSpinning() {
        for( int col = 0; col < SlotsActivity.NUM_COLS; col++ ) {
            mSymbolCols[col].startSpinning();
        }
    }
    void abortAllSpinning() {
        for( int col = 0; col < SlotsActivity.NUM_COLS; col++ ) {
            mSymbolCols[col].abortSpinning();
        }
        clearCanvas(mOffscreenCanvas);
        drawAllSymbols(mOffscreenCanvas);
        blitOffscreenBitmap();
    }

    void drawWidgets( Canvas canvas ) {
        Paint paint = new Paint();

        paint.setColor( Color.parseColor("#6695b0") );
        Rect dst = new Rect( mWidget0TopLeft.x, mWidget0TopLeft.y, mWidget0BottomRight.x, mWidget0BottomRight.y );
        canvas.drawRect( dst, paint );

        paint.setColor( Color.parseColor("#135d87") );
        dst = new Rect( dst.left + SlotsActivity.COLUMN_DIVIDER_WIDTH, dst.top + SlotsActivity.COLUMN_DIVIDER_WIDTH, dst.right - SlotsActivity.COLUMN_DIVIDER_WIDTH, dst.bottom - SlotsActivity.COLUMN_DIVIDER_WIDTH );
        canvas.drawRect( dst, paint );

        // right
        paint.setColor( Color.parseColor("#6695b0") );
        dst = new Rect( mWidget1TopLeft.x, mWidget1TopLeft.y, mWidget1BottomRight.x, mWidget1BottomRight.y );
        canvas.drawRect( dst, paint );

        paint.setColor( Color.parseColor("#135d87") );
        dst = new Rect( dst.left + SlotsActivity.COLUMN_DIVIDER_WIDTH, dst.top + SlotsActivity.COLUMN_DIVIDER_WIDTH, dst.right - SlotsActivity.COLUMN_DIVIDER_WIDTH, dst.bottom - SlotsActivity.COLUMN_DIVIDER_WIDTH );
        canvas.drawRect( dst, paint );

        // TB TODO - And then the ability to turn them on/off
        for( Widget widget : mWidgets ) {
            widget.draw(canvas, false);
        }
    }
    void drawAllSymbols( Canvas canvas ) {
        for( SymbolCol col : mSymbolCols ) {
            col.drawSymbols(canvas);
        }
    }
    void drawGameBorder( Canvas canvas ) {
        Paint p = new Paint();
        p.setColor( Color.parseColor("#6695b0") );
        p.setStrokeWidth(2);
        // top
        Rect r = new Rect(0, 0, 640, SlotsActivity.COLUMN_DIVIDER_WIDTH);
        canvas.drawRect(r,p);
        // Bottom
        r = new Rect( 0, 342+2, 640, 342+2+SlotsActivity.COLUMN_DIVIDER_WIDTH );
        canvas.drawRect(r,p);

        // Widget drawing code can handle this
        /*
        // Left
        r = new Rect(0, 0, SlotsActivity.COLUMN_DIVIDER_WIDTH, 342+2+SlotsActivity.COLUMN_DIVIDER_WIDTH);
        canvas.drawRect(r,p);
        // Right
        r = new Rect(638, 0, 640, 342+2+SlotsActivity.COLUMN_DIVIDER_WIDTH);
        canvas.drawRect(r,p);
        */
    }
    void drawPermanentCanvasElements( Canvas canvas ) {
        drawGameBorder(canvas);
        drawWidgets(canvas);
        for( SymbolCol col : mSymbolCols ) {
            col.drawColumnDivider( canvas );
        }

    }
    // TB TODO - Eventually this needs to draw back the static
    void clearCanvas( Canvas canvas ) {
        Paint paint = new Paint();
        //paint.setColor( Color.BLACK );
        paint.setColor( mBackgroundColor );
        Rect dst = new Rect( 0, 0, canvas.getWidth(), canvas.getHeight() );
        canvas.drawRect( dst, paint );

        drawPermanentCanvasElements( mOffscreenCanvas );
    }
    @Override
    public void updateCredits( Long intbalance ) {
        if( mFreeSpinsLeft == 0 ) {
            super.updateCredits(intbalance);
        }
        else {
            super.updateCredits( mFreeSpinsLeft * mCreditBTCValue, R.drawable.letter_free_spins );
        }
    }
	class ShowReelsRunnable implements Runnable {
    	// Position in the reels
    	int mColIndex;
    	Runnable mFinishedCallback;
        int[] mReelPositions;

    	ShowReelsRunnable( int[] reelPositions, Runnable finishedCallback ) {
    		mColIndex = 0;
    		mFinishedCallback = finishedCallback;
            mReelPositions = reelPositions;
    	}
    	
    	public void run() {

            mSymbolCols[mColIndex].stopAtReelPosition( mReelPositions[mColIndex] );
            mSymbolCols[mColIndex].drawSymbols(mOffscreenCanvas);

            blitOffscreenBitmap();

            mColIndex++;
            if( mColIndex == SlotsActivity.NUM_COLS ) {
                mFinishedCallback.run();
                return;
            }

            int delay = 100;
            mHandler.postDelayed(this, delay);
    	}

	};

	
	
	
    class NetUpdateTask extends NetAsyncTask<Long, Void, JSONSlotsUpdateResult> {
    	
    	NetUpdateTask( CommonActivity a ) { super(a); }
    	
    	public JSONSlotsUpdateResult go(Long...v) throws IOException {
            int last = 999999999;
            int chatlast = 999999999;
    		return mBVC.slotsUpdate( last, chatlast, mCreditBTCValue );
    	}
    	public void onSuccess(JSONSlotsUpdateResult result) {
    		mProgressiveJackpot = result.progressive_jackpot;
    		updateProgressiveJackpot(mProgressiveJackpot);
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
    		return mBVC.slotsReseed();
    	}
    	public void onSuccess(JSONReseedResult result) {
    		mServerSeedHash = result.server_seed_hash;
    		updateControls();
    		checkConnectingAlert();
    		if( mConnectingDialog == null && mAutodeal ) {
    			onPull(null);
    		}
    	}
    }
    class NetPullTask extends NetAsyncTask<Long, Void, JSONSlotsPullResult> {

        boolean mIsFreeSpin;
    	NetPullTask( CommonActivity a ) {
    		super(a);

            // TB TEMP TEST - Keep this running so that if the task is interrupted (phone call, etc), that the result
            // will still be shown. This is important so that free spins are not lost (game will never stop).
            mAllowAbort = false;

            boolean mIsFreeSpin = false;
            if( mFreeSpinsLeft > 0 ) {
                mFreeSpinsLeft -= 1;
                mIsFreeSpin = true;
                updateCredits( mUseFakeCredits ? mBVC.mFakeIntBalance : mBVC.mIntBalance );
            }
            else {
                updateCredits( (mUseFakeCredits ? mBVC.mFakeIntBalance : mBVC.mIntBalance) - (mLines * mCreditBTCValue) );
            }

            // TB TEMP TEST - There's probably a better place to put this?
            stopCountUpWins();
            clearCanvas(mOffscreenCanvas);
            blitOffscreenBitmap();
            mWinSummary.setText("");
            mLineWinPays.setText("");
            mPlayTwentyLines.setText("");

    		mIsWaitingForServer	= true;
    		mIsGameBusy = true;
            if( mIsFreeSpin ) {
                playSound( mSoundFreeSpin );
            }
            else {
                playSound( mSoundCoinPay );
            }
			updateWin( 0, false );
            startAllSpinning();
            setRevealState(WIN_REVEAL_STATE_DONE);

    		// TB - Credits are now dirty (so don't update credits with whatever we get from a balance update, since it will be incorrect)
			mCreditsAreDirty = true;
    		updateControls();
    	}
    	
    	public JSONSlotsPullResult go(Long...v) throws IOException {
    		String serverSeedHash = mServerSeedHash;
    		return mBVC.slotsPull( mLines, mCreditBTCValue, serverSeedHash, getClientSeed(), mUseFakeCredits );
    	}
    	@Override
    	public void onSuccess(final JSONSlotsPullResult result) {

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
                    handleError(result, String.format("Error from server: %s", result.error) );
                    abortAllSpinning();
    			}
                updateControls();
				return;
    		}
    		mPullResult = result;
            mServerSeedHash = result.server_seed_hash;

            mBVC.mIntBalance = result.intbalance;
            mBVC.mFakeIntBalance = result.fake_intbalance;

            mProgressiveJackpot = result.progressive_jackpot;
            mFreeSpinsLeft = result.free_spin_info.left;
            updateProgressiveJackpot( mProgressiveJackpot );

    		mShowReelsRunnable = new ShowReelsRunnable( result.reel_positions, new Runnable() {
				public void run() {

                    String winSummary = "";
                    int numLines = mPullResult.prizes.size();
                    int numScatters = mPullResult.num_scatters;
                    if( numLines > 0 || numScatters >= SlotsActivity.SCATTERS_FOR_PRIZE ) {
                        long delta = mCreditBTCValue;
                        if( result.intwinnings/mCreditBTCValue >= 50 ) {
                            delta = mCreditBTCValue * 5;
                        }
                        if( mIsAutoOn ) {
                            delta = result.intwinnings;
                        }
                        startCountUpWins(result.intwinnings, (mUseFakeCredits ? result.fake_intbalance : result.intbalance) - result.intwinnings, delta );
                        winSummary = "WIN ";
                        if( numLines > 0 ) {
                            winSummary += String.format("%d LINE", numLines);
                            if( numLines != 1 ) {
                                winSummary += "S";
                            }
                            if( numScatters >= SlotsActivity.SCATTERS_FOR_PRIZE ) {
                                winSummary += " AND ";
                            }
                        }
                        if( numScatters >= SlotsActivity.SCATTERS_FOR_PRIZE ) {
                            winSummary += String.format("%d SCATTER BONUS", numScatters);
                        }
                        mWinSummary.setText(winSummary);
                        setRevealState(WIN_REVEAL_STATE_SHOW_ALL);

                        if( result.num_scatters >= SCATTERS_FOR_PRIZE ) {
                            playSound( mSoundWinScatter );
                        }
                        else if( result.prizes.size() > 0 ) {
                            playSound( mSoundWin );
                        }
                    }

                    // TB TODO - Do this properly, in sequence, etc
                    //int lines[] = {1,2,3};
                    //int lines[] = new int[ result.prizes.size() ];
                    int idx = 0;
                    mWins = new Win[ result.prizes.size() ];
                    for (Map.Entry<String, Object[]> entry : result.prizes.entrySet()) {
                        //public char[] prizes;   // { "4": [[7,3], 5]
                        String lineID = entry.getKey();
                        Object[] details = entry.getValue();
                        Win win = new Win();
                        win.mLineID = Integer.parseInt(lineID);
                        ArrayList<Object> arrlist = (ArrayList<Object>) details[0];
                        // TB TODO - Get this working!
                        // TB TODO - This is insane
                        double foo = (Double) arrlist.get(1);
                        win.mNumSymbols = (int) foo;
                        foo = (Double) arrlist.get(0);
                        win.mSymbolID = (int) foo;
                        foo = (Double) details[1];
                        win.mPrize = (int) foo;
                        mWins[idx] = win;

                        //lines[idx] = win.mLineID;
                        idx++;
                    }

		    		mIsGameBusy = false;
		    		updateControls();
		    		checkAuto();
				} 
    		});
    		mShowReelsRunnable.run();

    	}

    	@Override
    	public void onError(final JSONSlotsPullResult result) {
    		mIsGameBusy = false;
            abortAllSpinning();
            updateControls();
    	}
    	@Override
    	public void onDone() {
    		mIsWaitingForServer = false;
    		
    		// TB - Credits are now clean. We can display the intbalance we get from the server again.
			mCreditsAreDirty = false;
    	}
    }
    class NetRulesetTask extends NetAsyncTask<Long, Void, JSONSlotsRulesetResult> {

        NetRulesetTask( CommonActivity a ) {
            super(a);
            mIsWaitingForServer	= true;
        }

        public JSONSlotsRulesetResult go(Long...v) throws IOException {
            return mBVC.slotsRuleset();
        }
        @Override
        public void onSuccess(final JSONSlotsRulesetResult result) {
            mRuleset = result;
            for( int col = 0; col < SlotsActivity.NUM_COLS; col++ ) {
                mSymbolCols[col].setRulesetReel( result.result.reels[col] );
            }

            checkConnectingAlert();
            updateControls();
        }
        @Override
        public void onDone() {
            mIsWaitingForServer = false;
        }
    }


}

class Widget {
    Rect mOutsideFill;
    Rect mInsideFill;
    Paint mOutsidePaint;
    Paint mInsidePaint;
    Paint mInsideOnPaint;
    Paint mInsideWinPaint;
    Paint mTextPaint;
    int mID;

    public boolean mIsOn;
    public boolean mIsWin;

    Widget( int id, int top, boolean isLeft, int winColor ) {
        mIsOn = false;
        mIsWin = false;

        mOutsidePaint = new Paint();
        mOutsidePaint.setColor( Color.parseColor("#6695b0"));

        mInsidePaint = new Paint();
        mInsidePaint.setColor( Color.parseColor("#135d87") );

        mInsideOnPaint = new Paint();
        mInsideOnPaint.setColor( Color.parseColor("#84b2cc") );

        mInsideWinPaint = new Paint();
        mInsideWinPaint.setColor( winColor );

        mTextPaint = new Paint();
        mTextPaint.setColor( Color.parseColor("#e2cc3b") );

        mID = id;
        if( isLeft ) {
            mOutsideFill = new Rect( SlotsActivity.mWidget0TopLeft.x, top, SlotsActivity.mWidget0BottomRight.x, top + SlotsActivity.WIDGET_HEIGHT );
        }
        else {
            mOutsideFill = new Rect( SlotsActivity.mWidget1TopLeft.x, top, SlotsActivity.mWidget1BottomRight.x, top + SlotsActivity.WIDGET_HEIGHT );
        }
        mInsideFill = new Rect( mOutsideFill.left + SlotsActivity.COLUMN_DIVIDER_WIDTH, mOutsideFill.top + SlotsActivity.COLUMN_DIVIDER_WIDTH,
                                mOutsideFill.right - SlotsActivity.COLUMN_DIVIDER_WIDTH, mOutsideFill.bottom - SlotsActivity.COLUMN_DIVIDER_WIDTH );
    }
    void draw( Canvas canvas, boolean isOn ) {
        canvas.drawRect( mOutsideFill, mOutsidePaint );

        Paint p = mInsidePaint;
        if( mIsWin ) {
            p = mInsideWinPaint;
        }
        else if( mIsOn ) {
            p = mInsideOnPaint;
        }
        canvas.drawRect( mInsideFill, p );
    }
    void drawPostBlit( Canvas liveCanvas, float startX, float startY, float scaleX, float scaleY ) {
        // Gotta draw the text on the full size canvas instead of the shrunken one so that it doesn't look crappy
        mTextPaint.setTextSize( 20 * scaleY );
        float left = startX + (mOutsideFill.left + 10) * scaleX;
        if( mID >= 9 ) {
            left = startX + (mOutsideFill.left + 5) * scaleX;
        }
        liveCanvas.drawText( String.format("%d",mID+1), left, startY + (mOutsideFill.bottom - 5) * scaleY, mTextPaint );

    }
}

class SymbolCol {
    public View mContainer;
    public SlotsActivity mActivity;
    private BitmapCache mBitmapCache;
    private final String TAG = "FooSymbolHolder";
    int[] mReel;
    boolean mIsSpinning;
    int mReelOffset;
    int mCol;

    Point mSymbolTopLeft, mSymbolBottomRight;
    Point mDividerTopLeft, mDividerBottomRight;
    int[] mSymbolTopY;
    int[] mRandomSymbols;

    public SymbolCol( SlotsActivity a, BitmapCache bitmapCache, int col ) {
        mActivity = a;
        mBitmapCache = bitmapCache;
        //mSymbolHolders = new SymbolHolder[SlotsActivity.NUM_ROWS];
        //mContainer = a.findViewById( containerResourceID );
        mIsSpinning = false;
        mReelOffset = -1;
        mCol = col;

        mSymbolTopLeft = new Point( SlotsActivity.WIDGET_WIDTH + mCol*SlotsActivity.SYMBOL_WIDTH + (mCol * SlotsActivity.COLUMN_DIVIDER_WIDTH), 0 + SlotsActivity.COLUMN_DIVIDER_WIDTH);
        mSymbolBottomRight = new Point( mSymbolTopLeft.x + SlotsActivity.SYMBOL_WIDTH, mSymbolTopLeft.y + 3*SlotsActivity.SYMBOL_HEIGHT );

        mDividerTopLeft = new Point( mSymbolBottomRight.x, mSymbolTopLeft.y );
        mDividerBottomRight = new Point( mDividerTopLeft.x + SlotsActivity.COLUMN_DIVIDER_WIDTH, mDividerTopLeft.y + 3*SlotsActivity.SYMBOL_HEIGHT );

        mSymbolTopY = new int[] { mSymbolTopLeft.y, mSymbolTopLeft.y + SlotsActivity.SYMBOL_HEIGHT, mSymbolTopLeft.y + 2*SlotsActivity.SYMBOL_HEIGHT };

        mRandomSymbols = new int[SlotsActivity.NUM_ROWS];
        Random r = new Random();
        for( int i = 0; i < SlotsActivity.NUM_ROWS; i++ ) {
            mRandomSymbols[i] = r.nextInt( SlotsActivity.NUM_SYMBOLS+1 );
        }
    }

    public void drawWinningScatters( Canvas canvas ) {
        for( int row = 0; row < SlotsActivity.NUM_ROWS; row++ ) {
            // TB TODO - Use ruleset instead of magic number 0
            if( getSymbolAtRow(row) == SlotsActivity.SCATTER_SYMBOL ) {
                drawWinningSymbol( canvas, row, Color.RED );
            }
        }
    }

    public void drawWinningSymbol( Canvas canvas, int row, int boxColor ) {
        final int WINNING_BOX_WIDTH = 5;
        int symbol = getSymbolAtRow(row);
        Paint paint = new Paint();

        final int rectRoundness = 20;

        paint.setColor( boxColor );
        //Rect dst = new Rect( 100 + mCol*SlotsActivity.SYMBOL_WIDTH, row*SlotsActivity.SYMBOL_HEIGHT, 100+ (mCol+1)*SlotsActivity.SYMBOL_WIDTH, (row+1)*SlotsActivity.SYMBOL_HEIGHT );
        Rect dst = new Rect( mSymbolTopLeft.x, mSymbolTopY[row], mSymbolBottomRight.x, mSymbolTopY[row] + SlotsActivity.SYMBOL_HEIGHT);
        //canvas.drawRect( dst, paint );
        RectF dstf = new RectF(dst);
        canvas.drawRoundRect( dstf, rectRoundness, rectRoundness, paint );

        // Then make a smaller draw with blue, which creates a red border
        dst.left += WINNING_BOX_WIDTH;
        dst.top += WINNING_BOX_WIDTH;
        dst.right -= WINNING_BOX_WIDTH;
        dst.bottom -= WINNING_BOX_WIDTH;
        paint.setColor( Color.parseColor("#85cdee"));
        //canvas.drawRect( dst, paint );
        dstf = new RectF(dst);
        canvas.drawRoundRect( dstf, rectRoundness, rectRoundness, paint );

        // Then draw the symbol on top of all that.
        drawSymbol( canvas, row, symbol );
    }
    public void drawColumnDivider( Canvas canvas ) {
        // Only draw col dividers to the right of the first 4 columns
        if( mCol == SlotsActivity.NUM_COLS-1 ) {
            return;
        }
        Paint paint = new Paint();
        paint.setColor( Color.parseColor("#6695b0") );
        //int startX = 100 + mCol*SlotsActivity.SYMBOL_WIDTH + (mCol * SlotsActivity.COLUMN_DIVIDER_WIDTH);
        //int endX = startX + SlotsActivity.COLUMN_DIVIDER_WIDTH;
        //Rect dst = new Rect( startX, 0, endX, 3*SlotsActivity.SYMBOL_HEIGHT );
        Rect dst = new Rect( mDividerTopLeft.x, mDividerTopLeft.y, mDividerBottomRight.x, mDividerBottomRight.y );
        canvas.drawRect( dst, paint );
    }
    public void drawSpinning( Canvas canvas ) {
        if( !mIsSpinning ) {
            return;
        }
        Paint paint = new Paint();
        //paint.setColor( Color.BLACK );
        paint.setColor( mActivity.mBackgroundColor );

        Bitmap b = mBitmapCache.getBitmap(R.drawable.s_symbols0_spinning);
        Random r = new Random();
        // TB TODO - No magic numb
        // TB - Minus 1 so that all 3 rows of image are guaranteed to fit.
        int srcOffsetY = SlotsActivity.SYMBOL_HEIGHT * r.nextInt(SlotsActivity.NUM_SPINNING_SYMBOLS-1);
        Rect src = new Rect(0, srcOffsetY, SlotsActivity.SYMBOL_WIDTH, srcOffsetY + 3*SlotsActivity.SYMBOL_HEIGHT);
        //Rect dst = new Rect( 100 + mCol*SlotsActivity.SYMBOL_WIDTH, 0, 100+ (mCol+1)*SlotsActivity.SYMBOL_WIDTH, 3*SlotsActivity.SYMBOL_HEIGHT );
        Rect dst = new Rect( mSymbolTopLeft.x, mSymbolTopLeft.y, mSymbolBottomRight.x, mSymbolBottomRight.y );
        canvas.drawRect( dst, paint );
        canvas.drawBitmap( b, src, dst, paint );
    }
    void clearSymbolBackground( Canvas canvas, int row ) {
        Paint paint = new Paint();
        //paint.setColor( Color.BLACK );
        paint.setColor( mActivity.mBackgroundColor );
        // Rect dst = new Rect( 100 + mCol*SlotsActivity.SYMBOL_WIDTH, row*SlotsActivity.SYMBOL_HEIGHT, 100+ (mCol+1)*SlotsActivity.SYMBOL_WIDTH, (row+1)*SlotsActivity.SYMBOL_HEIGHT );
        Rect dst = new Rect( mSymbolTopLeft.x, mSymbolTopY[row], mSymbolBottomRight.x, mSymbolTopY[row] + SlotsActivity.SYMBOL_HEIGHT);
        canvas.drawRect( dst, paint );
    }
    void drawSymbol( Canvas canvas, int row, int symbol ) {
        // TB TODO - Creating a new paint every time is dumb
        Paint paint = new Paint();
        //paint.setColor( Color.BLACK );
        paint.setColor( mActivity.mBackgroundColor );
        Bitmap b = mBitmapCache.getBitmap(R.drawable.s_symbols0);
        Rect src = new Rect(0, symbol*SlotsActivity.SYMBOL_HEIGHT, SlotsActivity.SYMBOL_WIDTH, (symbol+1)*SlotsActivity.SYMBOL_HEIGHT);
        //Rect dst = new Rect( 100 + mCol*SlotsActivity.SYMBOL_WIDTH, row*SlotsActivity.SYMBOL_HEIGHT, 100+ (mCol+1)*SlotsActivity.SYMBOL_WIDTH, (row+1)*SlotsActivity.SYMBOL_HEIGHT );
        Rect dst = new Rect( mSymbolTopLeft.x, mSymbolTopY[row], mSymbolBottomRight.x, mSymbolTopY[row] + SlotsActivity.SYMBOL_HEIGHT);
        canvas.drawBitmap( b, src, dst, paint );
    }
    public int getSymbolAtRow( int row ) {
        // Negative reel offset means that we haven't spun to anything yet, so just display the initial random symbols
        if( mReelOffset == -1 ) {
            return mRandomSymbols[row];
        }
        int pos = (mReelOffset + row) % mReel.length;
        int symbol = mReel[pos];
        return symbol;
    }
    public void drawSymbols( Canvas canvas ) {
        Random r = new Random();
        for( int row = 0; row < SlotsActivity.NUM_ROWS; row++ ) {
            int symbol = getSymbolAtRow(row);
            clearSymbolBackground( canvas, row );
            drawSymbol(canvas, row, symbol);
        }
    }

    public void setRulesetReel( int[] reel ) {
        mReel = reel;
        /*
        for( int i = 0; i < reel.length; i++ ) {
            Log.v(TAG, String.format("reel:%d", reel[i]) );
        }
        */
    }
    public void startSpinning() {
        mIsSpinning = true;
    }
    public void abortSpinning() {
        if( !mIsSpinning ) {
            Log.e(TAG, "Called abortSpinning() but mIsSpinning is false!");
        }
        mIsSpinning = false;
    }
    public void stopAtReelPosition( int reelOffset ) {
        if( !mIsSpinning ) {
            Log.e(TAG, "Symbol col was not spinning when stopAtReelPosition was called!");
        }
        /*
        for( int row = 0; row < SlotsActivity.NUM_ROWS; row++ ) {
            int pos = (startingOffset + row) % mReel.length;
            int symbol = mReel[pos];
            // mSymbolHolders[row].setSymbol(symbol);
            // TB TODO - Draw the symbol!
        }
        */
        mIsSpinning = false;
        mReelOffset = reelOffset;
    }
}

