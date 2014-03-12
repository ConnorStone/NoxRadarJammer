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

public class RadarListener implements Listener{

	private RadarJammer plugin;
	
	public RadarListener(RadarJammer plugin) {
		Bukkit.getPluginManager().registerEvents(this, plugin);
		
		this.plugin = plugin;
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerMove(PlayerMoveEvent event){
		
		//Return if player hasn't moved a full chunk
		if ((event.getFrom().getChunk() == event.getTo().getChunk()))
			return;
		
		PlayerChunkMoveEvent chunkEvent = new PlayerChunkMoveEvent(event);
		Bukkit.getPluginManager().callEvent(chunkEvent);
		
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerChunkMove(PlayerChunkMoveEvent event){
		Player p = event.getPlayer();
		
		if (p.hasPermission(RadarJammer.PERM_EXEMPT))
			return;
		
		plugin.getJammer().jam(p);
	}
	
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
