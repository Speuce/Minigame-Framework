package com.speuce.sql;

import org.bukkit.ChatColor;

public enum Rank{
	DEFAULT("default", null),
	PLUS("plus", ChatColor.GOLD.toString() + "+" );

	private String name;
	private String display;
	private Rank(String name, String prefix){
		this.name = name;
		this.display = prefix;
	}
	public String getName() {
		return name;
	}
	public String getPrefix() {
		return display;
	}
	public static Rank fromString(String st){
		for(Rank s : Rank.values()){
			if(s.getName().equalsIgnoreCase(st)){
				return s;
			}
		}
		return null;
		
	}
}