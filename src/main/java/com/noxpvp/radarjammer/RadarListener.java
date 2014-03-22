package com.noxpvp.radarjammer;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.noxpvp.radarjammer.events.PlayerChunkMoveEvent;
import com.noxpvp.radarjammer.events.PlayerMoveFullBlockEvent;

public class RadarListener implements Listener{

	private RadarJammer plugin;
	
	public RadarListener(RadarJammer plugin) {
		Bukkit.getPluginManager().registerEvents(this, plugin);
		
		this.plugin = plugin;
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerMove(PlayerMoveEvent event){
		
/*		if (((int) event.getTo().getBlockX() == (int) event.getFrom().getBlockX()) &&
			((int) event.getTo().getBlockZ() == (int) event.getFrom().getBlockZ()) &&
			((int) event.getTo().getBlockY() == (int) event.getFrom().getBlockY())){
			
			return;
		}
		
		Bukkit.getPluginManager().callEvent(new PlayerMoveFullBlockEvent(event));*/
		
		if ((event.getFrom().getChunk() == event.getTo().getChunk()))
			return;
		
		Bukkit.getPluginManager().callEvent(new PlayerChunkMoveEvent(event));
		
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerChunkMove(PlayerChunkMoveEvent event){
		Player p = event.getPlayer();
		
		if (p.hasPermission(RadarJammer.PERM_EXEMPT))
			return;
		
		plugin.getJammer().jamFullRad(p);
	}
	
	/*@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerFullBlockMove(PlayerMoveFullBlockEvent event){
		Player p = event.getPlayer();
		
		if (p.hasPermission(RadarJammer.PERM_EXEMPT))
			return;
		
		plugin.getJammer().jamBox(p);
	}*/
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent event){
		Player p = event.getPlayer();
		
		if (p.hasPermission(RadarJammer.PERM_EXEMPT))
			return;
		
		plugin.getJammer().addJam(p.getName());
			
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerLogout(PlayerQuitEvent event){
		plugin.getJammer().unJam(event.getPlayer().getName());
	}

}
