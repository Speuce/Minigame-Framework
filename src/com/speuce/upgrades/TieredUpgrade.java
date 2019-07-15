package com.speuce.upgrades;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.speuce.stats.Stats;
import com.speuce.store.BackHandler;
import com.speuce.store.ClickEvent;
import com.speuce.store.Currency;
import com.speuce.store.StaticShop;
import com.speuce.store.Transactions;

public class TieredUpgrade extends Upgrade{
	private int[] costs;
	private BackHandler back;
	private Currency c;
	private int[] levels;
	private ClickEvent e;
	public TieredUpgrade(Material m, String name,
			List<String> lore, int[] costs, Currency c, UpgradesManager man, BackHandler back, int[] levels) {
		super(m, name, lore, UpgradeType.LEVELED, man);
		this.costs = costs;
		this.back = back;
		this.c = c;
		this.levels = levels;
		this.e = this.getClickEvent();
	}
	public int getCost(int level){
		try{
			return this.costs[level];
		}catch(ArrayIndexOutOfBoundsException e){
			return -1;
		}
	}
	public int getLevelminforUpgrade(int level){
		try{
			return this.levels[level];
		}catch(ArrayIndexOutOfBoundsException e){
			return 0;
		}
	}
	public int getMaxLevel(){
		return this.costs.length;
	}
	public int getLevel(Player p){
		Object o = this.getMan().getData(p.getUniqueId(), this.getName());
		if(o == null || !(o instanceof Integer)){
			if(o == null){
				return -1;
			}
			return -1;
		}else{
			return (int) o;
		}
	}
	private void makeTransaction(Player p, int cost, final int levelTo){
		this.getMan().makeTransaction(p, this.getName() + " " + StaticShop.RomanNumerals(levelTo),this.c , cost, back, new Transactions(){

			@Override
			public boolean onComplete(Player p) {
				getMan().setData(p.getUniqueId(), getName(), levelTo);
				return true;

			}
			
		});
	}
	@Override
	public ClickEvent getClick(){
		return this.e;
	}
	private ClickEvent getClickEvent(){
		return new ClickEvent(){

			@Override
			public void onClick(Player p, Stats s, boolean t, boolean tt) {
				if(getLevel(p) >= 0 && getLevel(p) < getMaxLevel()){
					if(getLevelminforUpgrade(getLevel(p)) > s.getLevel()){
						p.sendMessage(ChatColor.RED + "This requires level " + ChatColor.DARK_RED + levels[getLevel(p)] + ChatColor.RED +" to purchase");
						return;
					}
					if(c.equals(Currency.BLIPS)){
						if(s.getBlips() >= getCost(getLevel(p)) && getLevel(p) != -1){
							makeTransaction(p, getCost(getLevel(p)), getLevel(p) + 1);
							return;
						}else{
							p.sendMessage(ChatColor.RED + "You need more Blips for that!");
							p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0F, 1.0F);
							return;
						}
					}else{
						if(s.getCredits() >= getCost(getLevel(p)) && getLevel(p) != -1){
							makeTransaction(p, getCost(getLevel(p)), getLevel(p) + 1);
							return;
						}else{
							p.sendMessage(ChatColor.RED + "You need more Credits for that!");
							p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0F, 1.0F);
							return;
						}
					}
				}
				p.sendMessage(ChatColor.RED + "You have reached the maximum level.");
				p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0F, 1.0F);
				
			}
			
		};
	}
	public Currency getCurrency(){
		return this.c;
	}

}
