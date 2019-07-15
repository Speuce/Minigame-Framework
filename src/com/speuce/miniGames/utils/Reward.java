package com.speuce.miniGames.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.speuce.stats.Stats;
import com.speuce.stats.StatsManager;

public class Reward implements CommandExecutor{
	StatsManager man;
	Plugin p;
	
	public Reward(StatsManager man, Plugin p){
		this.man = man;
		this.p = p;
	}
	
	
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String arg2,
			String[] args) {
		if(cmd.getName().equalsIgnoreCase("reward")){
			if(sender instanceof Player){
				Player pl = (Player) sender;
				if(man.hasPlayerStats(pl.getUniqueId()) && man.getPlayerStats(pl.getUniqueId()).getStaffRank().getPower() >= 7){
					if(args.length == 2){
						Player p = Bukkit.getPlayer(args[0]);
						if(p == null || !p.isOnline()){
							pl.sendMessage(ChatColor.RED + "Player isn't around!");
							return true;
						}else{
							int i = Integer.parseInt(args[1]);
							this.rewardAnimation(p, this.man.getPlayerStats(p.getUniqueId()).getBlips(),i, getNullFin());
							this.man.setBlips(p.getUniqueId(), this.man.getPlayerStats(p.getUniqueId()).getBlips() + i);
							pl.sendMessage(ChatColor.GREEN + "Did it...!");
							return true;
						}
					}else{
						pl.sendMessage(ChatColor.RED + "/reward <player> <amount>");
						return true;
					}
				}else{
					pl.sendMessage(ChatColor.RED + "LOL NICE TRY BUD!");
					return true;
				}
			}
		}else if(cmd.getName().equalsIgnoreCase("exp")){
			if(sender instanceof Player){
				Player pl = (Player) sender;
				if(man.hasPlayerStats(pl.getUniqueId()) && man.getPlayerStats(pl.getUniqueId()).getStaffRank().getPower() >= 7){
					if(args.length == 2){
						Player p = Bukkit.getPlayer(args[0]);
						if(p == null || !p.isOnline()){
							pl.sendMessage(ChatColor.RED + "Player isn't around!");
							return true;
						}else{
							int i = Integer.parseInt(args[1]);
							this.XpAnimation(p, this.man.getPlayerStats(p.getUniqueId()).getXp(),i, getNullFin());
							this.man.giveExp(p.getUniqueId(), i);
							pl.sendMessage(ChatColor.GREEN + "Did it...!");
							return true;
						}
					}else{
						pl.sendMessage(ChatColor.RED + "/exp <player> <amount>");
						return true;
					}
				}else{
					pl.sendMessage(ChatColor.RED + "LOL NICE TRY BUD!");
					return true;
				}
			}
		}
		return false;
	}
	private AniFinish getNullFin(){
		return new AniFinish(){

			@Override
			public void onFinish() {
				// TODO Auto-generated method stub
				
			}
			
		};
	}
	public void rewardAnimation(Player p, int before,int amount, AniFinish an){
			RewardAni r;
			if(amount < 200){
				r = new RewardAni(p, amount, before, 1, an);
			}else if(amount < 750){
				r = new RewardAni(p, amount, before, 2, an);
			}else{
				r = new RewardAni(p, amount, before, 3, an);
			}
			if(amount < 50){
				r.runTaskTimer(this.p, 1L, 2L);
			}else{
				r.runTaskTimer(this.p, 1L, 1L);
			}
	}
	public void XpAnimation(Player p, int before,int xp, AniFinish an){
			Stats stats = this.man.getPlayerStats(p.getUniqueId());
			XPani r;
			if(xp < 300){
				r = new XPani(p, stats.getLevel(), before, xp, 1, an);
			}else if(xp < 750){
				r = new XPani(p, stats.getLevel(), before, xp, 2, an);
			}else if(xp < 1600){
				r = new XPani(p, stats.getLevel(), before, xp, 3, an);
			}else{
				r = new XPani(p, stats.getLevel(), before, xp, 4, an);
			}

			if(xp >= 50){
				r.runTaskTimer(this.p, 1L, 1L);
			}else{
				r.runTaskTimer(this.p, 1L, 2L);
			}
	}

}
