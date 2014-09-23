package com.noxpvp.radarjammer;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.noxpvp.radarjammer.packet.UpdateMapScrambler;
import com.noxpvp.radarjammer.packet.UpdateTracerScrambler;

public class AsyncUpdateJamTimer extends BukkitRunnable {
	
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Static Fields
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	public static final int minPeriod = 4;
	
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Instance Fields
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	private final RadarJammer plugin;
	public List<String> toUpdate;
	
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Constructors
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	public AsyncUpdateJamTimer(RadarJammer plugin) {
	
		this.plugin = plugin;
	}
	
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Instance Methods
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	public void run() {
	
		final Future<List<Player>> players =
			Bukkit.getServer().getScheduler().callSyncMethod(plugin, plugin.getJammer().getUpdatedLocPlayers);
		
		try {
			if (players.get() != null && !players.get().isEmpty()) {
				for (final Player p : players.get()) {
					new UpdateMapScrambler(p).runTaskLaterAsynchronously(plugin, 1);
					new UpdateTracerScrambler(p).runTaskLaterAsynchronously(plugin, 1);
				}
			}
			
		} catch (final InterruptedException e) {
		} catch (final ExecutionException e) {
		} catch (final Exception e) {
		}
	}
	
}
