package com.bitcoinvideocasino.lib;


import java.util.Map;

public class JSONDiceUpdateResult extends JSONBaseResult {
	public int players_online;
	public int games_played;
	public long btc_winnings;
	public int progressive_jackpots_won;
	// TB TODO - chatlog
	// TB TODO - leaderboard
	
	// Dice only
    // {"5": 234243, "6": 23423443}
    public Map<String, Integer> progressive_jackpots;
    // TB TODO - last_numbers
}
