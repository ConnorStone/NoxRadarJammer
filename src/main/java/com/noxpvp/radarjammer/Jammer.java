package com.noxpvp.radarjammer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.dsh105.holoapi.util.TagIdGenerator;
import com.noxpvp.core.packet.NoxPacketUtil;
import com.noxpvp.radarjammer.packet.AsyncMapScrambler;
import com.noxpvp.radarjammer.packet.AsyncTracerScrambler;

public class Jammer {
	
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Static Fields
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	public final static int maxSize = 64, maxSpread = 20, minSpread = 2;
	public static int startId = 0;
	
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Instance Fields
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	public final WrappedDataWatcher crouchPlayer;
	public final WrappedDataWatcher invisPlayer;
	
	public Callable<List<Player>> getUpdatedLocPlayers;
	private ConcurrentHashMap<String, Location> jamming;
	
	private int radius;
	private int spread;
	
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Constructors
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	public Jammer(RadarJammer plugin, int updatePeriod) {
	
		jamming = new ConcurrentHashMap<String, Location>();
		
		crouchPlayer = new WrappedDataWatcher();
		crouchPlayer.setObject(0, (byte) 0x02);
		crouchPlayer.setObject(6, (float) 20);
		crouchPlayer.setObject(12, 0);
		
		invisPlayer = crouchPlayer.deepClone();
		invisPlayer.setObject(0, (byte) 0x20);
		
		radius = plugin.getRadarConfig().getInt(RadarJammer.NODE_RADIUS, 40);
		spread = plugin.getRadarConfig().getInt(RadarJammer.NODE_SPREAD, 8);
		
		if (radius > maxSize) {
			radius = maxSize;
		}
		if (spread > maxSpread) {
			spread = maxSpread;
		} else if (spread < minSpread) {
			spread = minSpread;
		}
		
		if (startId <= 0) {
			if (RadarJammer.isHoloAPIActive()) {
				startId = TagIdGenerator.nextId(1000);
			} else if (RadarJammer.isNoxCoreActive()) {
				startId = NoxPacketUtil.getNewEntityId(1000);
			} else {
				startId = 123456789;// This will still most likely be compatible with other entity id plugins like
									// holograms, even if its not holoapi
			}
		}
		
		for (final Player p : Bukkit.getOnlinePlayers()) {
			if (p.hasPermission(RadarJammer.PERM_EXEMPT)) {
				continue;
			}
			
			jamming.putIfAbsent(p.getName(), p.getLocation());
			sendMapScramble(p);
			
		}
		
		getUpdatedLocPlayers = new Callable<List<Player>>() {
			
			public List<Player> call() throws Exception {
			
				return updateLocations();
			}
		};
		
	}
	
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Instance Methods
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	public void addJam(Player p) {
	
		jamming.put(p.getName(), p.getLocation());
		
		sendMapScramble(p);
		sendFauxTracers(p);
	}
	
	public int getRadius() {
	
		return radius;
	}
	
	public int getSpread() {
	
		return spread;
	}
	
	public void sendFauxTracers(Player p) {
	
		final String name = p.getName();
		
		if (!p.isOnline()) {
			
			if (jamming.containsKey(name)) {
				jamming.remove(name);
			}
			
			return;
		} else if (!jamming.containsKey(name)) {
			return;
		}
		
		List<String> fakes;
		if ((fakes = JammingUtils.getNamesForPlayer(p)) != null) {
			new AsyncTracerScrambler(p, getRadius(), getSpread(), fakes).start(20);
		}
		
		return;
	}
	
	public void sendMapScramble(Player p) {
	
		final String name = p.getName();
		
		if (!p.isOnline()) {
			
			if (jamming.containsKey(name)) {
				jamming.remove(name);
			}
			
			return;
		} else if (!jamming.containsKey(name)) {
			return;
		}
		
		List<String> fakes;
		if ((fakes = JammingUtils.getNamesForPlayer(p)) != null) {
			new AsyncMapScrambler(p, getRadius(), getSpread(), fakes).start(20);
		}
		
		return;
	}
	
	public void unJam(String name) {
	
		if (jamming.contains(name)) {
			jamming.remove(name);
		}
	}
	
	private List<Player> updateLocations() {
	
		final List<Player> toUpdate = new ArrayList<Player>();
		
		for (final Player p : Bukkit.getOnlinePlayers()) {
			final String name = p.getName();
			if (!jamming.containsKey(name)) {
				continue;
			}
			
			final Location old = jamming.get(name);
			final Location cur = p.getLocation();
			
			if (old.getWorld() == cur.getWorld() && old.distance(cur) < 5) {
				continue;
			}
			
			jamming.remove(name);
			jamming.put(name, cur);
			
			toUpdate.add(p);
		}
		
		return toUpdate.isEmpty() ? null : toUpdate;
	}
	
}
