package com.noxpvp.radarjammer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.bergerkiller.bukkit.common.config.FileConfiguration;
import com.dsh105.holoapi.util.TagIdGenerator;

public class Jammer{

	public final static int maxSize = 64, maxSpread = 20, minSpread = 2;

	public static int startId = 0;
	
	private ConcurrentHashMap<String, Vector> jamming;
	public Callable<List<Player>> getUpdatedLocPlayers;
	
	private int radius, spread;
	
	public Jammer(RadarJammer plugin) {
		
		this.jamming = new ConcurrentHashMap<String, Vector>();
		
		FileConfiguration config = plugin.getRadarConfig();
		
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
				startId = Short.MAX_VALUE + 20000;//This will still most likely be compatible with other entity id plugins like holograms, even if its not holoapi
		}
		
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
	
/*	public void unJamAll(){
		int amount = (((radius * 2) / spread) * ((radius * 2) / spread));
		int[] ids = new int[amount];
		
		try {
			for (int i = startId, r = 0; i < (amount + startId); i++, r++)
				ids[r] = i;
			
			for (int i = 0; i < ids.length + 20; i = i + 20) {
				int[] temp = new int[20];
				for (int j = i; j < temp.length; j++) {
					temp[j] = ids[j];
					
				}
				
				CommonPacket destroyer = new CommonPacket(PacketType.OUT_ENTITY_DESTROY);
				destroyer.write(PacketType.OUT_ENTITY_DESTROY.entityIds, ids);
				PacketUtil.broadcastPacket(destroyer, false);
			}
			
		} catch (Exception e) {
			plugin.getLogger().logp(Level.SEVERE, "Jammer.java", "unJamAll()", "uh oh...");
			e.printStackTrace();
		}
	}*/
	
	public void unJam(String name){
		if (jamming.contains(name))
			jamming.remove(name);
	}
	
	public void addJam(String name){
		Player p = Bukkit.getPlayer(name);
		if (p == null)
			return;
		
		jamming.put(name, p.getLocation().toVector());
		
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
				if (!p.canSee(players[i]))
					continue;	
				
				names[i] = players[i].getName();
			}
			new JammerPacket(p, radius, spread, names).start();
		
		}
		
	}

	public void jamFullRadUpdate(Player p, Vector dif) {
		String name = p.getName();
		
		if (!p.isOnline() || !jamming.contains(name))
			return;

		new JammerUpdatePacket(p).start();
			
	}

}
