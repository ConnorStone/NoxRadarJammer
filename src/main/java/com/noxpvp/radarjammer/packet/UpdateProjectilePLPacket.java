package com.noxpvp.radarjammer.packet;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.scheduler.BukkitRunnable;

import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.reflect.FieldAccessException;

public class UpdateProjectilePLPacket extends BukkitRunnable {
	
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Instance Fields
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	private final ProtocolManager pm;
	private final Projectile p;
	private Location lastLoc;
	
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Constructors
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	public UpdateProjectilePLPacket(ProtocolManager pm, Projectile p) {
	
		this.pm = pm;
		this.p = p;
	}
	
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Instance Methods
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	public void run() {
	
		if (!p.isValid() || p.isOnGround() || p.isDead() || lastLoc != null && p.getLocation().equals(lastLoc)) {
			cancel();
			return;
		}
		
		lastLoc = p.getLocation();
		
		try {
			pm.updateEntity(p, getNearbyPlayers(p, 50));
		} catch (final FieldAccessException e) {
			cancel();
			return;
		}
		
	}
	
	private List<Player> getNearbyPlayers(Entity e, int radius) {
	
		final List<Player> list = new ArrayList<Player>();
		
		for (final Entity it : e.getNearbyEntities(radius, radius, radius)) {
			if (!(it instanceof Player)) {
				continue;
			} else {
				list.add((Player) it);
			}
		}
		
		return list;
	}
	
}
