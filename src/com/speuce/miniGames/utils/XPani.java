package com.speuce.miniGames.utils;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.speuce.stats.StatsManager;

public class XPani extends BukkitRunnable{
	private Player p;
	private int level;
	private int xpb;
	private int xpleft;
	private int levelup = 0;
	private int mult;
	private AniFinish fin;
	public XPani(Player p, int level, int xpbefore, int xp, int mult, AniFinish fin){
		this.p = p;
		this.level = level;
		this.xpb = xpbefore;
		this.xpleft = xp;
		this.mult = mult;
		this.fin = fin;
	}

	@Override
	public void run() {
		int xplevelup = StatsManager.getLevelUpXp(this.level);
		if(p.isOnline() && this.xpleft >= 0){
			if(this.xpb >= xplevelup && this.levelup == 0){
				p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 2F, 1.2F);
				this.level++;
				xplevelup = StatsManager.getLevelUpXp(this.level);
				this.levelup = 30;
				this.xpb = 0;
			}
			if(this.levelup > 0){
				if((this.levelup % 2) == 0){
					Mini.sendActionBar(p, ChatColor.GREEN + ChatColor.BOLD.toString() + "Level Up to Level " + this.level);
				}else{
					Mini.sendActionBar(p, ChatColor.WHITE.toString() + ChatColor.BOLD.toString() + "Level Up to Level " + this.level);
				}
				this.levelup--;
			}else{
				int interval = Math.abs(xplevelup/5);
				if(this.xpb < interval){
					p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2F, 2F);
					Mini.sendActionBar(p, ChatColor.AQUA.toString() + ChatColor.BOLD.toString() +
							"XP Earned - " + ChatColor.GOLD.toString() + ChatColor.BOLD.toString() + 
							this.xpleft + ChatColor.WHITE.toString() + ChatColor.BOLD.toString() + " >>>> " +
							ChatColor.GOLD.toString() + ChatColor.BOLD.toString() + this.xpb +
							ChatColor.RED.toString() + ChatColor.BOLD.toString() + "/" +
							ChatColor.AQUA.toString() + ChatColor.BOLD.toString() + xplevelup + "XP");
				}else if(this.xpb < interval*2){
					p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2F, 2F);
						Mini.sendActionBar(p, ChatColor.AQUA.toString() + ChatColor.BOLD.toString() +
								"XP Earned - " + ChatColor.GOLD.toString() + ChatColor.BOLD.toString() + 
								this.xpleft + ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD.toString() + 
								" >" + ChatColor.WHITE.toString() + ChatColor.BOLD.toString() + ">>> " +
								ChatColor.GOLD.toString() + ChatColor.BOLD.toString() + this.xpb +
								ChatColor.RED.toString() + ChatColor.BOLD.toString() + "/" +
								ChatColor.AQUA.toString() + ChatColor.BOLD.toString() + xplevelup + "XP");
				}else if(this.xpb < interval*3){
					p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2F, 2F);
						Mini.sendActionBar(p, ChatColor.AQUA.toString() + ChatColor.BOLD.toString() +
								"XP Earned - " + ChatColor.GOLD.toString() + ChatColor.BOLD.toString() + 
								this.xpleft + ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD.toString() + 
								" >>" + ChatColor.WHITE.toString() + ChatColor.BOLD.toString() + ">> " +
								ChatColor.GOLD.toString() + ChatColor.BOLD.toString() + this.xpb +
								ChatColor.RED.toString() + ChatColor.BOLD.toString() + "/" +
								ChatColor.AQUA.toString() + ChatColor.BOLD.toString() + xplevelup + "XP");
				}else if(this.xpb < interval*4){
					p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2F, 2F);
						Mini.sendActionBar(p, ChatColor.AQUA.toString() + ChatColor.BOLD.toString() +
								"XP Earned - " + ChatColor.GOLD.toString() + ChatColor.BOLD.toString() + 
								this.xpleft + ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD.toString() + 
								" >>>" + ChatColor.WHITE.toString() + ChatColor.BOLD.toString() + "> " +
								ChatColor.GOLD.toString() + ChatColor.BOLD.toString() + this.xpb +
								ChatColor.RED.toString() + ChatColor.BOLD.toString() + "/" +
								ChatColor.AQUA.toString() + ChatColor.BOLD.toString() + xplevelup + "XP");
				}else{
					p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2F, 2F);
						Mini.sendActionBar(p, ChatColor.AQUA.toString() + ChatColor.BOLD.toString() +
								"XP Earned - " + ChatColor.GOLD.toString() + ChatColor.BOLD.toString() + 
								this.xpleft + ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD.toString() + 
								" >>>> " +
								ChatColor.GOLD.toString() + ChatColor.BOLD.toString() + this.xpb +
								ChatColor.RED.toString() + ChatColor.BOLD.toString() + "/" +
								ChatColor.AQUA.toString() + ChatColor.BOLD.toString() + xplevelup + "XP");
				}
				if(this.xpleft < this.mult && this.xpleft > 0){
					this.xpb+= 1;
					this.xpleft=0;
				}else{
					this.xpb+=mult;
					this.xpleft-=mult;
				}

			}
			
		}else{
			this.fin.onFinish();
			this.cancel();
		}
		
	}
}
