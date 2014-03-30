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

	private ProtocolManager pm;
	private Projectile p;
	private Location lastLoc;

	public UpdateProjectilePLPacket(ProtocolManager pm, Projectile p) {
		this.pm = pm;
		this.p = p;
	}
	
	public void run() {
		if (!p.isValid() || p.isOnGround() || p.isDead() || (lastLoc != null && p.getLocation().equals(lastLoc))) {
			cancel();
			return;
		}
		
		lastLoc = p.getLocation();
		
		try {
			pm.updateEntity(p, getNearbyPlayers(p, 50));
		} catch (FieldAccessException e) {
			cancel();
			return;
		}
		
	}
	
	private List<Player> getNearbyPlayers(Entity e, int radius) {
		List<Player> list = new ArrayList<Player>();
		
		for (Entity it : e.getNearbyEntities(radius, radius, radius))
			if (!(it instanceof Player)) continue;
			else
				list.add((Player) it);
		
		return list;
	}

}
