package com.noxpvp.radarjammer.packet;

import java.util.logging.Level;

import org.apache.commons.lang.math.RandomUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.noxpvp.radarjammer.Jammer;
import com.noxpvp.radarjammer.JammingUtils;
import com.noxpvp.radarjammer.RadarJammer;

public class UpdateTracerScrambler extends BukkitRunnable {
	private RadarJammer plugin;
	
	private final Player p;
	private final Vector pLoc;
	private final int high, low;
	
	private int radius;
	private int spread;
	
	public UpdateTracerScrambler(Player p) {
		
		this.plugin = RadarJammer.getInstance();
		
		this.p = p;
		this.pLoc = p.getLocation().toVector();
		high = (int) (pLoc.getY() + 20);
		low = high - 40;
		
		this.radius = plugin.getJammer().getRadius();
		this.spread = plugin.getJammer().getSpread();
		
	}

	public void run() {
		try {
			
			final int px = (int) pLoc.getX(), pz = (int) pLoc.getZ();
			
			int id = Jammer.startId + 500;

			for (int x = px - radius; x < (px + (radius)); x = x + spread){
				for (int z = pz - radius; z < (pz + (radius)); z = z + spread){

					Location cur;
					do {
						cur = new Location(p.getWorld(), x, RandomUtils.nextInt(high - low) + low, z);
					} while (cur.toVector().distance(pLoc) < 8);
					
					JammingUtils.updateEntityLoc(p, cur, id++);
					
				}
			}
		} catch (Exception e) {
			plugin.getLogger().logp(Level.SEVERE, "UpdatePlPacket.java", "run()", "uh oh...");
			e.printStackTrace();
		}
		
	}

}
