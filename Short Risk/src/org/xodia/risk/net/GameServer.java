package org.xodia.risk.net;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.xodia.risk.game.Country;
import org.xodia.risk.game.Team;
import org.xodia.risk.net.message.BattleUpdate;
import org.xodia.risk.net.message.ClientMessage;
import org.xodia.risk.net.message.ClientRequest;
import org.xodia.risk.net.message.ServerMessage;
import org.xodia.risk.net.message.ServerRequest;
import org.xodia.risk.util.Utility;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

public class GameServer 
{
	
	private Server server;
	private GameStatus currentStatus;
	private volatile long id = 1;
	
	private long firstTurnID = -1;
	private long currentTurnID = -1;
	
	private boolean isBattleOn;
	private boolean isTroop;
	private volatile int attNum;
	private volatile int defNum;
	private long attID;
	private long defID;
	private volatile int attDice;
	private volatile int defDice;
	private volatile boolean isAttRoll;
	private volatile int attDonateNum;
	private volatile int defDonateNum;
	private volatile int origAttDonateNum;
	private volatile int origDefDonateNum;
	private List<Long> attDonatorsList = new CopyOnWriteArrayList<Long>();
	private List<Long> defDonatorsList = new CopyOnWriteArrayList<Long>();
	private volatile int battlePlayerCount; // Needs everyone to be here (TODO exclude players who lost)
	
	private ConcurrentHashMap<Connection, Team> teams = new ConcurrentHashMap<Connection, Team>();
	private List<Connection> connections = new ArrayList<Connection>();
	
	public GameServer()
	{
		server = new Server(256000, 256000);
		Network.register(server.getKryo());
		try
		{
			server.bind(Network.TCP_PORT);
			server.start();
			server.addListener(new Listener()
			{
				@Override
				public void connected(Connection c) 
				{
					handleConnection(c);
				}
				
				@Override
				public void received(Connection c, Object o) 
				{
					handleReceived(c, o);
				}
				
				@Override
				public void disconnected(Connection c) 
				{
					handleDisconnection(c);
				}
				
				@Override
				public void idle(Connection c) 
				{
					// Do Nothing
					handleIdle(c);
				}
			});
		}catch(IOException e)
		{
			e.printStackTrace();
			// Create Error Message
		}
		
		currentStatus = GameStatus.LOBBY;
	}
	
	public void disconnect()
	{
		server.close();
	}
	
	private void handleConnection(Connection c)
	{
		synchronized(connections)
		{
			connections.add(c);
		}
		
		c.sendTCP(ServerRequest.UsernameRequest);
	}
	
	private void handleReceived(Connection c, Object o)
	{
		if(o instanceof ClientMessage.UsernameMessage)
		{
			ClientMessage.UsernameMessage message = (ClientMessage.UsernameMessage) o;
			Team team = new Team(id++, message.username);
			teams.put(c, team);
			// Send the new information to the client
			ServerMessage.NewTeamMessage newTeamMessage = new ServerMessage.NewTeamMessage();
			newTeamMessage.id = team.getID();
			newTeamMessage.username = team.getUserName();
			ServerMessage.AddTeamMessage addTeamMessage = new ServerMessage.AddTeamMessage();
			addTeamMessage.id = team.getID();
			addTeamMessage.username = team.getUserName();
			c.sendTCP(newTeamMessage);
			server.sendToAllTCP(addTeamMessage);
		}
		
		if(o instanceof ClientMessage.KickTeamMessage)
		{
			ClientMessage.KickTeamMessage kickTeamMessage = (ClientMessage.KickTeamMessage) o;
			getConnection(kickTeamMessage.id).close();
		}
		
		if(o instanceof ClientMessage.BuyShipMessage)
		{
			ClientMessage.BuyShipMessage buyShipMessage = (ClientMessage.BuyShipMessage) o;
			Team team = getTeam(buyShipMessage.id);
			// Check with server's information
			int numCanPurchase = team.getCurrentMoney() / 15;
			if(buyShipMessage.number <= numCanPurchase)
				numCanPurchase = buyShipMessage.number;
			
			int totalCostOfPurchase = numCanPurchase * 15;
			team.setCurrentShip(team.getCurrentShip() + numCanPurchase);
			team.setCurrentMoney(team.getCurrentMoney() - totalCostOfPurchase);
			sendUpdateTeam(team.getID());
			ServerMessage.PurchaseMessage purchaseMessage = new ServerMessage.PurchaseMessage();
			purchaseMessage.message = "You have purchased " + numCanPurchase + " ship(s)!";
			c.sendTCP(purchaseMessage);
		}
		
		if(o instanceof ClientMessage.BuyTroopMessage)
		{
			ClientMessage.BuyTroopMessage buyTroopMessage = (ClientMessage.BuyTroopMessage) o;
			Team team = getTeam(buyTroopMessage.id);
			int numCanPurchase = team.getCurrentMoney() / 7;
			if(buyTroopMessage.number <= numCanPurchase)
				numCanPurchase = buyTroopMessage.number;
			
			int totalCostOfPurchase = numCanPurchase * 7;
			team.setCurrentTroop(team.getCurrentTroop() + numCanPurchase);
			team.setCurrentMoney(team.getCurrentMoney() - totalCostOfPurchase);
			sendUpdateTeam(team.getID());
			ServerMessage.PurchaseMessage purchaseMessage = new ServerMessage.PurchaseMessage();
			purchaseMessage.message = "You have purchased " + numCanPurchase + " troop(s)!";
			c.sendTCP(purchaseMessage);
		}
		
		if(o instanceof ClientMessage.DeclareBattleMessage)
		{
			ClientMessage.DeclareBattleMessage declareBattleMessage = (ClientMessage.DeclareBattleMessage) o;

			ServerMessage.TeamBattleMessage teamBattle = new ServerMessage.TeamBattleMessage();
			teamBattle.attID = declareBattleMessage.attID;
			teamBattle.defID = declareBattleMessage.defID;
			teamBattle.isTroop = declareBattleMessage.isTroop;
			
			for(Connection con : connections)
			{
				Team t = teams.get(con);
				if(!t.isDefeated())
					con.sendTCP(teamBattle);
			}
			
			if(declareBattleMessage.isTroop)
			{
				attNum = getTeam(declareBattleMessage.attID).getCurrentTroop();
				defNum = getTeam(declareBattleMessage.defID).getCurrentTroop();
			}else
			{
				attNum = getTeam(declareBattleMessage.attID).getCurrentShip();
				defNum = getTeam(declareBattleMessage.defID).getCurrentShip();
			}
					
			attID = declareBattleMessage.attID;
			defID = declareBattleMessage.defID;
			isBattleOn = true;
			isAttRoll = true;
			isTroop = declareBattleMessage.isTroop;
		}
		
		if(o instanceof ClientMessage.ConfirmBattleMessage)
		{
			ClientMessage.ConfirmBattleMessage confirmMessage = (ClientMessage.ConfirmBattleMessage) o;
			
			if(confirmMessage.id != attID && confirmMessage.id != defID)
			{
				if(confirmMessage.type == ClientMessage.ConfirmBattleMessage.ATTACKER)
				{
					attDonateNum += confirmMessage.num;
					attDonatorsList.add(confirmMessage.id);
				}else if(confirmMessage.type == ClientMessage.ConfirmBattleMessage.DEFENDER)
				{
					defDonateNum += confirmMessage.num;
					defDonatorsList.add(confirmMessage.id);
				}
			}

			battlePlayerCount++;
			
			if(battlePlayerCount == (connections.size() - getTotalDefeatedPlayers()))
			{
				// Start Game
				origAttDonateNum = attDonateNum;
				origDefDonateNum = defDonateNum;
				
				ServerMessage.StartGameMessage startGameMessage = new ServerMessage.StartGameMessage();
				startGameMessage.update = createBattleUpdate();
				server.sendToAllTCP(startGameMessage);
			}
		}
		
		if(o instanceof ClientMessage.AttackerRollMessage)
		{
			attDice = rollDice();
			isAttRoll = false;
			ServerMessage.UpdateBattleMessage updateBattleMessage = new ServerMessage.UpdateBattleMessage();
			updateBattleMessage.update = createBattleUpdate();
			server.sendToAllTCP(updateBattleMessage);
		}
		
		if(o instanceof ClientMessage.DefenderRollMessage)
		{
			defDice = rollDice();
			isAttRoll = true;
			
			// View results
			if(defDice >= attDice)
			{
				if(attDonateNum > 0)
				{
					attDonateNum -= 1;
				}else
				{
					attNum -= 1;
				}
			}else if(attDice > defDice)
			{
				if(defDonateNum > 0)
				{
					defDonateNum -= 1;
				}else
				{
					defNum -= 1;
				}
			}
			
			ServerMessage.UpdateBattleMessage updateBattleMessage = new ServerMessage.UpdateBattleMessage();
			updateBattleMessage.update = createBattleUpdate();
			server.sendToAllTCP(updateBattleMessage);
			
			if(defNum == 0 || ((isTroop) ? attNum == 1 : attNum == 0))
			{
				ServerMessage.EndBattleMessage endBattleMessage = new ServerMessage.EndBattleMessage();
				endBattleMessage.update = createBattleUpdate();
				
				Team def = getTeam(defID);
				Team att = getTeam(attID);
				
				if(defNum == 0)
				{
					endBattleMessage.result = "The attacker has won!";
					
					if(isTroop)
						def.setCurrentTroop(0);
					else
						def.setCurrentShip(0);
					
					sendUpdateTeam(defID);
				}else if(((isTroop) ? attNum == 1 : attNum == 0))
				{
					endBattleMessage.result = "The defender has successfully defended!";
					
					if(isTroop)
						att.setCurrentTroop(1);
					else
						att.setCurrentShip(0);
					
					sendUpdateTeam(attID);
				}
				
				server.sendToAllTCP(endBattleMessage);
				
				clearBattle();
				
				// Check for a country who lost
				if(def.getCurrentTroop() == 0 && def.getCurrentShip() < 3)
				{					
					att.setCurrentGDP(att.getCurrentGDP() + def.getCurrentGDP());
					att.setCurrentMoney(att.getCurrentMoney() + def.getCurrentMoney());
					att.setCurrentShip(att.getCurrentShip() + def.getCurrentShip());
					att.setCurrentTroop(att.getCurrentTroop() + def.getCurrentTroop());
					
					def.setDefeated(true);
					def.setCurrentGDP(0);
					def.setCurrentMoney(0);
					def.setCurrentShip(0);
					def.setCurrentTroop(0);
					
					sendUpdateTeam(def.getID());
					sendUpdateTeam(att.getID());
					
					ServerMessage.TeamDefeatedMessage defeatedMessage = new ServerMessage.TeamDefeatedMessage();
					defeatedMessage.defeatMessage = att.getUserName() + " has captured " + def.getUserName() + "!";
					server.sendToAllTCP(defeatedMessage);
					
					if(getTotalDefeatedPlayers() == connections.size() - 1)
					{
						// That means the attacker won!
						ServerMessage.TeamVictoryMessage victoryMessage = new ServerMessage.TeamVictoryMessage();
						victoryMessage.victoryID = att.getID();
						server.sendToAllTCP(victoryMessage);
					}
				}
			}
		}
		
		if(o instanceof ClientMessage.EndBattleMessage)
		{
			Team att = getTeam(attID);
			Team def = getTeam(defID);
			
			int origAtt = (isTroop) ? att.getCurrentTroop() : att.getCurrentShip();
			int origDef = (isTroop) ? def.getCurrentTroop() : def.getCurrentShip();
			
			int attTotal = origAttDonateNum + origAtt;
			int attTotalLoss = attTotal - (attNum + attDonateNum);
			
			int defTotal = origDefDonateNum + origDef;
			int defTotalLoss = defTotal - (defNum + defDonateNum);
			
			// We take the amount of damage
			if(origAttDonateNum != 0)
			{
				int offset = origAttDonateNum - attDonateNum;
				int index = 0;
				while(offset != 0)
				{
					// take the first donator and subtract
					Team donator = getTeam(attDonatorsList.get(index));
					int donatorNum = 0;
					int difference = 0;
					if(isTroop)
					{
						donatorNum = donator.getCurrentTroop();
						difference = donatorNum - offset;
						if(difference < 0)
						{
							offset = -difference;
						}else
						{
							offset = 0;
						}
						
						donator.setCurrentTroop(0);
						
						sendUpdateTeam(donator.getID());
					}else
					{
						donatorNum = donator.getCurrentShip();
						difference = donatorNum - offset;
						if(difference < 0)
						{
							offset = -difference;
						}else
						{
							offset = 0;
						}
						
						donator.setCurrentShip(0);
						
						sendUpdateTeam(donator.getID());
					}
					
					index++;
				}
			}
			
			// Now we find out if the attacker and defender lost units
			int attOffset = origAtt - attNum;
			if(attOffset != 0)
			{
				if(isTroop)
				{
					att.setCurrentTroop(attNum);
				}else
				{
					att.setCurrentShip(attNum);
				}
				sendUpdateTeam(att.getID());
			}
			
			int defOffset = origDef - defNum;
			if(defOffset != 0)
			{
				if(isTroop)
				{
					def.setCurrentTroop(defNum);
				}else
				{
					def.setCurrentShip(defNum);
				}
				sendUpdateTeam(def.getID());
			}
			
			ServerMessage.EndBattleMessage serverEndBattleMessage = new ServerMessage.EndBattleMessage();
			serverEndBattleMessage.update = createBattleUpdate();
			serverEndBattleMessage.result = att.getUserName() + " has lost " + attTotalLoss + " / " + attTotal + "in total.\n" +
											def.getUserName() + " has lost " + defTotalLoss + " / " + defTotal + "in total.";
			server.sendToAllTCP(serverEndBattleMessage);
			
			clearBattle();
		}
		
		if(o instanceof ClientMessage.EndTurnMessage)
		{
			determineTurn();
		}
		
		if(o instanceof ClientMessage.AllChatMessage)
		{
			ClientMessage.AllChatMessage allChatMessage = (ClientMessage.AllChatMessage) o;
			
			// Filter it
			String filteredMessage = Utility.censorString(allChatMessage.message);
			
			ServerMessage.AllChatMessage sAllChatMessage = new ServerMessage.AllChatMessage();
			sAllChatMessage.message = filteredMessage;
			sAllChatMessage.username = teams.get(c).getUserName();
			server.sendToAllTCP(sAllChatMessage);
		}
		
		if(o instanceof ClientMessage.WhisperChatMessage)
		{
			ClientMessage.WhisperChatMessage whisperChatMessage = (ClientMessage.WhisperChatMessage) o;
			
			String filteredMessage = Utility.censorString(whisperChatMessage.message);
			
			ServerMessage.WhisperChatMessage sWhisperChatMessage = new ServerMessage.WhisperChatMessage();
			sWhisperChatMessage.message = filteredMessage;
			sWhisperChatMessage.fromUsername = teams.get(c).getUserName();
			sWhisperChatMessage.toUsername = getTeam(whisperChatMessage.toID).getUserName();
			sWhisperChatMessage.toID = whisperChatMessage.toID;
			sWhisperChatMessage.fromID = teams.get(c).getID();
			
			getConnection(whisperChatMessage.toID).sendTCP(sWhisperChatMessage);
			c.sendTCP(sWhisperChatMessage);
		}
		
		if(o instanceof ClientRequest)
		{
			handleClientRequest(c, (ClientRequest) o);
		}
	}
	
	private void handleClientRequest(Connection c, ClientRequest request)
	{
		switch(request)
		{
		case PlayersInServerRequest:
			ServerMessage.ExistingPlayersMessage existingMessage = new ServerMessage.ExistingPlayersMessage();
			existingMessage.ids = new long[teams.size()];
			existingMessage.usernames = new String[teams.size()];
			int i = 0;
			for(Team t : teams.values())
			{
				existingMessage.ids[i] = t.getID();
				existingMessage.usernames[i] = t.getUserName();
				i++;
			}
			c.sendTCP(existingMessage);
			
			break;
		case StartGame:
			
			Country[] countries = Country.values();
			// Sort the countries
			Utility.shuffleArray(countries);
			
			// Create the Teams
			for(int j = 0; j < connections.size(); j++)
			{
				Team team = teams.get(connections.get(j));
				Country country = countries[j];
				team.setCountry(country);
				sendUpdateTeam(team.getID());
			}
			
			server.sendToAllTCP(ServerRequest.StartGameRequest);
			currentStatus = GameStatus.INGAME;
			// Load Who Goes First
			determineFirst();
			
			break;
		}
	}
	
	private void handleDisconnection(Connection c)
	{
		if(currentStatus == GameStatus.LOBBY)
		{
			Team t = teams.remove(c);
			connections.remove(c);
			ServerMessage.RemoveTeamMessage remove = new ServerMessage.RemoveTeamMessage();
			remove.id = t.getID();
			remove.username = t.getUserName();
			server.sendToAllTCP(remove);
			System.out.println("Server DEBUG: " + t.getUserName() + " has left the server!");
		}else if(currentStatus == GameStatus.INGAME)
		{
			// Save the game & exit
			// Unless the country is taken
			Team t = teams.remove(c);
			connections.remove(c);

			System.out.println("Server DEBUG: " + t.getUserName() + " has left the server!");
		}
	}
	
	private void handleIdle(Connection c)
	{
		
	}
	
	private int rollDice()
	{
		Random random = new Random();
		return random.nextInt(6) + 1;
	}
	
	private void determineFirst()
	{
		Random r = new Random();
		int i = r.nextInt(teams.size());
		Connection c = connections.get(i);
		Team t = teams.get(c);
		currentTurnID = firstTurnID = t.getID();
		ServerMessage.TeamTurnMessage teamTurnMessage = new ServerMessage.TeamTurnMessage();
		teamTurnMessage.id = t.getID();
		server.sendToAllTCP(teamTurnMessage);
	}
	
	private void determineTurn()
	{
		int index = 0;
		for(int i = 0; i < connections.size(); i++)
		{
			Connection c = connections.get(i);
			Team t = teams.get(c);
			if(t.getID() == currentTurnID)
			{
				index = i;
				break;
			}
		}
		
		int nextTurn = index + 1;
		
		while(true)
		{
			if(nextTurn > connections.size() - 1)
			{
				nextTurn = 0;
			}
			
			if(teams.get(connections.get(nextTurn)).isDefeated())
			{
				nextTurn++;
				
				if(nextTurn > connections.size() - 1)
				{
					nextTurn = 0;
				}
			}
			
			if(!teams.get(connections.get(nextTurn)).isDefeated())
			{
				break;
			}
		}
		
		System.out.println("Next Turn: " + nextTurn);
		
		Team team = teams.get(connections.get(nextTurn));
		currentTurnID = team.getID();
		if(currentTurnID == firstTurnID)
		{
			// Give everyone their gold
			for(int i = 0; i < connections.size(); i++)
			{
				Team t = teams.get(connections.get(i));
				t.setCurrentMoney(t.getCurrentMoney() + t.getCurrentGDP());
				sendUpdateTeam(t.getID());
			}
		}
		ServerMessage.TeamTurnMessage teamTurnMessage = new ServerMessage.TeamTurnMessage();
		teamTurnMessage.id = currentTurnID;
		server.sendToAllTCP(teamTurnMessage);
	}
	
	private void sendUpdateTeam(long id)
	{
		Team team = teams.get(getConnection(id));
		ServerMessage.UpdateTeamMessage update = new ServerMessage.UpdateTeamMessage();
		update.id = team.getID();
		update.country = team.getCountry();
		update.currentMoney = team.getCurrentMoney();
		update.currentShip = team.getCurrentShip();
		update.currentTroop = team.getCurrentTroop();
		update.currentGDP = team.getCurrentGDP();
		update.isDefeated = team.isDefeated();
		server.sendToAllTCP(update);
	}
	
	private BattleUpdate createBattleUpdate()
	{
		BattleUpdate update = new BattleUpdate();
		update.attDice = attDice;
		update.attDonateNum = attDonateNum;
		update.attID = attID;
		update.attNum = attNum;
		update.defDice = defDice;
		update.defDonateNum = defDonateNum;
		update.defID = defID;
		update.defNum = defNum;
		update.isAttToRoll = isAttRoll;
		return update;
	}
	
	private void clearBattle()
	{
		attDice = 0;
		attDonateNum = 0;
		attID = 0;
		attNum = 0;
		defDice = 0;
		defDonateNum = 0;
		defID = 0;
		defNum = 0;
		isAttRoll = false;
		isBattleOn = false;
		isTroop = false;
		attDonatorsList.clear();
		defDonatorsList.clear();
		battlePlayerCount = 0;
	}
	
	private void resetGame()
	{
		
	}
	
	private int getTotalDefeatedPlayers()
	{
		int count = 0;
		for(int i = 0; i < connections.size(); i++)
		{
			Team t = teams.get(connections.get(i));
			if(t.isDefeated())
				count++;
		}
		return count;
	}
	
	private Connection getConnection(long id)
	{
		for(int i = 0; i < connections.size(); i++)
		{
			Connection connection = connections.get(i);
			Team t = teams.get(connection);
			if(t.getID() == id)
				return connection;
		}
		
		return null;
	}
	
	private Team getTeam(long id)
	{
		for(Team t : teams.values())
		{
			if(t.getID() == id)
				return t;
		}
		
		return null;
	}
	
}
