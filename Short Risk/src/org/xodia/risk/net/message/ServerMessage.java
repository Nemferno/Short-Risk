package org.xodia.risk.net.message;

import org.xodia.risk.game.Country;


public class ServerMessage 
{

	/**
	 * 
	 * Sends a message that has details of the client's
	 * newly formed team (id, username, team)
	 * 
	 * @author 20161205
	 *
	 */
	public static class NewTeamMessage
	{
		public long id;
		public String username;
	}
	
	/**
	 * 
	 * Sends a message that contains all of the players
	 * still currently in the server
	 * 
	 * @author 20161205
	 *
	 */
	public static class ExistingPlayersMessage
	{
		public String[] usernames;
		public long[] ids;
	}
	
	/**
	 * 
	 * Sends a message to the clients to add that team
	 * 
	 * @author Jasper Bae
	 *
	 */
	public static class AddTeamMessage
	{
		public long id;
		public String username;
	}
	
	/**
	 * 
	 * Sends a message to the clients to remove that team
	 * 
	 * @author Jasper Bae
	 *
	 */
	public static class RemoveTeamMessage
	{
		public long id;
		public String username;
	}
	
	/**
	 * 
	 * Sends an updated version of a team to the clients
	 * 
	 * @author 20161205
	 *
	 */
	public static class UpdateTeamMessage
	{
		public Country country;
		public int currentTroop;
		public int currentShip;
		public int currentMoney;
		public int currentGDP;
		
		public boolean isDefeated;
		
		public long id;
	}
	
	/**
	 * 
	 * Sends an id of the player who can do stuff
	 * 
	 * @author Jasper Bae
	 *
	 */
	public static class TeamTurnMessage
	{
		public long id;
	}
	
	/**
	 * 
	 * Returns the server's result of the purchase
	 * 
	 * @author 20161205
	 *
	 */
	public static class PurchaseMessage
	{
		public String message;
	}
	
	/**
	 * 
	 * Sends a battle message to all the players in the server
	 * 
	 * @author 20161205
	 *
	 */
	public static class TeamBattleMessage
	{
		public long attID;
		public long defID;
		public boolean isTroop;
	}
	
	/**
	 * 
	 * Starts the battle
	 * 
	 * @author 20161205
	 *
	 */
	public static class StartGameMessage
	{
		public BattleUpdate update;
	}
	
	public static class UpdateBattleMessage
	{
		public BattleUpdate update;
	}
	
	public static class EndBattleMessage
	{
		public String result;
		public BattleUpdate update;
	}
	
	public static class TeamDefeatedMessage
	{
		public String defeatMessage;
	}
	
	public static class TeamVictoryMessage
	{
		public long victoryID;
	}
	
	public static class AllChatMessage
	{
		public String username;
		public String message;
	}
	
	public static class WhisperChatMessage
	{
		public String message;
		public String fromUsername;
		public String toUsername;
		public long fromID;
		public long toID;
	}
	
}
