package org.xodia.risk.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import org.xodia.risk.Application;
import org.xodia.risk.net.message.ClientMessage;

public class ChatPanel extends JPanel
{

	private static final long serialVersionUID = -216597318870943774L;

	private JTextField field;
	private JTextArea area;
	
	public ChatPanel()
	{	
		setPreferredSize(new Dimension(350, 125));
		setSize(getPreferredSize());
		setBorder(BorderFactory.createLineBorder(Color.BLACK));
		setFocusable(true);
		requestFocus();
		
		area = new JTextArea();
		area.setEditable(false);
		area.setBackground(Color.WHITE);
		area.setBorder(BorderFactory.createEtchedBorder());
		area.setLineWrap(true);
		area.setWrapStyleWord(true);
		area.setAutoscrolls(true);
		
		field = new JTextField();
		field.setPreferredSize(new Dimension(300, 25));
		
		Action sendAction = new AbstractAction() 
		{
			private static final long serialVersionUID = -4051625774678715857L;

			public void actionPerformed(ActionEvent e) 
			{
				if(!field.getText().trim().equals(""))
				{
					String content = field.getText();
					if(content.contains("/w "))
					{
						String[] strings = content.split(" ");
						int id = -1;
						try
						{
							id = Integer.parseInt(strings[1]);
						}catch(NumberFormatException ex)
						{
							ex.printStackTrace();
							field.setText("");
						}
						
						if(id != -1)
						{
							String message = content.substring(strings[0].length() + strings[1].length() + 2);
							ClientMessage.WhisperChatMessage whisperChatMessage = new ClientMessage.WhisperChatMessage();
							whisperChatMessage.toID = id;
							whisperChatMessage.message = message;
							Application.client.sendTCP(whisperChatMessage);
							field.setText("");
						}
					}else
					{
						ClientMessage.AllChatMessage allChatMessage = new ClientMessage.AllChatMessage();
						allChatMessage.message = field.getText();
						Application.client.sendTCP(allChatMessage);
						field.setText("");
					}
				}
			}
		};
		
		field.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), sendAction);
		
		JScrollPane jScrollPane = new JScrollPane(area);
		jScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		jScrollPane.setPreferredSize(new Dimension(300, 75));
		
		add(jScrollPane, BorderLayout.CENTER);
		add(field, BorderLayout.SOUTH);
	}
	
	public void clearText()
	{
		area.setText("");
	}
	
	public void addMessage(String message)
	{
		if(area.getText().equals(""))
		{
			area.setText(message);
		}else
		{
			area.setText(area.getText() + "\n" + message);
		}
	}
	
}
