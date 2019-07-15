package com.speuce.miniGames.utils;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class RewardAni extends BukkitRunnable{
	private Player p;
	private int amt;
	private int counter;
	private int arrow;
	private int starting;
	private int mult;
	private boolean toCancel = false;
	private AniFinish fin;
	public RewardAni(Player p, int amt, int starting, int mult, AniFinish fin){
		this.p = p;
		this.amt = amt;
		this.counter = 0;
		this.arrow = 0;
		this.starting = starting;
		this.mult = mult;
		this.fin = fin;
	}
	@Override
	public void run() {
		if(this.counter <= this.amt && this.p.isOnline()){
			if((this.counter % 20) == 0){
				if(this.arrow <= 3){
					this.arrow++;
				}else{
					this.arrow = 0;
				}

			}
			if(this.arrow == 1){
				Mini.sendActionBar(p, ChatColor.GREEN.toString() + ChatColor.BOLD.toString() +
						"Blips Earned - " + ChatColor.GOLD.toString() + ChatColor.BOLD.toString() + 
						(this.amt - this.counter + ChatColor.DARK_RED.toString() + ChatColor.BOLD.toString() +
								" >" + ChatColor.WHITE.toString() + ChatColor.BOLD.toString() + ">> " +
								ChatColor.GOLD.toString() + ChatColor.BOLD.toString() + (starting + counter) 
								+ ChatColor.RED.toString() + ChatColor.BOLD.toString() + " - Your Blips"));
			}else if(this.arrow == 2){
				Mini.sendActionBar(p, ChatColor.GREEN.toString() + ChatColor.BOLD.toString() +
						"Blips Earned - " + ChatColor.GOLD.toString() + ChatColor.BOLD.toString() + 
						(this.amt - this.counter + ChatColor.DARK_RED.toString() + ChatColor.BOLD.toString() +
								" >>" + ChatColor.WHITE.toString() + ChatColor.BOLD.toString() + "> " +
								ChatColor.GOLD.toString() + ChatColor.BOLD.toString() + (starting + counter) 
								+ ChatColor.RED.toString() + ChatColor.BOLD.toString() + " - Your Blips"));
			}else if(this.arrow == 3){
				Mini.sendActionBar(p, ChatColor.GREEN.toString() + ChatColor.BOLD.toString() +
						"Blips Earned - " + ChatColor.GOLD.toString() + ChatColor.BOLD.toString() + 
						(this.amt - this.counter + ChatColor.DARK_RED.toString() + ChatColor.BOLD.toString() +
								" >>> " +
								ChatColor.GOLD.toString() + ChatColor.BOLD.toString() + (starting + counter) 
								+ ChatColor.RED.toString() + ChatColor.BOLD.toString() + " - Your Blips"));
			}else{
				Mini.sendActionBar(p, ChatColor.GREEN.toString() + ChatColor.BOLD.toString() +
						"Blips Earned - " + ChatColor.GOLD.toString() + ChatColor.BOLD.toString() + 
						(this.amt - this.counter + ChatColor.WHITE.toString() + ChatColor.BOLD.toString() +
								" >>> " +
								ChatColor.GOLD.toString() + ChatColor.BOLD.toString() + (starting + counter) 
								+ ChatColor.RED.toString() + ChatColor.BOLD.toString() + " - Your Blips"));
			}
			if(this.toCancel){
				p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 2F, 1.5F);
				this.fin.onFinish();
				this.cancel();
			}
			if((this.amt - this.counter) < this.mult){
				this.counter = this.amt;
				this.toCancel = true;
			}else{
				this.counter+=this.mult;
			}
			p.playSound(p.getLocation(), Sound.BLOCK_DISPENSER_DISPENSE, 2F, 2F);

		}else{
			p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 2F, 1.5F);
			this.fin.onFinish();
			this.cancel();
		}
		
	}

}
