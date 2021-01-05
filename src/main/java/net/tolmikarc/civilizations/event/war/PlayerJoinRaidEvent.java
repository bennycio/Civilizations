/*
 * Copyright (c) 2021-2021 Tolmikarc All Rights Reserved
 */

package net.tolmikarc.civilizations.event.war;

import net.tolmikarc.civilizations.model.Civilization;
import net.tolmikarc.civilizations.war.Raid;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerJoinRaidEvent extends Event {
	private static final HandlerList handlers = new HandlerList();

	private final Raid raid;
	private final Player player;


	public PlayerJoinRaidEvent(Raid raid, Player player) {
		this.raid = raid;
		this.player = player;
	}

	public Raid getRaid() {
		return raid;
	}

	public Civilization getAttacker() {
		return raid.getCivRaiding();
	}

	public Civilization getDefender() {
		return raid.getCivBeingRaided();
	}

	@NotNull
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
