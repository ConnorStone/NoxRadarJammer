package com.noxpvp.radarjammer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.noxpvp.radarjammer.wrapper.WrapperPlayServerEntityTeleport;
import com.noxpvp.radarjammer.wrapper.WrapperPlayServerNamedEntitySpawn;

public class JammingUtils {
	
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Static Methods
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	public static List<String> getNamesForPlayer(Player p) {
	
		final Player[] players = Bukkit.getOnlinePlayers();
		final List<String> names = new ArrayList<String>();
		
		for (int i = 0; i < players.length; i++) {
			if (!p.canSee(players[i])) {
				continue;
			}
			
			names.add(players[i].getName());
		}
		
		if (!names.isEmpty()) {
			return names;
		}
		
		return null;
	}
	
	public static void sendCrouchedPlayer(Player receiver, Location loc, int entityId, String name) {
	
		final WrapperPlayServerNamedEntitySpawn packet = new WrapperPlayServerNamedEntitySpawn();
		
		packet.setEntityID(entityId);
		packet.setPlayerName(name);
		packet.setPlayerUUID(UUID.randomUUID().toString());
		packet.setPosition(loc.toVector());
		packet.setMetadata(RadarJammer.getInstance().getJammer().crouchPlayer);
		
		packet.sendPacket(receiver);
		
	}
	
	public static void sendInvisPlayer(Player receiver, Location loc, int entityId, String name) {
	
		final WrapperPlayServerNamedEntitySpawn packet = new WrapperPlayServerNamedEntitySpawn();
		
		packet.setEntityID(entityId);
		packet.setPlayerName(name);
		packet.setPlayerUUID(UUID.randomUUID().toString());
		packet.setPosition(loc.toVector());
		packet.setMetadata(RadarJammer.getInstance().getJammer().invisPlayer);
		
		packet.sendPacket(receiver);
	}
	
	public static void updateEntityLoc(Player receiver, Location to, int entityId) {
	
		final WrapperPlayServerEntityTeleport packet = new WrapperPlayServerEntityTeleport();
		
		packet.setEntityID(entityId);
		packet.setX(to.getX());
		packet.setY(to.getY());
		packet.setZ(to.getZ());
		
		packet.sendPacket(receiver);
	}
	
}
