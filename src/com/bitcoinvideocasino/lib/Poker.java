package com.bitcoinvideocasino.lib;

import java.util.Arrays;
import java.util.HashMap;

import android.util.Log;
import java.util.Map;
import java.util.Comparator;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

class Detail
{	
	public boolean is_good;
	public int rank;
	/*
	public String fifth_card;
	*/
	public int[] positions;
	
	private Detail() {
		// Don't call me
	}
	public Detail(boolean good) {
		is_good = good;
	}
}

class RankInfo
{
	public int[] ranks;
	public HashMap<Integer, Integer> rank_count;
	public int[] rank_count_sorted;
	public HashMap<Character, Integer> suit_count;
	public int[] suit_count_sorted;
	public HashMap<String, Integer> card_to_hand_index;
	public int jokers;
}

class GetIsStraightResult {
	public boolean is_straight;
	public int ending_high_card;
};
    

public class Poker {
	
	final static String TAG = "Poker";
	public String name;
	public String[] hand_names;
	public String[] hand_names_caps;
	public String[] hand_names_periods;
	//int[][] prizes;
	public char[] winning_pairs;
	public int[][] payouts;
	
	// Need these defined here so that the base code doesn't freak out
    public int HAND_NOTHING = 0;
    // JacksOrBetter or TensOrBetter depending on game played
    public int HAND_WINNING_PAIR = 1;
    public int HAND_TWO_PAIR = 2;
    public int HAND_THREE_OF_A_KIND = 3;
    public int HAND_STRAIGHT = 4;
    public int HAND_FLUSH = 5;
    public int HAND_FULL_HOUSE = 6;
    public int HAND_FOUR_OF_A_KIND = 7;
    public int HAND_STRAIGHT_FLUSH = 8;
    public int HAND_ROYAL_FLUSH = 9;
    public char high_card_cutoff = 'j';
    public int RARE_HAND = HAND_FOUR_OF_A_KIND;
    
	Poker() {
        name = "error";
        hand_names = null;
        hand_names_caps = null;
        //prizes = null;
	}
    public void test_recommend_hold() { }
    public int get_hand_prize_amount( int bet_size, int hand_eval ) {
        //return this.prizes[bet_size-1][hand_eval];
        return this.payouts[bet_size-1][hand_eval];
    }
    public boolean is_hand_ok_to_hold_all_cards( int hand_eval ) {
    	return hand_eval >= HAND_STRAIGHT;
    }
    private String get_hand_eval_name( int hand_eval ) {
        return this.hand_names[hand_eval];     
    }
    private String get_hand_eval_name_caps( int hand_eval ) {
        return this.hand_names_caps[hand_eval];     
    }
    private String get_hand_eval_name_periods( int hand_eval) {
        return this.hand_names_periods[hand_eval];     
    }
    public boolean is_joker_game() { 
    	return false; 
    }
    public boolean can_double_down( int hand_eval ) {
    	// Really ought to make this abstract since it has no meaning.
    	return false;
    }
    public boolean is_jackpot( int hand_eval ) {
    	return false;
    }

    char get_card_suit( String card ) {
    	return card.charAt(1);
    }
    int get_card_rank_number( String card ) {
        int value = 0;
        char ch = card.charAt(0);
        if( ch == 'a' ) {
            value = 14;
        }
        else if( ch == 'k' ) {
            value = 13;
        }
        else if( ch == 'q' ) {
            value = 12;
        }
        else if( ch == 'j' ) {
            value = 11;
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
    private int evaluate_hand( String[] hand ) {
    	return 0;
    }
    public void log_cards( String[] hand) {
    	Log.v("Cards", "---");
    	for( int i = 0; i < hand.length; i++ ) {
    		Log.v("Cards", hand[i]);
    	}
    }
    public int[] recommend_hold( String[] hand) {
    	return null;
    }

    private void assert_hand( String[] hand, int expected_eval, int expected_prize) {
        int e = this.evaluate_hand(hand);
        if( e != expected_eval ) {
            Log.v( TAG, this.name + " eval " + hand + " is not " + expected_eval + ". Code returned " + e );
        }

        int p = this.get_hand_prize_amount( 1, e );
        if( p != expected_prize ) {
            Log.v( TAG, "prize " + hand + " is not " + expected_prize + ". Code returned " + p );
        }
    }

    Detail get_four_of_a_kind_detail( String[] hand, HashMap<Integer, Integer> rank_count) {
        String fifth_card = null;
        int rank = -1;
        int[] positions = {0,0,0,0,0};
        //for( int r: rank_count ) {
        for( Map.Entry<Integer, Integer> entry : rank_count.entrySet()) {
            Integer key = entry.getKey();
            Integer value = entry.getValue();  
            if( value == 4 ) {
            	// TB TODO - What is r[0] supposed to be here? Does it even make sense in the javascript code??
                // rank = r[0];
            	rank = key;
                for( int i = 0; i < hand.length; i++ ) {
                    if( get_card_rank_number(hand[i]) == rank ) {
                        positions[i] = 1;
                    } else {
                        fifth_card = hand[i];
                    }
                }
                break;
            }
        }
        //return [rank, fifth_card, positions]
        Detail detail = new Detail( rank != -1 );
        detail.positions = positions;
        detail.rank = rank;
        return detail;
    }

    Detail get_n_to_a_royal_flush_detail( String[] hand, int n, int[] ranks, HashMap<Character, Integer> suit_count, int[] suit_count_sorted) {
        char suit = 0;
        //int other_cards = [];
        List<String> other_cards = new ArrayList<String>();
        int[] positions = {0,0,0,0,0};
        // If the last n items in ranks (since its sorted) are all >= 10 and are of the same suit
        if(suit_count_sorted[0] >= n && ranks.length > (5-n) && ranks[5-n] >= 10) {
            //for( var s in suit_count ) {
	        for( Map.Entry<Character, Integer> entry : suit_count.entrySet() ) {
	            Character key = entry.getKey();
	            Integer value = entry.getValue();  
                //if( suit_count[s] >= n ) {
                if( value >= n ) {
                    //suit = s[0];
                	suit = key;
                    for( int i = 0; i < 5; i++ ) {
                        if( get_card_suit(hand[i]) == key && ( get_card_rank_number(hand[i]) == 10 || get_card_rank_number(hand[i]) == 11 || get_card_rank_number(hand[i]) == 12 || get_card_rank_number(hand[i]) == 13 || get_card_rank_number(hand[i]) == 14) ) {
                            positions[i] = 1;
                        } else {
                            //other_cards.push(hand[i]);
                            other_cards.add(hand[i]);
                        }
                    }
                }
            }
        }
        
        Detail detail;
        if(other_cards.size() == (5-n)) {
        	detail = new Detail(true);
            //return [suit, other_cards, positions];
	        detail.positions = positions;
        } else {
        	detail = new Detail(false);
            //return [null, [], []];
        }
        return detail;
    }

    Detail get_full_house_detail( String[] hand, HashMap<Integer, Integer> rank_count, int[] rank_count_sorted) {
        int rank1 = -1; //3 of
        int rank2 = -1; //2 of
        if( rank_count_sorted[0] == 3 && rank_count_sorted[1] == 2 ) {
            //for( var r in rank_count ) {
	        for( Map.Entry<Integer, Integer> entry : rank_count.entrySet()) {
	            Integer key = entry.getKey();
	            Integer value = entry.getValue();  
                if( value == 3 ) {
                    rank1 = key;
                } else if( value == 2 ) {
                    rank2 = key;
                }
            }
        }
        Detail detail = new Detail( rank1 != -1 && rank2 != -1 );
        //return [rank1, rank2];
        return detail;
    }

    Detail get_three_of_a_kind_detail( String[] hand, HashMap<Integer, Integer> rank_count, int[] rank_count_sorted) {
        int rank = -1;
        List<String> other_cards = new ArrayList<String>();
        int[] positions = {0,0,0,0,0};
        
        //if( rank_count_sorted[0] == 3 && rank_count_sorted[1] == 1 && (rank_count_sorted[2] == undefined || rank_count_sorted[2] == 1 ) ) {
        // TB - It is possible thank rank_count_sorted[1] will go beyond the array: K K K 2 2.
        //if( rank_count_sorted[0] == 3 && rank_count_sorted[1] == 1 ) {
        if( rank_count_sorted[0] == 3 && (rank_count_sorted.length==1 || rank_count_sorted[1] == 1) ) {
            // for( var r in rank_count ) {
	        for( Map.Entry<Integer, Integer> entry : rank_count.entrySet()) {
	            Integer key = entry.getKey();
	            Integer value = entry.getValue();  
                if( value == 3 ) {
                    rank = key;
                    for( int i = 0; i < 5; i++ ) {
                        //if( hand[i][0] == r[0] ) {
                        if( get_card_rank_number(hand[i]) == key ) {
                            positions[i] = 1;
                        } 
	                    else {
                            //other_cards.push(hand[i]);
                            other_cards.add(hand[i]);
                        }
                    }
                }
            }
        }
        //return [rank, other_cards, positions];
        Detail detail = new Detail(rank != -1);
        detail.positions = positions;
        return detail;
    }
    
    
    // TB TODO - It doesn't seem like exclude_twos is used anywhere?
    //private Detail get_n_to_a_straight_flush_detail( String[] hand, int n, int suit_count, HashMap<String, Integer> card_to_hand_index, int exclude_twos) {
    Detail get_n_to_a_straight_flush_detail( String[] hand, int n, HashMap<Character, Integer> suit_count, HashMap<String, Integer> card_to_hand_index ) {
    	boolean exclude_twos = false;
        //var that = this;
        //boolean exclude_twos = exclude_twos || false;
        // for( var s in suit_count ) {
        for( Map.Entry<Character, Integer> entry : suit_count.entrySet() ) {
            Character key = entry.getKey();
            Integer value = entry.getValue();  
            if( value >= n ) {
                // rip out cards of this suit
                List<String> subset = new ArrayList<String>();
                for( int i = 0; i < hand.length; i++ ) {
                    if( get_card_suit(hand[i]) == key && (!exclude_twos || this.get_card_rank_number(hand[i]) != 2)) {
                        subset.add(hand[i]);
                    }
                }

                // sort the hand by rank
                //var sorted_subset = subset.slice(0);
		        Comparator<String> compy = new Comparator<String>() {
		        	//@Override
		        	public int compare(String x, String y) {
		        		return get_card_rank_number(x) - get_card_rank_number(y);
		        	}
		        }; 
                ArrayList<String> sorted_subset = new ArrayList<String>(subset);
                //sorted_subset.sort( function(a,b) { return that.get_card_rank_number(a)-that.get_card_rank_number(b); } );
                Collections.sort(sorted_subset, compy);

                // if there's an ace at the end, put it at the beginning too
                // this will allow us to find straights that start or end with an ace
                //if( sorted_subset[sorted_subset.length-1][0] == 'a' ) {
                //if( sorted_subset.get(sorted_subset.size()-1).charAt(0) == 'a' ) {
                if( get_card_rank_number(sorted_subset.get(sorted_subset.size()-1)) == 14 ) { 
                    //sorted_subset.unshift(sorted_subset[sorted_subset.length-1]);
                    sorted_subset.add(0,sorted_subset.get(sorted_subset.size()-1));
                }

                // build the rank array
                List<Integer> rarr = new ArrayList<Integer>();
                for( int i = 0; i < sorted_subset.size(); i++ ) {
                    int v = this.get_card_rank_number(sorted_subset.get(i));
                    if( v == 14 && i == 0 ) v = 1;
                    rarr.add(v);
                }

                // we've got a set of N to a flush, now look for N to a straight
                for( int c = 0; c < (subset.size() - n + 1); c++ ) {
                    //if( ( rarr[c+n-1] - rarr[c] ) < 5 ) {
                    if( ( rarr.get(c+n-1) - rarr.get(c) ) < 5 ) {
                        List<String> cards = new ArrayList<String>();
                        List<String> other_cards = new ArrayList<String>();
                        int[] positions = {0,0,0,0,0};

                        // now we have N cards all within 5 of eachother and all with the same suit.. we're golden!
                        for( int i = c; i < n; i++ ) {
                            positions[card_to_hand_index.get(sorted_subset.get(i))] = 1;
                            cards.add(sorted_subset.get(i));
                        }

                        for( int i = 0; i < hand.length; i++ ) {
                            if( positions[i] == 0 ) {
                                other_cards.add(hand[i]);
                            }
                        }

                        Detail detail = new Detail(true);
                        detail.positions = positions;
                        //return [s, cards, other_cards, positions];
                        return detail;
                    }
                }
            }
        }
        //return [null, [], [], [0,0,0,0,0]];
        Detail detail = new Detail(false);
        return detail;
    }

    Detail get_n_to_an_outside_straight_detail( String[] hand, int n, int[] rank_count_sorted, HashMap<String, Integer> card_to_hand_index) {
        //var that = this;

        if( rank_count_sorted[0] <= 2 && rank_count_sorted[1] == 1 ) { //at least 4 cards must be of different ranks, this check guarantees that
            // sort hand according to rank
            //var sorted_hand = hand.slice(0);
	        Comparator<String> compy = new Comparator<String>() {
	        	//@Override
	        	public int compare(String x, String y) {
	        		return get_card_rank_number(x) - get_card_rank_number(y);
	        	}
	        }; 
        	//String[] sorted_hand = hand;
	        String[] sorted_hand = hand.clone();
            //sorted_hand.sort( function(a,b) { return that.get_card_rank_number(a)-that.get_card_rank_number(b); } );
            Arrays.sort(sorted_hand, compy);

            // NOTE! A-2-3-4 is NOT an outside straight draw
            // // if there's an ace at the end, put it at the beginning too
            // // this will allow us to find straights that start or end with an ace
            // if( sorted_hand[sorted_hand.length-1][0] == 'a' ) {
            //     sorted_hand.unshift(sorted_hand[sorted_hand.length-1]);
            // }

            for( int c = 0; c < (sorted_hand.length-n+1); c++ ) {
                // if the next n cards are all sequentially in rank order, then we're good to go
                boolean good = true;
                for( int i = c; i < (c+n-1); i++ ) {
                    int v = this.get_card_rank_number(sorted_hand[i]);
                    //if( i == 0 && v == 14 ) v = 1;
                    if( v + 1 != this.get_card_rank_number(sorted_hand[i+1]) ) {
                        good = false;
                        break;
                    }
                }

                if(good) {
                    // the last card cannot be an ace, that's not an outside straight draw
                    if( this.get_card_rank_number(sorted_hand[c+n-1]) == 14 ) {
                        good = false;
                    }
                }

                if(good) {
                    List<String> cards = new ArrayList<String>();
                    String[] other_cards = hand.clone();
                    int[] position = {0,0,0,0,0};
                    for( int i = c; i < c+n; i++ ) {
                        cards.add(sorted_hand[i]);
                        position[card_to_hand_index.get(sorted_hand[i])] = 1;
                        // TB TODO - other_cards is not used, so just screw it?
                        //other_cards.splice(other_cards.indexOf(sorted_hand[i]), 1);
                    }
                    //return [cards, other_cards, position];
                    Detail detail = new Detail( true );
                    detail.positions = position;
                    return detail;
                }
            }
        }
        //return [null, [], [0,0,0,0,0]];
        return new Detail( false );
    }

    Detail get_n_to_a_straight_detail( String[] hand, int n, int[] rank_count_sorted, HashMap<String, Integer> card_to_hand_index, boolean exclude_twos) {
        //var that = this;
        //exclude_twos = exclude_twos || false;

        List<String> newhand = new ArrayList<String>();
        for( int i = 0; i < hand.length; i++ ) {
            if( !exclude_twos || ( this.get_card_rank_number(hand[i]) != 2 ) ) {
                newhand.add(hand[i]);
            }
        }

        // sort hand according to rank
        //var sorted_hand = newhand.slice(0);
        List<String> sorted_hand = newhand;
        Comparator<String> compy = new Comparator<String>() {
        	//@Override
        	public int compare(String x, String y) {
        		return get_card_rank_number(x) - get_card_rank_number(y);
        	}
        }; 
        //sorted_hand.sort( function(a,b) { return that.get_card_rank_number(a)-that.get_card_rank_number(b); } );
        Collections.sort(sorted_hand, compy);

        // if there's an ace at the end, put it at the beginning too
        // this will allow us to find straights that start or end with an ace
        //if( sorted_hand[sorted_hand.length-1][0] == 'a' ) {
        //if( sorted_hand.get(sorted_hand.size()-1).charAt(0) == 'a' ) {
        if( get_card_rank_number(sorted_hand.get(sorted_hand.size()-1)) == 14 ) {
            //sorted_hand.unshift(sorted_hand[sorted_hand.length-1]);
        	sorted_hand.add(0, sorted_hand.get(sorted_hand.size()-1));
        }

        for( int c = 0; c < (sorted_hand.size()-n+1); c++ ) {
            // build a hand spanning N cards, removing pairs/triples/etc 

            int lastv = this.get_card_rank_number(sorted_hand.get(c));
            if( c == 0 && lastv == 14 ) lastv = 1;

            List<String> testhand = new ArrayList<String>();
            testhand.add(sorted_hand.get(c));

            for( int i = 1; testhand.size() < n && (c+i) < sorted_hand.size(); i++ ) {
                int v = this.get_card_rank_number(sorted_hand.get(c+i));
                if( v == lastv ) continue;
                testhand.add(sorted_hand.get(c+i));
                lastv = v;
            }

            if( testhand.size() == n ) {
                // if the next n cards span less than a distance of 5, then we're good to go
                int distance = 4; //one less, since we only check 4 cards
                lastv = get_card_rank_number(testhand.get(0));
                if( lastv == 14 ) lastv = 1;
                for( int i = 1; i < n; i++ ) {
                    int v = this.get_card_rank_number(testhand.get(i));
                    int dx = v - lastv;
                    distance -= dx;
                    if( distance < 0 ) {
                        break;
                    }
                    lastv = v;
                }

                if( distance >= 0 ) { //got through N cards with less than 5 
                    List<String> cards = new ArrayList<String>();
                    //var other_cards = newhand.slice(0);
                    List<String> other_cards = newhand;
                    int[] position = {0,0,0,0,0};
                    for( int i = 0; i < n; i++ ) {
                        cards.add(testhand.get(i));
                        //position[card_to_hand_index[testhand[i]]] = 1;
                        position[card_to_hand_index.get(testhand.get(i))] = 1;
                        // TB TODO - This isn't being used for anything, so just screw it?
                        //other_cards.splice(other_cards.indexOf(testhand[i]), 1);
                    }
                    // return [cards, other_cards, position];
                    Detail detail = new Detail(true);
                    detail.positions = position;
                    return detail;
                }
            }
        }
        //return [null, [], [0,0,0,0,0]];
        return new Detail(false);
    }
 
    class PairData {
    	int rank;
    	List<String> curpair;
    	int[] positions;
    	PairData( int r, List<String> c, int[] p ) {
    		rank = r;
    		curpair = c;
    		positions = p;
    	}
    }
    class PairDetail extends Detail {
    	List <PairData> list;
    	PairDetail() {
    		super(false);
    		list = new ArrayList<PairData>();
    	}
	}
    PairDetail get_pair_details( String[] hand, HashMap<Integer, Integer> rank_count) {
        PairDetail detail = new PairDetail();
        //List pairs = new ArrayList<String>();
        int[] all_positions = {0,0,0,0,0};
        //for( var r in rank_count ) {
        for( Map.Entry<Integer, Integer> entry : rank_count.entrySet()) {
            Integer key = entry.getKey();
            Integer value = entry.getValue();  
            //if( rank_count[r] == 2 ) {
            if( value == 2 ) {
                List<String> curpair = new ArrayList<String>();
                int[] positions = {0,0,0,0,0};
                for( int i = 0; i < hand.length; i++ ) {
                    //if( hand[i].charAt(0) == key ) {
                	if( get_card_rank_number(hand[i]) == key ) {
                        curpair.add(hand[i]);
                        positions[i] = 1;
                        all_positions[i] = 1;
                    }
                }
                //pairs.push([r, curpair, positions]);
                detail.list.add( new PairData(key, curpair, positions) );
            }
        }

        //sort pairs based on rank
        // TB - This sorting is not needed since pairs variable is never used
        /*
        var that = this;
        pairs.sort( function(a,b) { return that.get_card_rank_number(a[0])-that.get_card_rank_number(b[0]); } );
        */

        //return [pairs.length, pairs, all_positions];
        detail.positions = all_positions;
        return detail;
    }

    //Detail get_n_to_a_flush_detail( String[] hand, int n, HashMap<Character, Integer> suit_count, boolean exclude_twos) {
    Detail get_n_to_a_flush_detail( String[] hand, int n, HashMap<Character, Integer> suit_count ) {
        char suit = 'x';
        List other_cards = new ArrayList<String>();
        int[] positions = {0,0,0,0,0};
        //exclude_twos = exclude_twos || false;
        // TB - exclude_twos is never passed?
        boolean exclude_twos = false;
        //for( var s in suit_count ) {
        for( Map.Entry<Character, Integer> entry : suit_count.entrySet()) {
            Character key = entry.getKey();
            Integer value = entry.getValue();  
            //if( suit_count[s] >= n ) {
            if( value >= n ) {
                //suit = s[0];
            	suit = key;
                for( int i = 0; i < hand.length; i++ ) {
                    if( get_card_suit(hand[i]) == suit && (!exclude_twos || this.get_card_rank_number(hand[i]) != 2) ) {
                        positions[i] = 1;
                    } else {
                        other_cards.add(hand[i]);
                    }
                }
                break;
            }
        }
        //return [suit, other_cards, positions]
        Detail detail = new Detail( suit != 'x' );
        detail.positions = positions;
        return detail;
    }
    
	class BysuitData {
		List<String> list;
		int[] pos = {0,0,0,0,0};
		BysuitData() {
			list = new ArrayList<String>();
		}
	};
	class HighCardDetail extends Detail {
        HashMap<Character, BysuitData> bysuit;
        List<String> cards;
		public HighCardDetail() {
			super(false);
			bysuit = new HashMap<Character, BysuitData>();
			bysuit.put( 's', new BysuitData() );
			bysuit.put( 'c', new BysuitData() );
			bysuit.put( 'h', new BysuitData() );
			bysuit.put( 'd', new BysuitData() );
			cards = new ArrayList<String>();
		}
	};

    HighCardDetail get_high_cards_detail( String[] hand) {
        List<String> cards = new ArrayList<String>();
        int[] position = {0,0,0,0,0};
        HighCardDetail detail = new HighCardDetail();
        //var bysuit = {'s': [[], [0,0,0,0,0]], 'c': [[], [0,0,0,0,0]], 'h': [[], [0,0,0,0,0]], 'd': [[], [0,0,0,0,0]]};
        //HashMap<Character, Fool> bysuit = new HashMap<Character, Fool>();
        
        for( int i = 0; i < hand.length; i++ ) {
            //if( get_card_rank_number(hand[i]) >= get_card_rank_number(this.high_card_cutoff) ) {
            if( get_card_rank_number(hand[i]) >= get_card_rank_number(this.high_card_cutoff + "h") ) {
                position[i] = 1;
                cards.add(hand[i]);
                //bysuit[hand[i][1]][0].push(hand[i]);
                //bysuit.get( hand[i].charAt(1) ).list.add( hand[i] );
                //detail.bysuit.get( hand[i].charAt(1) ).list.add( hand[i] );
                detail.bysuit.get( get_card_suit(hand[i]) ).list.add( hand[i] );
                
                //bysuit[hand[i][1]][1][i] = 1;
                //bysuit.get( hand[i].charAt(1) ).pos[i] = 1;
                //detail.bysuit.get( hand[i].charAt(1) ).pos[i] = 1;
                detail.bysuit.get( get_card_suit(hand[i]) ).pos[i] = 1;
            }
        }
        //return [cards, bysuit, position];
        detail.positions = position;
        detail.cards = cards;
        return detail;
    }

}

//
// TB TODO - Finish this :(
//
class PokerNoJoker extends Poker {
	PokerNoJoker() {
        this.HAND_NOTHING = 0;
        // JacksOrBetter or TensOrBetter depending on game played
        this.HAND_WINNING_PAIR = 1;
        this.HAND_TWO_PAIR = 2;
        this.HAND_THREE_OF_A_KIND = 3;
        this.HAND_STRAIGHT = 4;
        this.HAND_FLUSH = 5;
        this.HAND_FULL_HOUSE = 6;
        this.HAND_FOUR_OF_A_KIND = 7;
        this.HAND_STRAIGHT_FLUSH = 8;
        this.HAND_ROYAL_FLUSH = 9;
        //this.winning_pairs = [];
        //this.high_card_cutoff = 't';
        //this.RARE_HAND = this.HAND_FOUR_OF_A_KIND;
    }
    public boolean is_jackpot( int hand_eval ) {
    	return hand_eval == HAND_ROYAL_FLUSH;
    }
	
    RankInfo get_rank_info( final String[] hand) {
        HashMap<Integer, Integer> rank_count = new HashMap<Integer, Integer>();
        Integer ranks[] = new Integer[5];
        HashMap<Character, Integer> suit_count = new HashMap<Character, Integer>();
        HashMap<String, Integer> card_to_hand_index = new HashMap<String, Integer>();
        for( int ci = 0; ci < hand.length; ci++ ) {
            String card = hand[ci];

            card_to_hand_index.put(card, ci);

            //int rank = card.charAt(0);
            int rank = get_card_rank_number(card);
            if( rank_count.containsKey( rank) ) {
                //rank_count[card[0]] = rank_count[card[0]] + 1;
            	rank_count.put( rank, rank_count.get(rank) + 1 );
            } else {
                //rank_count[card[0]] = 1;
            	rank_count.put( rank, 1 );
            }

            //char suit = card.charAt(1);
            char suit = get_card_suit(card);
            if( suit_count.containsKey( suit )) {
                //suit_count[card[1]] = suit_count[card[1]] + 1;
            	suit_count.put( suit, suit_count.get(suit) + 1 );
            } else {
                //suit_count[card[1]] = 1;
            	suit_count.put( suit, 1 );
            }

            // ranks.push( this.get_card_rank_number(card) );
            ranks[ci] = get_card_rank_number(card);
        }
        
        Comparator<Integer> compyDesc = new Comparator<Integer>() {
        	public int compare(Integer x, Integer y) {
        		return y-x;
        	}
        };
        Comparator<Integer> compyAsc = new Comparator<Integer>() {
        	public int compare(Integer x, Integer y) {
        		return x-y;
        	}
        };

        // TB - Need to parseInt the values because javascript is treating the values as strings,
        // even though ranks only contains integers...
        //ranks.sort( function(a,b){ return parseInt(a)-parseInt(b); })
        //Arrays.sort( ranks, compy );
        //Arrays.sort( ranks, Collections.reverseOrder() );
        Arrays.sort( ranks, compyAsc );
        //Arrays.sort(array, comparator)

        // sort reversed
        Integer[] rank_count_sorted = new Integer[rank_count.size()];
        //for( var r: rank_count ) {
        int idx = 0;
        for( Map.Entry<Integer, Integer> entry : rank_count.entrySet()) {
            Integer key = entry.getKey();
            Integer value = entry.getValue();  
            //rank_count_sorted.push(rank_count[r]);
            rank_count_sorted[idx] = value;
            idx++;
        }
        //rank_count_sorted.sort( function(a,b){ return parseInt(b)-parseInt(a); })
        Arrays.sort( rank_count_sorted, compyDesc );

        // sort reversed
        Integer[] suit_count_sorted = new Integer[suit_count.size()];
        //for( int r: suit_count ) {
        idx = 0;
        for( Map.Entry<Character, Integer> entry : suit_count.entrySet()) {
            Character key = entry.getKey();
            Integer value = entry.getValue();  
            //suit_count_sorted.push(suit_count[r]);
            suit_count_sorted[idx] = value;
            idx++; 
        }
        //suit_count_sorted.sort( function(a,b){ return parseInt(b)-parseInt(a); })
        Arrays.sort( suit_count_sorted, compyDesc );
        
        // This is retarded
        int ranks_int[] = new int[5];
        for( int i = 0; i < 5; i++ ) {
        	ranks_int[i] = ranks[i];
        }
        int rank_count_sorted_int[] = new int[rank_count.size()];
        for( int i = 0; i < rank_count.size(); i++ ) {
        	rank_count_sorted_int[i] = rank_count_sorted[i];
        }
        int suit_count_sorted_int[] = new int[suit_count.size()];
        for( int i = 0; i < suit_count.size(); i++ ) {
        	suit_count_sorted_int[i] = suit_count_sorted[i];
        }
        
        // return [ranks, rank_count, rank_count_sorted, suit_count, suit_count_sorted, card_to_hand_index];
        RankInfo rank_info = new RankInfo();
        rank_info.ranks = ranks_int;
        rank_info.rank_count = rank_count;
        rank_info.rank_count_sorted = rank_count_sorted_int;
        rank_info.suit_count = suit_count;
        rank_info.suit_count_sorted = suit_count_sorted_int;
        rank_info.card_to_hand_index = card_to_hand_index;
        return rank_info;
    }
    public boolean can_double_down( int hand_eval) {
        return hand_eval > HAND_NOTHING && hand_eval <= HAND_FOUR_OF_A_KIND;
    }

    private int get_num_high( String[] hand) {
        int num_high = 0;
        for( int i = 0; i < hand.length; i++ ) {
            int value = this.get_card_rank_number(hand[i]);
            // TB TODO How did passing a single char to get_card_rank_number work in the JS version? Don't we need to pass a string "5d" or whatever?
            //if( value >= this.get_card_rank_number( this.high_card_cutoff) ) { //a 'high' card is one that satisfies the pair clause
            if( value >= this.get_card_rank_number( this.high_card_cutoff + "s" ) ) { //a 'high' card is one that satisfies the pair clause
                num_high += 1;
            } 
        }
        return num_high;
    }

    private int get_num_low( String[] hand) {
        return hand.length - this.get_num_high(hand);
    }

    int get_four_of_a_kind( HashMap<Integer, Integer> rank_count) {
        return HAND_FOUR_OF_A_KIND;
    }

    boolean get_is_flush( String[] hand) {
        boolean is_flush = true;
        for( int i = 0; i < hand.length - 1; i++ ) {
            //if(hand[i][1] != hand[i+1][1]) {
        	//if( hand[i].charAt(1) != hand[i+1].charAt(1) ) {
        	if( get_card_suit(hand[i]) != get_card_suit(hand[i+1]) ) {
                is_flush = false;
                break;
            }
        }
        return is_flush;
    }

    boolean get_is_straight( int[] ranks, int[] rank_count_sorted) {
        boolean is_straight = false;
        if(rank_count_sorted[0] == 1) {
            if( ranks[4] - ranks[0] == 4 ) {
                is_straight = true;
            }
            if( ranks[4] == 14 && ranks[3] == 5 ) {
                // Special A2345 case
                is_straight = true;
            }
        }
        return is_straight;
    }

    public boolean is_joker_game() {
        return false;
    }
    private boolean is_hand_eval_worth_fireworks( int hand_eval ) {
        return hand_eval >= this.HAND_FLUSH;
    }
    private int evaluate_hand( final String[] hand ) {
    
        // kh, 4s
        // card = rank + suit
        RankInfo rank_info = this.get_rank_info(hand);
        int[] ranks = rank_info.ranks;
        HashMap<Integer, Integer> rank_count = rank_info.rank_count;
        int[] rank_count_sorted = rank_info.rank_count_sorted;

        boolean is_flush = this.get_is_flush(hand);
        boolean is_straight = this.get_is_straight(ranks, rank_count_sorted);

        if( is_flush && is_straight ) {
            if( ranks[4] == 14 && ranks[3] == 13 ) {
                return this.HAND_ROYAL_FLUSH;
            }
            return this.HAND_STRAIGHT_FLUSH;
        }

        if( rank_count_sorted[0] == 4 ) {
            return this.get_four_of_a_kind(rank_count);
        }

        if( rank_count_sorted[0] == 3 && rank_count_sorted[1] == 2 ) {
            return this.HAND_FULL_HOUSE;
        } 

        if( is_flush ) {
            return this.HAND_FLUSH;
        }

        if( is_straight ) {
            return this.HAND_STRAIGHT;
        }

        if( rank_count_sorted[0] == 3 && rank_count_sorted[1] == 1 && rank_count_sorted[2] == 1 ) {
            return this.HAND_THREE_OF_A_KIND;
        }
        if( rank_count_sorted[0] == 2 && rank_count_sorted[1] == 2 && rank_count_sorted[2] == 1 ) {
            return this.HAND_TWO_PAIR;
        } 
            
        if( rank_count_sorted[0] == 2 && rank_count_sorted[1] == 1 && rank_count_sorted[2] == 1 && rank_count_sorted[3] == 1 ) {
            for( int i = 0; i < this.winning_pairs.length; i++ ) {
                int r = this.winning_pairs[i];
                //if( r in rank_count && rank_count[r] == 2 ) {
                if( rank_count.containsKey(r) && rank_count.get(r) == 2 ) {
                    return this.HAND_WINNING_PAIR;
                }
            }
        }

        return this.HAND_NOTHING;
    }

    public int[] recommend_hold( String[] hand) {
    	/*
    	int[] foo = {0,0,1,0,0};
    	return foo;
    	*/
    	
        int[] to_hold = {0,0,0,0,0};
        int[] all_hold = {1,1,1,1,1};
        int[] no_hold = {0,0,0,0,0};

        RankInfo rank_info = this.get_rank_info(hand);
        //var ranks = result[0];
        //var rank_count = result[1];
        //var rank_count_sorted = result[2];
        //var suit_count = result[3];
        //var suit_count_sorted = result[4];
        //var card_to_hand_index = result[5];
		int[] ranks = rank_info.ranks;
		HashMap<Integer, Integer> rank_count = rank_info.rank_count;
		int[] rank_count_sorted = rank_info.rank_count_sorted;
		HashMap<Character, Integer> suit_count = rank_info.suit_count;
		int[] suit_count_sorted = rank_info.suit_count_sorted;
		HashMap<String, Integer> card_to_hand_index = rank_info.card_to_hand_index;

        boolean is_flush = this.get_is_flush(hand);
        boolean is_straight = this.get_is_straight(ranks, rank_count_sorted);

        // 1. capture royal flush
        if( is_flush && is_straight ) return all_hold;

        // 2. capture four of a kind
        Detail fok_detail = this.get_four_of_a_kind_detail(hand, rank_count);
        if( fok_detail.is_good ) return fok_detail.positions;

        // 4 to a royal flush (use >= to capture would-be flushes)
        // If the last 4 items in ranks (since its sorted) are all >= 10
        Detail rf_detail = this.get_n_to_a_royal_flush_detail(hand, 4, ranks, suit_count, suit_count_sorted);
        if(rf_detail.is_good ) return rf_detail.positions;

        // Full House
        Detail fh_detail = this.get_full_house_detail(hand, rank_count, rank_count_sorted);
        if( fh_detail.is_good ) return all_hold;

        // Straight
        if( is_straight ) return all_hold;

        // Flush
        if( is_flush ) return all_hold;

        // Three of a kind
        Detail tok_detail = this.get_three_of_a_kind_detail(hand, rank_count, rank_count_sorted);
        if(tok_detail.is_good) return tok_detail.positions;

        // 4 to a straight flush
        Detail ftosf = this.get_n_to_a_straight_flush_detail(hand, 4, suit_count, card_to_hand_index);
        if(ftosf.is_good) return ftosf.positions;

        // 2 pair
        PairDetail pair_details = get_pair_details(hand, rank_count);
        //if(pair_details[0] == 2) return pair_details[2];
        if( pair_details.list.size() == 2 ) return pair_details.positions;

        // high pair (jacks or better)
        //if(pair_details[0] == 1 && this.get_card_rank_number(pair_details[1][0][0]) >= this.get_card_rank_number(this.high_card_cutoff)) return pair_details[1][0][2];
        //if( pair_details.list.size() == 1 && get_card_rank_number(pair_details.list.get(0).rank) >= get_card_rank_number(high_card_cutoff+"s"))
        if( pair_details.list.size() == 1 && pair_details.list.get(0).rank >= get_card_rank_number(high_card_cutoff+"s")) return pair_details.list.get(0).positions;

        // 3 to a royal flush
        rf_detail = this.get_n_to_a_royal_flush_detail(hand, 3, ranks, suit_count, suit_count_sorted);
        if(rf_detail.is_good) return rf_detail.positions;

        // 4 to a flush
        Detail ftof = this.get_n_to_a_flush_detail(hand, 4, suit_count);
        if(ftof.is_good) return ftof.positions;

        // low pair
        //if( pair_details[0] == 1 ) return pair_details[1][0][2];
        if( pair_details.list.size() == 1 ) return pair_details.list.get(0).positions;

        // 4 to an outside straight
        Detail ftoos = this.get_n_to_an_outside_straight_detail(hand, 4, rank_count_sorted, card_to_hand_index);
        if(ftoos.is_good) return ftoos.positions;

        // 2 suited high cards
        HighCardDetail hc_detail = this.get_high_cards_detail(hand);
        //for( var s in hc_detail[1] ) {
        for( Map.Entry<Character, BysuitData> entry : hc_detail.bysuit.entrySet()) {
            Character key = entry.getKey();
            BysuitData value = entry.getValue();  
            //if( hc_detail[1][s][0].length >= 2 ) {
            if( value.list.size() >= 2 ) {
                //return hc_detail[1][s][1];
                return value.pos;
            }
        }

        // 3 to a straight flush
        Detail thtosf = this.get_n_to_a_straight_flush_detail(hand, 3, suit_count, card_to_hand_index);
        if( thtosf.is_good ) return thtosf.positions;

        // 2 unsuited highcards (if more than 2, pick the lowest 2)
        //if( hc_detail[0].length >= 2 ) {
        if( hc_detail.cards.size() >= 2 ) {
            //var sorted_cards = hc_detail[0].slice(0);
        	List<String> sorted_cards = hc_detail.cards;
            //sorted_cards.sort( function(a,b) { return that.get_card_rank_number(a)-that.get_card_rank_number(b); } );
	        Comparator<String> compy = new Comparator<String>() {
	        	//@Override
	        	public int compare(String x, String y) {
	        		return get_card_rank_number(x) - get_card_rank_number(y);
	        	}
	        }; 
	        Collections.sort( sorted_cards, compy );

            int[] to_holddd = {0,0,0,0,0};
            for( int i = 0; i < 2; i++ ) {
                to_holddd[card_to_hand_index.get(sorted_cards.get(i))] = 1;
            }

            return to_holddd;
        }

        // suited 10/j, 10/q, or 10/k
        // CM TODO - not sure how this one ties into high_card_cutoff or why 10 is special to pair with only jqk but not a?
        if( suit_count_sorted[0] >= 2 ) {
            //for( var s in suit_count ) {
	        for( Map.Entry<Character, Integer> entry : suit_count.entrySet()) {
	            Character key = entry.getKey();
	            Integer value = entry.getValue();  
                //if( suit_count[s] >= 2 ) {
                if( value >= 2 ) {
                    String tc = null;
                    String oc = null;

                    for( int i = 0; i < 5; i++ ) {
                        //if( hand[i][1] == s[0] ) {
                        //if( hand[i].charAt(1) == key ) {
                        if( get_card_suit(hand[i]) == key ) {
                        	int rank = get_card_rank_number(hand[i]);
                            //if( hand[i].charAt(0) == 't' && tc == null ) tc = hand[i];
                            if( rank == 10 && tc == null ) tc = hand[i];
                            //if( ("jqk").indexOf(hand[i].charAt(0)) != -1 && oc == null ) oc = hand[i];
                            if( (rank == 11 || rank == 12 || rank == 13) && oc == null ) oc = hand[i];
                        }
                    }

                    if( tc != null && oc != null ) {
                        to_hold[card_to_hand_index.get(tc)] = 1;
                        to_hold[card_to_hand_index.get(oc)] = 1;
                        return to_hold;
                    }
                }
            }
        }

        // one high card
        //if( hc_detail[0].length == 1 ) return hc_detail[2];
        if( hc_detail.cards.size() == 1 ) return hc_detail.positions;

        // discard everything
        return no_hold;
    }
}

class PokerJacksOrBetter extends PokerNoJoker {
	PokerJacksOrBetter() {
        name = "Jacks or Better";
        hand_names = new String[] { "Nothing", "One Pair", "Two Pair", "3 of a Kind", "Straight", "Flush", "Full House", "4 of a Kind", "Straight Flush", "Royal Flush" };
        hand_names_caps = new String[] { "NOTHING", "JACKS OR BETTER", "TWO PAIR", "3 OF A KIND", "STRAIGHT", "FLUSH", "FULL HOUSE", "4 OF A KIND", "STRAIGHT FLUSH", "ROYAL FLUSH" };
        hand_names_periods = new String[] { "", "", "...............", "............", "...............", ".....................", "..........", "............", "..", "........"  };
        payouts = new int[][] { { 0, 1, 2, 3, 4, 6, 9, 25, 50, 250 },
        					{ 0, 2, 4, 6, 8, 12, 18, 50, 100, 500},
        					{ 0, 3, 6, 9, 12, 18, 27, 75, 150, 750},
        					{ 0, 4, 8, 12, 16, 24, 36, 100, 200, 1000},
        					{ 0, 5, 10, 15, 20, 30, 45, 125, 250, 4000}};
        winning_pairs = new char[] {'j', 'q', 'k', 'a'};
        high_card_cutoff = 'j';
    }
    private void checkit( String[] hand, int[] desired_hold) {
    	int[] actual_hold = recommend_hold(hand);
    	for( int i = 0; i < 5; i++ ) {
    		if( actual_hold[i] != desired_hold[i] ) {
    			Log.e("recommend_hold", String.format("%s %s %s %s %s", hand[0], hand[1], hand[2], hand[3], hand[4]) + " incorrectly held " + String.format("%d%d%d%d%d",actual_hold[0],actual_hold[1],actual_hold[2],actual_hold[3], actual_hold[4]) );
    			return;
    		}
    	}
    }

    public void test_recommend_hold() {
    	Log.v("recommend_hold", "STARTING TESTS!");
    	// four of a kind
    	checkit( new String[] { "4h", "4c", "4d", "9h", "4s"}, new int[] {1,1,1,0,1} );
    	// full house
    	checkit( new String[] { "4h", "4c", "8d", "8h", "4s"}, new int[] {1,1,1,1,1} );
    	// two pair
    	checkit( new String[] { "4h", "4c", "8d", "8h", "as"}, new int[] {1,1,1,1,0} );
    	
    	// high pair
    	checkit( new String[] { "ah", "4c", "as", "9h", "ts"}, new int[] {1,0,1,0,0} );
    	checkit( new String[] { "ah", "9s", "as", "5s", "4s"}, new int[] {1,0,1,0,0} );
    	
    	// high card
    	checkit( new String[] { "ah", "4c", "7d", "9h", "ts"}, new int[] {1,0,0,0,0} ); 
    	
    	// 4 to royal flush
    	// TB TODO - Hmm it seems to just go for the flush?
    	// checkit( new String[] { "th", "jh", "9h", "qh", "kh"}, new int[] {1,1,0,1,1} );
    	
    	// 4 to straight flush
    	// TB TODO - Or should it just hold the flush???
    	// checkit( new String[] { "6h", "7h", "9h", "th", "5h"}, new int[] {1,1,1,1,0} );
    	
    	// flush
    	checkit( new String[] { "6h", "9h", "jh", "2h", "5h"}, new int[] {1,1,1,1,1} );
    	
    	// straight
    	checkit( new String[] { "6h", "ts", "8s", "9c", "7s"}, new int[] {1,1,1,1,1} );
    	
    	// 4 to a straight outside straight
    	checkit( new String[] { "6h", "7s", "8s", "9c", "ks"}, new int[] {1,1,1,1,0} );
    	
    	// 4 to a inside straight... not worth going for it?
    	//checkit( new String[] { "6h", "7s", "2s", "tc", "9s"}, new int[] {1,1,1,1,0} );
    	
    	// 4 to a flush
    	checkit( new String[] { "7h", "9s", "as", "5s", "4s"}, new int[] {0,1,1,1,1} );
    	
    	// Low pair
    	checkit( new String[] { "7s", "th", "9h", "jh", "7c"}, new int[] {1,0,0,0,1} );
    	
    	// crap
    	checkit( new String[] { "7h", "9d", "4s", "2c", "3s"}, new int[] {0,0,0,0,0} );
    	Log.v("recommend_hold", "DONE!");
    }
}


//
// TB TOOD - OTHER GAMES
//

class PokerTensOrBetter extends PokerNoJoker {

    PokerTensOrBetter() {
        this.name = "Tens or Better";
        this.hand_names = new String[] { "Nothing", "One Pair", "Two Pair", "3 of a Kind", "Straight", "Flush", "Full House", "4 of a Kind", "Straight Flush", "Royal Flush" };
        this.hand_names_caps = new String[] { "NOTHING", "TENS OR BETTER", "TWO PAIR", "3 OF A KIND", "STRAIGHT", "FLUSH", "FULL HOUSE", "4 OF A KIND", "STRAIGHT FLUSH", "ROYAL FLUSH" };
        this.hand_names_periods = new String[] { "", "..", "...............", "............", "...............", ".....................", "..........", "............", "..", "........" };
        payouts = new int[][] { { 0, 1, 2, 3, 4, 5, 6, 25, 50, 250 },
        					{ 0, 2, 4, 6, 8, 10, 12, 50, 100, 500},
        					{ 0, 3, 6, 9, 12, 15, 18, 75, 150, 750},
        					{ 0, 4, 8, 12, 16, 20, 24, 100, 200, 1000},
        					{ 0, 5, 10, 15, 20, 25, 30, 125, 250, 4000}};
        this.winning_pairs = new char[] {'t', 'j', 'q', 'k', 'a'};
        this.high_card_cutoff = 't';
    }
}
class PokerBonusDeluxe extends PokerJacksOrBetter {
    PokerBonusDeluxe() {
        this.name = "Bonus Deluxe";
        payouts = new int[][] { { 0, 1, 1, 3, 4, 6, 8, 80, 50, 250 },
        					{ 0, 2, 2, 6, 8, 12, 16, 160, 100, 500},
        					{ 0, 3, 3, 9, 12, 18, 24, 240, 150, 750},
        					{ 0, 4, 4, 12, 16, 24, 32, 320, 200, 1000},
        					{ 0, 5, 5, 15, 20, 30, 40, 400, 250, 4000}};
    }
    public boolean can_double_down( int hand_eval ) {
        return hand_eval > this.HAND_NOTHING && hand_eval <= this.HAND_FULL_HOUSE;
    }
}
class PokerBonus extends PokerJacksOrBetter {
    int HAND_NOTHING = 0;
    int HAND_WINNING_PAIR = 1;
    int HAND_TWO_PAIR = 2;
    int HAND_THREE_OF_A_KIND = 3;
    int HAND_STRAIGHT = 4;
    int HAND_FLUSH = 5;
    int HAND_FULL_HOUSE = 6;
    int HAND_FOUR_OF_A_KIND_5_TO_K = 7;
    int HAND_FOUR_OF_A_KIND_2_TO_4 = 8;
    int HAND_FOUR_OF_A_KIND_ACES = 9;
    int HAND_STRAIGHT_FLUSH = 10;
    int HAND_ROYAL_FLUSH = 11;
    int RARE_HAND = HAND_FOUR_OF_A_KIND_5_TO_K;
    PokerBonus() {
        name = "Bonus";
        hand_names = new String[] { "Nothing", "One Pair", "Two Pair", "3 of a Kind", "Straight", "Flush", "Full House", "4 5-K", "4 2-4", "4 Aces", "Straight Flush", "Royal Flush" };
        hand_names_caps = new String[] { "NOTHING", "JACKS OR BETTER", "TWO PAIR", "3 OF A KIND", "STRAIGHT", "FLUSH", "FULL HOUSE", "4 5-K", "4 2-4", "4 ACES", "STRAIGHT FLUSH", "ROYAL FLUSH" };
        hand_names_periods = new String[] { "", "", "...............", "............", "...............", ".....................", "..........", "........................", "........................", "....................", "..", "........" };
        payouts = new int[][] { { 0, 1, 2, 3, 4, 5, 8, 25, 40, 80, 50, 250 },
                { 0, 2, 4, 6, 8, 10, 16, 50, 80, 160, 100, 500},
                { 0, 3, 6, 9, 12, 15, 24, 75, 120, 240, 150, 750},
                { 0, 4, 8, 12, 16, 20, 32, 100, 160, 320, 200, 1000},
                { 0, 5, 10, 15, 20, 25, 40, 125, 200, 400, 250, 4000}};
    }
    public boolean is_jackpot( int hand_eval ) {
        return hand_eval == HAND_ROYAL_FLUSH;
    }
    public boolean can_double_down( int hand_eval) {
        return hand_eval > HAND_NOTHING && hand_eval <= HAND_FULL_HOUSE;
    }
    int get_four_of_a_kind(HashMap<Integer, Integer> rank_count) {
        if( rank_count.get(14) == 4 ) {
            return HAND_FOUR_OF_A_KIND_ACES;
        }
        else if( rank_count.get(2) == 4 || rank_count.get(3) == 4 || rank_count.get(4) == 4 ) {
            return HAND_FOUR_OF_A_KIND_2_TO_4;
        }

        return HAND_FOUR_OF_A_KIND_5_TO_K;
    }
    Detail get_four_of_a_kind_detail( String[] hand, HashMap<Integer, Integer> rank_count ) {
        String fifth_card = null;
        int fifth_card_index=0;
        int rank = -1;
        int[] positions = {0,0,0,0,0};
        //for( var r in rank_count ) {
        for( Map.Entry<Integer, Integer> entry : rank_count.entrySet()) {
            Integer key = entry.getKey();
            Integer value = entry.getValue();
            //if( rank_count[r] == 4 ) {
            if( value == 4 ) {
                //rank = r[0];
                rank = key;
                for( int i = 0; i < hand.length; i++ ) {
                    //if( hand[i][0] == rank ) {
                    if( get_card_rank_number(hand[i]) == rank ) {
                        positions[i] = 1;
                    } else {
                        fifth_card = hand[i];
                        fifth_card_index = i;
                    }
                }
                int v = this.get_card_rank_number(fifth_card);
                if( v >= 2 && v <= 4 ) { // hold A-4 but throw away 5-K
                    positions[fifth_card_index] = 1;
                }
            }
        }
        //return [rank, fifth_card, positions]
        Detail detail = new Detail( rank != -1 );
        detail.positions = positions;
        detail.rank = rank;
        return detail;
    }

}


class PokerDoubleBonus extends PokerBonus {
	PokerDoubleBonus() {
        this.name = "Double Bonus";
        payouts = new int[][] { { 0, 1, 1, 3, 5, 7, 9, 50, 80, 160, 50, 250 },
        					{ 0, 2, 2, 6, 10, 14, 18, 100, 160, 320, 100, 500},
        					{ 0, 3, 3, 9, 15, 21, 27, 150, 240, 480, 150, 750},
        					{ 0, 4, 4, 12, 20, 28, 36, 200, 320, 640, 200, 1000},
        					{ 0, 5, 5, 15, 20, 35, 45, 250, 400, 800, 250, 4000}};
    }
}

class PokerDoubleDoubleBonus extends PokerJacksOrBetter {
    int HAND_NOTHING = 0;
    int HAND_WINNING_PAIR = 1;
    int HAND_TWO_PAIR = 2;
    int HAND_THREE_OF_A_KIND = 3;
    int HAND_STRAIGHT = 4;
    int HAND_FLUSH = 5;
    int HAND_FULL_HOUSE = 6;
    int HAND_FOUR_OF_A_KIND_5_TO_K = 7;
    int HAND_FOUR_OF_A_KIND_2_TO_4 = 8;
    int HAND_FOUR_OF_A_KIND_ACES = 9;
    int HAND_FOUR_OF_A_KIND_2_TO_4_WITH_A_TO_4 = 10;
    int HAND_FOUR_OF_A_KIND_ACES_WITH_2_TO_4 = 11;
    int HAND_STRAIGHT_FLUSH = 12;
    int HAND_ROYAL_FLUSH = 13;
    int RARE_HAND = this.HAND_FOUR_OF_A_KIND_ACES;
	PokerDoubleDoubleBonus() {
        this.name = "Dbl Dbl Bonus";
        this.hand_names = new String[] { "Nothing", "One Pair", "Two Pair", "3 of a Kind", "Straight", "Flush", "Full House", "4 5-K", "4 2-4", "4 Aces", "4 2-4 w/ A-4", "4 Aces w/ 2-4", "Straight Flush", "Royal Flush" };
        this.hand_names_caps = new String[] { "NOTHING", "JACKS OR BETTER", "TWO PAIR", "3 OF A KIND", "STRAIGHT", "FLUSH", "FULL HOUSE", "4 5-K", "4 2-4", "4 ACES", "4 2-4 W/ A-4", "4 Aces W/ 2-4", "STRAIGHT FLUSH", "ROYAL FLUSH" };
        this.hand_names_periods = new String[] { "", "", "...............", "............", "...............", ".....................", "..........", "........................", "........................", "....................", ".............", "..........", "..", "........" };
        payouts = new int[][] { { 0, 1, 1, 3, 4, 6, 9, 50, 80, 160, 160, 400, 50, 250},
        					{ 0, 2, 2, 6, 8, 12, 180, 100, 160, 320, 320, 800, 100, 500},
        					{ 0, 3, 3, 9, 12, 18, 27, 150, 240, 480, 480, 1200, 150, 750},
        					{ 0, 4, 4, 12, 16, 24, 36, 200, 320, 640, 640, 1600, 200, 1000},
        					{ 0, 5, 5, 15, 20, 30, 45, 250, 400, 800, 800, 2000, 250, 4000}};
    }
    public boolean is_jackpot( int hand_eval ) {
    	return hand_eval == HAND_ROYAL_FLUSH;
    }
    public boolean can_double_down( int hand_eval ) {
        return hand_eval > this.HAND_NOTHING && hand_eval <= this.HAND_FULL_HOUSE;
    }
    int get_four_of_a_kind(HashMap<Integer, Integer> rank_count) {
        if( rank_count.get(14) == 4 ) {
            if( rank_count.get(2) == 1 || rank_count.get(3) == 1 || rank_count.get(4) == 1 ) {
                return this.HAND_FOUR_OF_A_KIND_ACES_WITH_2_TO_4;
            }
            return this.HAND_FOUR_OF_A_KIND_ACES;
        }
        else if( rank_count.get(2) == 4 || rank_count.get(3) == 4 || rank_count.get(4) == 4 ) {
            if( rank_count.get(14) == 1 || rank_count.get(2) == 1 || rank_count.get(3) == 1 || rank_count.get(4) == 1 ) {
                return this.HAND_FOUR_OF_A_KIND_2_TO_4_WITH_A_TO_4;
            }
            return this.HAND_FOUR_OF_A_KIND_2_TO_4;
        }

        return this.HAND_FOUR_OF_A_KIND_5_TO_K;
    }

    Detail get_four_of_a_kind_detail( String[] hand, HashMap<Integer, Integer> rank_count ) {
        String fifth_card = null;
        int fifth_card_index = 0;
        int rank = -1;
        int[] positions = {0,0,0,0,0};
        //for( var r in rank_count ) {
        for( Map.Entry<Integer, Integer> entry : rank_count.entrySet()) {
            Integer key = entry.getKey();
            Integer value = entry.getValue();  
            //if( rank_count[r] == 4 ) {
            if( value == 4 ) {
                //rank = r[0];
            	rank = key;
                for( int i = 0; i < hand.length; i++ ) {
                    //if( hand[i][0] == rank ) {
                	if( get_card_rank_number(hand[i]) == rank ) {
                        positions[i] = 1;
                    } else {
                        fifth_card = hand[i];
                        fifth_card_index = i;
                    }
                }
                int v = this.get_card_rank_number(fifth_card);
                
                // TB TODO - This makes no sense... Why isn't it just using rank directly?
                //var r = this.get_card_rank_number(rank);
                //if( r == 14 ) { //four aces
                if( rank == 14 ) {
                    if( v >= 2 && v <= 4 ) { // hold 2-4 but throw away 5-K (fifth card will never be an ace..)
                        positions[fifth_card_index] = 1;
                    }
                //} else if ( r >= 2 && r <= 4 ) { // four 2s, 3s, or 4s
                }
                else if( rank >= 2 && rank <= 4 ) {
                    if( v == 14 || ( v >= 2 && v <= 4 ) ) { // fifth card is A-4
                        positions[fifth_card_index] = 1;
                    }
                } 
                else { 
                    //same as four aces
                    if( v >= 2 && v <= 4 ) { // hold 2-4 but throw away 5-K (fifth card will never be an ace..)
                        positions[fifth_card_index] = 1;
                    }
                }
            }
        }
        //return [rank, fifth_card, positions]
        Detail detail = new Detail( rank != -1 );
        detail.positions = positions;
        detail.rank = rank;
        return detail;
    }

}

class PokerDeucesWild extends Poker {
    int HAND_NOTHING = 0;
    int HAND_THREE_OF_A_KIND = 1;
    int HAND_STRAIGHT = 2;
    int HAND_FLUSH = 3;
    int HAND_FULL_HOUSE = 4;
    int HAND_FOUR_OF_A_KIND = 5;
    int HAND_STRAIGHT_FLUSH = 6;
    int HAND_FIVE_OF_A_KIND = 7;
    int HAND_WILD_ROYAL_FLUSH = 8;
    int HAND_FOUR_DEUCES = 9;
    int HAND_NATURAL_ROYAL_FLUSH = 10;
    int high_card_cutoff = 't';
    int RARE_HAND = this.HAND_FIVE_OF_A_KIND;
	PokerDeucesWild() {
        name = "Deuces Wild";
        hand_names = new String[] { "Nothing", "3 of a Kind", "Straight", "Flush", "Full House", "4 of a Kind", "Straight Flush", "5 of a Kind", "Wild R Flush", "4 Deuces", "Natural R Flush" };
        hand_names_caps = new String[] { "NOTHING", "3 OF A KIND", "STRAIGHT", "FLUSH", "FULL HOUSE", "4 OF A KIND", "STRAIGHT FLUSH", "5 OF A KIND", "WILD R FLUSH", "4 DEUCES", "NATURAL R FLUSH" };
        hand_names_periods = new String[] { "", ".............", "................", "......................", "...........", ".............", "...", ".............", "........", "................", "." };
        payouts = new int[][] { { 0, 1, 2, 3, 4, 4, 10, 15, 25, 200, 250},
        					{ 0, 2, 4, 6, 8, 8, 20, 30, 50, 400, 500},
        					{ 0, 3, 6, 9, 12, 12, 30, 45, 75, 600, 750},
        					{ 0, 4, 8, 12, 16, 16, 40, 60, 100, 800, 1000},
        					{ 0, 5, 10, 15, 20, 20, 50, 75, 125, 1000, 4000}};
    }
    public boolean is_jackpot( int hand_eval ) {
    	return hand_eval == HAND_NATURAL_ROYAL_FLUSH;
    }
    public boolean can_double_down(int hand_eval) {
        return hand_eval > this.HAND_NOTHING && hand_eval <= this.HAND_WILD_ROYAL_FLUSH;
    }
    public boolean is_hand_ok_to_hold_all_cards( int hand_eval ) {
    	return hand_eval >= HAND_STRAIGHT;
    }
    RankInfo get_rank_info( final String[] hand) {
        HashMap<Integer, Integer> rank_count = new HashMap<Integer, Integer>();
        //Integer ranks[] = new Integer[5];
        List<Integer> ranksList = new ArrayList<Integer>();
        HashMap<Character, Integer> suit_count = new HashMap<Character, Integer>();
        HashMap<String, Integer> card_to_hand_index = new HashMap<String, Integer>();
        int jokers = 0;
        for( int ci = 0; ci < hand.length; ci++ ) {
            String card = hand[ci];

            card_to_hand_index.put(card, ci);
            
            int value = this.get_card_rank_number(card);
            if( value == 2 ) {
                jokers += 1;
                continue;
            }

            int rank = get_card_rank_number(card);
            char suit = get_card_suit(card);
            // if(card[0] in rank_count) {
            if( rank_count.containsKey( rank) ) {
                //rank_count[card[0]] = rank_count[card[0]] + 1;
            	rank_count.put( rank, rank_count.get(rank) + 1 );
            } 
            else {
                //rank_count[card[0]] = 1;
            	rank_count.put( rank, 1 );
            }

            //if(card[1] in suit_count) {
            if( suit_count.containsKey( suit )) {
                //suit_count[card[1]] = suit_count[card[1]] + 1
            	suit_count.put( suit, suit_count.get(suit) + 1 );
            } 
            else {
                //suit_count[card[1]] = 1;
            	suit_count.put( suit, 1 );
            }

            //ranks.push( value );
            //ranks[ci] = get_card_rank_number(card);
            ranksList.add( get_card_rank_number(card) );
        }
        Comparator<Integer> compyDesc = new Comparator<Integer>() {
        	public int compare(Integer x, Integer y) {
        		return y-x;
        	}
        };
        Comparator<Integer> compyAsc = new Comparator<Integer>() {
        	public int compare(Integer x, Integer y) {
        		return x-y;
        	}
        };

        // TB - Need to parseInt the values because javascript is treating the values as strings,
        // even though ranks only contains integers...
        //ranks.sort( function(a,b){ return parseInt(a)-parseInt(b); })
        Integer[] ranks = new Integer[ ranksList.size() ];
        for( int i = 0; i < ranksList.size(); i++ ) {
        	ranks[i] = ranksList.get(i);
        }
        Arrays.sort( ranks, compyAsc );
        

        /*
        // sort reversed
        var rank_count_sorted = new Array();
        for( var r in rank_count ) {
            rank_count_sorted.push(rank_count[r]);
        }
        rank_count_sorted.sort( function(a,b){ return parseInt(b)-parseInt(a); })

        // sort reversed
        var suit_count_sorted = new Array();
        for( var r in suit_count ) {
            suit_count_sorted.push(suit_count[r]);
        }
        suit_count_sorted.sort( function(a,b){ return parseInt(b)-parseInt(a); })
        */
        // sort reversed
        Integer[] rank_count_sorted = new Integer[rank_count.size()];
        //for( var r: rank_count ) {
        int idx = 0;
        for( Map.Entry<Integer, Integer> entry : rank_count.entrySet()) {
            Integer key = entry.getKey();
            Integer value = entry.getValue();  
            //rank_count_sorted.push(rank_count[r]);
            rank_count_sorted[idx] = value;
            idx++;
        }
        //rank_count_sorted.sort( function(a,b){ return parseInt(b)-parseInt(a); })
        Arrays.sort( rank_count_sorted, compyDesc );

        // sort reversed
        Integer[] suit_count_sorted = new Integer[suit_count.size()];
        //for( int r: suit_count ) {
        idx = 0;
        for( Map.Entry<Character, Integer> entry : suit_count.entrySet()) {
            Character key = entry.getKey();
            Integer value = entry.getValue();  
            //suit_count_sorted.push(suit_count[r]);
            suit_count_sorted[idx] = value;
            idx++; 
        }
        //suit_count_sorted.sort( function(a,b){ return parseInt(b)-parseInt(a); })
        Arrays.sort( suit_count_sorted, compyDesc );
        
        // This is retarded
        int ranks_int[] = new int[ ranks.length ];
        for( int i = 0; i < ranks.length; i++ ) {
        	ranks_int[i] = ranks[i];
        }
        int rank_count_sorted_int[] = new int[rank_count.size()];
        for( int i = 0; i < rank_count.size(); i++ ) {
        	rank_count_sorted_int[i] = rank_count_sorted[i];
        }
        int suit_count_sorted_int[] = new int[suit_count.size()];
        for( int i = 0; i < suit_count.size(); i++ ) {
        	suit_count_sorted_int[i] = suit_count_sorted[i];
        }
 
        //return [ranks, rank_count, rank_count_sorted, suit_count, suit_count_sorted, card_to_hand_index, jokers];
        RankInfo rank_info = new RankInfo();
        rank_info.ranks = ranks_int;
        rank_info.rank_count = rank_count;
        rank_info.rank_count_sorted = rank_count_sorted_int;
        rank_info.suit_count = suit_count;
        rank_info.suit_count_sorted = suit_count_sorted_int;
        rank_info.card_to_hand_index = card_to_hand_index;
        rank_info.jokers = jokers;
        return rank_info;
    }

    boolean get_is_flush( String[] hand) {
        boolean is_flush = true;
        char required_suit = 'x';
        for( int i = 0; i < hand.length; i++ ) {
            // Skip jokers, effectively making them fill in the suit they should be
            //if( this.get_card_rank_number( hand[i][0] ) == 2 ) {
            if( this.get_card_rank_number( hand[i] ) == 2 ) {
                continue;
            }

            if( required_suit == 'x' ) {
                required_suit = get_card_suit(hand[i]);
                continue;
            }

            if( get_card_suit(hand[i]) != required_suit ) {
                is_flush = false;
                break;
            }
        }
        return is_flush;
    }

    GetIsStraightResult get_is_straight( int[] ranks, int[] rank_count_sorted, int jokers) {
        boolean is_straight = false;
        int required_rank = -1;
        int required_jokers = 0;
        int i = 0;
        
        //console.log("Ranks: " + ranks);
        //console.log("jokers: " + jokers);
        if( rank_count_sorted[0] == 1 ) {
            while( i < ranks.length ) {

                if( required_rank == -1 ) {
                    if( ranks[ranks.length-1] == 14 && ranks[ranks.length-2] <= 5 ) {
                        // Convert ace into a 1 and insert in front of list
                        required_rank = 2;
                        // CM weird that [1]+ranks.slice was causing the list to get all fucked up.
                        // Using unshift seems to have fixed it
                        //ranks = ranks.slice(0, ranks.length-1); 
                        //ranks.unshift(1) 
                        //ranks = new int[] { 1, ranks[0], ranks[1], ranks[2], ranks[3], ranks[4] };
                        int[] new_ranks = new int[ ranks.length + 1];
                        new_ranks[0] = 1;
                        for( int foo = 0; foo < ranks.length; foo++ ) {
                        	new_ranks[foo+1] = ranks[foo];
                        }
                        ranks = new_ranks;
                        
                        //console.log("Fixed ranks: " + ranks);
                    }
                    else {
                        required_rank = ranks[0] + 1;
                    }
                    i += 1;
                    continue;
                }

                //console.log("Check: " + required_rank + "==" + ranks[i] );
                if( ranks[i] != required_rank ) {
                    required_jokers += 1;
                }
                else {
                    i += 1;
                }

                required_rank += 1;
            }

            //console.log("required_jokers: " + required_jokers);
            if( jokers >= required_jokers ) {
                is_straight = true;
            }
                
        }

        int ending_high_card = ranks[ranks.length-1] + jokers - required_jokers;
        //console.log("ending_high: " + ending_high_card);
        if( ending_high_card > 14 ) {
            ending_high_card = 14;
        }
        
        //return [is_straight, ending_high_card];
        GetIsStraightResult result = new GetIsStraightResult();
        result.is_straight = is_straight;
        result.ending_high_card = ending_high_card;
        return result;
    }

    public boolean is_joker_game() {
        return true;
    }
    public boolean is_hand_eval_worth_fireworks( int hand_eval ) {
        return hand_eval >= this.HAND_STRAIGHT_FLUSH;
    }
    private int evaluate_hand( String[] hand ) {
    	/*
        var result = this.get_rank_info(hand);
        var ranks = result[0];
        var rank_count = result[1];
        var rank_count_sorted = result[2];
        var jokers = result[6];
        
        var is_flush = this.get_is_flush(hand);
        var result = this.get_is_straight(ranks, rank_count_sorted, jokers);
        var is_straight = result[0];
        var ending_high_card = result[1];
        */
        RankInfo rank_info = this.get_rank_info(hand);
        int[] ranks = rank_info.ranks;
        HashMap<Integer, Integer> rank_count = rank_info.rank_count;
        int[] rank_count_sorted = rank_info.rank_count_sorted;
        int jokers = rank_info.jokers; 
        boolean is_flush = this.get_is_flush(hand);
        GetIsStraightResult is_straight_result = this.get_is_straight(ranks, rank_count_sorted, jokers);
        boolean is_straight = is_straight_result.is_straight;
        int ending_high_card = is_straight_result.ending_high_card;

        if( is_flush && is_straight ) {
            if( ending_high_card == 14 ) {
                if( jokers == 0 ) {
                    return this.HAND_NATURAL_ROYAL_FLUSH;
                }
            }
        }

        if( jokers == 4 ) {
            return this.HAND_FOUR_DEUCES;
        }

        if( is_flush && is_straight ) {
            if( ending_high_card == 14 ) {
                return this.HAND_WILD_ROYAL_FLUSH;
            }
        }

        if( rank_count_sorted[0] + jokers == 5 ) {
            return this.HAND_FIVE_OF_A_KIND;
        }

        if( is_flush && is_straight ) {
            return this.HAND_STRAIGHT_FLUSH;
        }

        if( rank_count_sorted[0] + jokers == 4 ) {
            return this.HAND_FOUR_OF_A_KIND;
        }

        int required_jokers = 3 - rank_count_sorted[0] + 2 - rank_count_sorted[1];
        if( jokers >= required_jokers ) {
            return this.HAND_FULL_HOUSE;
        }

        if( is_flush ) {
            return this.HAND_FLUSH;
        }

        if( is_straight ) {
            return this.HAND_STRAIGHT;
        }

        if( rank_count_sorted[0] + jokers == 3 && rank_count_sorted[1] == 1 && rank_count_sorted[2] == 1 ) {
            return this.HAND_THREE_OF_A_KIND;
        }

        return this.HAND_NOTHING;
    }

    public int[] recommend_hold( String[] hand) {
    	/*
        var that = this;
        var to_hold = [0,0,0,0,0];
        var all_hold = [1,1,1,1,1];
        var no_hold = [0,0,0,0,0];

        var result = this.get_rank_info(hand);
        var ranks = result[0];
        var rank_count = result[1];
        var rank_count_sorted = result[2];
        var suit_count = result[3];
        var suit_count_sorted = result[4];
        var card_to_hand_index = result[5];
        var jokers = result[6];
        */
        int[] to_hold = {0,0,0,0,0};
        int[] all_hold = {1,1,1,1,1};
        int[] no_hold = {0,0,0,0,0};

        RankInfo rank_info = this.get_rank_info(hand);
		int[] ranks = rank_info.ranks;
		HashMap<Integer, Integer> rank_count = rank_info.rank_count;
		int[] rank_count_sorted = rank_info.rank_count_sorted;
		HashMap<Character, Integer> suit_count = rank_info.suit_count;
		int[] suit_count_sorted = rank_info.suit_count_sorted;
		HashMap<String, Integer> card_to_hand_index = rank_info.card_to_hand_index;
		int jokers = rank_info.jokers;


        // 1. four deuces
        Detail fok_detail = this.get_four_of_a_kind_detail(hand, rank_count);
        if( fok_detail.is_good && fok_detail.rank == 2 ) return fok_detail.positions;
        
        boolean is_flush = this.get_is_flush(hand);
        //var is_straight = this.get_is_straight(ranks, rank_count_sorted, 0)[0];
        GetIsStraightResult is_straight_result = this.get_is_straight(ranks, rank_count_sorted, jokers);
        boolean is_straight = is_straight_result.is_straight;
        int ending_high_card = is_straight_result.ending_high_card;

        HighCardDetail hc_detail = this.get_high_cards_detail(hand);
        if( jokers == 3 ) {
            //three of the cards are 2s so if the other two are suited high cards, then we have a royal flush and hold all cards
            //for( var s in suit_count ) {
	        for( Map.Entry<Character, Integer> entry : suit_count.entrySet() ) {
	            Character key = entry.getKey();
	            Integer value = entry.getValue();  
                //if( hc_detail[1][s][0].length == 2 ) return all_hold;
	            if( hc_detail.bysuit.get(key).list.size() == 2 ) {
	            	return all_hold;
	            }
            }
            
            //CM TODO five of a kind (2 + 3 jokers) ?
            //CM TODO flush (other two cards are the same suit)
            //CM TODO straight (other two cards are within 5 ranks)

            //otherwise, just hold the 2s
            for( int i = 0; i < hand.length; i++ ) {
                if( this.get_card_rank_number(hand[i]) == 2 ) {
                    to_hold[i] = 1;
                }
            }
            return to_hold;
        } 
        
        PairDetail pair_details = this.get_pair_details(hand, rank_count);
        if( jokers == 2 ) {
            //if we have three suited high cards then we have a RF
            //for( var s in suit_count ) {
	        for( Map.Entry<Character, Integer> entry : suit_count.entrySet() ) {
	            Character key = entry.getKey();
	            Integer value = entry.getValue();  
                //if( hc_detail[1][s][0].length == 3 ) return all_hold;
	            if( hc_detail.bysuit.get(key).list.size() == 3 ) return all_hold;
            }

            // if we have three of the same rank, then we have five of a kind
            Detail thok_detail = this.get_three_of_a_kind_detail(hand, rank_count, rank_count_sorted);
            if( thok_detail.is_good ) return all_hold; 

            //if we have any pairs other than the 2s hold the four of a kind
            //if( pair_details[0] > 1 ) {
            if( pair_details.list.size() > 1 ) {
                //for( var i = 0; i < pair_details[0]; i++ ) {
                for( int i = 0; i < pair_details.list.size(); i++ ) {
                    //if( this.get_card_rank_number(pair_details[1][i][0]) != 2 ) {
                    if( pair_details.list.get(i).rank != 2 ) {
                        // hold the jokers
                        //for( var j = 0; j < hand.length; j++ ) {
                        for( int j = 0; j < hand.length; j++ ) {
                            if( this.get_card_rank_number(hand[j]) == 2 ) {
                                //pair_details[1][i][2][j] = 1;
                            	pair_details.list.get(i).positions[j] = 1;
                            }
                        }
                        //return pair_details[1][i][2];
                        return pair_details.list.get(i).positions;
                    }
                }
            }
            
            // TB TODO - Can not port this function, because it's accessing an undefined variable i...
            // TB TODO - Fix this in the javascript code, too
            /*
            //if we have two suited high cards plus 2 jokers, then we're 4 to a RF
            //for( var s in suit_count ) {
	        for( Map.Entry<Character, Integer> entry : suit_count.entrySet() ) {
	            Character key = entry.getKey();
	            Integer value = entry.getValue();  
                //if( hc_detail[1][s][0].length == 2 ) {
	            if( hc_detail.bysuit.get(key).list.size() == 2 ) {
                    // hold the jokers
                    for( int j = 0; j < hand.length; j++ ) {
                        if( this.get_card_rank_number(hand[j]) == 2 ) {
                            //pair_details[1][i][2][j] = 1;
                        	pair_details.list.get(i).positions[j] = 1;
                        }
                    }
                    return pair_details[1][i][2];
                }
            }
            */

            //CM TODO - 4 to a straight flush with 2 consecutive singletons,6-7 or higher

            //hold the jokers
            for( int i = 0; i < hand.length; i++ ) {
                if( this.get_card_rank_number(hand[i]) == 2 ) {
                    to_hold[i] = 1;
                }
            }
            return to_hold;
        } 

        if( jokers == 1 ) {
            // if we have three of the same rank, then we have four of a kind
            Detail thok_detail = this.get_three_of_a_kind_detail(hand, rank_count, rank_count_sorted);
            if( thok_detail.is_good ) {
                for( int i = 0; i < hand.length; i++ ) {
                    if( this.get_card_rank_number(hand[i]) == 2 ) {
                        //only 1 joker, return the 3 of a kind + joker
                        //thok_detail[2][i] = 1;
                    	thok_detail.positions[i] = 1;
                        //return thok_detail[2];
                    	return thok_detail.positions;
                    }
                }
            }

            //rf
            Detail rf_detail = this.get_n_to_a_royal_flush_detail(hand, 4, ranks, suit_count, suit_count_sorted);
            if(rf_detail.is_good) return all_hold;

            //4 to a royal flush (3+joker)
            rf_detail = this.get_n_to_a_royal_flush_detail(hand, 3, ranks, suit_count, suit_count_sorted);
            if(rf_detail.is_good) {
                for( int i = 0; i < hand.length; i++ ) {
                    if( this.get_card_rank_number(hand[i]) == 2 ) {
                        //rf_detail[2][i] = 1;
                    	rf_detail.positions[i] = 1;
                        //return rf_detail[2];
                    	return rf_detail.positions;
                    }
                }
            }

            //Full house -- only way we can have full house is if there are two other pairs (which can't be 2s, otherwise we'd have more than 1 joker)
            //if( pair_details[0] == 2 ) return all_hold;
            if( pair_details.list.size() == 2 ) return all_hold;

            //CM TODO - 4 to a straight flush with 3 consecutive singletons,5-7 or higher

            //3 of a kind
            //if( pair_details[0] == 1 ) {
            if( pair_details.list.size() == 2 ) {
                for( int i = 0; i < hand.length; i++ ) {
                    if( this.get_card_rank_number(hand[i]) == 2 ) {
                        //pair_details[1][0][2][i] = 1;
                    	pair_details.list.get(0).positions[i] = 1;
                    	
                        //return pair_details[1][0][2];
                    	return pair_details.positions;
                    }
                }
            }

            //straight (straight flushes will be caught here, regardless)
            Detail ftos_detail = this.get_n_to_a_straight_detail(hand, 4, rank_count_sorted, card_to_hand_index, true);
            if( ftos_detail.is_good ) return all_hold;

            //flush (2s are not counted in the suits)
            Detail ftof_detail = this.get_n_to_a_flush_detail(hand, 4, suit_count);
            if( ftof_detail.is_good ) return all_hold;

            //3 to a royal flush
            //if we have two suited high cards plus 1 joker, then we're 3 to a RF
            //for( var s in suit_count ) {
	        for( Map.Entry<Character, Integer> entry : suit_count.entrySet() ) {
	            Character key = entry.getKey();
	            Integer value = entry.getValue();  
                //if( hc_detail[1][s][0].length == 2 ) {
	            if( hc_detail.bysuit.get(key).list.size() == 2 ) { 
                    // hold the joker
                    for( int j = 0; j < hand.length; j++ ) {
                        if( this.get_card_rank_number(hand[j]) == 2 ) {
                            //hc_detail[1][s][1][j] = 1;
                        	hc_detail.bysuit.get(key).pos[j] = 1;
                            //return hc_detail[1][s][1];
                        	return hc_detail.bysuit.get(key).pos;
                        }
                    }
                }
            }

            //CM TODO - 3 to a straight flush with 2 consecutive singletons,6-7 or higher

            //joker + 1 high card
            //if( hc_detail[0].length >= 1 ) {
	        if( hc_detail.cards.size() >= 1 ) {
                //take the highest of the high cards
                int high_rank = 0;
                int last_i = -1;
                int[] res = {0,0,0,0,0};
                for( int i = 0; i < hand.length; i++ ) {
                    if( this.get_card_rank_number(hand[i]) == 2 ) {
                        //hc_detail[2][i] = 1;
                        //return hc_detail[2];
                        res[i] = 1;
                    } else if( this.get_card_rank_number(hand[i]) > high_rank ) {
                        //if(last_i != null) res[last_i] = 0;
                        if(last_i != -1) res[last_i] = 0;
                        res[i] = 1;
                        last_i = i;
                        high_rank = this.get_card_rank_number(hand[i]);
                    }
                }
                return res;
            }

            //deuce only
            for( int i = 0; i < hand.length; i++ ) {
                if( this.get_card_rank_number(hand[i]) == 2 ) {
                    to_hold[i] = i;
                    return to_hold;
                }
            }
        } 
        
        // no jokers  
        {
            if( is_flush && is_straight ) return all_hold; //captures royal flush

            // 4 to a royal flush (use >= to capture would-be flushes)
            // If the last 4 items in ranks (since its sorted) are all >= 10
            Detail rf_detail = this.get_n_to_a_royal_flush_detail(hand, 4, ranks, suit_count, suit_count_sorted);
            if(rf_detail.is_good ) return rf_detail.positions;

            // anything between three of a kind and four of a kind
            {
                // Four of a kind
                if( fok_detail.is_good ) return fok_detail.positions;

                // Full House
                Detail fh_detail = this.get_full_house_detail(hand, rank_count, rank_count_sorted);
                if( fh_detail.is_good ) return all_hold;

                // flush and straight
                if( is_flush || is_straight ) return all_hold;

                // Three of a kind
                Detail tok_detail = this.get_three_of_a_kind_detail(hand, rank_count, rank_count_sorted);
                if(tok_detail.is_good) return tok_detail.positions;
            }

            // 4 to a straight flush
            Detail ftosf = this.get_n_to_a_straight_flush_detail(hand, 4, suit_count, card_to_hand_index);
            if(ftosf.is_good) return ftosf.positions;

            // 3 to a royal flush
            rf_detail = this.get_n_to_a_royal_flush_detail(hand, 3, ranks, suit_count, suit_count_sorted);
            if(rf_detail.is_good) return rf_detail.positions;

            // 4 to a flush
            Detail ftof = this.get_n_to_a_flush_detail(hand, 4, suit_count);
            if(ftof.is_good) return ftof.positions;

            // 4 to an outside straight
            Detail ftoos = this.get_n_to_an_outside_straight_detail(hand, 4, rank_count_sorted, card_to_hand_index);
            if(ftoos.is_good) return ftoos.positions;

            // 3 to a straight flush
            Detail thtosf = this.get_n_to_a_straight_flush_detail(hand, 3, suit_count, card_to_hand_index);
            if( thtosf.is_good) return thtosf.positions;

            // 4 to any straight
            Detail ftois = this.get_n_to_a_straight_detail(hand, 4, rank_count_sorted, card_to_hand_index, true);
            if( ftois.is_good) return ftois.positions;

            // a pair, any pair
            //if( pair_details[0] >= 1 ) return pair_details[1][0][2];
            if( pair_details.list.size() >= 1 ) return pair_details.list.get(0).positions;

            // 3 to an outside straight
            ftoos = this.get_n_to_an_outside_straight_detail(hand, 3, rank_count_sorted, card_to_hand_index);
            if(ftoos.is_good) return ftoos.positions;

            // CM 2 to a royal flush, JQ high
            //for( var s in hc_detail[1] ) {
	        for( Map.Entry<Character, BysuitData> entry : hc_detail.bysuit.entrySet()) {
	            Character key = entry.getKey();
	            BysuitData value = entry.getValue();  
                //var hc = hc_detail[1][s];
	            
                //if( hc[0].length == 2 ) {
	            if( value.list.size() == 2 ) {
	            	/*
                    if( ( this.get_card_rank_number(hc[0][0]) == 11 && this.get_card_rank_number(hc[0][1]) == 12 ) ||
                        ( this.get_card_rank_number(hc[0][1]) == 11 && this.get_card_rank_number(hc[0][0]) == 12 ) ) {
                        return hc[1];
                    }
                    */
	            	if( ( get_card_rank_number(value.list.get(0)) == 11 && get_card_rank_number(value.list.get(1)) == 12 ) ||
	            		( get_card_rank_number(value.list.get(1)) == 11 && get_card_rank_number(value.list.get(0)) == 12 )) {
	            		return value.pos;
	            	}
                }
            }

            // 2 suited high cards
            //for( var s in hc_detail[1] ) {
	        for( Map.Entry<Character, BysuitData> entry : hc_detail.bysuit.entrySet()) {
	            Character key = entry.getKey();
	            BysuitData value = entry.getValue();  
                //if( hc_detail[1][s][0].length >= 2 ) {
	            if( value.list.size() >= 2 ) {
                    //return hc_detail[1][s][1];
	            	return value.pos;
                }
            }

            // 3 to any straight
            ftois = this.get_n_to_a_straight_detail(hand, 3, rank_count_sorted, card_to_hand_index, true);
            if( ftois.is_good) return ftois.positions;
        }

        // discard everything
        return no_hold;
    }
}

//
// TB TODO - TESTS
//

/*
function run_poker_js_unit_tests( poker_games ) {
    // TB - Because the prizes come form the server, we need to call this after init_videopoker is called...
    console.log("Starting poker_eval.js unit tests!");
    
    var deuces = poker_games[5];
    deuces.assert_hand( ['kc', 'ac', 'qc', 'jc', 'tc'], deuces.HAND_NATURAL_ROYAL_FLUSH, 250 )
    deuces.assert_hand( ['ks', 'as', 'qs', 'js', 'ts'], deuces.HAND_NATURAL_ROYAL_FLUSH, 250 )
    
    deuces.assert_hand( ['2c', '2s', '9c', '2h', '2d'], deuces.HAND_FOUR_DEUCES, 200 )
    deuces.assert_hand( ['2c', '2d', 'qc', '2h', '2s'], deuces.HAND_FOUR_DEUCES, 200 )
    deuces.assert_hand( ['2c', '2d', '2s', '2h', 'qd'], deuces.HAND_FOUR_DEUCES, 200 )
    deuces.assert_hand( ['2c', '2d', '2s', '2h', 'ad'], deuces.HAND_FOUR_DEUCES, 200 )
    
    deuces.assert_hand( ['kc', '2d', 'qc', 'jc', 'tc'], deuces.HAND_WILD_ROYAL_FLUSH, 25 )
    deuces.assert_hand( ['kc', '2d', 'qc', '2h', 'tc'], deuces.HAND_WILD_ROYAL_FLUSH, 25 )
    deuces.assert_hand( ['2c', '2d', 'qc', '2h', 'tc'], deuces.HAND_WILD_ROYAL_FLUSH, 25 )
    deuces.assert_hand( ['2c', '2d', 'as', '2h', 'qs'], deuces.HAND_WILD_ROYAL_FLUSH, 25 )
    deuces.assert_hand( ['2c', '2d', 'as', '2h', 'ks'], deuces.HAND_WILD_ROYAL_FLUSH, 25 )
    deuces.assert_hand( ['2c', '2d', 'as', '2h', 'ts'], deuces.HAND_WILD_ROYAL_FLUSH, 25 )
    deuces.assert_hand( ['2c', '2d', 'js', '2h', 'ts'], deuces.HAND_WILD_ROYAL_FLUSH, 25 )
    
    deuces.assert_hand( ['5c', '5s', '2s', '5h', '5d'], deuces.HAND_FIVE_OF_A_KIND, 15 )
    deuces.assert_hand( ['qc', 'qs', '2s', '2h', 'qd'], deuces.HAND_FIVE_OF_A_KIND, 15 )
    deuces.assert_hand( ['qc', 'qs', '2s', '2h', 'qd'], deuces.HAND_FIVE_OF_A_KIND, 15 )
    deuces.assert_hand( ['2c', 'qs', '2s', '2h', 'qd'], deuces.HAND_FIVE_OF_A_KIND, 15 )
    
    deuces.assert_hand( ['7c', '2s', '9c', '2h', '2d'], deuces.HAND_STRAIGHT_FLUSH, 10 )
    deuces.assert_hand( ['7c', '8c', '9c', 'jc', '2d'], deuces.HAND_STRAIGHT_FLUSH, 10 )
    deuces.assert_hand( ['7c', '8c', '9c', 'tc', '2d'], deuces.HAND_STRAIGHT_FLUSH, 10 )
    deuces.assert_hand( ['2d', '8c', '9c', 'tc', '7c'], deuces.HAND_STRAIGHT_FLUSH, 10 )
    deuces.assert_hand( ['as', '2s', '3s', '4s', '5s'], deuces.HAND_STRAIGHT_FLUSH, 10 )
    deuces.assert_hand( ['2s', '5s', '3s', 'as', '4s'], deuces.HAND_STRAIGHT_FLUSH, 10 )
    
    deuces.assert_hand( ['7c', '2s', '9d', '2h', '2d'], deuces.HAND_FOUR_OF_A_KIND, 4 )
    deuces.assert_hand( ['7c', '8s', '2c', '2h', '2d'], deuces.HAND_FOUR_OF_A_KIND, 4 )
    deuces.assert_hand( ['2c', '2d', 'as', '2h', 'kh'], deuces.HAND_FOUR_OF_A_KIND, 4 )
    deuces.assert_hand( ['5c', '5s', 'as', '5h', '5d'], deuces.HAND_FOUR_OF_A_KIND, 4 )
    deuces.assert_hand( ['2c', '2d', '3c', '3h', '5s'], deuces.HAND_FOUR_OF_A_KIND, 4 )
    deuces.assert_hand( ['8c', '8d', '2c', 'qh', '2s'], deuces.HAND_FOUR_OF_A_KIND, 4 )
    
    deuces.assert_hand( ['5c', '5d', '3c', '3h', '5s'], deuces.HAND_FULL_HOUSE, 4 )
    deuces.assert_hand( ['5c', '2d', '3c', '3h', '5s'], deuces.HAND_FULL_HOUSE, 4 )
    deuces.assert_hand( ['8c', '8d', '2c', 'qh', 'qs'], deuces.HAND_FULL_HOUSE, 4 )
    
    deuces.assert_hand( ['5c', '6c', 'tc', 'jc', 'qc'], deuces.HAND_FLUSH, 3 )
    deuces.assert_hand( ['5c', '6c', '2d', 'jc', 'qc'], deuces.HAND_FLUSH, 3 )
    deuces.assert_hand( ['5c', '2h', '2d', 'jc', 'qc'], deuces.HAND_FLUSH, 3 )
    
    deuces.assert_hand( ['5c', '6s', '7c', '8c', '9c'], deuces.HAND_STRAIGHT, 2 )
    deuces.assert_hand( ['5c', '6s', '2c', '8c', '9c'], deuces.HAND_STRAIGHT, 2 )
    deuces.assert_hand( ['5c', '6s', '2c', '8c', '2h'], deuces.HAND_STRAIGHT, 2 )
    deuces.assert_hand( ['5c', '4s', 'as', '2h', '3d'], deuces.HAND_STRAIGHT, 2 )
    deuces.assert_hand( ['7c', '4s', '5s', '3h', '6d'], deuces.HAND_STRAIGHT, 2 )
    deuces.assert_hand( ['9c', '8s', '2s', '5h', '6d'], deuces.HAND_STRAIGHT, 2 )
    deuces.assert_hand( ['7c', '8s', '2c', '5h', '6d'], deuces.HAND_STRAIGHT, 2 )
    deuces.assert_hand( ['7c', '2s', '9c', '2h', 'jd'], deuces.HAND_STRAIGHT, 2 )
    deuces.assert_hand( ['ac', '2s', '3c', '4h', '2d'], deuces.HAND_STRAIGHT, 2 )
    deuces.assert_hand( ['7c', '8s', '9c', '2h', '2d'], deuces.HAND_STRAIGHT, 2 )
    deuces.assert_hand( ['ks', 'ah', 'qs', 'js', 'ts'], deuces.HAND_STRAIGHT, 2 )
    deuces.assert_hand( ['ks', 'ah', '2s', 'js', 'ts'], deuces.HAND_STRAIGHT, 2 )
    
    deuces.assert_hand( ['7c', '2s', '9c', '2h', 'qd'], deuces.HAND_THREE_OF_A_KIND, 1 )
    deuces.assert_hand( ['7c', '2s', '9c', '2h', 'qd'], deuces.HAND_THREE_OF_A_KIND, 1 )
    deuces.assert_hand( ['7c', '7s', '9c', '2h', 'qd'], deuces.HAND_THREE_OF_A_KIND, 1 ) 
    deuces.assert_hand( ['7c', '7s', '9c', '7h', 'qd'], deuces.HAND_THREE_OF_A_KIND, 1 ) 
    
    deuces.assert_hand( ['7c', '8s', 'tc', '5h', '6d'], deuces.HAND_NOTHING, 0 )
    deuces.assert_hand( ['7c', '8s', '9c', '4h', '2d'], deuces.HAND_NOTHING, 0 )
    deuces.assert_hand( ['7c', '8s', '9c', 'th', '5d'], deuces.HAND_NOTHING, 0 )
    deuces.assert_hand( ['7c', '8s', '9c', 'th', 'qd'], deuces.HAND_NOTHING, 0 )
    deuces.assert_hand( ['7c', '2s', '9c', 'th', 'qd'], deuces.HAND_NOTHING, 0 )
    deuces.assert_hand( ['5c', '3h', '2d', 'jc', 'qc'], deuces.HAND_NOTHING, 0 )
    
    var bonus = poker_games[2];
    bonus.assert_hand( ['5c', '5s', 'as', '5h', '5d'], bonus.HAND_FOUR_OF_A_KIND_5_TO_K, 25 );
    bonus.assert_hand( ['qc', 'ks', 'kc', 'kh', 'kd'], bonus.HAND_FOUR_OF_A_KIND_5_TO_K, 25 );
    bonus.assert_hand( ['4c', '4s', 'as', '4h', '4d'], bonus.HAND_FOUR_OF_A_KIND_2_TO_4, 40 );
    bonus.assert_hand( ['2c', '2s', 'as', '2h', '2d'], bonus.HAND_FOUR_OF_A_KIND_2_TO_4, 40 );
    bonus.assert_hand( ['ac', 'as', 'qs', 'ah', 'ad'], bonus.HAND_FOUR_OF_A_KIND_ACES, 80 );
    
    var double_bonus = poker_games[3];
    double_bonus.assert_hand( ['5c', '5s', 'as', '5h', '5d'], double_bonus.HAND_FOUR_OF_A_KIND_5_TO_K, 50 )
    double_bonus.assert_hand( ['qc', 'ks', 'kc', 'kh', 'kd'], double_bonus.HAND_FOUR_OF_A_KIND_5_TO_K, 50 )
    double_bonus.assert_hand( ['4c', '4s', 'as', '4h', '4d'], double_bonus.HAND_FOUR_OF_A_KIND_2_TO_4, 80 )
    double_bonus.assert_hand( ['2c', '2s', 'as', '2h', '2d'], double_bonus.HAND_FOUR_OF_A_KIND_2_TO_4, 80 )
    double_bonus.assert_hand( ['ac', 'as', 'qs', 'ah', 'ad'], double_bonus.HAND_FOUR_OF_A_KIND_ACES, 160 )
    double_bonus.assert_hand( ['ac', 'as', '3s', '4h', '5d'], double_bonus.HAND_WINNING_PAIR, 1 )
    double_bonus.assert_hand( ['ac', 'as', '3s', '3h', '5d'], double_bonus.HAND_TWO_PAIR, 1 )
    double_bonus.assert_hand( ['ac', 'as', 'ah', '4h', '5d'], double_bonus.HAND_THREE_OF_A_KIND, 3 )
    
    var double_double_bonus = poker_games[4];
    double_double_bonus.assert_hand( ['5c', '5s', 'as', '5h', '5d'], double_double_bonus.HAND_FOUR_OF_A_KIND_5_TO_K, 50 )
    double_double_bonus.assert_hand( ['qc', 'ks', 'kc', 'kh', 'kd'], double_double_bonus.HAND_FOUR_OF_A_KIND_5_TO_K, 50 )
    double_double_bonus.assert_hand( ['4c', '4s', '5s', '4h', '4d'], double_double_bonus.HAND_FOUR_OF_A_KIND_2_TO_4, 80 )
    double_double_bonus.assert_hand( ['2c', '2s', '8s', '2h', '2d'], double_double_bonus.HAND_FOUR_OF_A_KIND_2_TO_4, 80 )
    double_double_bonus.assert_hand( ['ac', 'as', 'qs', 'ah', 'ad'], double_double_bonus.HAND_FOUR_OF_A_KIND_ACES, 160 )
    double_double_bonus.assert_hand( ['4c', '4s', 'as', '4h', '4d'], double_double_bonus.HAND_FOUR_OF_A_KIND_2_TO_4_WITH_A_TO_4, 160 )
    double_double_bonus.assert_hand( ['2c', '2s', '4s', '2h', '2d'], double_double_bonus.HAND_FOUR_OF_A_KIND_2_TO_4_WITH_A_TO_4, 160 )
    double_double_bonus.assert_hand( ['ac', 'as', '2s', 'ah', 'ad'], double_double_bonus.HAND_FOUR_OF_A_KIND_ACES_WITH_2_TO_4, 400 )
    double_double_bonus.assert_hand( ['ac', 'as', '4s', 'ah', 'ad'], double_double_bonus.HAND_FOUR_OF_A_KIND_ACES_WITH_2_TO_4, 400 )
    
    var tens_or_better = poker_games[1];
    tens_or_better.assert_hand( ['kc', '9s', 'ks', 'as', 'ts'], tens_or_better.HAND_WINNING_PAIR, 1 );
    tens_or_better.assert_hand( ['tc', '9s', 'ks', 'as', 'ts'], tens_or_better.HAND_WINNING_PAIR, 1 );
    tens_or_better.assert_hand( ['8c', '9s', 'ts', 'as', '8s'], tens_or_better.HAND_NOTHING, 0 );
    
    var jacks_or_better = poker_games[0];
    jacks_or_better.assert_hand( ['kh', 'ks', 'kc', '4c', 'kd'], jacks_or_better.HAND_FOUR_OF_A_KIND, 25 ) 
    
    jacks_or_better.assert_hand( ['kh', 'ks', 'kc', '4c', '4d'], jacks_or_better.HAND_FULL_HOUSE, 9 )
    jacks_or_better.assert_hand( ['4s', '9c', '4h', '4c', '4d'], jacks_or_better.HAND_FOUR_OF_A_KIND, 25 )
    jacks_or_better.assert_hand( ['4s', '5s', '6s', '7s', '8s'], jacks_or_better.HAND_STRAIGHT_FLUSH, 50 )
    jacks_or_better.assert_hand( ['as', '2s', '3s', '4s', '5s'], jacks_or_better.HAND_STRAIGHT_FLUSH, 50 )
    jacks_or_better.assert_hand( ['2s', '5s', '3s', 'as', '4s'], jacks_or_better.HAND_STRAIGHT_FLUSH, 50 )
    jacks_or_better.assert_hand( ['4s', '9s', 'ks', 'as', 'ts'], jacks_or_better.HAND_FLUSH, 6 )
    jacks_or_better.assert_hand( ['kc', '9s', 'ks', 'as', 'ts'], jacks_or_better.HAND_WINNING_PAIR, 1 )
    jacks_or_better.assert_hand( ['tc', '9s', 'ks', 'as', 'ts'], jacks_or_better.HAND_NOTHING, 0 )
    jacks_or_better.assert_hand( ['8c', '9s', '8s', 'as', 'ts'], jacks_or_better.HAND_NOTHING,0 )
    jacks_or_better.assert_hand( ['8c', '9s', '8s', '9d', 'ts'], jacks_or_better.HAND_TWO_PAIR,2 )
    jacks_or_better.assert_hand( ['8c', '8d', '8s', '9d', 'qs'], jacks_or_better.HAND_THREE_OF_A_KIND,3 )
    jacks_or_better.assert_hand( ['8c', '9d', 'js', 'qd', 'ts'], jacks_or_better.HAND_STRAIGHT,4 )
    jacks_or_better.assert_hand( ['kc', 'ad', 'js', 'qd', 'ts'], jacks_or_better.HAND_STRAIGHT,4 )
    jacks_or_better.assert_hand( ['2c', '4d', 'as', '3d', '5s'], jacks_or_better.HAND_STRAIGHT,4 )
    jacks_or_better.assert_hand( ['kc', 'ac', 'qc', 'jc', 'tc'], jacks_or_better.HAND_ROYAL_FLUSH, 250 )
    
    var bonus_deluxe = poker_games[6];

    bonus_deluxe.assert_hand( ['4s', '9c', '4h', '4c', '4d'], bonus_deluxe.HAND_FOUR_OF_A_KIND, 80 );
    bonus_deluxe.assert_hand( ['8c', '9s', '8s', '9d', 'ts'], bonus_deluxe.HAND_TWO_PAIR,1 );
    bonus_deluxe.assert_hand( ['tc', '8c', '8s', '9d', 'ts'], bonus_deluxe.HAND_TWO_PAIR,1 );
    bonus_deluxe.assert_hand( ['8c', '9s', '8s', 'as', 'ts'], bonus_deluxe.HAND_NOTHING,0 );
    bonus_deluxe.assert_hand( ['as', '2s', '3s', '4s', '5s'], bonus_deluxe.HAND_STRAIGHT_FLUSH, 50 );

    //console.log("what the fuck - " + (([1] + [3,4,5]) == ([13,4,5])));
}

if( false ) {
    $(document).ready(function() {
        Blackjack.test();
    });
} 
*/
