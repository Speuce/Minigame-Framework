package com.speuce.stats;

import org.bukkit.ChatColor;

public enum StaffRank {
	NONE("none", 0, null),
	BUILDER("builder", 4, ChatColor.DARK_GREEN.toString() + "B"),
	OWNER("owner", 10, ChatColor.DARK_RED.toString() + ChatColor.BOLD.toString() + "O");

	private String name;
	private int power;
	private String tag;
	private StaffRank(String name, int power, String tag){
		this.name = name;
		this.power = power;
		this.tag = tag;
	}
	public String getName() {
		return name;
	}
	public int getPower() {
		return power;
	}
	public String getTag() {
		return tag;
	}
	public static StaffRank fromString(String st){
		for(StaffRank s : StaffRank.values()){
			if(s.getName().equalsIgnoreCase(st)){
				return s;
			}
		}
		return null;
		
	}
}
