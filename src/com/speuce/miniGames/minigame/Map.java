package com.speuce.miniGames.minigame;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;

public class Map {
	private List<String> description;
	private String name;
	private String worldName;
	private boolean worldLoaded;
	private World world;
	private List<Location> locations;
	private String author = "";
	private List<String> data;
	public Map(List<String> description, String name, String worldName, List<Location> locations, List<String> data) {
		super();
		this.description = description;
		this.name = name;
		this.worldName = worldName;
		this.locations = locations;
		this.data = data;
	}
	public Map(World w, String name){
		this(new ArrayList<String>(), name, w.getName(), new ArrayList<Location>(), new ArrayList<String>());
	}
	public List<String> getDescription() {
		return description;
	}
	public void setDescription(List<String> description) {
		this.description = description;
	}
	public String getName() {
		return name;
	}
	public void setAuthor(String auth){
		this.author = auth;
	}
	public String getAuthor(){
		return this.author;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getWorldName() {
		return worldName;
	}
	public void setWorldName(String worldName) {
		this.worldName = worldName;
	}
	public void setLocations(List<Location> locs){
		this.locations = locs;
	}
	public World getWorld() {
		if(!this.worldLoaded || this.world == null){
			World w = Bukkit.getWorld(worldName);
			if(w == null){
				WorldCreator wc = new WorldCreator(this.worldName);
				wc.environment(Environment.NORMAL);
				wc.generateStructures(false);
				wc.type(WorldType.FLAT);
				w = wc.createWorld();
				this.worldLoaded = true;
				this.world = w;
				w.setMonsterSpawnLimit(0);
				w.setAnimalSpawnLimit(0);
				return w;
			}else{
				this.worldLoaded = true;
				this.world = w;
				return w;
			}
		}else{
			return this.world;

		}
	}
	public List<Location> getLocations(){
		return this.locations;
	}
	public Location getLocation(int index){
		if(index <= this.locations.size() + 1){
			return this.locations.get(index);
		}else{
			return this.world.getSpawnLocation();
		}
	}
	public void removeLocation(int index){
		if(index > -1 && this.locations.size() > index){
			this.locations.remove(index);
		}
	}
	public List<String> getData(){
		return this.data;
	}
	public void setData(List<String> data){
		this.data = data;
	}
}
