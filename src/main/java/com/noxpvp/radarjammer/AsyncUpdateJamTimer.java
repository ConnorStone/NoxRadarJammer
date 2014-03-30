package com.noxpvp.radarjammer;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.noxpvp.radarjammer.packet.UpdateBKPacket;
import com.noxpvp.radarjammer.packet.UpdatePLPacket;

public class AsyncUpdateJamTimer extends BukkitRunnable {

	public static final int minPeriod = 4;

	private final RadarJammer plugin;
	public List<String> toUpdate;

	private boolean usePL;
	
	public AsyncUpdateJamTimer(RadarJammer plugin) {
		
		this.plugin = plugin;
		this.usePL = !RadarJammer.isBkCommonLibActive();
	}
	
	public void run() {
		Future<List<Player>> players = Bukkit.getServer().getScheduler().callSyncMethod(plugin, plugin.getJammer().getUpdatedLocPlayers);
		
		try {
			if (players.get() != null && !players.get().isEmpty())
				if (usePL) {
					for (Player p : players.get())
						new UpdatePLPacket(p).runTaskAsynchronously(plugin);
				} else {
					for (Player p : players.get())
						new UpdateBKPacket(p).runTaskAsynchronously(plugin);
				}
		}
		catch (InterruptedException e) {}
		catch (ExecutionException e) {}
		catch (Exception e) {}
	}

}
