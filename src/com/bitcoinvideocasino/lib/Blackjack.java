package com.bitcoinvideocasino.lib;

import java.util.ArrayList;

import java.util.List;
import java.util.Random;
import android.util.Log;

public class Blackjack {
	
	final static String TAG = "Blackjack";
	public class Command {
		final static public int DEAL = 0;
		final static public int HIT = 1;
		final static public int STAND = 2;
		final static public int DOUBLE = 3;
		final static public int SPLIT = 4;
		final static public int INSURANCE = 5;
		final static public int PLAYOUT_DEALER = 6;
	}
	
    static public int get_card_rank_number( String card ) {
    	if( card == "back" ) {
    		return 0;
    	}
        int value = 0;
        char ch = card.charAt(0);
        if( ch == 'a' ) {
            value = 1;
        }
        else if( ch == 'k' ) {
            value = 10;
        }
        else if( ch == 'q' ) {
            value = 10;
        }
        else if( ch == 'j' ) {
            value = 10;
        }
        else if( ch == 't' ) {
            value = 10;
        }
        else {
            // value = parseInt(ch, 10);
        	value = Character.getNumericValue(ch);
        }
            
        return value;
    }
	
	static public int[] score_hand( List<String> cards ) {
		int val = 0;
		boolean hasAce = false;
		for( int i = 0; i < cards.size(); i++ ) {
			int rank = get_card_rank_number(cards.get(i));
			val += rank;
			if( rank == 1 ) {
				hasAce = true;
			} 
		}
		
		if( hasAce ) {
			// It doesn't matter how many aces you have, since at most one can count as 11 (since otherwise you'd bust!)
			if( val + 10 <= 21 ) {
				return new int[] { val, val + 10 };
			}
		}
		return new int[] { val };
	}
	
	static public int final_score_hand( List<String> cards ) {
		int[] scores = score_hand(cards);
		return scores[ scores.length - 1 ];
	}
	
	static public boolean is_blackjack( List<String> cards ) {
	    return cards.size() == 2 && final_score_hand(cards) == 21;
	}

	static public boolean is_bust( List<String> cards ) {
	    return final_score_hand(cards) > 21;
	}

	static public boolean is_21( List<String> cards ) {
	    return final_score_hand(cards) == 21;
	}
	// standard tables at 
	// http://wizardofodds.com/games/blackjack/strategy/calculator/
	// but doesn't show what player should do with 2-2 when not allowed to split (imagine getting a bunch of 2s)
	// real hand I was dealt once:
	// 8c 8s   (split)
	// 8c7d4c (stand) 8s8h (split)
	// 8c7d4c 8sjs (stand) 8h8d (split)
	// 8c7d4c 8sjs 8h6h3d (stand) 8d8d (can't split any more..)
	// you can imagine twos instead of eights here...so I added a 4 row
	// based on the table at http://wizardofodds.com/games/blackjack/ halfway down the page.
	static final int[][] player_hard_table = {
		{},  // 0
		{},  // 1
		{},  // 2
		{},  // 3
	    //     A                 2                 3                 4                 5                 6                 7                 8                 9                T 
		{ Command.HIT   , Command.HIT   , Command.HIT   , Command.HIT   , Command.HIT   , Command.HIT   , Command.HIT   , Command.HIT   , Command.HIT   , Command.HIT   },
	    { Command.HIT   , Command.HIT   , Command.HIT   , Command.HIT   , Command.HIT   , Command.HIT   , Command.HIT   , Command.HIT   , Command.HIT   , Command.HIT   },
	    { Command.HIT   , Command.HIT   , Command.HIT   , Command.HIT   , Command.HIT   , Command.HIT   , Command.HIT   , Command.HIT   , Command.HIT   , Command.HIT   },
	    { Command.HIT   , Command.HIT   , Command.HIT   , Command.HIT   , Command.HIT   , Command.HIT   , Command.HIT   , Command.HIT   , Command.HIT   , Command.HIT   },
	    { Command.HIT   , Command.HIT   , Command.HIT   , Command.HIT   , Command.HIT   , Command.HIT   , Command.HIT   , Command.HIT   , Command.HIT   , Command.HIT   },
	    { Command.HIT   , Command.HIT   , Command.DOUBLE, Command.DOUBLE, Command.DOUBLE, Command.DOUBLE, Command.HIT   , Command.HIT   , Command.HIT   , Command.HIT   },
	    { Command.HIT   , Command.DOUBLE, Command.DOUBLE, Command.DOUBLE, Command.DOUBLE, Command.DOUBLE, Command.DOUBLE, Command.DOUBLE, Command.DOUBLE, Command.HIT   },
	    { Command.HIT   , Command.DOUBLE, Command.DOUBLE, Command.DOUBLE, Command.DOUBLE, Command.DOUBLE, Command.DOUBLE, Command.DOUBLE, Command.DOUBLE, Command.HIT   },
	    { Command.HIT   , Command.HIT   , Command.HIT   , Command.STAND , Command.STAND , Command.STAND , Command.HIT   , Command.HIT   , Command.HIT   , Command.HIT   },
	    { Command.HIT   , Command.STAND , Command.STAND , Command.STAND , Command.STAND , Command.STAND , Command.HIT   , Command.HIT   , Command.HIT   , Command.HIT   },
	    { Command.HIT   , Command.STAND , Command.STAND , Command.STAND , Command.STAND , Command.STAND , Command.HIT   , Command.HIT   , Command.HIT   , Command.HIT   },
	    { Command.HIT   , Command.STAND , Command.STAND , Command.STAND , Command.STAND , Command.STAND , Command.HIT   , Command.HIT   , Command.HIT   , Command.HIT   },
	    { Command.HIT   , Command.STAND , Command.STAND , Command.STAND , Command.STAND , Command.STAND , Command.HIT   , Command.HIT   , Command.HIT   , Command.HIT   },
	    { Command.STAND , Command.STAND , Command.STAND , Command.STAND , Command.STAND , Command.STAND , Command.STAND , Command.STAND , Command.STAND , Command.STAND },
	    { Command.STAND , Command.STAND , Command.STAND , Command.STAND , Command.STAND , Command.STAND , Command.STAND , Command.STAND , Command.STAND , Command.STAND },
	    { Command.STAND , Command.STAND , Command.STAND , Command.STAND , Command.STAND , Command.STAND , Command.STAND , Command.STAND , Command.STAND , Command.STAND },
	    { Command.STAND , Command.STAND , Command.STAND , Command.STAND , Command.STAND , Command.STAND , Command.STAND , Command.STAND , Command.STAND , Command.STAND },
	    { Command.STAND , Command.STAND , Command.STAND , Command.STAND , Command.STAND , Command.STAND , Command.STAND , Command.STAND , Command.STAND , Command.STAND},
	};
	static final int[][] player_soft_table = {
		{}, // 0
		{}, // 1
		{}, // 2
		{}, // 3
		{}, // 4
		{}, // 5
		{}, // 6
		{}, // 7
		{}, // 8
		{}, // 9
		{}, // 10
		{}, // 11
		{}, // 12
	      //                A                 2                 3                 4                 5                 6                 7                 8                 9                T 
	    { Command.HIT   , Command.HIT   , Command.HIT   , Command.HIT   , Command.DOUBLE, Command.DOUBLE, Command.HIT   , Command.HIT   , Command.HIT   , Command.HIT   },
	    { Command.HIT   , Command.HIT   , Command.HIT   , Command.HIT   , Command.DOUBLE, Command.DOUBLE, Command.HIT   , Command.HIT   , Command.HIT   , Command.HIT   },
	    { Command.HIT   , Command.HIT   , Command.HIT   , Command.DOUBLE, Command.DOUBLE, Command.DOUBLE, Command.HIT   , Command.HIT   , Command.HIT   , Command.HIT   },
	    { Command.HIT   , Command.HIT   , Command.HIT   , Command.DOUBLE, Command.DOUBLE, Command.DOUBLE, Command.HIT   , Command.HIT   , Command.HIT   , Command.HIT   },
	    { Command.HIT   , Command.HIT   , Command.DOUBLE, Command.DOUBLE, Command.DOUBLE, Command.DOUBLE, Command.HIT   , Command.HIT   , Command.HIT   , Command.HIT   },
	    { Command.HIT   , Command.STAND , Command.DOUBLE, Command.DOUBLE, Command.DOUBLE, Command.DOUBLE, Command.STAND , Command.STAND , Command.HIT   , Command.HIT   },
	    { Command.STAND , Command.STAND , Command.STAND , Command.STAND , Command.STAND , Command.STAND , Command.STAND , Command.STAND , Command.STAND , Command.STAND },
	    { Command.STAND , Command.STAND , Command.STAND , Command.STAND , Command.STAND , Command.STAND , Command.STAND , Command.STAND , Command.STAND , Command.STAND },
	    { Command.STAND , Command.STAND , Command.STAND , Command.STAND , Command.STAND , Command.STAND , Command.STAND , Command.STAND , Command.STAND , Command.STAND},
	};

	static final int[][] player_pair_table = {
	      // dealer_shows:
	      //                A                 2                 3                 4                 5                 6                 7                 8                 9                T 
		{}, // 0
	    { Command.HIT   , Command.SPLIT , Command.SPLIT , Command.SPLIT , Command.SPLIT , Command.SPLIT , Command.SPLIT , Command.SPLIT , Command.SPLIT , Command.SPLIT},
	    { Command.HIT   , Command.SPLIT , Command.SPLIT , Command.SPLIT , Command.SPLIT , Command.SPLIT , Command.SPLIT , Command.HIT   , Command.HIT   , Command.HIT   },
	    { Command.HIT   , Command.SPLIT , Command.SPLIT , Command.SPLIT , Command.SPLIT , Command.SPLIT , Command.SPLIT , Command.HIT   , Command.HIT   , Command.HIT   },
	    { Command.HIT   , Command.HIT   , Command.HIT   , Command.HIT   , Command.SPLIT , Command.SPLIT , Command.SPLIT , Command.HIT   , Command.HIT   , Command.HIT   },
	    { Command.HIT   , Command.DOUBLE, Command.DOUBLE, Command.DOUBLE, Command.DOUBLE, Command.DOUBLE, Command.DOUBLE, Command.DOUBLE, Command.DOUBLE, Command.HIT   },
	    { Command.HIT   , Command.SPLIT , Command.SPLIT , Command.SPLIT , Command.SPLIT , Command.SPLIT , Command.HIT   , Command.HIT   , Command.HIT   , Command.HIT   },
	    { Command.HIT   , Command.SPLIT , Command.SPLIT , Command.SPLIT , Command.SPLIT , Command.SPLIT , Command.SPLIT , Command.HIT   , Command.HIT   , Command.HIT   },
	    { Command.HIT   , Command.SPLIT , Command.SPLIT , Command.SPLIT , Command.SPLIT , Command.SPLIT , Command.SPLIT , Command.SPLIT , Command.SPLIT , Command.HIT   },
	    { Command.STAND , Command.SPLIT , Command.SPLIT , Command.SPLIT , Command.SPLIT , Command.SPLIT , Command.STAND , Command.SPLIT , Command.SPLIT , Command.STAND },
	    { Command.STAND , Command.STAND , Command.STAND , Command.STAND , Command.STAND , Command.STAND , Command.STAND , Command.STAND , Command.STAND , Command.STAND },
		
	};
	
	/*
	static public int player_action( String dealer_shows, String[] player_hands, int hand_index, List<Integer> actions, int take_insurance_freq, int max_split_count, long progressive_bet, long original_bet, long num_credits) {
		Random r = new Random();
		if( r.nextInt(100) < 50 ) {
			return Command.HIT; 
		}
		else {
			return Command.STAND;
		}
	}
	*/

	static public int player_action( String dealer_shows, List<List<String>> player_hands, int hand_index, List<Integer> actions, double take_insurance_freq, int max_split_count, long progressive_bet, long original_bet, long num_credits) {
		
		// Sanity check
		if( player_hands.get(hand_index).size() <= 1 ) {
			throw new RuntimeException("player_action() was given a hand with only 1 card");
		}
		
	    // player_action should never be called on a busted hand
	    int dealer_shows_rank = Blackjack.get_card_rank_number(dealer_shows);
	    //if( player_hands[hand_index].length == 2 && dealer_shows_rank == 1 && indexOf(actions, 'I') < 0 && num_credits >= (original_bet/2) ) {
	    if( player_hands.get(hand_index).size() == 2 && dealer_shows_rank == 1 && actions.size() == 0 && num_credits >= (original_bet/2) ) {
	        if( Math.random() < take_insurance_freq ) {
	        	return Blackjack.Command.INSURANCE;
	        }
	    }

	    //var has_pair = (player_hands[hand_index].length == 2) && (Blackjack.get_card_rank_number(player_hands[hand_index][0]) == Blackjack.get_card_rank_number(player_hands[hand_index][1]));
	    boolean has_pair = player_hands.get(hand_index).size() == 2 && (Blackjack.get_card_rank_number(player_hands.get(hand_index).get(0)) == Blackjack.get_card_rank_number(player_hands.get(hand_index).get(1)) );
	    //var pair_rank = Blackjack.get_card_rank_number(player_hands[hand_index][0]);
	    int pair_rank = Blackjack.get_card_rank_number( player_hands.get(hand_index).get(0));

	    // player can only split if they have enough money to pay for it.
	    if( has_pair ) {
	        int action = Blackjack.player_pair_table[pair_rank][dealer_shows_rank-1];

	        // can only split up to a certain # of times.  if we can't split, continue on
	        // as if we had no pair...
	        //if( action != Blackjack.SPLIT || count_elem(actions, 'S') < max_split_count ) {
	        if( action != Blackjack.Command.SPLIT || player_hands.size()-1 < max_split_count ) {
	            if( num_credits >= original_bet || ( action == Blackjack.Command.HIT || action == Blackjack.Command.STAND ) ) {
	                return action;
	            }
	        } else if( pair_rank == 1 ) {
	            // we would only get here if we are allowed to hit on these aces, otherwise the
	            // blackjack game would skip over that hand and not even ask for action
	            // pair of aces should just get hit, if it can't be split
	            // everything else falls through to regular hand evaluation
	            return Blackjack.Command.HIT;
	        }
	    }

	    int[] hand_score_t = Blackjack.score_hand(player_hands.get(hand_index));
	    //var hand_score = new Array();
	    List<Integer> hand_score = new ArrayList<Integer>();
	    for( int i = 0; i < hand_score_t.length; i++ ) {
	        if( hand_score_t[i] <= 21 ) hand_score.add(hand_score_t[i]);
	    }

	    int high_score = 0;
	    for( int i = 0; i < hand_score.size(); i++ ) {
	        if( hand_score.get(i) <= 21 && hand_score.get(i) > high_score ) high_score = hand_score.get(i);
	    }

	    boolean is_soft = hand_score.size() > 1;
	    if( is_soft ) { // only happens if we have an ace AND we aren't forced into a score

	        // with 1 ace, the score must be >= 13 (2+11+...)
	        int action = Blackjack.player_soft_table[high_score][dealer_shows_rank-1];
	        if( action == Blackjack.Command.DOUBLE && player_hands.get(hand_index).size() > 2 ) {

	            // the chart says "Double or stand" for 18 value hands..
	            if( high_score == 18 ) {
	                action = Blackjack.Command.STAND;
	            } else {
	                // the other slots are "double or hit"
	                action = Blackjack.Command.HIT;
	            }
	        }

	        if( num_credits >= original_bet || ( action == Blackjack.Command.HIT || action == Blackjack.Command.STAND ) ) {
	            return action;
	        }
	    } 

	    //will be >=4 and <=21.  4 is only possible with 2,2 (which we will get if we hit our split limit) or A,3 (caught as soft 14)
	    int action = Blackjack.player_hard_table[high_score][dealer_shows_rank-1];

	    if( action == Blackjack.Command.DOUBLE && player_hands.get(hand_index).size() > 2 ) {
	        action = Blackjack.Command.HIT;
	    }

	    if( num_credits >= original_bet || ( action == Blackjack.Command.HIT || action == Blackjack.Command.STAND ) ) {
	        return action;
	    }

	    if( action == Blackjack.Command.DOUBLE ) return Blackjack.Command.HIT;

	    Log.v(TAG, "auto-play error. please alert the website operators that this occurred.");
	    return Blackjack.Command.STAND;
	}
	


}
