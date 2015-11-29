package org.xodia.risk.ui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;

import org.xodia.risk.Application;
import org.xodia.risk.net.message.BattleUpdate;
import org.xodia.risk.net.message.ClientMessage;

public class BattleDialog extends JDialog
{

	private static final long serialVersionUID = 73270494123479653L;

	private JLabel attDiceLabel;
	private JLabel defDiceLabel;
	private JButton rollButton;
	
	private JLabel attNumLabel;
	private JLabel defNumLabel;
	private JLabel attUsernameLabel;
	private JLabel defUsernameLabel;
	
	private JButton leave;
	
	private BattleUpdate currentUpdate; // The current update it is on
	
	private static BattleDialog instance;
	
	private BattleDialog(BattleUpdate update)
	{
		super(Application.getApplication(), "Battle", false);
		
		setSize(300, 150);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setLocationRelativeTo(Application.getApplication());
		setLayout(null);
		setResizable(false);
		setAlwaysOnTop(true);
		requestFocus();
		
		createComponents(update);
	}
	
	private void createComponents(BattleUpdate update)
	{
		attUsernameLabel = new JLabel(Application.client.getTeam(update.attID).getUserName());
		attUsernameLabel.setSize(100, 28);
		attUsernameLabel.setLocation(10, 5);
		
		defUsernameLabel = new JLabel(Application.client.getTeam(update.defID).getUserName());
		defUsernameLabel.setSize(100, 28);
		defUsernameLabel.setLocation(290 - defUsernameLabel.getWidth(), 5);
		
		attNumLabel = new JLabel(String.valueOf(update.attNum + update.attDonateNum));
		attNumLabel.setHorizontalAlignment(JLabel.CENTER);
		attNumLabel.setSize(50, 28);
		attNumLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		attNumLabel.setLocation(25, 36);
		
		attDiceLabel = new JLabel(String.valueOf(update.attDice));
		attDiceLabel.setHorizontalAlignment(JLabel.CENTER);
		attDiceLabel.setSize(50, 36);
		attDiceLabel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Dice"));
		attDiceLabel.setLocation(25, 76);
		
		defDiceLabel = new JLabel(String.valueOf(update.defDice));
		defDiceLabel.setHorizontalAlignment(JLabel.CENTER);
		defDiceLabel.setSize(50, 36);
		defDiceLabel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Dice"));
		defDiceLabel.setLocation(225, 76);
		
		defNumLabel = new JLabel(String.valueOf(update.defNum + update.defDonateNum));
		defNumLabel.setHorizontalAlignment(JLabel.CENTER);
		defNumLabel.setSize(50, 28);
		defNumLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		defNumLabel.setLocation(225, 36);
		
		if(Application.client.getID() == update.attID)
		{
			leave = new JButton("Give Up");
			leave.setSize(80, 28);
			leave.setLocation(110, 24);
			leave.addActionListener(new ActionListener() 
			{
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					Application.client.sendTCP(new ClientMessage.EndBattleMessage());
				}
			});
			add(leave);
		}
		
		if(Application.client.getID() == update.attID || Application.client.getID() == update.defID)
		{
			rollButton = new JButton("Roll");
			rollButton.setSize(36, 28);
			rollButton.setEnabled(false);
			
			if(update.attID == Application.client.getID())
			{
				rollButton.setLocation(80, 80);
				if(update.isAttToRoll)
					rollButton.setEnabled(true);
			}else if(update.defID == Application.client.getID())
			{
				rollButton.setLocation(140, 80);
				if(!update.isAttToRoll)
					rollButton.setEnabled(true);
			}
			
			rollButton.addActionListener(new ActionListener() 
			{
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					if(currentUpdate.attID == Application.client.getID())
					{
						Application.client.sendTCP(new ClientMessage.AttackerRollMessage());
					}else if(currentUpdate.defID == Application.client.getID())
					{
						Application.client.sendTCP(new ClientMessage.DefenderRollMessage());
					}
					
					rollButton.setEnabled(false);
				}
			});
			
			add(rollButton);
		}
		
		add(attUsernameLabel);
		add(defUsernameLabel);
		add(attNumLabel);
		add(defNumLabel);
		add(attDiceLabel);
		add(defDiceLabel);
		
		currentUpdate = update;
	}
	
	public synchronized static void create(BattleUpdate update)
	{
		if(instance == null)
		{
			instance = new BattleDialog(update);
			instance.setVisible(true);
		}
	}
	
	public synchronized static void update(BattleUpdate update)
	{
		if(instance != null)
		{
			if(update.attID == Application.client.getID() || update.defID == Application.client.getID())
			{
				if(update.isAttToRoll)
				{
					if(update.attID == Application.client.getID())
					{
						instance.rollButton.setEnabled(true);
					}else
					{
						instance.rollButton.setEnabled(false);
					}
				}else
				{
					if(update.defID == Application.client.getID())
					{
						instance.rollButton.setEnabled(true);
					}else
					{
						instance.rollButton.setEnabled(false);
					}
				}
			}
			
			instance.attNumLabel.setText(String.valueOf(update.attNum + update.attDonateNum));
			instance.attDiceLabel.setText(String.valueOf(update.attDice));
			instance.defNumLabel.setText(String.valueOf(update.defNum + update.defDonateNum));
			instance.defDiceLabel.setText(String.valueOf(update.defDice));
			
			instance.currentUpdate = update;
			instance.invalidate();
			instance.repaint();
		}
	}
	
	public synchronized static void destroy()
	{
		if(instance != null)
		{
			instance.setVisible(false);
			instance.dispose();
			instance = null;
		}
	}
	
}
