package com.speuce.miniGames.minigame;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class WorldManager implements Listener, CommandExecutor{
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String arg2, String[] args) {
		if(cmd.getName().equalsIgnoreCase("w")){
			if(sender instanceof Player){
				Player p = (Player) sender;
				if(p.isOp() || p.hasPermission("world")){
					if(args.length != 0 && args.length <= 2){
						if(args[0].equalsIgnoreCase("tp") || args[0].equalsIgnoreCase("teleport")){
							if(args.length == 2){
								World w = Bukkit.getWorld(args[1]);
								if(w != null){
									p.teleport(w.getSpawnLocation());
									p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 2.0F, 0.6F);
									return true;
								}else{
									p.sendMessage(ChatColor.RED + "World doesnt exsist");
									return true;
								}
							}else{
								p.sendMessage(ChatColor.RED + "Invalid args");
								return true;
							}
							
							
						}else if(args[0].equalsIgnoreCase("list")){
							String s = "";
							for(World w : Bukkit.getServer().getWorlds()){
								s += ChatColor.GREEN.toString() + w.getName() + ChatColor.YELLOW + "(" + ChatColor.AQUA + w.getPlayers().size() + ChatColor.YELLOW + ")" + " ";
							}
							p.sendMessage(s);
							return true;
						}else if(args[0].equalsIgnoreCase("create")){
							if(args.length == 2){
								World w = Bukkit.getWorld(args[1]);
								if(w == null){
									WorldCreator wc = new WorldCreator(args[1]);
									wc.environment(Environment.NORMAL);
									wc.generateStructures(false);
									wc.type(WorldType.FLAT);
									wc.createWorld();
									p.sendMessage(ChatColor.GREEN + "World '" + args[1] + "' Created!");
									return true;
								}else{
									p.sendMessage(ChatColor.RED + "World already exists.");
									return true;
								}
							}else{
								p.sendMessage(ChatColor.RED + "Invalid args");
								return true;
							}
						}else if(args[0].equalsIgnoreCase("unload")){
							if(args.length == 2){
								if(this.worldLoaded(args[1])){
									World w = Bukkit.getWorld(args[1]);
									if(w != null){
										Bukkit.getServer().unloadWorld(w, true);
										p.sendMessage(ChatColor.GREEN + "Unloaded world: " + args[1]);
										return true;
									}else{
										p.sendMessage(ChatColor.RED + "World: " + args[1] + " Doesn't exsist!");
										return true;
									}									
								}else{
									p.sendMessage(ChatColor.GREEN + "World: " + args[1] + " Isn't loaded!");
									return true;
								}
							}else{
								p.sendMessage(ChatColor.RED + "Invalid args");
								return true;
							}
						}else{
							p.sendMessage(ChatColor.RED + "Invalid args");
							return true;
						}
					}else{
						p.sendMessage(ChatColor.RED + "Invalid args");
						return true;
					}
				}else{
					p.sendMessage(ChatColor.RED + "You don't have perms to do that");
					return true;
				}
			}
		}
		return false;
	}
	private boolean worldLoaded(String name){
		
		for(World w: Bukkit.getServer().getWorlds()){
			if(w.getName().equals(name)){
				return true;
			}
		}
		return false;
		
	}

}
