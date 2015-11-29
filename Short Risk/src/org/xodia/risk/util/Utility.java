package org.xodia.risk.util;

import java.util.HashMap;
import java.util.Random;

import org.xodia.risk.game.Country;

public class Utility 
{

	private static HashMap<String, String> censorMap = new HashMap<String, String>();
	
	static
	{
		censorMap.put("fuck", "fudge");
		censorMap.put("shit", "poopie");
		censorMap.put("sex", "thing");
		censorMap.put("pussy", "cat");
		censorMap.put("penis", "moomoo");
		censorMap.put("vagina", "moo");
		censorMap.put("bitch", "dog");
	}
	
	public static void shuffleArray(Country[] countries)
	{
		Random r = new Random();
		for(int i = countries.length - 1; i > 0; i--)
		{
			int index = r.nextInt(i + 1);
			Country a = countries[index];
			countries[index] = countries[i];
			countries[i] = a;
		}
	}
	
	public static String censorString(String message)
	{
		String current = message;
		for(String value : censorMap.keySet())
		{
			current = current.replaceAll(value, censorMap.get(value));
		}
		return current;
	}
	
}
