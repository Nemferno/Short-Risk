package org.xodia.risk.game;

public class Team 
{
	
	private Country country;
	private int currentTroop;
	private int currentShip;
	private int currentMoney;
	private int currentGDP;
	
	private String username;
	
	private boolean isDefeated;
	
	private long id;
	
	public Team(long id, String username)
	{
		this.username = username;
		this.id = id;
	}
	
	public void setCountry(Country c)
	{
		this.country = c;
		setCurrentTroop(c.getStartingTroop());
		setCurrentMoney(c.getStartingMoney());
		setCurrentShip(c.getStartingShip());
		setCurrentGDP(c.getGDP());
	}
	
	public void setDefeated(boolean defeated)
	{
		this.isDefeated = defeated;
	}
	
	public void setCurrentGDP(int gdp)
	{
		this.currentGDP = gdp;
	}
	
	public void setCurrentTroop(int troop)
	{
		this.currentTroop = troop;
	}
	
	public void setCurrentShip(int ship)
	{
		this.currentShip = ship;
	}
	
	public void setCurrentMoney(int money)
	{
		this.currentMoney = money;
	}
	
	public void setUserName(String name)
	{
		this.username = name;
	}
	
	public boolean isDefeated()
	{
		return isDefeated;
	}
	
	public int getCurrentGDP()
	{
		return currentGDP;
	}
	
	public int getCurrentTroop()
	{
		return currentTroop;
	}
	
	public int getCurrentShip()
	{
		return currentShip;
	}
	
	public int getCurrentMoney()
	{
		return currentMoney;
	}
	
	public long getID()
	{
		return id;
	}
	
	public String getUserName()
	{
		return username;
	}
	
	public Country getCountry()
	{
		return country;
	}

}
