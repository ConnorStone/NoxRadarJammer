package com.noxpvp.radarjammer;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

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
import org.bukkit.event.player.PlayerTeleportEvent;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.noxpvp.radarjammer.packet.UpdateProjectilePLPacket;

public class RadarListener extends PacketAdapter implements Listener {
	
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Instance Fields
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	private final RadarJammer plugin;
	private final ProtocolManager pm;
	private final String voxelMapStopper;
	
	private final List<Integer> updating;
	
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Constructors
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	public RadarListener(RadarJammer plugin, ProtocolManager pm, boolean stopVoxelRadar, boolean stopVoxelCave) {
	
		super(plugin, PacketType.Play.Server.SPAWN_ENTITY);
		
		this.plugin = plugin;
		this.pm = pm;
		
		voxelMapStopper =
			new StringBuilder("§0.").append(stopVoxelRadar ? " §3 §6 §3 §6 §3 §6 §e" : "")
				.append(stopVoxelCave ? " §3 §6 §3 §6 §3 §6 §d " : "").toString();
		
		updating = new ArrayList<Integer>();
	}
	
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Instance Methods
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	@Override
	public void onPacketSending(PacketEvent arg0) {
	
		final PacketContainer packet = arg0.getPacket();
		
		final Entity e = packet.getEntityModifier(arg0).read(0);
		if (e != null && e instanceof Projectile && !updating.contains(e.getEntityId())) {
			updating.add(e.getEntityId());
			new UpdateProjectilePLPacket(pm, (Projectile) packet.getEntityModifier(arg0).read(0)).runTaskTimer(plugin,
				0, 3);
		}
		
		return;
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent event) {
	
		final Player p = event.getPlayer();
		
		if (p.hasPermission(RadarJammer.PERM_EXEMPT)) {
			return;
		}
		
		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			
			public void run() {
			
				try {
					final PacketContainer message = new PacketContainer(PacketType.Play.Server.CHAT);
					message.getChatComponents().write(0, WrappedChatComponent.fromChatMessage(voxelMapStopper)[0]);
					
					pm.sendServerPacket(p, message);
				} catch (final Exception e) {
					getPlugin().getLogger().logp(Level.SEVERE, "RadarListener.java",
						"onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent event)", "Oh nos...");
					e.printStackTrace();
				}
				
				plugin.getJammer().addJam(p);
			}
		}, 2);
		
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerLogout(PlayerQuitEvent event) {
	
		plugin.getJammer().unJam(event.getPlayer().getName());
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerTeleport(PlayerTeleportEvent event) {
	
		if (!event.getFrom().getWorld().equals(event.getTo().getWorld())
			|| event.getFrom().distance(event.getTo()) > 50) {
			
			plugin.getJammer().sendMapScramble(event.getPlayer());
			plugin.getJammer().sendFauxTracers(event.getPlayer());
		}
	}
	
	public void onRespawn(PlayerRespawnEvent event) {
	
		final Player p;
		if ((p = event.getPlayer()) != null) {
			Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
				
				public void run() {
				
					plugin.getJammer().sendMapScramble(p);
					
				}
			}, 5);
		}
	}
	
}
