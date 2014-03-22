package com.noxpvp.radarjammer.events;

import org.bukkit.Location;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveFullBlockEvent extends PlayerEvent{

	private Location to, from;
	
	public PlayerMoveFullBlockEvent(PlayerMoveEvent event) {
		super(event.getPlayer());
		
		this.from = event.getFrom();
		this.to = event.getTo();
	}
	
	public Location getFrom(){
		return from;
	}
	
	public Location getTo(){
		return to;
	}
	
	private static final HandlerList handlers = new HandlerList();

	@Override
    public HandlerList getHandlers() {
    	
        return handlers;
    }
    
    public static HandlerList getHandlerList() {

		return handlers;
	}

}
