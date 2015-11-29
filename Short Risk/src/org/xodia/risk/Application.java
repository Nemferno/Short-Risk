package org.xodia.risk;

import java.awt.Font;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.xodia.risk.net.GameClient;
import org.xodia.risk.net.GameServer;

public class Application 
{

	public static final int WIDTH = 640;
	public static final int HEIGHT = 480;
	
	private static JFrame application;
	private static String username;
	
	private static GameServer server;
	public static GameClient client;
	public static JPanel currentPanel;
	
	public static Font titleFont;
	public static Font messageFont;
	public static Font buttonFont;
	
	public static void main(String[] args)
	{
		//SwingUtilities.invokeLater(new Runnable()
		//{
		//	public void run()
		//	{
				createAndShowGUI();
		//	}
		//});
	}
	
	private static void createAndShowGUI()
	{
		application = new JFrame("Short Risk");
		
		createResources();
		createGameComponents();
		
		application.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JFrame.setDefaultLookAndFeelDecorated(true);
		application.setSize(WIDTH, HEIGHT);
		application.setLocationRelativeTo(null);
		application.setResizable(false);
		application.setVisible(true);
	}
	
	private static void createGameComponents()
	{
		checkUsernameExist();
		toMenu();
	}
	
	private static void checkUsernameExist()
	{
		File dataFile = new File("player.data");
		
		try
		{
			if(!dataFile.exists())
			{
				dataFile.createNewFile();
				// Prompt user for name
				String username = JOptionPane.showInputDialog(application, "Input Username", System.getProperty("user.name"));
				BufferedWriter bw = new BufferedWriter(new FileWriter(dataFile));
				bw.write("Username:" + username);
				bw.close();
				Application.username = username;
			}else
			{
				BufferedReader br = new BufferedReader(new FileReader(dataFile));
				String nameLine = br.readLine();
				br.close();
				String username = nameLine.substring(nameLine.indexOf(':') + 1, nameLine.length());
				Application.username = username;
			}
		}catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public static void createServer()
	{
		// Create Server
		server = new GameServer();
	}
	
	public static void terminateServer()
	{
		// Save Game
		server.disconnect();
		server = null;
	}
	
	public static void toMenu()
	{
		clearScreen();
		currentPanel = new MenuPanel();
		application.getContentPane().add(currentPanel);
		validateScreen();
	}
	
	public static void toGame()
	{
		clearScreen();
		currentPanel = new GamePanel();
		application.getContentPane().add(currentPanel);
		validateScreen();
	}
	
	public static void toLobby()
	{
		clearScreen();
		currentPanel = new LobbyPanel();
		application.getContentPane().add(currentPanel);
		validateScreen();
	}
	
	public static JFrame getApplication()
	{
		return application;
	}
	
	public static String getUserName()
	{
		return username;
	}
	
	public static boolean isHost()
	{
		return server != null;
	}
	
	private static void createResources()
	{
		titleFont = new Font("Arial", Font.BOLD, 36);
		buttonFont = new Font("Arial", Font.PLAIN, 24);
		messageFont = new Font("Times New Roman", Font.BOLD | Font.ITALIC, 18);
	}
	
	private static void clearScreen()
	{
		JComponent component = (JComponent) application.getContentPane();
		component.removeAll();
	}
	
	private static void validateScreen()
	{
		JComponent component = (JComponent) application.getContentPane();
		component.revalidate();
		component.repaint();
	}
	
}
