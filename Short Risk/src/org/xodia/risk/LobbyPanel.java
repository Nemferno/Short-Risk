package org.xodia.risk;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

import org.xodia.risk.game.Team;
import org.xodia.risk.net.message.ClientRequest;
import org.xodia.risk.ui.ChatPanel;
import org.xodia.risk.ui.TeamLobbyCellRenderer;

public class LobbyPanel extends JPanel
{

	private static final long serialVersionUID = -3585585196175528348L;

	private static final int TITLEY = 20;
	private static final int LISTY = 100;
	private static final int BUTTONY = 100;
	private static final int TEXTBOXY = 300;
	
	private JList teamList;
	private DefaultListModel teamModel;
	private ChatPanel chatPanel;
	private JLabel title;
	private JButton start;
	private JButton exit;
	
	public LobbyPanel()
	{
		setLayout(null);
		createComponents();
	}
	
	private void createComponents()
	{
		title = new JLabel("Lobby");
		title.setFont(Application.titleFont);
		title.setSize(400, 60);
		title.setHorizontalAlignment(JLabel.CENTER);
		title.setLocation(Application.WIDTH / 2 - title.getWidth() / 2, TITLEY);
		
		teamModel = new DefaultListModel();
		for(Team t : Application.client.getTeams())
		{
			teamModel.addElement(t);
		}
		
		teamList = new JList(teamModel);
		teamList.setSize(200, 160);
		teamList.setLocation(120, LISTY);
		teamList.setFixedCellHeight(28);
		teamList.setBorder(BorderFactory.createEtchedBorder());
		teamList.setCellRenderer(new TeamLobbyCellRenderer());
		
		if(Application.isHost())
		{
			start = new JButton("Start");
			start.setSize(160, 30);
			start.setLocation(360, BUTTONY + 60);
			start.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					// Send Start to Server
					Application.client.sendTCP(ClientRequest.StartGame);
				}
			});
			
			add(start);
		}
		
		exit = new JButton("Exit");
		exit.setSize(160, 30);
		exit.setLocation(360, BUTTONY);
		exit.addActionListener(new ActionListener() 
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				Application.client.disconnect();
				
				if(Application.isHost())
					Application.terminateServer();
			}
		});
		
		chatPanel = new ChatPanel();
		chatPanel.setLocation(145, TEXTBOXY);
		
		add(title);
		add(teamList);
		add(exit);
		add(chatPanel);
	}
	
	public void updateChat(String message)
	{
		chatPanel.addMessage(message);
	}
	
	public void addTeam(Team team)
	{
		teamModel.addElement(team);
	}
	
	public void removeTeam(Team team)
	{
		teamModel.removeElement(team);
	}
	
}
