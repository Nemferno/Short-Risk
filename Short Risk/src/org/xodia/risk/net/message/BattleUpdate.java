package org.xodia.risk.net.message;

public class BattleUpdate 
{

	public long attID;
	public int attNum;
	public int attDonateNum;
	
	public long defID;
	public int defNum;
	public int defDonateNum;
	
	public int attDice;
	public int defDice;
	
	public boolean isAttToRoll;
	
	public String toString()
	{
		return "Attacker (" + attID + ") with " + attNum + " + " + attDonateNum + "\n" +
			   "Defender (" + defID + ") with " + defNum + " + " + defDonateNum + "\n" +
			   "Die: " + attDice + " to " + defDice + "\n" +
			   "Is Attacker Roll: " + isAttToRoll;
	}
	
}
