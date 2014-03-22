package com.noxpvp.radarjammer;

import java.util.logging.Level;

import org.apache.commons.lang.math.RandomUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.bergerkiller.bukkit.common.AsyncTask;
import com.bergerkiller.bukkit.common.protocol.CommonPacket;
import com.bergerkiller.bukkit.common.protocol.PacketType;
import com.bergerkiller.bukkit.common.utils.PacketUtil;
import com.bergerkiller.bukkit.common.wrappers.DataWatcher;
import com.dsh105.holoapi.util.TagIdGenerator;
import com.noxpvp.radarjammer.Jammer.JamMode;

public class JammerPacket extends AsyncTask {
	private static int Ids = 0;

	RadarJammer plugin;
	
	private CommonPacket jammerPacket;
	private JamMode mode;
	private int nextId;

	private int radius;
	private int spread;
	private int height;		
	
	private String[] names;
	private final Player p;
	private final int px;
	private final int py;
	private final int pz;
	
	public JammerPacket(Player p, int radius, int spread, JamMode mode, String[] names) {
		super("RadarJammer - " + p.getName(), Thread.MIN_PRIORITY);
		
		if (Ids <= 0) {
			if (RadarJammer.isHoloAPIActive())
				Ids = TagIdGenerator.nextId(500);
			else if (RadarJammer.isNoxCoreActive())
				Ids = com.noxpvp.core.packet.PacketUtil.getNewEntityId(500);
			else
				Ids = Short.MAX_VALUE;
		}
		
		
		this.plugin = RadarJammer.getInstance();
		
		DataWatcher dw = new DataWatcher();
		dw.set(0, mode.getByte());
		dw.set(6, (float) RandomUtils.nextInt(20 - 4) + 4);
		dw.set(12, (int) 0);
		
		jammerPacket = new CommonPacket(PacketType.OUT_ENTITY_SPAWN_NAMED);
		jammerPacket.write(PacketType.OUT_ENTITY_SPAWN_NAMED.dataWatcher, dw);
		
		this.p = p;
		this.nextId = Ids;
		
		this.radius = radius;
		this.spread = spread;
		this.mode = mode;
		this.names = names;		
		
		Location pLoc = p.getLocation();
		px = (int) pLoc.getX();
		py = (int) pLoc.getY() + 40;
		pz = (int) pLoc.getZ();
		
	}

	public void run() {
		try {
		
			for (int x = px - radius; x < (px + (radius)); x = x + spread){
				for (int z = pz - radius; z < (pz + (radius)); z = z + spread){
					
					//Each jammer has random height, from 0 - (player Y + 40)
					height = (int) ((mode == JamMode.CROUCHED)? -2 : Math.floor(RandomUtils.nextInt((py) - 10) + 10));
					//Random actual player names, from players on the server
					String random = names[RandomUtils.nextInt(names.length)];
					
					jammerPacket.write(PacketType.OUT_ENTITY_SPAWN_NAMED.entityId, ++nextId);
					jammerPacket.write(PacketType.OUT_ENTITY_SPAWN_NAMED.profile, random);
					jammerPacket.write(PacketType.OUT_ENTITY_SPAWN_NAMED.x, (int) (x + RandomUtils.nextInt(2)) * 32);
					jammerPacket.write(PacketType.OUT_ENTITY_SPAWN_NAMED.y, (int) height * 32);
					jammerPacket.write(PacketType.OUT_ENTITY_SPAWN_NAMED.z, (int) (z + RandomUtils.nextInt(2)) * 32);
				 
					PacketUtil.sendPacket(p, jammerPacket, false);
					
				}
			}
		} catch (Exception e) {
			plugin.getLogger().logp(Level.SEVERE, "Jammer.java", "jam(org.bukkit.entity.Player)", "uh oh...");
			e.printStackTrace();
		}
		
	}

}
