package com.noxpvp.radarjammer.packet;

import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.noxpvp.radarjammer.Jammer;
import com.noxpvp.radarjammer.JammingUtils;
import com.noxpvp.radarjammer.RadarJammer;

public class UpdateMapScrambler extends BukkitRunnable {
	
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Instance Fields
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	private final RadarJammer plugin;
	
	private final Player p;
	private final Vector pLoc;
	private final int radius;
	private final int spread;
	
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Constructors
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	public UpdateMapScrambler(Player p) {
	
		plugin = RadarJammer.getInstance();
		
		this.p = p;
		pLoc = p.getLocation().toVector();
		
		radius = plugin.getJammer().getRadius();
		spread = plugin.getJammer().getSpread();
		
	}
	
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Instance Methods
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	public void run() {
	
		try {
			
			final int px = (int) pLoc.getX(), pz = (int) pLoc.getZ();
			
			int id = Jammer.startId;
			
			for (int x = px - radius; x < px + radius; x = x + spread) {
				for (int z = pz - radius; z < pz + radius; z = z + spread) {
					
					JammingUtils.updateEntityLoc(p, new Location(p.getWorld(), x, -2, z), id++);
					
				}
			}
		} catch (final Exception e) {
			plugin.getLogger().logp(Level.SEVERE, getClass().getName(), "run()", "uh oh...");
			e.printStackTrace();
		}
		
	}
	
}
