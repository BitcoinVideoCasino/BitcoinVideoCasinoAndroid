package com.bitcoinvideocasino.lib;

public class Dice {
    final static String TAG = "Dice";

    public long getWinCutoff( boolean isHigh, long chance ) {
        if( isHigh ) {
            return 1000000 - chance - 1;
        }
        else {
            return chance;
        }
    }
}
