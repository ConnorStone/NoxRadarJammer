package com.noxpvp.radarjammer;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.noxpvp.radarjammer.packet.UpdateProjectilePLPacket;

public class RadarListener extends PacketAdapter implements Listener {

	private RadarJammer plugin;
	private ProtocolManager pm;
	
	private List<Integer> updating;
	
	public RadarListener(RadarJammer plugin, ProtocolManager pm) {
		super(plugin, PacketType.Play.Server.SPAWN_ENTITY);
		
		this.plugin = plugin;
		this.pm = pm;
		
		this.updating = new ArrayList<Integer>();
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent event){
		final Player p = event.getPlayer();
		
		if (p.hasPermission(RadarJammer.PERM_EXEMPT))
			return;
		
		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			
			public void run() {
				p.sendMessage("§6Enabling the disabling of cheating things... §3 §6 §3 §6 §3 §6 §e ");				
				plugin.getJammer().addJam(p);
			}
		}, 2);
		
			
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerLogout(PlayerQuitEvent event){
		plugin.getJammer().unJam(event.getPlayer().getName());
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onRespawn(PlayerRespawnEvent event) {
		final Player p;
		if ((p = event.getPlayer()) != null)
			Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
				public void run() {
					plugin.getJammer().jamFullRad(p);
				
				}
			}, 5);
	}

	@Override
	public void onPacketSending(PacketEvent arg0) {
		PacketContainer packet = arg0.getPacket();

		Entity e = packet.getEntityModifier(arg0).read(0);
		if (e != null && e instanceof Projectile && !updating.contains(e.getEntityId())) {
			updating.add(e.getEntityId());
			new UpdateProjectilePLPacket(pm, (Projectile) packet.getEntityModifier(arg0).read(0)).runTaskTimer(plugin, 0, 3);
		}
		
		return;
	}

}
