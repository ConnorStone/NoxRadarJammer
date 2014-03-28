package com.noxpvp.radarjammer;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.bergerkiller.bukkit.common.AsyncTask;

public class AsyncUpdateJamTimer extends AsyncTask {

	public static final int minPeriod = 4;

	private final int period;
	private final RadarJammer plugin;
	public List<String> toUpdate;
	
	public AsyncUpdateJamTimer(RadarJammer plugin, int secondPeriod) {
		super("NoxRadarJammer: " + AsyncUpdateJamTimer.class.getName());
		
		this.period = secondPeriod * 1000;
		this.plugin = plugin;
	}
	
	public void run() {
		Future<List<Player>> players = Bukkit.getServer().getScheduler().callSyncMethod(plugin, plugin.getJammer().getUpdatedLocPlayers);
		
		try {
			if (players.get() != null && !players.get().isEmpty())
				for (Player p : players.get())
					new JammerUpdatePacket(p).start();
		}
		catch (InterruptedException e) {}
		catch (ExecutionException e) {}
		catch (Exception e) {}
		
		if (!Bukkit.isPrimaryThread())
			sleep(period);
	}

}
