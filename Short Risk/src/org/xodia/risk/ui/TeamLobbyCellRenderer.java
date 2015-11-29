package org.xodia.risk.ui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import org.xodia.risk.Application;
import org.xodia.risk.game.Team;

public class TeamLobbyCellRenderer extends JPanel implements ListCellRenderer
{

	private static final long serialVersionUID = -1605939578350098307L;
	
	private Team team;
	private JLabel name;
	
	public TeamLobbyCellRenderer()
	{
		setOpaque(true);
		setBorder(BorderFactory.createLineBorder(Color.BLACK));
		setLayout(null);
		
		name = new JLabel();
		name.setSize(200, 28);
		name.setLocation(0, 0);
		name.setHorizontalAlignment(JLabel.CENTER);
		name.setFont(Application.buttonFont);
		add(name);
	}
	
	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index,
			boolean isSelected, boolean cellHasFocus)
	{
		team = (Team) value;
		name.setText(team.getID() + ": " + team.getUserName());
		return this;
	}
	
	public Team getTeam()
	{
		return team;
	}
	
}
