package org.xodia.risk.net;

import org.xodia.risk.game.Country;
import org.xodia.risk.net.message.BattleUpdate;
import org.xodia.risk.net.message.ClientMessage;
import org.xodia.risk.net.message.ClientRequest;
import org.xodia.risk.net.message.ServerMessage;
import org.xodia.risk.net.message.ServerRequest;

import com.esotericsoftware.kryo.Kryo;

public class Network 
{

	public static final int TCP_PORT = 9498;
	
	public static void register(Kryo k)
	{
		Class[] classes = new Class[]
		{
			String.class,
			String[].class,
			long[].class,
			Country.class,
			BattleUpdate.class,
			
			ClientRequest.class,
			ClientMessage.UsernameMessage.class,
			ClientMessage.KickTeamMessage.class,
			ClientMessage.BuyTroopMessage.class,
			ClientMessage.BuyShipMessage.class,
			ClientMessage.EndTurnMessage.class,
			ClientMessage.DeclareBattleMessage.class,
			ClientMessage.ConfirmBattleMessage.class,
			ClientMessage.AttackerRollMessage.class,
			ClientMessage.DefenderRollMessage.class,
			ClientMessage.EndBattleMessage.class,
			ClientMessage.AllChatMessage.class,
			ClientMessage.WhisperChatMessage.class,
			
			ServerRequest.class,
			ServerMessage.NewTeamMessage.class,
			ServerMessage.ExistingPlayersMessage.class,
			ServerMessage.AddTeamMessage.class,
			ServerMessage.RemoveTeamMessage.class,
			ServerMessage.UpdateTeamMessage.class,
			ServerMessage.TeamTurnMessage.class,
			ServerMessage.PurchaseMessage.class,
			ServerMessage.TeamBattleMessage.class,
			ServerMessage.StartGameMessage.class,
			ServerMessage.UpdateBattleMessage.class,
			ServerMessage.EndBattleMessage.class,
			ServerMessage.TeamDefeatedMessage.class,
			ServerMessage.TeamVictoryMessage.class,
			ServerMessage.AllChatMessage.class,
			ServerMessage.WhisperChatMessage.class
		};
		
		for(Class c : classes)
			k.register(c);
	}
	
}
