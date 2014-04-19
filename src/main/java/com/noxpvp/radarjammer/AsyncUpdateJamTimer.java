package com.noxpvp.radarjammer;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.noxpvp.radarjammer.packet.UpdatePLPacket;

public class AsyncUpdateJamTimer extends BukkitRunnable {

	public static final int minPeriod = 4;

	private final RadarJammer plugin;
	public List<String> toUpdate;

	
	public AsyncUpdateJamTimer(RadarJammer plugin) {
		
		this.plugin = plugin;
	}
	
	public void run() {
		Future<List<Player>> players = Bukkit.getServer().getScheduler().callSyncMethod(plugin, plugin.getJammer().getUpdatedLocPlayers);
		
		try {
			if (players.get() != null && !players.get().isEmpty())
				for (Player p : players.get())
					new UpdatePLPacket(p).runTaskAsynchronously(plugin);

		}
		catch (InterruptedException e) {}
		catch (ExecutionException e) {}
		catch (Exception e) {}
	}

}
