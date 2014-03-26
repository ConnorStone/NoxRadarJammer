package com.noxpvp.radarjammer;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Vector;

import com.noxpvp.radarjammer.events.PlayerChunkMoveEvent;

public class RadarListener implements Listener{

	private RadarJammer plugin;
	private boolean usePerBlockUpdate;
	
	public RadarListener(RadarJammer plugin, boolean usePerBlockUpdate) {
		Bukkit.getPluginManager().registerEvents(this, plugin);
		
		this.plugin = plugin;
		this.usePerBlockUpdate = usePerBlockUpdate;
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerMove(PlayerMoveEvent event){
		Player p = event.getPlayer();
		
		if (p == null)
			return;
		
		if (!usePerBlockUpdate) {			
			if ((event.getFrom().getChunk() == event.getTo().getChunk()))
				return;

			if (p.hasPermission(RadarJammer.PERM_EXEMPT))
				return;

			Bukkit.getPluginManager().callEvent(new PlayerChunkMoveEvent(event));
			
		} else {
			if (((int) event.getTo().getBlockX() == (int) event.getFrom().getBlockX()) &&
					((int) event.getTo().getBlockZ() == (int) event.getFrom().getBlockZ()) &&
					((int) event.getTo().getBlockY() == (int) event.getFrom().getBlockY())){
				
				return;
			}
			
			if (p.hasPermission(RadarJammer.PERM_EXEMPT))
				return;
			
			Vector fromLoc = new Vector(event.getFrom().getBlockX(), event.getFrom().getBlockY(), event.getFrom().getBlockZ()),
					toLoc = new Vector(event.getTo().getBlockX(), event.getTo().getBlockY(), event.getTo().getBlockZ());
			
			plugin.getJammer().jamFullRadUpdate(p, toLoc.subtract(fromLoc));
			
		}
		
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onChunkMove(PlayerChunkMoveEvent event) {
		
		plugin.getJammer().jamFullRad(event.getPlayer());
		
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
