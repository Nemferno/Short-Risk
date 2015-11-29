package org.xodia.risk.ui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import org.xodia.risk.game.Team;

public class TeamGameCellRenderer extends JPanel implements ListCellRenderer
{
	
	private static final long serialVersionUID = -6361257319787356537L;
	
	private Team team;
	private JLabel teamLabel;
	
	public TeamGameCellRenderer()
	{
		setOpaque(true);
		setBorder(BorderFactory.createLineBorder(Color.BLACK));
		setLayout(null);
		
		teamLabel = new JLabel();
		teamLabel.setSize(200, 28);
		teamLabel.setLocation(0, 0);
		add(teamLabel);
	}
	
	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index,
			boolean isSelected, boolean cellHasFocus)
	{
		team = (Team) value;
		
		teamLabel.setText(team.getID() + ": " + team.getUserName() + " \t\tT: " + team.getCurrentTroop() + "| \t\tS: " + team.getCurrentShip() + "| \t\t$: " + team.getCurrentMoney());
		
		if(team.isDefeated())
		{
			teamLabel.setBackground(Color.darkGray);
			teamLabel.setForeground(Color.lightGray);
		}else
		{
			teamLabel.setBackground(list.getBackground());
			teamLabel.setForeground(list.getForeground());
		}
		
		return this;
	}

}
