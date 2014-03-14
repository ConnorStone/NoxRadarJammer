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
import com.noxpvp.radarjammer.Jammer.JamMode;

public class JammerPacket extends AsyncTask{

	RadarJammer plugin;
	
	int nextId;
	int radius;
	int spread;
	int height;
	
	JamMode mode;
	
	CommonPacket jammerPacket,
	metaPacket;
	
	String[] names;
	final Player p;
	final int px;
	final int py;
	final int pz;
	
	public JammerPacket(Player p, int radius, int spread, JamMode mode, String[] names) {
		super("RadarJammer's haksor slaying thread", Thread.MIN_PRIORITY);
		
		this.plugin = RadarJammer.getInstance();
		
		nextId = Short.MAX_VALUE;
		
		this.p = p;
		
		this.radius = radius;
		this.spread = spread;
		this.mode = mode;
		
		this.names = names;
				
		{
			Location pLoc = p.getLocation();
			px = (int) pLoc.getX();
			py = (int) pLoc.getY() + 40;
			pz = (int) pLoc.getZ();
		}
		
	}

	public void run() {
		
		DataWatcher dw = new DataWatcher();
		dw.set(0, (byte) mode.getByte());
		dw.set(6, (float) RandomUtils.nextInt(20 - 12) + 12);
		dw.set(12, (int) 0);
		
		try {
			for (int x = px - radius; x < (px + (radius)); x = x + spread){
				for (int z = pz - radius; z < (pz + (radius)); z = z + spread){
					
					//Each jammer has random height, from 0 - (player Y + 40)
					height = (int) ((mode == JamMode.CROUCHED)? -2 : Math.floor(RandomUtils.nextInt((py) - 5) + 5));
					
					jammerPacket = new CommonPacket(PacketType.OUT_ENTITY_SPAWN_NAMED);
					
					String random = names[RandomUtils.nextInt(names.length)];
					
					jammerPacket.write(PacketType.OUT_ENTITY_SPAWN_NAMED.entityId, nextId);
					jammerPacket.write(PacketType.OUT_ENTITY_SPAWN_NAMED.profile, random);
					jammerPacket.write(PacketType.OUT_ENTITY_SPAWN_NAMED.x, (int) (x + RandomUtils.nextInt(3 - 1)) * 32);
					jammerPacket.write(PacketType.OUT_ENTITY_SPAWN_NAMED.y, (int) height * 32);
					jammerPacket.write(PacketType.OUT_ENTITY_SPAWN_NAMED.z, (int) (z + RandomUtils.nextInt(3 - 1)) * 32);
					jammerPacket.write(PacketType.OUT_ENTITY_SPAWN_NAMED.dataWatcher, dw);
				 
					PacketUtil.sendPacket(p, jammerPacket, false);
					
					nextId++;
				}
			}
		} catch (Exception e) {
			plugin.getLogger().logp(Level.SEVERE, "Jammer.java", "jam(org.bukkit.entity.Player)", "uh oh...");
			e.printStackTrace();
		}
		
	}

}
