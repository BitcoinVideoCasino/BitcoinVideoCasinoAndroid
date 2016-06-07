package com.bitcoinvideocasino.lib;

import android.util.Log;

//Bitcoin translation functions
public class Bitcoin {

	//convert an integer to a string representation in the form XXXXXXXXXX.YYYYYYYY
	public static String longAmountToString(long intamount) {
	
		String neg = "";
		if( intamount < 0 ) {
			neg = "-";
			intamount *= 1;
		} 
	
		String res = "" + (int) Math.floor(intamount/100000000);
	
		long dec = intamount % 100000000;
		if( dec != 0 ) {
			
			// TB TODO - PORT THIS
			//dec = padleft(dec, 8).replace(/0+$/g, '');
			String sdec = String.format("%08d", dec);
			sdec = sdec.replaceAll("0+$", "");
			if( sdec.length() == 0 ) {
				sdec = "0";
			}
			res = res + "." + sdec;
		}
	
		return neg + res;
	}
	public static String longAmountToStringChopped(long intamount) {
		String s = longAmountToString(intamount);
		double f = Double.valueOf(s);
		
		// Chop off 0.000XXX stuff (don't round)
		f *= 1000;
		f = Math.floor(f);
		f /= 1000;
		return String.format("%.3f", f);
	}

	//convert an amount (as a string) in the form of XXXXXXXXX.YYYYYYYY to an int
	public static long stringAmountToLong( String amount ) {

		// Replace commas
		// TB TODO 
		amount = amount.replaceAll("\\s+","").replaceAll("\\s+$","").replaceAll(",","");

		// If the number starts with a '-' we need to remember that
		int neg = 1;
		if( amount.length() > 0 && amount.charAt(0) == '-' ) {
			neg = -1;
			amount = amount.substring(1);
		}

		// The rest of the number can only be 0..9 and a period
		String amountcheck = amount.replaceAll("[0-9]", "").replaceAll("\\.","");
		if( amountcheck.length() != 0 ) {	
			// TB TODO - What should this return?
			//return undefined;
			return -1;
		}

		// Handle case when zero or empty string is passed in
		// TB TODO - PORT
		amount = amount.replace("^0+", "");
		if( amount.length() == 0 ) {
			return 0;
		}

		long scale = 100000000;

		// Find the first '.' (if more sneak by the above check, then parseInt will error)
		int i = amount.indexOf('.');
		if( i == amount.length() - 1 ) {
			amount = amount.substring(0, amount.length() - 1);
			if( amount.length() == 0 ) {
				return 0;
			}
			i = -1;
		}

		long v = 0;
		if( i < 0 ) {
			// No '.' found, use it as a whole number
			//v = parseInt(amount) * scale;
			v = Integer.parseInt(amount) * scale;
		} 
		else { 
			String dec = amount.substring(i+1).replaceAll("0+$","");
			if( dec.indexOf('.') != -1 ) {			
				// TB TODO - Return what?
				//return undefined;
				return -1;
			}

			scale = (long) Math.floor(scale / Math.pow(10, dec.length()));

			amount = amount.substring(0,i) + dec;

			// TB - Trim leading zeroes so that "025" becomes "25" so that retarded opera doesn't evaluate the string in octal...
			amount = amount.replaceAll("^0+",""); 
			if( amount.length() == 0 ) {
				return 0;
			}

			//v = parseInt(amount) * scale;
			v = Long.parseLong(amount) * scale;
		}
		return neg * v;
	}
	
	static private void checkLongAmountToString( long longIn, String expectedString ) {
		String result = Bitcoin.longAmountToString(longIn);
		if( !result.equals(expectedString) ) {
			Log.v("Bitcoin", "check failed: " + longIn + " --> " + result + " != " + expectedString ); 
		}
	}
	static private void checkStringAmountToLong( String stringIn, long expectedLong ) {
		long result = Bitcoin.stringAmountToLong(stringIn);
		if( result != expectedLong ) {
			Log.v("Bitcoin", "check failed: " + stringIn + " --> " + result + " != " + expectedLong ); 
		}
	}

	
	static void test() {
		Log.v("Bitcoin", "Start testing...");
		Bitcoin.checkLongAmountToString(0L, "0");
		Bitcoin.checkLongAmountToString(1L, "0.00000001");
		Bitcoin.checkLongAmountToString(100L, "0.000001");
		Bitcoin.checkLongAmountToString(10000000L, "0.1"); 
		Bitcoin.checkLongAmountToString(200000000L, "2");
		Bitcoin.checkLongAmountToString(2300001009L, "23.00001009");
		//assert(Bitcoin.longAmountToString("foo") == "NaN");
		
		Bitcoin.checkStringAmountToLong("-0", 0);
		Bitcoin.checkStringAmountToLong("0", 0);
		Bitcoin.checkStringAmountToLong("-0.", 0);
		Bitcoin.checkStringAmountToLong("0.", 0);
		Bitcoin.checkStringAmountToLong("0.000", 0);
		Bitcoin.checkStringAmountToLong("    -0.3     ", -30000000);
		Bitcoin.checkStringAmountToLong("    -0.3.     ", -1);
		Bitcoin.checkStringAmountToLong("1234.1234", 123412340000L);
		Bitcoin.checkStringAmountToLong("1234.00000009", 123400000009L);
		Bitcoin.checkStringAmountToLong("0.025", 2500000L);
		Log.v("Bitcoin", "Done testing...");
	}



}
