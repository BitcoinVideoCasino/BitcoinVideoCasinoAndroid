package com.bitcoinvideocasino.lib;

public class JSONUpdateResult extends JSONBaseResult {
    public int players_online;
    public int games_played;
    public long btc_winnings;
    public int progressive_jackpots_won;
    public JSONChatEvent[] chatlog;
    // TB TODO - leaderboard
}
