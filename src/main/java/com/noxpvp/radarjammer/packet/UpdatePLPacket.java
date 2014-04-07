package com.noxpvp.radarjammer.packet;

import java.util.logging.Level;

import org.apache.commons.lang.math.RandomUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.noxpvp.radarjammer.Jammer;
import com.noxpvp.radarjammer.RadarJammer;

public class UpdatePLPacket extends BukkitRunnable {
	private RadarJammer plugin;
	
	private WrapperPlayServerEntityTeleport plUpdatePacketWrapper;
	private ProtocolManager pm;
	
	private final Player p;
	private final Location pLoc;

	private int radius;
	private int spread;
	
	public UpdatePLPacket(Player p) {
		
		this.plugin = RadarJammer.getInstance();
		this.pm = ProtocolLibrary.getProtocolManager();
		
		this.p = p;
		this.pLoc = p.getLocation();
		
		this.radius = plugin.getJammer().getRadius();
		this.spread = plugin.getJammer().getSpread();
		
	}

	public void run() {
		try {
			
			final int px = (int) pLoc.getX(), pz = (int) pLoc.getZ(), py = (int) pLoc.getY();
			
			int id = Jammer.startId;

			for (int x = px - radius; x < (px + (radius)); x = x + spread){
				for (int z = pz - radius; z < (pz + (radius)); z = z + spread){
					
					int low = py - 30, high = py + 30;
					int y = (int) Math.floor(RandomUtils.nextInt(high - low) + low);

					plUpdatePacketWrapper = new WrapperPlayServerEntityTeleport();
					
					plUpdatePacketWrapper.setEntityID(id++);
					plUpdatePacketWrapper.setX(x);
					plUpdatePacketWrapper.setY(y);
					plUpdatePacketWrapper.setZ(z);
					
					pm.sendServerPacket(p, plUpdatePacketWrapper.getHandle(), false);
					
				}
			}
		} catch (Exception e) {
			plugin.getLogger().logp(Level.SEVERE, "JammerUpdatePacket.java", "run()", "uh oh...");
			e.printStackTrace();
		}
		
	}

}
