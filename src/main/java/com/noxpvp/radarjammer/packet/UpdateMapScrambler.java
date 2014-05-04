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
	private RadarJammer plugin;
	
	private final Player p;
	private final Vector pLoc;
	private int radius;
	private int spread;
	
	public UpdateMapScrambler(Player p) {
		
		this.plugin = RadarJammer.getInstance();
		
		this.p = p;
		this.pLoc = p.getLocation().toVector();
		
		this.radius = plugin.getJammer().getRadius();
		this.spread = plugin.getJammer().getSpread();
		
	}

	public void run() {
		try {
			
			final int px = (int) pLoc.getX(), pz = (int) pLoc.getZ();
			
			int id = Jammer.startId;

			for (int x = px - radius; x < (px + (radius)); x = x + spread){
				for (int z = pz - radius; z < (pz + (radius)); z = z + spread){

					JammingUtils.updateEntityLoc(p, new Location(p.getWorld(), x, -2, z), id++);
					
				}
			}
		} catch (Exception e) {
			plugin.getLogger().logp(Level.SEVERE, "UpdatePlPacket.java", "run()", "uh oh...");
			e.printStackTrace();
		}
		
	}

}
