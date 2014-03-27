package com.noxpvp.radarjammer;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.bergerkiller.bukkit.common.config.FileConfiguration;
import com.bergerkiller.bukkit.common.protocol.CommonPacket;
import com.bergerkiller.bukkit.common.protocol.PacketType;
import com.bergerkiller.bukkit.common.utils.PacketUtil;
import com.dsh105.holoapi.util.TagIdGenerator;

public class Jammer{

	public final static int maxSize = 64, maxSpread = 20, minSpread = 2;

	public static int startId = 0;
	
	private RadarJammer plugin;
	private List<String> jamming;
	
	private int radius, spread;
	private JamMode mode;

	public enum JamMode{
		
		INVISIBLE((byte) 0x20),
		CROUCHED((byte) 0x02);
		
		JamMode(byte bit){
			this.bit = bit;
		}
		
		byte bit;
		
		byte getByte(){
			return bit;
		}
	}
	
	public Jammer(RadarJammer plugin) {
		
		this.plugin = plugin;
		this.jamming = new ArrayList<String>();
		
		FileConfiguration config = plugin.getRadarConfig();
		
		this.mode = JamMode.valueOf(config.get(RadarJammer.NODE_MODE, String.class, JamMode.INVISIBLE.name()));
		this.radius = config.get(RadarJammer.NODE_RADIUS, Integer.class, 40);
		this.spread = config.get(RadarJammer.NODE_SPREAD, Integer.class, 8);
		
		if (radius > maxSize)
			radius = maxSize;
		if (spread > maxSpread)
			spread = maxSpread;
		else if (spread < minSpread)
			spread = minSpread;
		
		if (startId <= 0) {
			if (RadarJammer.isHoloAPIActive())
				startId = TagIdGenerator.nextId(500);
			else if (RadarJammer.isNoxCoreActive())
				startId = com.noxpvp.core.packet.PacketUtil.getNewEntityId(500);
			else
				startId = Short.MAX_VALUE;
		}
		
		for (Player p : Bukkit.getOnlinePlayers()){
			if (p.hasPermission(RadarJammer.PERM_EXEMPT))
				continue;
			
			String name = p.getName();
			
			if (!jamming.contains(name))
				jamming.add(name);
			
			jamFullRad(p);
				
		}
		
	}
	
	public void unJamAll(){
		int amount = (((radius * 2) / spread) * ((radius * 2) / spread));
		int[] ids = new int[amount + 5];
		
		try {
			for (int i = startId, r = 0; i < (amount + startId); i++, r++)
				ids[r] = i;
			
			CommonPacket destroyer = new CommonPacket(PacketType.OUT_ENTITY_DESTROY);
			destroyer.write(PacketType.OUT_ENTITY_DESTROY.entityIds, ids);
			
			PacketUtil.broadcastPacket(destroyer, false);
		} catch (Exception e) {
			plugin.getLogger().logp(Level.SEVERE, "Jammer.java", "unJamAll()", "uh oh...");
			e.printStackTrace();
		}
	}
	
	public void unJam(String name){
		if (jamming.contains(name))
			jamming.remove(name);
	}
	
	public void addJam(String name){
		if (!jamming.contains(name))
			jamming.add(name);
		
		jamFullRad(Bukkit.getPlayer(name));
	}
	
	public void jamFullRad(Player p){
		String name = p.getName();
		
		if (!p.isOnline()){
			
			if (jamming.contains(name))
				jamming.remove(name);
			
			return;
		} else if (!jamming.contains(name))
			return;
		
		{
			final Player[] players = Bukkit.getOnlinePlayers();
			String[] names = new String[players.length];
			
			for (int i = 0; i < players.length; i++){
				if (!p.canSee(players[i]))
					continue;	
				
				names[i] = players[i].getName();
			}
			new JammerPacket(p, radius, spread, mode, names).start();
		
		}
		
	}

	public void jamFullRadUpdate(Player p, Vector dif) {
		String name = p.getName();
		
		if (!p.isOnline() || !jamming.contains(name))
			return;

		new JammerUpdatePacket(p, dif, radius, spread).start();
			
	}

}
