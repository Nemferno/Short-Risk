package org.xodia.risk.net;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.xodia.risk.Application;
import org.xodia.risk.GamePanel;
import org.xodia.risk.LobbyPanel;
import org.xodia.risk.game.Team;
import org.xodia.risk.net.message.BattleUpdate;
import org.xodia.risk.net.message.ClientMessage;
import org.xodia.risk.net.message.ClientRequest;
import org.xodia.risk.net.message.ServerMessage;
import org.xodia.risk.net.message.ServerRequest;
import org.xodia.risk.ui.BattleDialog;
import org.xodia.risk.ui.WaitingBattleDialog;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

public class GameClient 
{

	private Client client;
	private GameStatus currentStatus;
	
	private long id;
	private final int CLIENT_TIMEOUT = 5000;
	
	private List<Team> teams = new ArrayList<Team>();

	public GameClient()
	{
		client = new Client();
		client.addListener(new Listener(){
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
				handleIdle(c);
			}
		});
		client.setKeepAliveTCP(1000);
		Network.register(client.getKryo());
		client.start();
	}
	
	private void handleConnection(Connection c)
	{
		connectToServer();
		Application.toLobby();
		currentStatus = GameStatus.LOBBY;
	}
	
	private void handleReceived(Connection c, Object o)
	{
		if(o instanceof ServerRequest)
		{
			handleRequest(c, (ServerRequest) o);
		}
		
		if(o instanceof ServerMessage.AddTeamMessage)
		{
			ServerMessage.AddTeamMessage addTeamMessage = (ServerMessage.AddTeamMessage) o;
			final Team team = new Team(addTeamMessage.id, addTeamMessage.username);
			teams.add(team);
			
			// Add Team to Lobby
			if(currentStatus == GameStatus.LOBBY)
			{
				LobbyPanel lPanel = (LobbyPanel) Application.currentPanel;
				lPanel.addTeam(team);
			}
		}
		
		if(o instanceof ServerMessage.ExistingPlayersMessage)
		{
			ServerMessage.ExistingPlayersMessage existingMessage = (ServerMessage.ExistingPlayersMessage) o;
			for(int i = 0; i < existingMessage.ids.length; i++)
			{
				final Team t = new Team(existingMessage.ids[i], existingMessage.usernames[i]);
				teams.add(t);
				
				// Add team to Lobby
				if(currentStatus == GameStatus.LOBBY)
				{
					LobbyPanel lPanel = (LobbyPanel) Application.currentPanel;
					lPanel.addTeam(t);
				}
			}
		}
		
		if(o instanceof ServerMessage.NewTeamMessage)
		{
			ServerMessage.NewTeamMessage newTeamMessage = (ServerMessage.NewTeamMessage) o;
			id = newTeamMessage.id;
		}
		
		if(o instanceof ServerMessage.RemoveTeamMessage)
		{
			ServerMessage.RemoveTeamMessage removeTeamMessage = (ServerMessage.RemoveTeamMessage) o;
			final Team t = getTeam(removeTeamMessage.id);
			teams.remove(t);
			
			// Remove Team to Lobby
			if(currentStatus == GameStatus.LOBBY)
			{
				LobbyPanel lPanel = (LobbyPanel) Application.currentPanel;
				lPanel.removeTeam(t);
			}
		}
		
		if(o instanceof ServerMessage.UpdateTeamMessage)
		{
			ServerMessage.UpdateTeamMessage update = (ServerMessage.UpdateTeamMessage) o;
			Team team = getTeam(update.id);
			team.setCountry(update.country);
			team.setCurrentMoney(update.currentMoney);
			team.setCurrentShip(update.currentShip);
			team.setCurrentTroop(update.currentTroop);
			team.setCurrentGDP(update.currentGDP);
			team.setDefeated(update.isDefeated);
			
			if(currentStatus == GameStatus.INGAME)
			{
				GamePanel gPanel = (GamePanel) Application.currentPanel;
				gPanel.updateList();
			}
		}
		
		if(o instanceof ServerMessage.TeamTurnMessage)
		{
			ServerMessage.TeamTurnMessage teamTurnMessage = (ServerMessage.TeamTurnMessage) o;
			if(currentStatus == GameStatus.INGAME)
			{
				if(teamTurnMessage.id == id)
				{
					GamePanel gPanel = (GamePanel) Application.currentPanel;
					gPanel.myTurn();
					JOptionPane.showMessageDialog(Application.getApplication(), "It is your turn!");
				}else
				{
					Team t = getTeam(teamTurnMessage.id);
					JOptionPane.showMessageDialog(Application.getApplication(), "It is " + t.getUserName() + "'s turn!");
				}
			}
		}
		
		if(o instanceof ServerMessage.PurchaseMessage)
		{
			ServerMessage.PurchaseMessage purchaseMessage = (ServerMessage.PurchaseMessage) o;
			JOptionPane.showMessageDialog(Application.getApplication(), purchaseMessage.message);
		}
		
		if(o instanceof ServerMessage.TeamBattleMessage)
		{
			final ServerMessage.TeamBattleMessage teamBattleMessage = (ServerMessage.TeamBattleMessage) o;
			ClientMessage.ConfirmBattleMessage confirm = new ClientMessage.ConfirmBattleMessage();
			confirm.id = id;
			
			if(teamBattleMessage.attID == id || teamBattleMessage.defID == id)
			{
				
				if(teamBattleMessage.defID == id)
				{
					WaitingBattleDialog.create(teamBattleMessage.attID);
				}else
				{
					WaitingBattleDialog.create(teamBattleMessage.defID);
				}
			}else
			{
				// Gives Prompt
				String[] options = { "Abstain", "Defender", "Attacker" };
				String option = (String) JOptionPane.showInputDialog(Application.getApplication(), "Which side do you want to aide?", 
						"Battle", JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
				if(option != null)
				{
					if(option.equals(options[0]))
					{
						confirm.type = ClientMessage.ConfirmBattleMessage.ABSTAIN;
					}else if(option.equals(options[1]) || option.equals(options[2]))
					{
						if(option.equals(options[1]))
							confirm.type = ClientMessage.ConfirmBattleMessage.ATTACKER;
						else
							confirm.type = ClientMessage.ConfirmBattleMessage.DEFENDER;		
						
						Team own = getTeam();
						boolean notEnough = false;
						// Check if you have enough units to donate
						if(teamBattleMessage.isTroop)
						{
							if(own.getCurrentTroop() < 2)
								notEnough = true;
						}else
						{
							if(own.getCurrentShip() == 2)
								notEnough = true;
						}
						
						if(notEnough)
						{
							JOptionPane.showMessageDialog(Application.getApplication(), "You do not have enough units to donate!");
							
							confirm.type = ClientMessage.ConfirmBattleMessage.ABSTAIN;
						}else
						{
							try
							{
								Double amount = new Double(JOptionPane.showInputDialog(Application.getApplication(), "How many units do you want to donate?", (teamBattleMessage.isTroop ? own.getCurrentTroop() : own.getCurrentShip())));
								int intAmount = amount.intValue();
								if(teamBattleMessage.isTroop)
								{
									if(intAmount > own.getCurrentTroop())
										intAmount = own.getCurrentTroop();
									else if(intAmount < 2)
										intAmount = 2;
								}else
								{
									if(intAmount > own.getCurrentShip())
										intAmount = own.getCurrentShip();
									else if(intAmount < 1)
										intAmount = 1;
								}
								
								confirm.num = intAmount;
							}catch(NumberFormatException e)
							{
								e.printStackTrace();
								
								JOptionPane.showMessageDialog(Application.getApplication(), "You have typed in an incorrect amount! You have been abstained!");
								
								confirm.type = ClientMessage.ConfirmBattleMessage.ABSTAIN;
							}
						}
					}
				}else
				{
					confirm.type = ClientMessage.ConfirmBattleMessage.ABSTAIN;
				}
				
				WaitingBattleDialog.create(-1);
			}
			
			sendTCP(confirm);
		}
		
		if(o instanceof ServerMessage.StartGameMessage)
		{
			ServerMessage.StartGameMessage startGameMessage = (ServerMessage.StartGameMessage) o;
			BattleUpdate update = startGameMessage.update;
			WaitingBattleDialog.destroy();
			BattleDialog.create(update);
		}
		
		if(o instanceof ServerMessage.UpdateBattleMessage)
		{
			ServerMessage.UpdateBattleMessage updateBattleMessage = (ServerMessage.UpdateBattleMessage) o;
			BattleDialog.update(updateBattleMessage.update);
		}
		
		if(o instanceof ServerMessage.EndBattleMessage)
		{
			ServerMessage.EndBattleMessage endBattleMessage = (ServerMessage.EndBattleMessage) o;
			BattleDialog.destroy();
			
			JOptionPane.showMessageDialog(Application.getApplication(), endBattleMessage.result);
		}
		
		if(o instanceof ServerMessage.TeamDefeatedMessage)
		{
			ServerMessage.TeamDefeatedMessage defeatedMessage = (ServerMessage.TeamDefeatedMessage) o;
			JOptionPane.showMessageDialog(Application.getApplication(), defeatedMessage.defeatMessage);
		}
		
		if(o instanceof ServerMessage.TeamVictoryMessage)
		{
			ServerMessage.TeamVictoryMessage victoryMessage = (ServerMessage.TeamVictoryMessage) o;
			if(victoryMessage.victoryID == id)
			{
				JOptionPane.showMessageDialog(Application.getApplication(), "You won the game!");
			}else
			{
				JOptionPane.showMessageDialog(Application.getApplication(), "You lost the game!");
			}
			
			Application.toLobby();
		}
		
		if(o instanceof ServerMessage.AllChatMessage)
		{
			ServerMessage.AllChatMessage allChatMessage = (ServerMessage.AllChatMessage) o;
			
			if(currentStatus == GameStatus.LOBBY)
			{
				LobbyPanel lPanel = (LobbyPanel) Application.currentPanel;
				lPanel.updateChat(allChatMessage.username + ": " + allChatMessage.message);
			}else if(currentStatus == GameStatus.INGAME)
			{
				GamePanel gPanel = (GamePanel) Application.currentPanel;
				gPanel.updateChat(allChatMessage.username + ": " + allChatMessage.message);
			}
		}
		
		if(o instanceof ServerMessage.WhisperChatMessage)
		{
			ServerMessage.WhisperChatMessage whisperChatMessage = (ServerMessage.WhisperChatMessage) o;
			if(currentStatus == GameStatus.LOBBY)
			{
				LobbyPanel lPanel = (LobbyPanel) Application.currentPanel;
				String message = null;
				if(whisperChatMessage.toID == getID())
					message = "From " + whisperChatMessage.fromUsername + ": " + whisperChatMessage.message;
				else
					message = "To " + whisperChatMessage.toUsername + ": " + whisperChatMessage.message;
				
				lPanel.updateChat(message);
			}else if(currentStatus == GameStatus.INGAME)
			{
				GamePanel gPanel = (GamePanel) Application.currentPanel;
				String message = null;
				if(whisperChatMessage.toID == getID())
					message = "From " + whisperChatMessage.fromUsername + ": " + whisperChatMessage.message;
				else
					message = "To " + whisperChatMessage.toUsername + ": " + whisperChatMessage.message;
				
				gPanel.updateChat(message);
			}
		}
	}
	
	private void handleRequest(Connection c, ServerRequest request)
	{
		switch(request)
		{
		case UsernameRequest:
			ClientMessage.UsernameMessage usernameMessage = new ClientMessage.UsernameMessage();
			usernameMessage.username = Application.getUserName();
			sendTCP(usernameMessage);
			break;
		case StartGameRequest:
			// Go to Game
			Application.toGame();
			
			currentStatus = GameStatus.INGAME;
			break;
		}
	}
	
	private void handleDisconnection(Connection c)
	{
		// Return to Menu
		Application.client = null;
		Application.toMenu();
	}
	
	private void handleIdle(Connection c)
	{
		// Do Nothing
	}
	
	private void connectToServer()
	{
		sendTCP(ClientRequest.PlayersInServerRequest);
	}
	
	public void sendTCP(Object o)
	{
		client.sendTCP(o);
	}
	
	public void connect(String ip)
	{
		try {
			client.connect(CLIENT_TIMEOUT, ip, Network.TCP_PORT);
		} catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	public void disconnect()
	{
		client.close();
	}
	
	public long getID()
	{
		return id;
	}
	
	public List<Team> getTeams()
	{
		return teams;
	}
	
	public Team getTeam()
	{
		for(Team t : teams)
		{
			if(t.getID() == id)
				return t;
		}
		
		return null;
	}
	
	public Team getTeam(long id)
	{
		for(Team t : teams)
		{
			if(t.getID() == id)
				return t;
		}
		
		return null;
	}
	
}
