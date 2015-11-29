package org.xodia.risk;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.xodia.risk.game.Team;
import org.xodia.risk.net.message.ClientMessage;
import org.xodia.risk.ui.ChatPanel;
import org.xodia.risk.ui.TeamGameCellRenderer;
import org.xodia.risk.ui.WaitingBattleDialog;

public class GamePanel extends JPanel
{

	private static final long serialVersionUID = -532048422529808713L;

	private DefaultListModel teamModel;
	private JList teamList;
	private JLabel clientTeamLabel;
	private JButton exit;
	private JButton buy;
	private JButton end;
	private ChatPanel chatPanel;
	private final String[] options = { "Troop", "Ship" };
	
	private MouseListener listMouseListener = new MouseAdapter()
	{
		public void mouseClicked(MouseEvent e) 
		{
			if(e.getSource() instanceof JList)
			{
				if(isMyTurn)
				{
					if(e.getClickCount() == 2)
					{
						JList list = (JList) e.getSource();
						Team team = (Team) list.getSelectedValue();
						Team own = Application.client.getTeam();
						
						if(!team.isDefeated())
						{
							int option = JOptionPane.showConfirmDialog(Application.getApplication(), "Do you want to battle " + team.getUserName() + "?", "Battle", JOptionPane.YES_NO_OPTION);
							if(option == JOptionPane.YES_OPTION)
							{
								// Find out which units does the country have
								String[] newOptions = null;
								if(team.getCurrentShip() > 0 && team.getCurrentTroop() > 0)
								{
									newOptions = options;
								}else if(team.getCurrentShip() > 0)
								{
									newOptions = new String[]{ options[1] };
								}else if(team.getCurrentTroop() > 0)
								{
									newOptions = new String[]{ options[0] };
								}else
								{
									JOptionPane.showMessageDialog(Application.getApplication(), "You cannot fight this country!");
									return;
								}
								
								String optionType = (String) JOptionPane.showInputDialog(Application.getApplication(), 
										"What type of unit do you want to battle with?", "Battle", JOptionPane.QUESTION_MESSAGE, 
										null, newOptions, newOptions[0]);
								
								// Send Server Message To Battle
								ClientMessage.DeclareBattleMessage declareBattleMessage = new ClientMessage.DeclareBattleMessage();
								declareBattleMessage.attID = Application.client.getID();
								declareBattleMessage.defID = team.getID();
								
								if(optionType != null)
								{
									if(optionType.equals(options[0]))
										declareBattleMessage.isTroop = true;
									else if(optionType.equals(options[1]))
										declareBattleMessage.isTroop = false;
									
									if(declareBattleMessage.isTroop)
									{
										if(own.getCurrentTroop() < 2)
										{
											JOptionPane.showMessageDialog(Application.getApplication(), "You do not have enough troops to fight!");
											return;
										}
									}else
									{
										if(own.getCurrentShip() == 0)
										{
											JOptionPane.showMessageDialog(Application.getApplication(), "You do not have enough ships to fight!");
											return;
										}
									}
									
									Application.client.sendTCP(declareBattleMessage);
									WaitingBattleDialog.create(team.getID());
								}
							}
						}
					}
				}
			}
		};
	};
	
	private volatile boolean isMyTurn;
	
	public GamePanel()
	{
		setLayout(null);
		createComponents();
	}
	
	private void createComponents()
	{
		Team own = Application.client.getTeam();
		
		clientTeamLabel = new JLabel(own.getUserName() + " \t\tT: " + own.getCurrentTroop() + " | \t\tS: " + 
					own.getCurrentShip() + " | \t\t$: " + own.getCurrentMoney());
		clientTeamLabel.setFont(Application.buttonFont);
		clientTeamLabel.setSize(Application.WIDTH, 28);
		clientTeamLabel.setLocation(0, 60);
		clientTeamLabel.setBorder(BorderFactory.createEtchedBorder());
		
		teamModel = new DefaultListModel();
		for(Team t : Application.client.getTeams())
		{
			if(t != own)
				teamModel.addElement(t);
		}
		
		teamList = new JList(teamModel);
		teamList.setSize(300, 160);
		teamList.setLocation(20, 100);
		teamList.setFixedCellHeight(28);
		teamList.setBorder(BorderFactory.createEtchedBorder());
		teamList.setCellRenderer(new TeamGameCellRenderer());
		teamList.addMouseListener(listMouseListener);
		
		exit = new JButton("Exit");
		exit.setSize(160, 32);
		exit.setLocation(380, 100);
		exit.setFont(Application.buttonFont);
		exit.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				int i = JOptionPane.showConfirmDialog(Application.getApplication(), "Do you want to leave the game?", "Exit", JOptionPane.OK_CANCEL_OPTION);
				if(i == JOptionPane.OK_OPTION)
				{
					Application.client.disconnect();
				}else if(i == JOptionPane.CANCEL_OPTION)
				{
					// Do Nothing
				}
			}
		});
		
		buy = new JButton("Buy");
		buy.setSize(160, 32);
		buy.setLocation(380, 150);
		buy.setFont(Application.buttonFont);
		buy.setEnabled(false);
		buy.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				String[] newOptions = null;
				
				Team own = Application.client.getTeam();
				
				if(!own.getCountry().isLandLocked())
				{
					newOptions = options;
				}else
				{
					newOptions = new String[]{ options[0] };
				}
				
				String option = (String) JOptionPane.showInputDialog(Application.getApplication(), "Which type of unit do you want to buy?", "Purchase", 
						JOptionPane.QUESTION_MESSAGE, null, newOptions, newOptions[0]);
				if(option != null)
				{
					if(option.equals(options[0]))
					{
						int numOfTroopsPurchase = Application.client.getTeam().getCurrentMoney() / 7;
						
						if(numOfTroopsPurchase == 0)
						{	
							JOptionPane.showMessageDialog(Application.getApplication(), "You have not enough cash!");
						}else
						{
							try
							{
								Double number = new Double(JOptionPane.showInputDialog("How many troops do you want to create? (" + numOfTroopsPurchase + ")", numOfTroopsPurchase));
								
								int numOfTroops = number.intValue();
								
								if(numOfTroops > numOfTroopsPurchase)
									numOfTroops = numOfTroopsPurchase;
								else if(numOfTroops < 1)
									numOfTroops = 1;
								
								ClientMessage.BuyTroopMessage buy = new ClientMessage.BuyTroopMessage();
								buy.number = numOfTroops;
								buy.id = Application.client.getID();
								Application.client.sendTCP(buy);
							}catch(NumberFormatException ex)
							{
								ex.printStackTrace();
							}
						}
					}else if(option.equals(options[1]))
					{
						int numOfShipsPurchase = Application.client.getTeam().getCurrentMoney() / 15;
						
						if(numOfShipsPurchase == 0)
						{
							JOptionPane.showMessageDialog(Application.getApplication(), "You have not enough cash!");
						}else
						{
							try
							{
								Number number = new Double(JOptionPane.showInputDialog("How many ships do you want to create? (" + numOfShipsPurchase + ")", numOfShipsPurchase));
								
								int numOfShips = number.intValue();
								
								if(numOfShips > numOfShipsPurchase)
									numOfShips = numOfShipsPurchase;
								else if(numOfShips < 1)
									numOfShips = 1;
								
								ClientMessage.BuyShipMessage buy = new ClientMessage.BuyShipMessage();
								buy.number = numOfShips;
								buy.id = Application.client.getID();
								Application.client.sendTCP(buy);
							}catch(NumberFormatException ex)
							{
								ex.printStackTrace();
							}
						}
					}
				}
			}
		});
		
		end = new JButton("End");
		end.setSize(160, 32);
		end.setLocation(380, 200);
		end.setFont(Application.buttonFont);
		end.setEnabled(false);
		end.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				ClientMessage.EndTurnMessage endTurnMessage = new ClientMessage.EndTurnMessage();
				endTurnMessage.id = Application.client.getID();
				Application.client.sendTCP(endTurnMessage);
				notMyTurn();
			}
		});
		
		chatPanel = new ChatPanel();
		chatPanel.setLocation(145, 300);
		
		add(clientTeamLabel);
		add(teamList);
		add(exit);
		add(end);
		add(buy);
		add(chatPanel);
	}
	
	public void updateList()
	{
		Team own = Application.client.getTeam();
		
		clientTeamLabel.setText(own.getUserName() + " \t\tT: " + own.getCurrentTroop() + " | \t\tS: " + 
					own.getCurrentShip() + " | \t\t$: " + own.getCurrentMoney());
		
		if(own.isDefeated())
		{
			teamList.setEnabled(false);
		}
		
		for(int i = 0; i < teamModel.getSize(); i++)
		{
			Team t = (Team) teamModel.get(i);
			if(t != own)
				teamModel.setElementAt(t, i);
		}
	}
	
	public void myTurn()
	{
		buy.setEnabled(true);
		end.setEnabled(true);
		isMyTurn = true;
	}
	
	public void notMyTurn()
	{
		buy.setEnabled(false);
		end.setEnabled(false);
		isMyTurn = false;
	}
	
	public void updateChat(String message)
	{
		chatPanel.addMessage(message);
	}
	
}
