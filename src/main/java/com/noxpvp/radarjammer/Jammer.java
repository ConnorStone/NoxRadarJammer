package com.noxpvp.radarjammer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.dsh105.holoapi.util.TagIdGenerator;
import com.noxpvp.core.packet.NoxPacketUtil;
import com.noxpvp.radarjammer.packet.JammerBKPacket;
import com.noxpvp.radarjammer.packet.JammerPLPacket;

public class Jammer{

	public final static int maxSize = 64, maxSpread = 20, minSpread = 2;

	public static int startId = 0;
	
	public Callable<List<Player>> getUpdatedLocPlayers;

	private ConcurrentHashMap<String, Vector> jamming;
	private boolean useProtocolLib;
	private int radius, spread, period;
	
	public Jammer(RadarJammer plugin, int updatePeriod) {
		
		this.jamming = new ConcurrentHashMap<String, Vector>();
		this.period = updatePeriod;
		
		this.radius = plugin.getRadarConfig().getInt(RadarJammer.NODE_RADIUS, 40);
		this.spread = plugin.getRadarConfig().getInt(RadarJammer.NODE_SPREAD, 8);
		
		if (radius > maxSize)
			radius = maxSize;
		if (spread > maxSpread)
			spread = maxSpread;
		else if (spread < minSpread)
			spread = minSpread;
		
		if (startId <= 0) {
			if (RadarJammer.isHoloAPIActive())
				startId = TagIdGenerator.nextId(1000);
			else if (RadarJammer.isNoxCoreActive())
				startId = NoxPacketUtil.getNewEntityId(1000);
			else
				startId = Short.MAX_VALUE + 20000;//This will still most likely be compatible with other entity id plugins like holograms, even if its not holoapi
		}
		
		useProtocolLib = !RadarJammer.isBkCommonLibActive();
		
		for (Player p : Bukkit.getOnlinePlayers()){
			if (p.hasPermission(RadarJammer.PERM_EXEMPT))
				continue;
			
			jamming.putIfAbsent(p.getName(), p.getLocation().toVector());
			jamFullRad(p);
				
		}
		
		this.getUpdatedLocPlayers = new Callable<List<Player>>() {
			
			public List<Player> call() throws Exception {
				return updateLocations();
			}
		};
		
	}
	
	public int getRadius() {
		return this.radius;
	}
	
	public int getSpread() {
		return this.spread;
	}
	
	
	private List<Player> updateLocations() {
		List<Player> toUpdate = new ArrayList<Player>();
		
		for (Player p : Bukkit.getOnlinePlayers()) {
			String name = p.getName();
			if (!jamming.containsKey(name))
				continue;
			
			Vector old = jamming.get(name);
			Vector cur = p.getLocation().toVector();
			
			if (old.distance(cur) < 10)//Must move 10 blocks from last known location for an update
				continue;
			
			jamming.remove(name);
			jamming.put(name, cur);
			
			toUpdate.add(p);
		}
		
		return toUpdate.isEmpty()? null : toUpdate;
	}
	
	public void unJam(String name){
		if (jamming.contains(name))
			jamming.remove(name);
	}
	
	public void addJam(Player p){
		jamming.put(p.getName(), p.getLocation().toVector());
		jamFullRad(p);
	}
	
	public void jamFullRad(Player p){
		String name = p.getName();
		
		if (!p.isOnline()){
			
			if (jamming.containsKey(name))
				jamming.remove(name);
			
			return;
		} else if (!jamming.containsKey(name))
			return;
		
		{
			final Player[] players = Bukkit.getOnlinePlayers();
			String[] names = new String[players.length];
			
			for (int i = 0; i < players.length; i++){
				if (!p.canSee(players[i])/* || players[i].equals(p)*/)
					continue;	
				
				names[i] = players[i].getName();
			}
			if (names[0] != null && names.length > 0)
				if (useProtocolLib)
					new JammerPLPacket(p, radius, spread, names).runTaskAsynchronously(RadarJammer.getInstance());
				else
					new JammerBKPacket(p, radius, spread, names).runTaskAsynchronously(RadarJammer.getInstance());
		}
		
	}
	
}
