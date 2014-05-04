package com.noxpvp.radarjammer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.noxpvp.radarjammer.packet.AsyncMapScrambler;
import com.noxpvp.radarjammer.wrapper.WrapperPlayServerEntityTeleport;
import com.noxpvp.radarjammer.wrapper.WrapperPlayServerNamedEntitySpawn;


public class JammingUtils {
	
	public static void sendCrouchedPlayer(Player receiver, Location loc, int entityId, String name) {
		WrapperPlayServerNamedEntitySpawn packet = new WrapperPlayServerNamedEntitySpawn();
		
		packet.setEntityID(entityId);
		packet.setPlayerName(name);
		packet.setPlayerUUID(new UUID(10, 0).toString());
		packet.setPosition(loc.toVector());
		packet.setMetadata(RadarJammer.getInstance().getJammer().crouchPlayer);
		
		packet.sendPacket(receiver);

	}
	
	public static void sendInvisPlayer(Player receiver, Location loc, int entityId, String name) {
		WrapperPlayServerNamedEntitySpawn packet = new WrapperPlayServerNamedEntitySpawn();
		
		packet.setEntityID(entityId);
		packet.setPlayerName(name);
		packet.setPlayerUUID(new UUID(10, 0).toString());
		packet.setPosition(loc.toVector());
		packet.setMetadata(RadarJammer.getInstance().getJammer().invisPlayer);
		
		packet.sendPacket(receiver);
	}
	
	public static void updateEntityLoc(Player receiver, Location to, int entityId) {
		WrapperPlayServerEntityTeleport packet = new WrapperPlayServerEntityTeleport();
		
		packet.setEntityID(entityId);
		packet.setX(to.getX());
		packet.setY(to.getY());
		packet.setZ(to.getZ());
		
		packet.sendPacket(receiver);
	}
	
	public static List<String> getNamesForPlayer(Player p) {
		Player[] players = Bukkit.getOnlinePlayers();
		List<String> names = new ArrayList<String>();
		
		for (int i = 0; i < players.length; i++){
			if (!p.canSee(players[i]))
				continue;	
			
			names.add(players[i].getName());
		}
		
		if (!names.isEmpty() || names.size() > 0) {
			names.add("FlycoderIsGay");
			return names;
		}
		
		return null;
	}

}
