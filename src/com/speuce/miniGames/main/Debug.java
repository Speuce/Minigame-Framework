package com.speuce.miniGames.main;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Debug implements CommandExecutor{
	private boolean enabled = false;
	private JavaPlugin p;
	private List<Player> players;
	
	public Debug(JavaPlugin p){
		this.p = p;
		this.enabled = false;
		this.players = new ArrayList<Player>();
	}
	public void setEnabled(boolean enabled){
		this.enabled = enabled;
	}
	public void log(String s){
		if(this.enabled){
			this.p.getLogger().log(Level.INFO, s);
		}
		if(!players.isEmpty()){
			logPlayer(ChatColor.GREEN.toString() + "Log: " + ChatColor.RESET.toString() + ChatColor.AQUA.toString() + s);
		}
	}
	public void severeLog(String s){
		if(this.enabled){
			this.p.getLogger().log(Level.SEVERE, s);
		}
		if(!players.isEmpty()){
			logPlayer(ChatColor.RED.toString() + ChatColor.BOLD.toString() + "SEVERE: " + ChatColor.RESET.toString() + ChatColor.AQUA.toString() + s);
		}
	}
	public void addPlayer(Player p){
		if(!this.players.contains(p)){
			this.players.add(p);
		}
	}
	public void removePlayer(Player p){
		if(this.players.contains(p)){
			this.players.remove(p);
		}
	}
	private void logPlayer(String s){
		for(Player p: this.players){
			if(p.isOnline()){
				p.sendMessage(s);
			}else{
				this.players.remove(p);
			}
		}
	}
	@Override
	public boolean onCommand(CommandSender sender, Command arg1, String arg2,
			String[] args) {
		if(arg1.getName().equalsIgnoreCase("debug")){
			if(args.length == 0){
				if(sender instanceof Player){
					Player p = (Player) sender;
					if(this.players.contains(p)){
						this.players.remove(p);
						p.sendMessage(ChatColor.GREEN + "Debug Disabled!");
					}else{
						this.players.add(p);
						p.sendMessage(ChatColor.GREEN + "Debug Enabled");
					}
					return true;
				}else{
					if(this.enabled){
						this.enabled = false;
						sender.sendMessage(ChatColor.GREEN + "Debug Disabled");
					}else{
						this.enabled = true;
						sender.sendMessage(ChatColor.GREEN + "Debug Enabled");
					}
				}
				return true;
			}else{
				sender.sendMessage(ChatColor.RED + "No args needed!");
				return true;
			}
		}
		return false;
	}
	
}
