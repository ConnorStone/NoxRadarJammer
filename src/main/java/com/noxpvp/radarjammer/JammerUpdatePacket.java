package com.noxpvp.radarjammer;

import java.util.logging.Level;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.bergerkiller.bukkit.common.AsyncTask;
import com.bergerkiller.bukkit.common.protocol.CommonPacket;
import com.bergerkiller.bukkit.common.protocol.PacketType;
import com.bergerkiller.bukkit.common.utils.PacketUtil;

public class JammerUpdatePacket extends AsyncTask {
	RadarJammer plugin;
	
	private CommonPacket updatePacket;
	private int nextId;	
	
	private final Player p;
	private final Vector dif;

	private int radius;
	private int spread;
	
	public JammerUpdatePacket(Player p, Vector dif, int radius, int spread) {
		super("RadarJammer - " + p.getName(), Thread.MIN_PRIORITY);
		
		this.plugin = RadarJammer.getInstance();
		
		this.nextId = Jammer.startId;
		
		this.p = p;
		this.dif = dif;
		
		this.radius = radius;
		this.spread = spread;
		
	}

	public void run() {
		try {
			
			int amount = (((radius * 2) + 1) / spread);
			amount = amount * amount;
			
			int[] ids = new int[amount];
			
			for (int i = nextId, r = 0; i < (amount + nextId); i++, r++)
				ids[r] = i;
			
			for (int id : ids) {
				updatePacket = new CommonPacket(PacketType.OUT_ENTITY_MOVE);
				
				updatePacket.write(PacketType.OUT_ENTITY_MOVE.entityId, id);
				updatePacket.write(PacketType.OUT_ENTITY_MOVE.dx, (byte) (dif.getX() * 32));
				updatePacket.write(PacketType.OUT_ENTITY_MOVE.dy, (byte) (dif.getY() * 32));
				updatePacket.write(PacketType.OUT_ENTITY_MOVE.dz, (byte) (dif.getZ() * 32));
				
				PacketUtil.sendPacket(p, updatePacket, false);
				
			}
			
		} catch (Exception e) {
			plugin.getLogger().logp(Level.SEVERE, "Jammer.java", "jam(org.bukkit.entity.Player)", "uh oh...");
			e.printStackTrace();
		}
		
	}

}
