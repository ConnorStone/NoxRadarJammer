package com.noxpvp.radarjammer.packet;

import java.util.List;
import java.util.logging.Level;

import org.apache.commons.lang.math.RandomUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.noxpvp.radarjammer.Jammer;
import com.noxpvp.radarjammer.JammingUtils;
import com.noxpvp.radarjammer.RadarJammer;

public class AsyncTracerScrambler extends BukkitRunnable {
	private RadarJammer plugin;
	
	private int nextId;

	private int radius;
	private int spread;
	
	private List<String> names;
	private final Player p;
	private final Vector pLoc;
	private final int high, low;
	
	public AsyncTracerScrambler(Player p, int radius, int spread, List<String> names) {
		
		this.plugin = RadarJammer.getInstance();
		
		this.p = p;
		this.nextId = Jammer.startId + 500;
		
		this.radius = radius;
		this.spread = spread;
		this.names = names;		
		
		pLoc = p.getLocation().toVector();
		high = (int) (pLoc.getY() + 20);
		low = high - 40;
		
	}
	
	public void start(int delayTicks) {
		runTaskLaterAsynchronously(plugin, delayTicks);
	}

	public void run() {
		try {
			for (int x = (int) (pLoc.getX() - radius); x < (pLoc.getX() + radius); x = x + spread){
				for (int z = (int) (pLoc.getZ() - radius); z < (pLoc.getZ() + radius); z = z + spread){					
					
					Location cur;
					do {
						cur = new Location(p.getWorld(), x, RandomUtils.nextInt(high - low) + low, z);
					} while (cur.toVector().distance(pLoc) < 8);
					
					String random = names.get(names.size() < 2? 0 : RandomUtils.nextInt(names.size() - 1));
					
					JammingUtils.sendInvisPlayer(p, cur, ++nextId, random);
					
				}
			}
		} catch (Exception e) {
			plugin.getLogger().logp(Level.SEVERE, this.getClass().getName(), "run()", "uh oh...");
			e.printStackTrace();
		}
		
	}

}
