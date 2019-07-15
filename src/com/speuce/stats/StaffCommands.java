package com.speuce.stats;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class StaffCommands implements CommandExecutor{

	private StatsManager man;
	@SuppressWarnings("unused")
	private JavaPlugin p;
	
	public StaffCommands(StatsManager man, JavaPlugin p){
		this.man = man;
		this.p = p;
	}
	
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String arg2,
			String[] args) {
		if(sender instanceof Player){
			Player p = (Player) sender;
			if(cmd.getName().equalsIgnoreCase("gm")){
				if(man.getStaffRank(p.getUniqueId()).getPower() >= 4){
					p.sendMessage(ChatColor.RED + "Sorry, this required staff power 4+");
					return true;
				}
				if(args.length == 0){
					p.sendMessage(ChatColor.RED + "You need to pick a damn gamemode.");
					return true;
				}
				if(args[0].equalsIgnoreCase("c")){
					p.sendMessage(ChatColor.GREEN + "Gamemode set to creative");
					p.setGameMode(GameMode.SURVIVAL);
					return true;
				}else if(args[0].equalsIgnoreCase("a")){
					p.sendMessage(ChatColor.GREEN + "Gamemode set to adventure");
					p.setGameMode(GameMode.ADVENTURE);
					return true;
				}else if(args[0].equalsIgnoreCase("sp")){
					p.sendMessage(ChatColor.GREEN + "Gamemode set to spectator");
					p.setGameMode(GameMode.SPECTATOR);
					return true;
				}else{
					p.sendMessage(ChatColor.GREEN + "Gamemode set to survival");
					p.setGameMode(GameMode.SURVIVAL);
					return true;
				}
			}
		}
		return false;
	}

}
