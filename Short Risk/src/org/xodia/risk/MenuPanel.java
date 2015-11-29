package org.xodia.risk;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.xodia.risk.net.GameClient;

public class MenuPanel extends JPanel
{

	private static final long serialVersionUID = -5128196662716183963L;

	private static final int TITLEY = 20;
	private static final int MESSAGEY = 100;
	private static final int BUTTONY = 225;
	private static final int BUTTONGAP = 30;
	
	private JLabel title;
	private JLabel message;
	private JButton create, join, exit;
	
	private ActionListener actionListener = new ActionListener()
	{
		@Override
		public void actionPerformed(ActionEvent e) 
		{
			if(e.getSource() == create)
			{
				// Create Server & Client
				// Move to Lobby
				Application.createServer();
				Application.client = new GameClient();
				Application.client.connect("localhost");
			}else if(e.getSource() == join)
			{
				// Prompt User For IP
				// Try to Connect
				// If connection successful, move to lobby
				String address = JOptionPane.showInputDialog(MenuPanel.this, "Server Address");
				Application.client = new GameClient();
				Application.client.connect(address);
			}else if(e.getSource() == exit)
			{
				int i = JOptionPane.showConfirmDialog(MenuPanel.this, "Do you want to quit?", "Exit", JOptionPane.OK_CANCEL_OPTION);
				if(i == JOptionPane.OK_OPTION)
				{
					System.exit(0);
				}else if(i == JOptionPane.CANCEL_OPTION)
				{
					// Do Nothing
				}
			}
		}
	};
	
	public MenuPanel()
	{
		setLayout(null);
		createComponents();
	}
	
	private void createComponents()
	{
		title = new JLabel("Risk");
		title.setFont(Application.titleFont);
		title.setSize(400, 60);
		title.setHorizontalAlignment(JLabel.CENTER);
		title.setLocation(Application.WIDTH / 2 - title.getWidth() / 2, TITLEY);
		
		message = new JLabel("Welcome " + Application.getUserName() + "!");
		message.setFont(Application.messageFont);
		message.setSize(400, 60);
		message.setHorizontalAlignment(JLabel.CENTER);
		message.setLocation(Application.WIDTH / 2 - message.getWidth() / 2, MESSAGEY);
		
		create = new JButton("Create");
		create.setFont(Application.buttonFont);
		create.setSize(240, 40);
		create.setHorizontalAlignment(JLabel.CENTER);
		create.setLocation(Application.WIDTH / 2 - create.getWidth() / 2, BUTTONY);
		create.addActionListener(actionListener);
		
		join = new JButton("Join");
		join.setFont(Application.buttonFont);
		join.setSize(240, 40);
		join.setHorizontalAlignment(JLabel.CENTER);
		join.setLocation(Application.WIDTH / 2 - join.getWidth() / 2, BUTTONY + 40 + BUTTONGAP);
		join.addActionListener(actionListener);
		
		exit = new JButton("Exit");
		exit.setFont(Application.buttonFont);
		exit.setSize(240, 40);
		exit.setHorizontalAlignment(JLabel.CENTER);
		exit.setLocation(Application.WIDTH / 2 - exit.getWidth() / 2, BUTTONY + 80 + BUTTONGAP * 2);
		exit.addActionListener(actionListener);
		
		add(title);
		add(message);
		add(create);
		add(join);
		add(exit);
	}
	
}
