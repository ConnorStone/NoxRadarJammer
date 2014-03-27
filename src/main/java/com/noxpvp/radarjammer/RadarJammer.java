package com.noxpvp.radarjammer;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import com.bergerkiller.bukkit.common.Common;
import com.bergerkiller.bukkit.common.PluginBase;
import com.bergerkiller.bukkit.common.config.FileConfiguration;
import com.bergerkiller.bukkit.common.metrics.Metrics;
import com.dsh105.holoapi.HoloAPI;
import com.noxpvp.core.NoxCore;
import com.noxpvp.radarjammer.Jammer.JamMode;

public class RadarJammer extends PluginBase{
	
	//Tag
	public final static String PLUGIN_TAG = ChatColor.RED + "Nox" + ChatColor.GOLD + "RadarJammer";

	//Permissions
	public final static String PERM_NODE = "radarjammer";
	public final static String PERM_EXEMPT = PERM_NODE + ".exempt";
	public final static String PERM_RELOAD = PERM_NODE + ".reload";
	
	//Config nodes
	public final static String NODE_RADIUS = "jammer.radius";
	public final static String NODE_SPREAD = "jammer.spread";
	public final static String NODE_MODE = "jammer.mode";
	public final static String NODE_PER_BLOCK_UPDATE = "jammer.per-block-update";
	
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
	
	public final HoloAPI getHoloAPI() {
		return holoAPI;
	}
	
	public final NoxCore getNoxCore() {
		return noxCore;
	}
	
	private static RadarJammer instance;
	
	private static HoloAPI holoAPI;
	private static NoxCore noxCore;
	
	private static FileConfiguration config;
	private RadarListener radarListener;
	private Jammer jammer;
	
	private boolean perBlockUpdate;
	
	public static RadarJammer getInstance(){
		return instance;
	}
	
	public FileConfiguration getRadarConfig(){
		if (config == null)
			config = new FileConfiguration(this, "config.yml");
		
		if (config.exists()) {
			config.load();
		} else {
			config.setHeader("Nox RadarJammer || Authors: Connor Stone AKA bbcsto13, Chris krier AKA coaster3000\nhttp://dev.bukkit.org/bukkit-plugins/radarjammer/ \n http://Noxpvp.com/");
			
			config.setHeader(NODE_RADIUS, "MAX: " + Jammer.maxSize + ". The square radius around the player to add jamming entitys");
			config.set(NODE_RADIUS, 40);
			
			config.setHeader(NODE_SPREAD, "MAX: " + Jammer.maxSpread + " MIN: " + Jammer.minSpread + ". The distance each jammer is from one another. ie: higher spread = less jammers");
			config.set(NODE_SPREAD, 8);

			config.setHeader(NODE_MODE, "Modes: " + JamMode.INVISIBLE.name() + ", " + JamMode.CROUCHED.name() + ". Note: crouched mode will force jammers at Y -2 and will only be useful for combating minimaps at low levels");
			config.set(NODE_MODE, JamMode.INVISIBLE.name());
			
			config.setHeader(NODE_PER_BLOCK_UPDATE, "Should we update the fake entity every time a player moves 1 block?");
			config.set(NODE_PER_BLOCK_UPDATE, Boolean.FALSE);
			
			config.save();
		}
		
		return config;
	}
	
	public Jammer getJammer(){
		return this.jammer;
	}

	@Override
	public void disable() {
		config.save();
		
	}

	@Override
	public void enable() {
		if (instance != null)
		{
			log(Level.SEVERE, "This plugin already has an instance running! Disabling second run.");
			setEnabled(false);
			
			return;
		}
		setInstance(this);
		
		PluginManager pm = Bukkit.getPluginManager();
		pm.addPermission(new Permission(PERM_EXEMPT, "Makes the player exempt from radar jamming", PermissionDefault.OP));
		pm.addPermission(new Permission(PERM_RELOAD, "Allows the player to reload the plugin", PermissionDefault.OP));
		
		perBlockUpdate = getRadarConfig().get(NODE_PER_BLOCK_UPDATE, Boolean.class, Boolean.FALSE);
		
		radarListener = new RadarListener(this, perBlockUpdate);
		jammer = new Jammer(this);
		
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
		
		Metrics.initialize(this);
		//TODO cool graphs
	}

	private void setInstance(RadarJammer radarJammer) {
		if (radarJammer != null)
			RadarJammer.instance = radarJammer;
	}
	
	@Override
	public void reloadConfig() {
		config.load();
		
		this.perBlockUpdate = getRadarConfig().get(NODE_PER_BLOCK_UPDATE, Boolean.class);
		
		jammer.unJamAll();
		jammer = new Jammer(this);
		
		HandlerList.unregisterAll(radarListener);
		radarListener = null;
		radarListener = new RadarListener(getInstance(), perBlockUpdate);
	}

	public int getMinimumLibVersion() {
		return Common.VERSION;
	}
	
	public void sendHelpMessage(CommandSender sender){
		if (sender instanceof Player){
			sender.sendMessage(PLUGIN_TAG + ChatColor.AQUA + ": These are the available commands");
			sender.sendMessage(PLUGIN_TAG + ChatColor.AQUA + ": /" + COMMAND_RADAR.toString() + " " + ARG_HELP.toString());
			sender.sendMessage(PLUGIN_TAG + ChatColor.AQUA + ":" + ChatColor.GREEN + "         Shows this message");
			sender.sendMessage(PLUGIN_TAG + ChatColor.AQUA + ": /" + COMMAND_RADAR.toString() + " " + ARG_RELOAD.toString());
			sender.sendMessage(PLUGIN_TAG + ChatColor.AQUA + ":" + ChatColor.GREEN + "         Reloads the config");
		} else {
			log(Level.INFO, PLUGIN_TAG + ": These are the available commands");
			log(Level.INFO, PLUGIN_TAG + ": /" + COMMAND_RADAR.toString() + " " + ARG_HELP.toString());
			log(Level.INFO, PLUGIN_TAG + ":         Shows this message");
			log(Level.INFO, PLUGIN_TAG + ": /" + COMMAND_RADAR.toString() + " " + ARG_RELOAD.toString());
			log(Level.INFO, PLUGIN_TAG + ":         Reloads the config");
		}
	}

	@Override
	public boolean command(CommandSender sender, String command, String[] args) {
		if (COMMAND_RADAR.contains(command.toLowerCase())){
			if (args == null || args.length < 1){
				sendHelpMessage(sender);
				return true;
			}
			String arg = args[0];
			
			if (ARG_RELOAD.contains(arg)){
				this.reloadConfig();
				sender.sendMessage(PLUGIN_TAG + ChatColor.GREEN + ": Reloaded");
				
				return true;
			} else if (ARG_HELP.contains(arg)) {
				sendHelpMessage(sender);
				
				return true;
			} else if (ARG_VERSION.contains(arg)) {
				sender.sendMessage(PLUGIN_TAG + ChatColor.GREEN + ": " + getVersion());
				
				return true;
			}
		}
		
		return false;
				
	}

}
