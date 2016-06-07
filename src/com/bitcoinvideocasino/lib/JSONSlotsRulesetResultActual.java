package com.bitcoinvideocasino.lib;

import java.util.Map;

public class JSONSlotsRulesetResultActual extends JSONBaseResult {
	public int[][] lines;
    public int num_scatters_for_bonus;
    public int bonus_game_lines;
    public int[][] order_of_wins;
    public int[] wild_can_be;
    public long[] valid_credit_sizes;
    public long progressive_init;
    public int[][] reels;
    public int progrsessive_contribution;
    public int wild;
    public int scatter;

    //public char[] paytable; // { "(1,5)": 10000,
    //public char[] bonus_multipliers;  // {"2": 2, "3": 5

    public Map<String, Long> paytable;
    public Map<String, Integer> bonus_multipliers;
}
