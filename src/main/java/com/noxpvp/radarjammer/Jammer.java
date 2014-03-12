package com.noxpvp.radarjammer;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.apache.commons.lang.math.RandomUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.bergerkiller.bukkit.common.config.FileConfiguration;
import com.bergerkiller.bukkit.common.protocol.CommonPacket;
import com.bergerkiller.bukkit.common.protocol.PacketType;
import com.bergerkiller.bukkit.common.utils.PacketUtil;
import com.bergerkiller.bukkit.common.wrappers.DataWatcher;

public class Jammer{

	public final static int maxSize = 64, maxSpread = 20, minSpread = 2;
	
	private RadarJammer plugin;
	private List<String> jamming;
	
	private int radius, spread, height;
	private JamMode mode;

	public enum JamMode{
		
		INVISIBLE((byte) 0x20),
		CROUCHED((byte) 0x02);
		
		JamMode(byte bit){
			this.bit = bit;
		}
		
		byte bit;
		
		byte getByte(){
			return bit;
		}
	}
	
	public Jammer(RadarJammer plugin) {
		
		this.plugin = plugin;
		this.jamming = new ArrayList<String>();
		
		FileConfiguration config = plugin.getRadarConfig();
		
		this.mode = JamMode.valueOf(config.get(RadarJammer.NODE_MODE, String.class, JamMode.CROUCHED.name()));
		this.radius = config.get(RadarJammer.NODE_RADIUS, Integer.class, 40);
		this.spread = config.get(RadarJammer.NODE_SPREAD, Integer.class, 8);
		
		if (radius > maxSize)
			radius = maxSize;
		if (spread > maxSpread)
			spread = maxSpread;
		else if (spread < minSpread)
			spread = minSpread;
		
		for (Player p : Bukkit.getOnlinePlayers()){
			if (p.hasPermission(RadarJammer.PERM_EXEMPT))
				continue;
			
			String name = p.getName();
			
			if (!jamming.contains(name))
				jamming.add(name);
			
			jam(p);
				
		}
		
	}
	
	public void unJamAll(){
		int amount = (((radius * 2) / spread) * ((radius * 2) / spread));
		int[] ids = new int[amount + 5];
		
		try {
			for (int i = Short.MAX_VALUE, r = 0; i < (amount + Short.MAX_VALUE); i++, r++)
				ids[r] = i;
			
			CommonPacket destroyer = new CommonPacket(PacketType.OUT_ENTITY_DESTROY);
			destroyer.write(PacketType.OUT_ENTITY_DESTROY.entityIds, ids);
			
			PacketUtil.broadcastPacket(destroyer, false);
		} catch (Exception e) {
			plugin.getLogger().logp(Level.SEVERE, "Jammer.java", "unJamAll()", "uh oh...");
			e.printStackTrace();
		}
	}
	
	public void unJam(String name){
		if (jamming.contains(name))
			jamming.remove(name);
	}
	
	public void addJam(String name){
		if (!jamming.contains(name))
			jamming.add(name);
		
		jam(Bukkit.getPlayer(name));
	}
	
	public void jam(Player p){
		String name = p.getName();
		
		if (!p.isOnline()){
			
			if (jamming.contains(name))
				jamming.remove(name);
			
			return;
		} else if (!jamming.contains(name))
			return;
		
		final Location pLoc = p.getLocation();
		 
		int nextId = Short.MAX_VALUE;
		
		int px = (int) pLoc.getX();
		int py = (int) pLoc.getY();
		int pz = (int) pLoc.getZ();
		
		CommonPacket jammerPacket,
				metaPacket;
		
		DataWatcher dw = new DataWatcher();
		dw.set(0, (byte) 0);
		dw.set(12, (int) 0);

/*		WatchableObjectRef dw2 = new WatchableObjectRef();
		dw2.set(0, mode.getByte());
		dw2.set(6, (float) RandomUtils.nextInt(20 - 12) + 12);
		
		List<DataWatcher> invisMeta = new ArrayList<DataWatcher>(Arrays.asList(dw2));*///FIXME
		
		try {
			for (int x = px - radius; x < (px + (radius)); x = x + spread){
				for (int z = pz - radius; z < (pz + (radius)); z = z + spread){
					
					height = (int) ((mode == JamMode.CROUCHED)? -2 : Math.floor(RandomUtils.nextInt((py + 40) - 5) + 5));
					
					jammerPacket = new CommonPacket(PacketType.OUT_ENTITY_SPAWN_NAMED);
					metaPacket = new CommonPacket(PacketType.OUT_ENTITY_METADATA);
					
					Player random = Bukkit.getOnlinePlayers()[RandomUtils.nextInt(Bukkit.getOnlinePlayers().length)];
					
					jammerPacket.write(PacketType.OUT_ENTITY_SPAWN_NAMED.entityId, nextId);
					jammerPacket.write(PacketType.OUT_ENTITY_SPAWN_NAMED.profile, random.getName());
					jammerPacket.write(PacketType.OUT_ENTITY_SPAWN_NAMED.x, (int) (x + RandomUtils.nextInt(3 - 1)) * 32);
					jammerPacket.write(PacketType.OUT_ENTITY_SPAWN_NAMED.y, (int) height * 32);
					jammerPacket.write(PacketType.OUT_ENTITY_SPAWN_NAMED.z, (int) (z + RandomUtils.nextInt(3 - 1)) * 32);
					jammerPacket.write(PacketType.OUT_ENTITY_SPAWN_NAMED.dataWatcher, dw);
					
/*					metaPacket.write(PacketType.OUT_ENTITY_METADATA.entityId, nextId);
					metaPacket.write(PacketType.OUT_ENTITY_METADATA.watchedObjects, invisMeta);*///FIXME
				 
					PacketUtil.sendPacket(p, jammerPacket, false);
//					PacketUtil.sendPacket(p, invisPacket, false);//FIXME
					
					nextId++;
				}
			}
		} catch (Exception e) {
			plugin.getLogger().logp(Level.SEVERE, "Jammer.java", "jam(org.bukkit.entity.Player)", "uh oh...");
			e.printStackTrace();
		}
		
	}
	

}
