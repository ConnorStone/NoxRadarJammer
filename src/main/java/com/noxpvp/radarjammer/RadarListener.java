package com.noxpvp.radarjammer;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class RadarListener implements Listener{

	private RadarJammer plugin;
	
	public RadarListener(RadarJammer plugin) {
		Bukkit.getPluginManager().registerEvents(this, plugin);
		
		this.plugin = plugin;
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent event){
		final Player p = event.getPlayer();
		
		if (p.hasPermission(RadarJammer.PERM_EXEMPT))
			return;
		
		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			
			public void run() {
				p.sendMessage("§f======== §3 §6 §3 §6 §3 §6 §e §f========");				
			}
		}, 2);
		
		plugin.getJammer().addJam(p.getName());
			
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerLogout(PlayerQuitEvent event){
		plugin.getJammer().unJam(event.getPlayer().getName());
	}

}
