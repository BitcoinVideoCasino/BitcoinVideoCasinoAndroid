package com.bitcoinvideocasino.lib;

public class JSONBlackjackRulesetResultActual extends JSONBaseResult {
	public int[] blackjack_pays;
	public int[] insurance_pays;
	public int number_of_decks;
	public int[] progressive_bets;
	public int[] progressive_paytable;
	public int max_split_count;
	public boolean can_hit_split_aces;
	public boolean can_resplit_aces;
	public boolean dealer_hits_on_soft_17;
	public boolean dealer_peeks;
	public String can_double_on;
	public boolean can_double_after_split;
	public boolean loses_only_original_bet_on_dealer_blackjack;
	public int maximum_bet;
	public int bet_resolution;
	public int progressive_init; 
}
