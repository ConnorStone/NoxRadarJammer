package com.noxpvp.radarjammer;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class RadarListener implements Listener {

	private RadarJammer plugin;
	
	public RadarListener(RadarJammer plugin) {
		this.plugin = plugin;
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
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onProjectileShoot(ProjectileLaunchEvent event) {
		
	}

}
