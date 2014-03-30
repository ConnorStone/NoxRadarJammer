package com.noxpvp.radarjammer.packet;

import java.util.logging.Level;

import org.apache.commons.lang.math.RandomUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.bergerkiller.bukkit.common.protocol.CommonPacket;
import com.bergerkiller.bukkit.common.protocol.PacketType;
import com.bergerkiller.bukkit.common.utils.PacketUtil;

import com.noxpvp.radarjammer.Jammer;
import com.noxpvp.radarjammer.RadarJammer;

public class UpdateBKPacket extends BukkitRunnable {
	private RadarJammer plugin;
	
	private CommonPacket updatePacket;
	
	private int nextId;	
	
	private final Player p;
	private final Location pLoc;

	private int radius;
	private int spread;
	
	public UpdateBKPacket(Player p) {
		
		this.plugin = RadarJammer.getInstance();
		
		this.nextId = Jammer.startId;
		
		this.p = p;
		this.pLoc = p.getLocation();
		
		this.radius = plugin.getJammer().getRadius();
		this.spread = plugin.getJammer().getSpread();
		
	}

	public void run() {
		try {
			
			final int px = (int) pLoc.getX(), pz = (int) pLoc.getZ(), py = (int) pLoc.getY();
			
			int amount = (((radius * 2) + 1) / spread);
			amount = (amount * amount);
			
			int[] ids = new int[amount];
			
			for (int i = nextId, r = 0; i < (amount + nextId); i++, r++)
				ids[r] = i;
			
			int i = 0;
			for (int x = px - radius; x < (px + (radius)) && i < amount; x = x + spread){
				for (int z = pz - radius; z < (pz + (radius)) && i < amount; z = z + spread){
					
					int id = ids[i];
					int low = py - 30, high = py + 30;
					int y = (int) Math.floor(RandomUtils.nextInt(high - low) + low);
					
					updatePacket = new CommonPacket(PacketType.OUT_ENTITY_TELEPORT);
					
					updatePacket.write(PacketType.OUT_ENTITY_TELEPORT.entityId, id);
					updatePacket.write(PacketType.OUT_ENTITY_TELEPORT.x, (int) (x * 32));
					updatePacket.write(PacketType.OUT_ENTITY_TELEPORT.y, (int) (y * 32));
					updatePacket.write(PacketType.OUT_ENTITY_TELEPORT.z, (int) (z * 32));
					
					PacketUtil.sendPacket(p, updatePacket, false);
					
					i++;
				}
			}
		} catch (Exception e) {
			plugin.getLogger().logp(Level.SEVERE, "JammerUpdatePacket.java", "run()", "uh oh...");
			e.printStackTrace();
		}
		
	}

}
