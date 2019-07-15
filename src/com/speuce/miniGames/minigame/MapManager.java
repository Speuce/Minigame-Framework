package com.speuce.miniGames.minigame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;


public class MapManager implements CommandExecutor{
	private YamlConfiguration conf;
	private Map currentMap;
	private Plugin plu;
	private MiniGame mg;
	private HashMap<String, Map> maps;
	public MapManager(MiniGame mg, Plugin plu){
		this.conf = mg.getConf();
		this.plu = plu;
		this.mg = mg;
		this.maps = new HashMap<String, Map>();
	}
	
	public Map getCurrentMap(){
		return this.currentMap;
	}
	private void addMap(Map ma){
		List<String> tmaps = this.conf.getStringList("maps");
		if(!tmaps.contains(ma.getWorldName())){
			tmaps.add(ma.getWorldName());
		}
		List<String> map = new ArrayList<String>();
		for(Location l : ma.getLocations()){
			map.add(this.LocToString(l));
		}
		if(!this.conf.contains(ma.getWorldName())){
			this.conf.createSection(ma.getWorldName());
		}
		this.conf.set(ma.getWorldName() + ".desc", ma.getDescription());
		this.conf.set(ma.getWorldName() + ".name", ma.getName());
		this.conf.set(ma.getWorldName() + ".author", ma.getAuthor());
		this.conf.set(ma.getWorldName() + ".locs", map);
		this.conf.set(ma.getWorldName() + ".data", ma.getData());
		this.conf.set("maps", tmaps);
		
	}
	private void addLocInMap(Location place, String map){
		if(this.conf.getStringList("maps").contains(map)){
			List<String> before = this.conf.getStringList(map + ".locs");
			before.add(this.LocToString(place));
			this.conf.set(map + ".locs", before);
			this.mg.saveConfig();
		}
	}

	private void removeMap(String map){
		if(this.conf.getStringList("maps").contains(map)){
			List<String> before = this.conf.getStringList("maps");
			before.remove(map);
			this.conf.set(map, null);
			this.conf.set("maps", before);
			this.mg.saveConfig();
		}
	}
	public void load(){
		BukkitRunnable br = new BukkitRunnable(){

			@Override
			public void run() {
				if(!conf.contains("maps")){
					conf.createSection("maps");
				}
				List<String> st = conf.getStringList("maps");
				if(st.size() == 0){
					System.out.println("No map found!");
					return;
				}
				Random r = new Random();
				int i = r.nextInt(st.size());
				String get = st.get(i);
				String name = conf.getString(get + ".name");
				plu.getLogger().log(Level.INFO, "Picked map: " + name);
				String author = conf.getString(get + ".author");
				List<String> description = conf.getStringList(get + ".desc");
				List<String> locs = conf.getStringList(get + ".locs");
				List<Location> loc = new ArrayList<Location>();
				List<String> data = conf.getStringList(get + ".data");
				Map m = new Map(description,name, get, loc, data);
				m.setAuthor(author);
				World w = m.getWorld();
				for(String sr: locs){
					loc.add(LocFromString(sr, w));
				}
				m.setLocations(loc);
				currentMap = m;
				
			}
			
		};
		br.runTask(this.plu);

	}
	private String LocToString(Location l){
		String toAdd = new String();
		toAdd += l.getBlockX() + ":";
		toAdd += l.getBlockY() + ":";
		toAdd += l.getBlockZ() + ":";
		toAdd += l.getYaw() + ":";
		toAdd += l.getPitch();
		return toAdd;
	}
	private Location LocFromString(String sr, World w){
		String[] ss = sr.split(":");
		int x = Integer.parseInt(ss[0]);
		int y = Integer.parseInt(ss[1]);
		int z = Integer.parseInt(ss[2]);
		Float yaw = Float.parseFloat(ss[3]);
		Float pitch = Float.parseFloat(ss[4]);
		return new Location(w,x,y,z,yaw,pitch);
	}
	private Map getMapp(String map){
		if(this.maps.containsKey(map)){
			return this.maps.get(map);
		}
		if(this.conf.getStringList("maps").contains(map)){
			String name = conf.getString(map + ".name");
			plu.getLogger().log(Level.INFO, "Loaded map: " + name);
			String author = conf.getString(map + ".author");
			List<String> description = conf.getStringList(map + ".desc");
			List<String> locs = conf.getStringList(map + ".locs");
			List<String> data = conf.getStringList(map + ".data");
			List<Location> loc = new ArrayList<Location>();
			Map m = new Map(description,name, map, loc, data);
			m.setAuthor(author);
			World w = m.getWorld();
			for(String sr: locs){
				loc.add(LocFromString(sr, w));
			}
			m.setLocations(loc);
			return m;
		}else{
			System.out.print(ChatColor.RED + map + " Error: map doesn't exsist!");
			return null;
		}
	}
	private void setMap(Map m){
		for(Player p: Bukkit.getOnlinePlayers()){
			if(p.getWorld() != m.getWorld()){
				p.teleport(m.getWorld().getSpawnLocation());
			}
		}
		this.mg.onMapChange(this.getCurrentMap(), m);
		this.currentMap = m;
	}
	
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String arg2, String[] args) {
		if(cmd.getName().equalsIgnoreCase("map")){
			if(sender instanceof Player){
				Player p = (Player) sender;
				if(p.isOp() || p.hasPermission("map")){
					if(args.length == 1){
						World w = p.getWorld();
						String s = w.getName();
							if(args[0].equalsIgnoreCase("add")){
							if(this.conf.getStringList("maps").contains(s)){
								this.addLocInMap(p.getLocation(), s);
								p.sendMessage(ChatColor.GREEN + "Added spot on world: " + s + " for MiniGame: " + this.mg.getName());
								return true;
							}else{
								p.sendMessage(ChatColor.RED + "World " + s + " is not a map for the minigame: " + this.mg.getName());
								return true;
							}
						}else if(args[0].equalsIgnoreCase("remove")){
							if(this.conf.getStringList("maps").contains(s)){
								this.removeMap(s);
								p.sendMessage(ChatColor.GREEN + "Removed world: " + s + " for MiniGame: " + this.mg.getName());
								return true;
							}else{
								p.sendMessage(ChatColor.RED + "World " + s + " is not a map for the minigame: " + this.mg.getName());
								return true;
							}
						}else{
							showHelp(p);
							return true;
						}
						
						
						
					}else if(args.length == 2){
						if(args[0].equalsIgnoreCase("set")){
							World w = p.getWorld();
							String s = w.getName();
							if(!this.conf.getStringList("maps").contains(s)){
								this.addMap(new Map(w, args[1]));
								p.sendMessage(ChatColor.GREEN + "Added world: " + s + " for MiniGame: " + this.mg.getName());
								return true;
							}else{
								p.sendMessage(ChatColor.RED + "World is already a map for the minigame: " + this.mg.getName());
								return true;
							}
						}else if(args[0].equalsIgnoreCase("loc")){
							String s = args[1];
							World w = p.getWorld();
							String name = w.getName();
							int i;
							if(this.conf.getStringList("maps").contains(name)){
								try{
									i = Integer.parseInt(s);
									i -= 1;
								}catch(NumberFormatException e){
									p.sendMessage(ChatColor.RED + "Dat ain't no numba!");
									return true;
								}
								Map m = this.getMapp(name);
								if(m == null){
									p.sendMessage(ChatColor.RED + "Map is null..");
									return true;
								}
								List<Location> l = m.getLocations();
								if(l.size() > i && i > -1){
									p.teleport(l.get(i));
									p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 2.0F, 0.6F);
									return true;
								}else{
									p.sendMessage(ChatColor.RED + "That map only has: " + l.size() + " locs!");
									return true;
								}
							}else{
								p.sendMessage(ChatColor.RED + "The world you are in is not considered a map!");
								return true;
							}
						}else if(args[0].equalsIgnoreCase("dloc")){
							String s = args[1];
							World w = p.getWorld();
							int i;
							if(this.conf.getStringList("maps").contains(w.getName())){
								try{
									i = Integer.parseInt(s);
									i -= 1;
								}catch(NumberFormatException e){
									p.sendMessage(ChatColor.RED + "Dat ain't no numba!");
									return true;
								}
								Map m = this.getMapp(w.getName());
								if(m == null){
									p.sendMessage(ChatColor.RED + "Map is null..");
									return true;
								}
								List<Location> l = m.getLocations();
								if(l.size() > i && i > -1){
									l.remove(i);
									m.setLocations(l);
									this.addMap(m);;
									p.sendMessage(ChatColor.GREEN + "Removed location: " + i + " from map: " + m.getName());
									return true;
								}else{
									p.sendMessage(ChatColor.RED + "That map only has: " + l.size() + " locs!");
									return true;
								}
							}else{
								p.sendMessage(ChatColor.RED + "The world you are in is not considered a map!");
								return true;
							}
							
						}else if(args[0].equalsIgnoreCase("pick")){
							String s = args[1];
							if(this.conf.getStringList("maps").contains(s)){
								Map m = this.getMapp(s);
								if(m != null){
									Bukkit.broadcastMessage(ChatColor.GREEN + s + ChatColor.GOLD.toString() + " Has been picked as the new map!");
									this.setMap(m);
									return true;
								}else{
									p.sendMessage(ChatColor.RED + "Map is null..");
									return true;
								}
							}else{
								sender.sendMessage(ChatColor.RED + "Map: " + s + " is not a map for Minigame: " + mg.getName());
								return true;
							}
						}else{
							showHelp(p);
							return true;
						}
					}else{
						showHelp(p);
						return true;
					}
				}else{
					p.sendMessage(ChatColor.RED + "You don't have perms to do that");
					return true;
				}
			}else{
				sender.sendMessage("You aint no playa!");
				return true;
			}

		}
		return false;
	}
	private void showHelp(Player p){
		p.sendMessage(ChatColor.RED + "/map set <name> - Register a map");
		p.sendMessage(ChatColor.RED + "/map remove <name> - Un-Register a map");
		p.sendMessage(ChatColor.RED + "/map add - Add a location to map");
		p.sendMessage(ChatColor.RED + "/map loc <int> - Teleport to map location (Starts at 1)");
		p.sendMessage(ChatColor.RED + "/map dloc <int> - Teleport to map location (Starts at 1)");
	}

}
