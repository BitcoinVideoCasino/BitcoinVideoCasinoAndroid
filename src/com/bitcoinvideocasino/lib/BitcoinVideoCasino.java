package com.bitcoinvideocasino.lib;

import java.net.URL;

import java.net.HttpURLConnection;
import java.io.UnsupportedEncodingException;

import com.google.gson.Gson;
import java.net.URLEncoder;
import java.io.BufferedReader;
import java.io.OutputStreamWriter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.util.Log;

import javax.net.ssl.*;

public class BitcoinVideoCasino {

    // Change RUN_ENVIRONMENT to production before releasing
    public class RunEnvironment {
        public final static int PRODUCTION = 0;
        public final static int LOCAL = 1;
        public final static int EMULATOR = 2;
    }
    public final static int RUN_ENVIRONMENT = BitcoinVideoCasino.RunEnvironment.PRODUCTION;

	private String mServerAddress;

	private static final String TAG = "BitcoinVideoCasino";
	final int CONNECT_TIMEOUT = 6000;
	final int READ_TIMEOUT = 6000;
	// By using our referral system on new accounts, we can see how many new android users were created.
	final String NEW_ACCOUNT_REFERRAL_KEY_PRODUCTION = "2877209438";
    final String NEW_ACCOUNT_REFERRAL_KEY_LOCAL = "3678545110";

	public String mAccountKey;
	public long mIntBalance;
	public long mFakeIntBalance;
	public boolean mUnconfirmed;
	
	public String mDepositAddress;
    public String mLastWithdrawAddress;

    private static BitcoinVideoCasino mInstance = null;

    public class ChatCommand {
        final static public int TALK = 0;
        final static public int ENTER = 1;
        final static public int EXIT = 2;
        final static public int RENAME = 3;
    }

	// Prevent public access
	private BitcoinVideoCasino() {
		// TB TODO - Generate a new account, and then store it on the phone.
		// Let the user specify his account key as well, if he's transferring
		// his account.
		mAccountKey = null;
		mIntBalance	= -1;
		mFakeIntBalance	= -1;
		mDepositAddress	= null;

        if(BitcoinVideoCasino.RUN_ENVIRONMENT == BitcoinVideoCasino.RunEnvironment.EMULATOR) {
            mServerAddress = "https://10.0.2.2:9366";
            trustEveryone();
        }
        else if(BitcoinVideoCasino.RUN_ENVIRONMENT == BitcoinVideoCasino.RunEnvironment.LOCAL) {
			mServerAddress = "https://192.168.1.42:9366";
			trustEveryone();
		} else {
			mServerAddress = "https://bitcoinvideocasino.com";
		}

	}

	public static BitcoinVideoCasino getInstance(Context ctx) {
		
		// TB TODO - Could do settings stuff here???
		// accountkey, etc, etc
		// SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		// String syncConnPref = sharedPref.getString(SettingsActivity.KEY_PREF_SYNC_CONN, "");
		
		if (mInstance == null) {
			mInstance = new BitcoinVideoCasino();
		}
		
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
		mInstance.mAccountKey = sharedPref.getString("account_key", null);
		mInstance.mLastWithdrawAddress = sharedPref.getString("last_withdraw_address", null);

        // TB - Don't remember the deposit address, so we don't run into the same problem of people
        // being stuck with an old address that is no longer valid.
        // mInstance.mDepositAddress = sharedPref.getString("deposit_address", null);

		return mInstance;
	}

	// TB - Radically insecure. Should never call this on production!
	private void trustEveryone() {
		try {
			HttpsURLConnection
					.setDefaultHostnameVerifier(new HostnameVerifier() {
						public boolean verify(String hostname,
								SSLSession session) {
							return true;
						}
					});
			SSLContext context = SSLContext.getInstance("TLS");
			context.init(null, new X509TrustManager[] { new X509TrustManager() {
				public void checkClientTrusted(X509Certificate[] chain,
						String authType) throws CertificateException {
				}

				public void checkServerTrusted(X509Certificate[] chain,
						String authType) throws CertificateException {
				}

				public X509Certificate[] getAcceptedIssuers() {
					return new X509Certificate[0];
				}
			} }, new SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(context
					.getSocketFactory());
		} catch (Exception e) { // should never happen
			e.printStackTrace();
		}
	}

	private HttpURLConnection connect(String path, String accountKey) throws IOException {
		URL u = new URL(mServerAddress + "/" + path);
		HttpURLConnection conn = (HttpURLConnection) u.openConnection();
		conn.setConnectTimeout(CONNECT_TIMEOUT);
		conn.setReadTimeout(READ_TIMEOUT);
		// conn.setInstanceFollowRedirects(true);

        // TB TODO - Support password encrypted accounts
        if( accountKey != null ) {
            conn.setRequestProperty("Cookie", "account_key=" + accountKey);
        }

		return conn;
	}

	public InputStreamReader getInputStreamReader(String path, String params, String accountKey) throws IOException {
		String p = path;
		if (params != null) {
			p += "?" + params;
		}
		
		Log.v("GET", p);

		HttpURLConnection conn = connect(p, accountKey);

		return new InputStreamReader(conn.getInputStream(), "UTF-8");
	}

	private InputStreamReader getInputStreamReader(String path) throws IOException {
		return getInputStreamReader(path, null, null);
	}

	private InputStreamReader postInputStreamReader(String path, String params, String accountKey) throws IOException {
		URL u = new URL(mServerAddress + "/" + path);
		HttpURLConnection conn = connect(path, accountKey);
		conn.setRequestMethod("POST");

		// POST
		conn.setDoOutput(true);
		if (params != null) {
			OutputStreamWriter wr = new OutputStreamWriter(
					conn.getOutputStream());
			wr.write(params);
			wr.flush();
			wr.close();
		}

		// Standard
		conn.setConnectTimeout(CONNECT_TIMEOUT);
		conn.setReadTimeout(READ_TIMEOUT);
		conn.setInstanceFollowRedirects(true);

		return new InputStreamReader(conn.getInputStream(), "UTF-8");
	}

	/*
	 * private InputStreamReader postInputStreamReader( String path ) throws
	 * IOException { return postInputStreamReader(path, null); }
	 */
	public String joinString(int[] vals, String delim) {
		StringBuilder sb = new StringBuilder();
		String currDelim = "";
		for (int v = 0; v < vals.length; v++) {
			sb.append(currDelim).append(vals[v]);
			currDelim = delim;
		}
		return sb.toString();
	}

	public String encodeKeyValuePair(String key, String value) throws UnsupportedEncodingException {
		if (value == null) {
			return "";
		}

		return URLEncoder.encode(key, "UTF-8") + "="
				+ URLEncoder.encode(value, "UTF-8");
	}

	public String encodeKeyValuePair(String key, long value) throws UnsupportedEncodingException {
		return encodeKeyValuePair(key, Long.toString(value));
	}
	public String encodeKeyValuePair(String key, int value) throws UnsupportedEncodingException {
		return encodeKeyValuePair(key, Integer.toString(value));
	}
	public String encodeKeyValuePair(String key, boolean value) throws UnsupportedEncodingException {
		return encodeKeyValuePair(key, Boolean.toString(value));
	}

	/*
	 * private String encodeArray( String arrayName, int[] values ) throws
	 * UnsupportedEncodingException { String params = ""; for( int i = 0; i <
	 * values.length; i++ ) { if( i > 0 ) { params += "&"; } params +=
	 * encodeKeyValuePair(arrayName + "[]", Integer.toString(values[i])); }
	 * return params; }
	 */
	
	//
	// MISC COMMANDS
	//
	public JSONAndroidAppVersionResult getAndroidAppVersion() throws UnsupportedEncodingException, IOException {
		InputStreamReader is = getInputStreamReader("android/app_version", "", null);
		JSONAndroidAppVersionResult result = new Gson().fromJson(is, JSONAndroidAppVersionResult.class);
		return result;
	}

	//
	// ACCOUNT COMMANDS
	//
	public JSONCreateAccountResult getCreateAccount() throws UnsupportedEncodingException, IOException {
        String referralKey;
        if( RUN_ENVIRONMENT == RunEnvironment.PRODUCTION ) {
            referralKey = NEW_ACCOUNT_REFERRAL_KEY_PRODUCTION;
        }
        else {
            referralKey = NEW_ACCOUNT_REFERRAL_KEY_LOCAL;
        }
		String params = encodeKeyValuePair("r", referralKey);
		InputStreamReader is = getInputStreamReader("account/new", params, null);
		JSONCreateAccountResult result = new Gson().fromJson(is, JSONCreateAccountResult.class);
		return result;
	}
	
	private void printInputStreamReader( InputStreamReader is )
	{
		java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
	    String str = s.hasNext() ? s.next() : "";
	    Log.v(TAG, str);
	}
	
	public JSONWithdrawResult getWithdraw( String address, long intAmount ) throws UnsupportedEncodingException, IOException {
		String params = encodeKeyValuePair("address", address);
		params += "&" + encodeKeyValuePair("intamount", intAmount);
		
		InputStreamReader is = getInputStreamReader("account/withdraw", params, mAccountKey);
		
		JSONWithdrawResult result = new Gson().fromJson(is, JSONWithdrawResult.class);
		return result;
	}

	// We also use this service call to check the validity of an account_key before setting it,
	// so we need this method to ignore what value is currently set in this class.
	public JSONBalanceResult getBalance(String accountKey) throws UnsupportedEncodingException, IOException {
		InputStreamReader is = getInputStreamReader("account/balance", null, accountKey);
		
		JSONBalanceResult result = new Gson().fromJson(is, JSONBalanceResult.class);
		return result; 
	}
	public JSONBalanceResult getBalance() throws UnsupportedEncodingException, IOException {
		return getBalance(mAccountKey); 
	}
	public JSONBitcoinAddressResult getBitcoinAddress() throws UnsupportedEncodingException, IOException {
		InputStreamReader is = getInputStreamReader("account/bitcoinaddress", null, mAccountKey);
		JSONBitcoinAddressResult result = new Gson().fromJson(is, JSONBitcoinAddressResult.class);
		
		return result;
	}

	//
	// VIDEO POKER COMMANDS
	//
	public JSONReseedResult videoPokerReseed() throws UnsupportedEncodingException, IOException {
		InputStreamReader is = getInputStreamReader("videopoker/reseed", null, mAccountKey);
		JSONReseedResult result = new Gson().fromJson(is, JSONReseedResult.class);
		return result;
	}
	public JSONVideoPokerUpdateResult videoPokerUpdate( int last, int chatlast, long creditBTCValue ) throws UnsupportedEncodingException, IOException {
		String params = encodeKeyValuePair("last", last);
		params += "&" + encodeKeyValuePair("chatlast", chatlast);
		params += "&" + encodeKeyValuePair("credit_btc_value", creditBTCValue);
		
		InputStreamReader is = getInputStreamReader("videopoker/update", params, mAccountKey);
		JSONVideoPokerUpdateResult result = new Gson().fromJson(is, JSONVideoPokerUpdateResult.class);
		return result;
	}

	public JSONVideoPokerDealResult videoPokerDeal(int betSize, int paytable, long creditBTCValue, String serverSeedHash, String clientSeed, boolean useFakeCredits) throws UnsupportedEncodingException, IOException {
		String params = encodeKeyValuePair("bet_size", betSize);
		params += "&" + encodeKeyValuePair("paytable", paytable);
		params += "&" + encodeKeyValuePair("credit_btc_value", creditBTCValue);
		params += "&" + encodeKeyValuePair("server_seed_hash", serverSeedHash);
		params += "&" + encodeKeyValuePair("client_seed", clientSeed);
		params += "&" + encodeKeyValuePair("use_fake_credits", useFakeCredits);

		InputStreamReader is = getInputStreamReader("videopoker/deal", params, mAccountKey);
		JSONVideoPokerDealResult result = new Gson().fromJson(is, JSONVideoPokerDealResult.class);
		return result;
	}

	public JSONVideoPokerHoldResult videoPokerHold(String gameID, String holds, String serverSeed) throws UnsupportedEncodingException, IOException {
		String params = encodeKeyValuePair("game_id", gameID);
		params += "&" + encodeKeyValuePair("holds", holds);
		params += "&" + encodeKeyValuePair("server_seed", serverSeed);

		InputStreamReader is = getInputStreamReader("videopoker/hold", params, mAccountKey);
		JSONVideoPokerHoldResult result = new Gson().fromJson(is, JSONVideoPokerHoldResult.class);
		return result;
	}
	
	public JSONVideoPokerDoubleDealerResult videoPokerDoubleDealer(String gameID, String serverSeedHash, String clientSeed, int level ) throws UnsupportedEncodingException, IOException {
		String params = encodeKeyValuePair("game_id", gameID);
		params += "&" + encodeKeyValuePair("server_seed_hash", serverSeedHash);
		params += "&" + encodeKeyValuePair("client_seed", clientSeed);
		params += "&" + encodeKeyValuePair("level", level);

		InputStreamReader is = getInputStreamReader("videopoker/double_dealer", params, mAccountKey);
		JSONVideoPokerDoubleDealerResult result = new Gson().fromJson(is, JSONVideoPokerDoubleDealerResult.class);
		return result;
	}
	
	public JSONVideoPokerDoublePickResult videoPokerDoublePick(String gameID, int level, int hold ) throws UnsupportedEncodingException, IOException {
		String params = encodeKeyValuePair("game_id", gameID);
		params += "&" + encodeKeyValuePair("level", level);
		params += "&" + encodeKeyValuePair("hold", hold);

		InputStreamReader is = getInputStreamReader("videopoker/double_pick", params, mAccountKey);
		JSONVideoPokerDoublePickResult result = new Gson().fromJson(is, JSONVideoPokerDoublePickResult.class);
		return result;
	}
	
	//
	// BLACKJACK COMMANDS
	//
	public JSONReseedResult blackjackReseed() throws UnsupportedEncodingException, IOException {
		InputStreamReader is = getInputStreamReader("blackjack/reseed", null, mAccountKey);
		JSONReseedResult result = new Gson().fromJson(is, JSONReseedResult.class);
		return result;
	}
	public JSONBlackjackRulesetResult blackjackRuleset() throws UnsupportedEncodingException, IOException {
		InputStreamReader is = getInputStreamReader("blackjack/ruleset", null, null );
		JSONBlackjackRulesetResult result = new Gson().fromJson(is, JSONBlackjackRulesetResult.class);
		return result;
	}
	public JSONBlackjackCommandResult blackjackDeal(long bet, long progressiveBet, String serverSeedHash, String clientSeed, boolean useFakeCredits) throws UnsupportedEncodingException, IOException {
		String params = encodeKeyValuePair("bet", bet);
		params += "&" + encodeKeyValuePair("progressive_bet", progressiveBet);
		params += "&" + encodeKeyValuePair("server_seed_hash", serverSeedHash);
		params += "&" + encodeKeyValuePair("client_seed", clientSeed);
		params += "&" + encodeKeyValuePair("use_fake_credits", useFakeCredits);

		InputStreamReader is = getInputStreamReader("blackjack/deal", params, mAccountKey);
		JSONBlackjackCommandResult result = new Gson().fromJson(is, JSONBlackjackCommandResult.class);
		return result;
	}
	public JSONBlackjackCommandResult blackjackCommand( String url, String gameID, int handIndex ) throws UnsupportedEncodingException, IOException {
		String params = encodeKeyValuePair("game_id", gameID);
		params += "&" + encodeKeyValuePair("hand_index", handIndex);
		InputStreamReader is = getInputStreamReader(url, params, mAccountKey);
		JSONBlackjackCommandResult result = new Gson().fromJson(is, JSONBlackjackCommandResult.class);
		return result; 
	}
	public JSONBlackjackCommandResult blackjackStand( String gameID, int handIndex ) throws UnsupportedEncodingException, IOException {
		return blackjackCommand("blackjack/stand", gameID, handIndex);
	}
	public JSONBlackjackCommandResult blackjackHit( String gameID, int handIndex ) throws UnsupportedEncodingException, IOException {
		return blackjackCommand("blackjack/stand", gameID, handIndex);
	}
	public JSONBlackjackCommandResult blackjackSplit( String gameID, int handIndex ) throws UnsupportedEncodingException, IOException {
		return blackjackCommand("blackjack/split", gameID, handIndex);
	}
	public JSONBlackjackCommandResult blackjackDouble( String gameID, int handIndex ) throws UnsupportedEncodingException, IOException {
		return blackjackCommand("blackjack/double", gameID, handIndex);
	}
	public JSONBlackjackCommandResult blackjackInsurance( String gameID, int handIndex ) throws UnsupportedEncodingException, IOException {
		return blackjackCommand("blackjack/insurance", gameID, handIndex);
	}

    //
    // SLOTS COMMANDS
    //

    public JSONSlotsRulesetResult slotsRuleset() throws UnsupportedEncodingException, IOException {
        String params = null;
        InputStreamReader is = getInputStreamReader("slots/ruleset", params, null);
        JSONSlotsRulesetResult result = new Gson().fromJson(is, JSONSlotsRulesetResult.class);
        return result;
    }
    public JSONReseedResult slotsReseed() throws UnsupportedEncodingException, IOException {
        InputStreamReader is = getInputStreamReader("slots/reseed", null, mAccountKey);
        JSONReseedResult result = new Gson().fromJson(is, JSONReseedResult.class);
        return result;
    }

    public JSONSlotsPullResult slotsPull(int lines, long creditBTCValue, String serverSeedHash, String clientSeed, boolean useFakeCredits) throws UnsupportedEncodingException, IOException {
        String params = encodeKeyValuePair("num_lines", lines);
        params += "&" + encodeKeyValuePair("credit_btc_value", creditBTCValue);
        params += "&" + encodeKeyValuePair("server_seed_hash", serverSeedHash);
        params += "&" + encodeKeyValuePair("client_seed", clientSeed);
		params += "&" + encodeKeyValuePair("use_fake_credits", useFakeCredits);

        InputStreamReader is = getInputStreamReader("slots/pull", params, mAccountKey);
        JSONSlotsPullResult result = new Gson().fromJson(is, JSONSlotsPullResult.class);
        return result;
    }

    public JSONSlotsUpdateResult slotsUpdate( int last, int chatlast, long creditBTCValue ) throws UnsupportedEncodingException, IOException {
        String params = encodeKeyValuePair("last", last);
        params += "&" + encodeKeyValuePair("chatlast", chatlast);
        params += "&" + encodeKeyValuePair("credit_btc_value", creditBTCValue);

        InputStreamReader is = getInputStreamReader("slots/update", params, mAccountKey);
        JSONSlotsUpdateResult result = new Gson().fromJson(is, JSONSlotsUpdateResult.class);
        return result;
    }

    //
    // DICE COMMANDS
    //

    public JSONDiceRulesetResult diceRuleset() throws UnsupportedEncodingException, IOException {
        InputStreamReader is = getInputStreamReader("dice/ruleset", null, null);
        JSONDiceRulesetResult result = new Gson().fromJson(is, JSONDiceRulesetResult.class);
        return result;
    }
    public JSONReseedResult diceReseed() throws UnsupportedEncodingException, IOException {
        InputStreamReader is = getInputStreamReader("dice/reseed", null, mAccountKey);
        JSONReseedResult result = new Gson().fromJson(is, JSONReseedResult.class);

        return result;
    }

    public JSONDiceThrowResult diceThrow(String serverSeedHash, String clientSeed, long bet, long payout, String target, boolean useFakeCredits) throws UnsupportedEncodingException, IOException {
        String params = encodeKeyValuePair("server_seed_hash", serverSeedHash);
        params += "&" + encodeKeyValuePair("client_seed", clientSeed);
        params += "&" + encodeKeyValuePair("bet", bet);
        params += "&" + encodeKeyValuePair("payout", payout);
        params += "&" + encodeKeyValuePair("target", target);
		params += "&" + encodeKeyValuePair("use_fake_credits", useFakeCredits);

        Log.v(TAG, params);

        InputStreamReader is = getInputStreamReader("dice/throw", params, mAccountKey);
        JSONDiceThrowResult result = new Gson().fromJson(is, JSONDiceThrowResult.class);
        return result;
        /*
        printInputStreamReader(is);
        return null;
        */
    }

    public JSONDiceUpdateResult diceUpdate( int last, int chatlast, long creditBTCValue ) throws UnsupportedEncodingException, IOException {
        String params = encodeKeyValuePair("last", last);
        params += "&" + encodeKeyValuePair("chatlast", chatlast);
        params += "&" + encodeKeyValuePair("credit_btc_value", creditBTCValue);

        InputStreamReader is = getInputStreamReader("dice/update", params, mAccountKey);
        JSONDiceUpdateResult result = new Gson().fromJson(is, JSONDiceUpdateResult.class);
        return result;
    }

}
