package com.bitcoinvideocasino.lib;

class JSONNotifyTransaction {
	public float amount;
	public long intamount;
	public String txid;
}

public class JSONBalanceResult extends JSONBaseResult {
	public int shutdown_time;
    public JSONNotifyTransaction notify_transaction;
    public long intbalance;
    public long fake_intbalance;
    public String sender_address;
    public boolean unconfirmed;
}
