package com.noxpvp.radarjammer.packet;

import java.util.UUID;
import java.util.logging.Level;

import org.apache.commons.lang.math.RandomUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.noxpvp.radarjammer.Jammer;
import com.noxpvp.radarjammer.RadarJammer;

public class JammerPLPacket extends BukkitRunnable {
	private RadarJammer plugin;
	
	private WrappedDataWatcher watcher;
	private ProtocolManager pm;
	
	private int nextId;

	private int radius;
	private int spread;
	private int height;		
	
	private String[] names;
	private final Player p;
	private final int px;
	private final int py;
	private final int pz;
	
	public JammerPLPacket(Player p, int radius, int spread, String[] names) {
		
		this.plugin = RadarJammer.getInstance();
		
		watcher = new WrappedDataWatcher();
		watcher.setObject(0, (byte) 0x20);
		watcher.setObject(6, (float) RandomUtils.nextInt(20 - 4) + 4);
		watcher.setObject(12, (int) 0);
		
		pm = ProtocolLibrary.getProtocolManager();
		
		this.p = p;
		this.nextId = Jammer.startId;
		
		this.radius = radius;
		this.spread = spread;
		this.names = names;		
		
		Location pLoc = p.getLocation();
		px = (int) pLoc.getX();
		py = (int) pLoc.getY();
		pz = (int) pLoc.getZ();
		
	}

	public void run() {
		try {
			for (int x = px - radius; x < (px + (radius)); x = x + spread){
				for (int z = pz - radius; z < (pz + (radius)); z = z + spread){
					
					int low = py - 30, high = py + 30;
					height = (int) Math.floor(RandomUtils.nextInt(high - low) + low);
					
					String random = names[RandomUtils.nextInt(names.length - 1)];
					if (random == null || random == "")
						continue;
					
					WrapperPlayServerNamedEntitySpawn plJammerPacketWrapper = new WrapperPlayServerNamedEntitySpawn();
					
					plJammerPacketWrapper.setEntityID(++nextId);
					plJammerPacketWrapper.setPlayerUUID(new UUID(10, 0).toString());
					plJammerPacketWrapper.setPlayerName(random);
					plJammerPacketWrapper.setX(x);
					plJammerPacketWrapper.setY(height);
					plJammerPacketWrapper.setZ(z);
					plJammerPacketWrapper.setMetadata(watcher);
					
					pm.sendServerPacket(p, plJammerPacketWrapper.getHandle(), false);
					
				}
			}
		} catch (Exception e) {
			plugin.getLogger().logp(Level.SEVERE, "JammerPLPacket.java", "run()", "uh oh...");
			e.printStackTrace();
		}
		
	}

}
