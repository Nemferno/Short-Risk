package org.xodia.risk.net.message;

public class ClientMessage 
{

	/**
	 * 
	 * Returns the client's username
	 * 
	 * @author 20161205
	 *
	 */
	public static class UsernameMessage
	{
		public String username;
	}
	
	/**
	 * 
	 * The host sends the server a message to kick a team
	 * 
	 * @author Jasper Bae
	 *
	 */
	public static class KickTeamMessage
	{
		public long id;
	}
	
	/**
	 * 
	 * The client sends the server the message to create troops
	 * 
	 * @author 20161205
	 *
	 */
	public static class BuyTroopMessage
	{
		public int number;
		public long id;
	}
	
	/**
	 * 
	 * The client sends the server the message to create troops
	 * 
	 * @author 20161205
	 *
	 */
	public static class BuyShipMessage
	{
		public int number;
		public long id;
	}
	
	/**
	 * 
	 * The client sends the server it's ending his turn
	 * 
	 * @author Jasper Bae
	 *
	 */
	public static class EndTurnMessage
	{
		public long id;
	}
	
	/**
	 * 
	 * The client sends the server that it is declaring a battle
	 * 
	 * @author 20161205
	 *
	 */
	public static class DeclareBattleMessage
	{
		public long attID; // Attacker
		public long defID; // Defender
		public boolean isTroop;
	}
	
	public static class ConfirmBattleMessage
	{
		public static final int ABSTAIN = 0,
								DEFENDER = 1,
								ATTACKER = 2;
		
		public long id;
		public int type;
		public int num;
	}
	
	public static class AttackerRollMessage {}
	public static class DefenderRollMessage {}
	public static class EndBattleMessage {}
	
	public static class AllChatMessage
	{
		public String message;
	}
	
	public static class WhisperChatMessage
	{
		public String message;
		public long toID;
	}
	
}
