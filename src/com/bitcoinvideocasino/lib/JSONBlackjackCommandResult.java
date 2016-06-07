package com.bitcoinvideocasino.lib;


public class JSONBlackjackCommandResult extends JSONBaseResult {
	public String deal_hash;
	public String game_id;
	public boolean finished;
	public String dealer_shows;
	public String[] cards;
	public int next_hand;
	public String unique_id;
	public long intbalance;
	public long fake_intbalance;

	// If finished is true
	public String game_seed;
	public String client_seed;
	public String deal_hash_source;
	public long original_bet;
	public long[] bets;
	public String actions;
	public long[] prizes;
	public long prize_total;
	public long progressive_win;
	public String game_eval;
	public long progressive_jackpot;
	public String server_seed_hash; 
	public String[] dealer_hand;
}
