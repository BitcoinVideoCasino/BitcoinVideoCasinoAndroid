package com.bitcoinvideocasino.lib;

import java.util.Map;

// TB TODO!
public class JSONSlotsPullResult extends JSONBaseResult {
    public long intbalance;
    public long fake_intbalance;
    public String game_seed;
    public String game_id;
    public long intgameearnings;
    public long progressive_win;
    public String server_seed_hash;
    public int unique_id;
    public long intwinnings;
	public long progressive_jackpot;
    public int shutdown_time;
    public int num_scatters;
    public int bonus_multiplier;
    public int[] reel_positions;
    public JSONSlotsPullResultFreeSpinInfo free_spin_info;

    // TB TODO - Hmm the value type is an array with 2 different types (array and then int)... can Java handle that?
    //public char[] prizes;   // { "4": [[7,3], 5]
    //public Map<String, String> prizes;
    public Map<String, Object[]> prizes;
}
