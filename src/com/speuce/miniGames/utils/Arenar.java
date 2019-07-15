package com.speuce.miniGames.utils;

import java.util.Collections;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;

public class Arenar{
	private List<Location> locs;
	private String name;
	private World w;

	public Arenar(String name, List<Location> l, World w){
		this.name = name;
		this.locs = l;
		this.w = w;
	}
	public List<Location> getLocations(){
		return locs;
	}
	public Location getLocation(int i){
		return locs.get(i);
	}
	public void removeLocation(int i){
		this.locs.remove(i);
	}
	public int size(){
		return locs.size();
	}
	public String getName(){
		return this.name;
	}
	public void Shuffle(){
		Collections.shuffle(locs);
	}
	public World getWorld(){
		return this.w;
	}
	
}
