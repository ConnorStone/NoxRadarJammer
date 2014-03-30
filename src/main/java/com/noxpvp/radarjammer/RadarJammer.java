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

import com.bergerkiller.bukkit.common.internal.CommonPlugin;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.dsh105.holoapi.HoloAPI;
import com.noxpvp.core.NoxCore;

public class RadarJammer extends JavaPlugin {
	
	//Tag
	public final static String PLUGIN_TAG = ChatColor.RED + "Nox" + ChatColor.GOLD + "RadarJammer";
	public final static String VERSION = "v1.2.5";

	//Permissions
	public final static String PERM_NODE = "radarjammer";
	public final static String PERM_EXEMPT = PERM_NODE + ".exempt";
	public final static String PERM_RELOAD = PERM_NODE + ".reload";
	
	//Config nodes
	public final static String NODE_RADIUS = "jammer.radius";
	public final static String NODE_SPREAD = "jammer.spread";
	public final static String NODE_MOVEMENT_TIMER = "jammer.update-period";
	
	//Commands
	public final static List<String> COMMAND_RADAR = Arrays.asList("radarjammer", "rj", "jammer", "radar");
	public final static List<String> ARG_RELOAD = Arrays.asList("reload", "r");
	public final static List<String> ARG_HELP = Arrays.asList("help", "h");
	public final static List<String> ARG_VERSION = Arrays.asList("v", "version");
	
	public static boolean isHoloAPIActive() {
		return holoAPI != null && Bukkit.getPluginManager().isPluginEnabled(holoAPI);
	}
	
	public static boolean isNoxCoreActive() {
		return noxCore != null && Bukkit.getPluginManager().isPluginEnabled(noxCore);
	}
	
	public static boolean isProtocolLibActive() {
		return protocolLib != null && Bukkit.getPluginManager().isPluginEnabled(protocolLib);
	}
	
	public static  boolean isBkCommonLibActive() {
		return bkCommonLib != null && Bukkit.getPluginManager().isPluginEnabled(bkCommonLib);
	}
	
	public final HoloAPI getHoloAPI() {
		return holoAPI;
	}
	
	public final NoxCore getNoxCore() {
		return noxCore;
	}
	
	public final ProtocolLibrary getPL() {
		return protocolLib;
	}
	
	private static RadarJammer instance;
	
	private static HoloAPI holoAPI;
	private static NoxCore noxCore;
	private static ProtocolLibrary protocolLib;
	private static CommonPlugin bkCommonLib;
	
	private FileConfiguration config;
	
	private RadarListener radarListener;
	private AsyncUpdateJamTimer updateTimer;
	private Jammer jammer;
	
	private int asyncPeriod;
	
	public static RadarJammer getInstance(){
		return instance;
	}
	
	public FileConfiguration getRadarConfig(){
		if (config == null)
			config = getConfig();
		
		saveDefaultConfig();
		
		return config;
	}
	
	public Jammer getJammer(){
		return this.jammer;
	}
	
	public AsyncUpdateJamTimer getUpdateJamTimer() {
		return this.updateTimer;
	}

	@Override
	public void onDisable() {
		try {
			config.save(getDataFolder());
		} catch (IOException e) { e.printStackTrace(); }
		
	}

	@Override
	public void onEnable() {
		if (instance != null)
		{
			getLogger().log(Level.SEVERE, "This plugin already has an instance running! Disabling second run.");
			setEnabled(false);
			
			return;
		}
		setInstance(this);
		
		PluginManager pm = Bukkit.getPluginManager();
		pm.addPermission(new Permission(PERM_EXEMPT, "Makes the player exempt from radar jamming", PermissionDefault.OP));
		pm.addPermission(new Permission(PERM_RELOAD, "Allows the player to reload the plugin", PermissionDefault.OP));
		
		{
			Plugin plugin = pm.getPlugin("HoloAPI");
			if (plugin != null && plugin instanceof HoloAPI)
				holoAPI = (HoloAPI) plugin;
		}
		{
			Plugin plugin = pm.getPlugin("NoxCore");
			if (plugin != null && plugin instanceof NoxCore)
				noxCore = (NoxCore) plugin;
		}
		{
			Plugin plugin = pm.getPlugin("ProtocolLib");
			if (plugin != null && plugin instanceof ProtocolLibrary) {
				protocolLib = (ProtocolLibrary) plugin;
			}
		}
		{
			Plugin plugin = pm.getPlugin("BKCommonLib");
			if (plugin != null && plugin instanceof CommonPlugin) {
				bkCommonLib = (CommonPlugin) plugin;
			}
		}

		asyncPeriod = getRadarConfig().getInt(NODE_MOVEMENT_TIMER, 6);
		
		ProtocolManager plM = ProtocolLibrary.getProtocolManager();
		plM.addPacketListener(radarListener = new RadarListener(this, plM));
		
		pm.registerEvents(radarListener, instance);
		jammer = new Jammer(this, asyncPeriod);
		
		
		try {
		    Metrics metrics = new Metrics(this);
		    metrics.start();
		} catch (IOException e) {
			getLogger().log(Level.WARNING, "You can probably ignore this...", e);
		}
		//TODO cool graphs
		
		try {
			updateTimer = new AsyncUpdateJamTimer(getInstance());
			updateTimer.runTaskTimerAsynchronously(instance, 20, asyncPeriod * 20);
		} catch (Exception e) {}
	}

	private void setInstance(RadarJammer radarJammer) {
		if (radarJammer != null)
			RadarJammer.instance = radarJammer;
	}
	
	public void reloadRadarConfig() {
		reloadConfig();
		this.config = getConfig();
		
		this.asyncPeriod = getRadarConfig().getInt(NODE_MOVEMENT_TIMER, 4);
		
		jammer = null;
		jammer = new Jammer(this, asyncPeriod);
		
		ProtocolManager pm = ProtocolLibrary.getProtocolManager();
		
		HandlerList.unregisterAll(radarListener);
		pm.removePacketListener(radarListener);
		
		radarListener = null;
		Bukkit.getPluginManager().registerEvents((radarListener = new RadarListener(this, pm)), instance);
		pm.addPacketListener(radarListener);
		
		if (updateTimer != null)
			this.updateTimer.cancel();
		
		this.updateTimer = new AsyncUpdateJamTimer(getInstance());
		this.updateTimer.runTaskTimerAsynchronously(getInstance(), 0, asyncPeriod * 20);
	}
	
	public void sendHelpMessage(CommandSender sender){
		if (sender instanceof Player){
			sender.sendMessage(PLUGIN_TAG + ChatColor.AQUA + ": These are the available commands");
			sender.sendMessage(PLUGIN_TAG + ChatColor.AQUA + ": /" + COMMAND_RADAR.toString() + " " + ARG_HELP.toString());
			sender.sendMessage(PLUGIN_TAG + ChatColor.AQUA + ":" + ChatColor.GREEN + "         Shows this message");
			sender.sendMessage(PLUGIN_TAG + ChatColor.AQUA + ": /" + COMMAND_RADAR.toString() + " " + ARG_RELOAD.toString());
			sender.sendMessage(PLUGIN_TAG + ChatColor.AQUA + ":" + ChatColor.GREEN + "         Reloads the config");
		} else {
			Logger log = getLogger();
			
			log.log(Level.INFO, PLUGIN_TAG + ": These are the available commands");
			log.log(Level.INFO, PLUGIN_TAG + ": /" + COMMAND_RADAR.toString() + " " + ARG_HELP.toString());
			log.log(Level.INFO, PLUGIN_TAG + ":         Shows this message");
			log.log(Level.INFO, PLUGIN_TAG + ": /" + COMMAND_RADAR.toString() + " " + ARG_VERSION.toString());
			log.log(Level.INFO, PLUGIN_TAG + ":         Checks the plugin version");
			log.log(Level.INFO, PLUGIN_TAG + ": /" + COMMAND_RADAR.toString() + " " + ARG_RELOAD.toString());
			log.log(Level.INFO, PLUGIN_TAG + ":         Reloads the config");
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (COMMAND_RADAR.contains(label.toLowerCase())){
			if (args == null || args.length < 1){
				sendHelpMessage(sender);
				return true;
			}
			String arg = args[0];
			
			if (ARG_RELOAD.contains(arg)){
				if (!sender.hasPermission(PERM_RELOAD)) {
					sender.sendMessage(PLUGIN_TAG + ChatColor.RED + " : You don't have permission -> " + PERM_RELOAD);
					return true;
				}
					
				this.reloadRadarConfig();
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

}
