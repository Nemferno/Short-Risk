package org.xodia.risk.game;

public enum Country 
{

	A(9, 20, 0, 7, false, true),
	B(7, 15, 5, 3, false, true), // 3
	C(5, 10, 6, 2, false, true), // 4
	D(4, 7, 5, 1, false, true), // 5
	E(3, 5, 8, 0, true, true), // 8
	F(2, 2, 6, 0, true, false); // 6
	
	private int gdp;
	private int startingMoney;
	private int startingTroop;
	private int startingShip;
	private boolean isLandLocked;
	private boolean isBlockadeable;
	
	Country(int gdp, int startingMoney, int startingTroop, int startingShip,
			boolean isLandLocked, boolean isBlockadeable)
	{
		this.gdp = gdp;
		this.startingMoney = startingMoney;
		this.startingTroop = startingTroop;
		this.startingShip = startingShip;
		this.isLandLocked = isLandLocked;
		this.isBlockadeable = isBlockadeable;
	}
	
	public int getGDP()
	{
		return gdp;
	}
	
	public int getStartingMoney()
	{
		return startingMoney;
	}
	
	public int getStartingTroop()
	{
		return startingTroop;
	}
	
	public int getStartingShip()
	{
		return startingShip;
	}
	
	public boolean isLandLocked()
	{
		return isLandLocked;
	}
	
	public boolean isBlockadeable()
	{
		return isBlockadeable;
	}
	
}
