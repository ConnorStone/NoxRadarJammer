package com.noxpvp.radarjammer.packet;

import java.util.logging.Level;

import org.apache.commons.lang.math.RandomUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.bergerkiller.bukkit.common.protocol.CommonPacket;
import com.bergerkiller.bukkit.common.protocol.PacketType;
import com.bergerkiller.bukkit.common.utils.PacketUtil;
import com.bergerkiller.bukkit.common.wrappers.DataWatcher;
import com.noxpvp.radarjammer.Jammer;
import com.noxpvp.radarjammer.RadarJammer;

public class JammerBKPacket extends BukkitRunnable {
	private RadarJammer plugin;
	
	private CommonPacket jammerPacket;
	private DataWatcher dw;
	
	private int nextId;

	private int radius;
	private int spread;
	private int height;		
	
	private String[] names;
	private final Player p;
	private final int px;
	private final int py;
	private final int pz;
	
	public JammerBKPacket(Player p, int radius, int spread, String[] names) {
		
		this.plugin = RadarJammer.getInstance();
		
		dw = new DataWatcher();
		dw.set(0, (byte) 0x20);
		dw.set(6, (float) RandomUtils.nextInt(20 - 4) + 4);
		dw.set(12, (int) 0);

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
					
					String random = names[RandomUtils.nextInt(names.length)];
					
					jammerPacket = new CommonPacket(com.bergerkiller.bukkit.common.protocol.PacketType.OUT_ENTITY_SPAWN_NAMED);
					
					jammerPacket.write(PacketType.OUT_ENTITY_SPAWN_NAMED.entityId, ++nextId);
					jammerPacket.write(PacketType.OUT_ENTITY_SPAWN_NAMED.profile, random);
					jammerPacket.write(PacketType.OUT_ENTITY_SPAWN_NAMED.x, (int) x * 32);
					jammerPacket.write(PacketType.OUT_ENTITY_SPAWN_NAMED.y, (int) height * 32);
					jammerPacket.write(PacketType.OUT_ENTITY_SPAWN_NAMED.z, (int) z * 32);
					jammerPacket.write(PacketType.OUT_ENTITY_SPAWN_NAMED.dataWatcher, dw);
					
					PacketUtil.sendPacket(p, jammerPacket, false);	
				}
			}
		} catch (Exception e) {
			plugin.getLogger().logp(Level.SEVERE, "Jammer.java", "jam(org.bukkit.entity.Player)", "uh oh...");
			e.printStackTrace();
		}
		
	}

}
