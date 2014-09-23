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

public class AsyncMapScrambler extends BukkitRunnable {
	
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Instance Fields
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	private final RadarJammer plugin;
	
	private int nextId;
	
	private final int radius;
	private final int spread;
	
	private final List<String> names;
	private final Player p;
	private final Vector pLoc;
	
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Constructors
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	public AsyncMapScrambler(Player p, int radius, int spread, List<String> names) {
	
		plugin = RadarJammer.getInstance();
		
		this.p = p;
		nextId = Jammer.startId;
		
		this.radius = radius;
		this.spread = spread;
		this.names = names;
		
		pLoc = p.getLocation().toVector();
		
	}
	
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Instance Methods
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	public void run() {
	
		try {
			for (int x = (int) (pLoc.getX() - radius); x < pLoc.getX() + radius; x = x + spread) {
				for (int z = (int) (pLoc.getZ() - radius); z < pLoc.getZ() + radius; z = z + spread) {
					
					final String random = names.get(names.size() < 2 ? 0 : RandomUtils.nextInt(names.size() - 1));
					
					JammingUtils.sendCrouchedPlayer(p, new Location(p.getWorld(), x, -2, z), ++nextId, random);
					
				}
			}
		} catch (final Exception e) {
			plugin.getLogger().logp(Level.SEVERE, getClass().getName(), "run()", "uh oh...");
			e.printStackTrace();
		}
		
	}
	
	public void start(int delayTicks) {
	
		runTaskLaterAsynchronously(plugin, delayTicks);
	}
	
}
