package com.speuce.miniGames.minigame;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.speuce.miniGames.utils.AniFinish;
import com.speuce.miniGames.utils.Mini;
import com.speuce.miniGames.utils.Reward;
import com.speuce.stats.Stats;
import com.speuce.stats.StatsManager;

public class RewardManager {
	private StatsManager stats;
	private Reward r;
	private Map<Player, Integer> blipsEarned;
	private Map<Player, Integer> xpEarned;
	private MiniGame m;
	private Plugin p;
	public RewardManager(StatsManager st, Reward r, MiniGame m, Plugin p){
		this.stats = st;
		this.r = r;
		this.p = p;
		this.m = m;
		this.blipsEarned = new HashMap<Player, Integer>();
		this.xpEarned = new HashMap<Player, Integer>();
	}
	public void out(Player p){
		if(this.blipsEarned.containsKey(p)){
			this.blipsEarned.remove(p);
		}
		if(this.xpEarned.containsKey(p)){
			this.xpEarned.remove(p);
		}
	}
	
	public void giveRewards(final Player p){
		if(this.stats.hasPlayerStats(p.getUniqueId())){
			int bblips = this.stats.getPlayerStats(p.getUniqueId()).getBlips();
			final int bxp = this.stats.getPlayerStats(p.getUniqueId()).getXp();
			if(this.blipsEarned.containsKey(p)){
				int i = this.blipsEarned.get(p);
				this.blipsEarned.remove(p);
				this.r.rewardAnimation(p, bblips, i, new AniFinish(){

					@Override
					public void onFinish() {
						if(xpEarned.containsKey(p)){
							int it = xpEarned.get(p);
							xpEarned.remove(p);
							delayedXpReward(p, bxp, it);
						}else{
							m.getF(p).onFinish();
						}
						
					}
					
				});
			}else if(this.xpEarned.containsKey(p)){
				int it = xpEarned.get(p);
				r.XpAnimation(p, bxp, it, m.getF(p));
			}else{
				Mini.sendActionBar(p, ChatColor.RED + "You Earned no rewards this game!");
			}
		}
	}

	public void rewardBlipsXp(Player p, int blips, int xp){
		if(this.stats.hasPlayerStats(p.getUniqueId())){
			Stats s = this.stats.getPlayerStats(p.getUniqueId());
			int blipsb = s.getBlips();
			this.stats.setBlips(p.getUniqueId(), blipsb + blips);
			this.stats.giveExp(p.getUniqueId(), xp);
		}
		if(this.blipsEarned.containsKey(p)){
			int i = this.blipsEarned.get(p);
			i +=blips;
			this.blipsEarned.put(p, i);

		}else{
			this.blipsEarned.put(p, blips);
		}
		if(this.xpEarned.containsKey(p)){
			int i = this.xpEarned.get(p);
			i += xp;
			this.xpEarned.put(p, i);

		}else{
			this.xpEarned.put(p, xp);
		}
		Mini.sendActionBar(p, ChatColor.AQUA.toString() + "+" + blips + " Blips!      " + ChatColor.DARK_AQUA.toString() + "+" + xp + " XP!");
		p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2F, 2F);
	}
	
	public void rewardBlips(Player p, int amount){
		if(this.stats.hasPlayerStats(p.getUniqueId())){
			int blips = this.stats.getPlayerStats(p.getUniqueId()).getBlips();
			this.stats.setBlips(p.getUniqueId(), blips + amount);
		}
		if(this.blipsEarned.containsKey(p)){
			int i = this.blipsEarned.get(p);
			i += amount;
			this.blipsEarned.put(p, i);

		}else{
			this.blipsEarned.put(p, amount);
		}
		Mini.sendActionBar(p, ChatColor.AQUA.toString() + "+" + amount + " Blips!");
		p.playSound(p.getLocation(), Sound.BLOCK_DISPENSER_FAIL, 5F, 2F);
	}
	public void rewardXp(Player p, int amount){
		if(this.stats.hasPlayerStats(p.getUniqueId())){
			this.stats.giveExp(p.getUniqueId(), amount);
		}
		if(this.xpEarned.containsKey(p)){
			int i = this.xpEarned.get(p);
			i += amount;
			this.xpEarned.put(p, i);

		}else{
			this.xpEarned.put(p, amount);
		}
		Mini.sendActionBar(p, ChatColor.DARK_AQUA.toString() + "+" + amount + " XP!");
		p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2F, 2F);
	}
	private void delayedXpReward(final Player p,final int bxp,final int xp){
		BukkitRunnable br = new BukkitRunnable(){

			@Override
			public void run() {
				r.XpAnimation(p, bxp, xp, m.getF(p));
				this.cancel();
			}
			
		};
		br.runTaskLater(this.p, 50L);
	}
}
