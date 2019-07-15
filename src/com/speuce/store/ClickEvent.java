package com.speuce.store;

import org.bukkit.entity.Player;

import com.speuce.stats.Stats;

public interface ClickEvent {
	public void onClick(Player p, Stats s, boolean right, boolean shift);
}
