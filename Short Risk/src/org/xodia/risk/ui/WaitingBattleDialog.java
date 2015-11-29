package org.xodia.risk.ui;

import java.awt.BorderLayout;

import javax.swing.JDialog;
import javax.swing.JLabel;

import org.xodia.risk.Application;

public class WaitingBattleDialog extends JDialog
{

	private static final long serialVersionUID = 2110781463388841847L;

	private static WaitingBattleDialog instance = null;
	
	private WaitingBattleDialog(long vsID)
	{
		super(Application.getApplication(), "Waiting Battle", false);
		
		setSize(200, 50);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setResizable(false);
		setLocationRelativeTo(Application.getApplication());
		
		JLabel label = new JLabel("Waiting for Battle" + (vsID == -1 ? "" : " against " + Application.client.getTeam(vsID).getUserName()) + "...");
		label.setHorizontalAlignment(JLabel.CENTER);
		
		add(label, BorderLayout.CENTER);
	}
	
	public synchronized static void create(long vsID)
	{
		if(instance == null)
		{
			instance = new WaitingBattleDialog(vsID);
			instance.setVisible(true);
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
