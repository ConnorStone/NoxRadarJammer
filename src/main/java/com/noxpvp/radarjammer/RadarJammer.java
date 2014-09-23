package com.noxpvp.radarjammer;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.dsh105.holoapi.HoloAPI;
import com.noxpvp.core.NoxCore;

public class RadarJammer extends JavaPlugin {
	
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Static Fields
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	// Tag
	public final static String PLUGIN_TAG = ChatColor.RED + "Nox" + ChatColor.GOLD + "RadarJammer";
	public final static String VERSION = "v1.3.4 Beta";
	
	// Permissions
	public final static String PERM_NODE = "radarjammer";
	public final static String PERM_EXEMPT = PERM_NODE + ".exempt";
	public final static String PERM_RELOAD = PERM_NODE + ".reload";
	
	// Config nodes
	public final static String NODE_RADIUS = "jammer.radius";
	public final static String NODE_SPREAD = "jammer.spread";
	public final static String NODE_MOVEMENT_TIMER = "jammer.update-period";
	private final static String NODE_VOXELRADAR = "jammer.stop-voxelmap-radar";
	private final static String NODE_VOXELCAVE = "jammer.stop-voxelmap-cave";
	
	// Commands
	public final static List<String> COMMAND_RADAR = Arrays.asList("radarjammer", "rj", "jammer", "radar");
	public final static List<String> ARG_RELOAD = Arrays.asList("reload", "r");
	public final static List<String> ARG_HELP = Arrays.asList("help", "h");
	public final static List<String> ARG_VERSION = Arrays.asList("v", "version");
	
	// Var
	private static RadarJammer instance;
	private static HoloAPI holoAPI;
	private static NoxCore noxCore;
	private static ProtocolLibrary protocolLib;
	
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Instance Fields
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	private FileConfiguration config;
	
	private RadarListener radarListener;
	private AsyncUpdateJamTimer updateTimer;
	private Jammer jammer;
	private int asyncPeriod;
	
	private boolean stopVoxelRadar;
	private boolean stopVoxelCave;
	
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Static Methods
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	public static RadarJammer getInstance() {
	
		return instance;
	}
	
	public static boolean isHoloAPIActive() {
	
		return holoAPI != null && Bukkit.getPluginManager().isPluginEnabled(holoAPI);
	}
	
	public static boolean isNoxCoreActive() {
	
		return noxCore != null && Bukkit.getPluginManager().isPluginEnabled((Plugin) noxCore);
	}
	
	public static boolean isProtocolLibActive() {
	
		return protocolLib != null && Bukkit.getPluginManager().isPluginEnabled(protocolLib);
	}
	
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Instance Methods
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	public Jammer getJammer() {
	
		return jammer;
	}
	
	public final NoxCore getNoxCore() {
	
		return noxCore;
	}
	
	public final ProtocolLibrary getPL() {
	
		return protocolLib;
	}
	
	public FileConfiguration getRadarConfig() {
	
		if (config == null) {
			config = getConfig();
		}
		
		saveDefaultConfig();
		
		return config;
	}
	
	public AsyncUpdateJamTimer getUpdateJamTimer() {
	
		return updateTimer;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
	
		if (COMMAND_RADAR.contains(label.toLowerCase())) {
			if (args == null || args.length < 1) {
				sendHelpMessage(sender);
				return true;
			}
			final String arg = args[0];
			
			if (ARG_RELOAD.contains(arg)) {
				if (!sender.hasPermission(PERM_RELOAD)) {
					sender.sendMessage(PLUGIN_TAG + ChatColor.RED + " : You don't have permission -> " + PERM_RELOAD);
					return true;
				}
				
				reloadRadarConfig();
				sender.sendMessage(PLUGIN_TAG + ChatColor.GREEN + ": Reloaded");
				
				return true;
			} else if (ARG_HELP.contains(arg)) {
				sendHelpMessage(sender);
				
				return true;
			} else if (ARG_VERSION.contains(arg)) {
				sender.sendMessage(PLUGIN_TAG + ChatColor.GREEN + ": " + VERSION);
				
				return true;
			}
		}
		
		return false;
		
	}
	
	@Override
	public void onDisable() {
	
		getInstance().saveConfig();
		
	}
	
	@Override
	public void onEnable() {
	
		if (instance != null) {
			getLogger().log(Level.SEVERE, "This plugin already has an instance running! Disabling second run.");
			setEnabled(false);
			
			return;
		}
		setInstance(this);
		
		final PluginManager pm = Bukkit.getPluginManager();
		pm.addPermission(new Permission(PERM_EXEMPT, "Makes the player exempt from radar jamming", PermissionDefault.OP));
		pm.addPermission(new Permission(PERM_RELOAD, "Allows the player to reload the plugin", PermissionDefault.OP));
		
		{
			final Plugin plugin = pm.getPlugin("HoloAPI");
			if (plugin != null && plugin instanceof HoloAPI) {
				holoAPI = (HoloAPI) plugin;
			}
		}
		{
			final Plugin plugin = pm.getPlugin("NoxCore");
			if (plugin != null && plugin instanceof NoxCore) {
				noxCore = (NoxCore) plugin;
			}
		}
		{
			final Plugin plugin = pm.getPlugin("ProtocolLib");
			if (plugin != null && plugin instanceof ProtocolLibrary) {
				protocolLib = (ProtocolLibrary) plugin;
			}
		}
		
		asyncPeriod = getRadarConfig().getInt(NODE_MOVEMENT_TIMER, 6);
		stopVoxelRadar = getRadarConfig().getBoolean(NODE_VOXELRADAR, true);
		stopVoxelCave = getRadarConfig().getBoolean(NODE_VOXELCAVE, true);
		
		final ProtocolManager plM = ProtocolLibrary.getProtocolManager();
		plM.addPacketListener(radarListener = new RadarListener(this, plM, stopVoxelRadar, stopVoxelCave));
		
		pm.registerEvents(radarListener, instance);
		jammer = new Jammer(this, asyncPeriod);
		
		try {
			final Metrics metrics = new Metrics(this);
			metrics.start();
		} catch (final IOException e) {
			getLogger().log(Level.WARNING, "You can probably ignore this...", e);
		}
		// TODO cool graphs
		
		try {
			updateTimer = new AsyncUpdateJamTimer(getInstance());
			updateTimer.runTaskTimerAsynchronously(instance, 20, asyncPeriod * 20);
		} catch (final Exception e) {
		}
	}
	
	public void reloadRadarConfig() {
	
		reloadConfig();
		config = getConfig();
		
		asyncPeriod = getRadarConfig().getInt(NODE_MOVEMENT_TIMER, 4);
		stopVoxelRadar = getRadarConfig().getBoolean(NODE_VOXELRADAR, true);
		stopVoxelCave = getRadarConfig().getBoolean(NODE_VOXELCAVE, true);
		
		jammer = null;
		jammer = new Jammer(this, asyncPeriod);
		
		final ProtocolManager pm = ProtocolLibrary.getProtocolManager();
		
		HandlerList.unregisterAll(radarListener);
		pm.removePacketListener(radarListener);
		
		radarListener = null;
		Bukkit.getPluginManager().registerEvents(
			radarListener = new RadarListener(this, pm, stopVoxelRadar, stopVoxelCave), instance);
		pm.addPacketListener(radarListener);
		
		if (updateTimer != null) {
			updateTimer.cancel();
		}
		
		updateTimer = new AsyncUpdateJamTimer(getInstance());
		updateTimer.runTaskTimerAsynchronously(getInstance(), 0, asyncPeriod * 20);
	}
	
	public void sendHelpMessage(CommandSender sender) {
	
		if (sender instanceof Player) {
			sender.sendMessage(PLUGIN_TAG + ChatColor.AQUA + ": These are the available commands");
			sender.sendMessage(PLUGIN_TAG + ChatColor.AQUA + ": /" + COMMAND_RADAR.toString() + " "
				+ ARG_HELP.toString());
			sender.sendMessage(PLUGIN_TAG + ChatColor.AQUA + ":" + ChatColor.GREEN + "         Shows this message");
			sender.sendMessage(PLUGIN_TAG + ChatColor.AQUA + ": /" + COMMAND_RADAR.toString() + " "
				+ ARG_RELOAD.toString());
			sender.sendMessage(PLUGIN_TAG + ChatColor.AQUA + ":" + ChatColor.GREEN + "         Reloads the config");
		} else {
			final Logger log = getLogger();
			
			log.log(Level.INFO, PLUGIN_TAG + ": These are the available commands");
			log.log(Level.INFO, PLUGIN_TAG + ": /" + COMMAND_RADAR.toString() + " " + ARG_HELP.toString());
			log.log(Level.INFO, PLUGIN_TAG + ":         Shows this message");
			log.log(Level.INFO, PLUGIN_TAG + ": /" + COMMAND_RADAR.toString() + " " + ARG_VERSION.toString());
			log.log(Level.INFO, PLUGIN_TAG + ":         Checks the plugin version");
			log.log(Level.INFO, PLUGIN_TAG + ": /" + COMMAND_RADAR.toString() + " " + ARG_RELOAD.toString());
			log.log(Level.INFO, PLUGIN_TAG + ":         Reloads the config");
		}
	}
	
	private void setInstance(RadarJammer radarJammer) {
	
		if (radarJammer != null) {
			RadarJammer.instance = radarJammer;
		}
	}
	
}
