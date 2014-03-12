package com.noxpvp.radarjammer;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;

import com.bergerkiller.bukkit.common.Common;
import com.bergerkiller.bukkit.common.PluginBase;
import com.bergerkiller.bukkit.common.config.FileConfiguration;
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
	
	//Commands
	public final static List<String> COMMAND_RADAR = Arrays.asList("radarjammer", "rj", "jammer", "radar");
	public final static List<String> ARG_RELOAD = Arrays.asList("reload", "r");
	public final static List<String> ARG_HELP = Arrays.asList("help", "h");
	
	private static RadarJammer instance;
	
	private static FileConfiguration config;
	private RadarListener radarListener;
	private Jammer jammer;
	
	public static RadarJammer getInstance(){
		return instance;
	}
	
	public FileConfiguration getRadarConfig(){
		if (config == null)
			config = new FileConfiguration(this, "config.yml");
		
		if (config.exists()) {
			config.load();
		} else {
			config.setHeader("Nox RadarJammer || Authors: Connor Stone AKA bbcsto13, Chris krier AKA coaster3000");
			
			config.setHeader(NODE_RADIUS, "MAX: " + Jammer.maxSize + ". The square radius around the player to add jamming entitys");
			config.set(NODE_RADIUS, 40);
			
			config.setHeader(NODE_SPREAD, "MAX: " + Jammer.maxSpread + " MIN: " + Jammer.minSpread + ". The distance each jammer is from one another. ie: higher spread = less jammers");
			config.set(NODE_SPREAD, 8);

			config.setHeader(NODE_MODE, "Modes: " + JamMode.INVISIBLE.name() + ", " + JamMode.CROUCHED.name() + ". Note: crouched mode will force jammers at Y -2");
			config.set(NODE_MODE, JamMode.INVISIBLE.name());
			
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
		
		radarListener = new RadarListener(this);
		jammer = new Jammer(this);
	}

	private void setInstance(RadarJammer radarJammer) {
		if (radarJammer != null)
			RadarJammer.instance = radarJammer;
	}
	
	@Override
	public void reloadConfig() {
		config.load();
		
		jammer.unJamAll();
		jammer = new Jammer(this);
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
				
				return true;
			} else if (ARG_HELP.contains(arg))
				sendHelpMessage(sender);
			
			return true;
		}
		
		return false;
				
	}

}
