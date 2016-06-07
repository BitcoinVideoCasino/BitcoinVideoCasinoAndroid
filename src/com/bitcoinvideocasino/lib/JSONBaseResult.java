package com.bitcoinvideocasino.lib;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
// TB TODO - Can use @SerializedName("foo_bar") to use java camel case variable naming convention.

public class JSONBaseResult {

	public int time;
	public String error; 
	
	// If the request handler throws an exception, the result will look like this:
	// {"status": "error", "message": "invalid", "class": "Exception"}
	public String status;
	public String message;
	// Can't use class as a identifier, can tell the parser to use a different variable name if we need this value.
	//public String class; 
	
	// Maybe server return codes should just be checked in NetTask's onSuccess, while onError
	// is used for network screwing up and things like that
	/*
	public boolean isSuccess() {
		// TB TODO - Check for presense of 'error' or somehitng like that.
		return true;
	}
	*/
	
    public static Date dateFromJSONDateString( String stringDate ) {
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd"); 
        Date d = null;
        try {
			d = formatter.parse(stringDate);
        }
        catch( ParseException e ) {
        	e.printStackTrace();
        }
    	
        return d;
    }
    
    public static String prettyStringFromJSONDateString( String stringDate ) {
        // TB TODO - Get time in addition to date
        // TB TODO - Is there a better way to get the date? Perhaps during the json parsing???
		
    	Date d = dateFromJSONDateString(stringDate);
		DateFormat df = new SimpleDateFormat("EE, MMM d, yyyy");	        	
		return df.format( d );
    }
    
    public static String shortStringFromJSONDateString( String stringDate ) {
    	// TB TODO - Should do it short relative twitter-style like in non-native opinionated (3h, 5d, 2m)
    	Date d = dateFromJSONDateString(stringDate);
		DateFormat df = new SimpleDateFormat("MMM d");	        	
		return df.format( d );
    }
	
}
