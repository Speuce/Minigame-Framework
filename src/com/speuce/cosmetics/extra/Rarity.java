package com.speuce.cosmetics.extra;

import org.bukkit.ChatColor;

public enum Rarity {
	COMMON(ChatColor.GRAY, "Common"),
	UNCOMMON(ChatColor.BLUE, "Uncommon"),
	RARE(ChatColor.LIGHT_PURPLE, "Rare"),
	LEGENDARY(ChatColor.AQUA, "Legendary"),
	KRYPTED(ChatColor.RED, "Krypted"),
	SPECIAL(ChatColor.DARK_AQUA, "Special");
	
	private ChatColor color;
	private String name;
	private Rarity(ChatColor color, String name){
		this.color = color;
		this.name = name;
	}
	public ChatColor getColor(){
		return this.color;
	}
	public String getName(){
		return this.name;
	}
	
	
}
