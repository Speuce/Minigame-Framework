package com.speuce.upgrades;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.speuce.stats.Stats;
import com.speuce.store.BackHandler;
import com.speuce.store.ClickEvent;
import com.speuce.store.Currency;
import com.speuce.store.Transactions;

public class Perk extends Upgrade{
	private int cost;
	private BackHandler back;
	private Currency c;
	private int levelmin;
	private ClickEvent e;
	public Perk(Material m, String name,
			List<String> lore, int cost , Currency c, UpgradesManager man, BackHandler back, int levelmin) {
		super(m, name, lore, UpgradeType.TIMED, man);
		this.cost = cost;
		this.back = back;
		this.c = c;
		this.levelmin = levelmin;
		this.e = this.getClickEvent();
	}
	public int getCost(){
		return this.cost;
	}
	public Long getExpiry(Player p){
		Object o = this.getMan().getData(p.getUniqueId(), this.getName());
		if(o == null || !(o instanceof Long)){
			return Stats.getCurrentTime() - 100L;
		}else{
			//TODO TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of("Z")));
			return (Long) o;
		}
	}
	public int getLevelmin(){
		return this.levelmin;
	}
	public boolean canUse(Player p){
		return this.getExpiry(p) > Stats.getCurrentTime();
	}
	public long getTimeleftSeconds(Player p){
		Long s = this.getExpiry(p) - Stats.getCurrentTime();
		if(s <= 0){
			return 0L;
		}else{
			return s;
		}
	}
	public void addTimes(Player p, int hours, int days){
		Long s = this.getExpiry(p);
		s += (hours * 3600);
		s += (days * 24 * 3600);
		getMan().setData(p.getUniqueId(), getName(), s);
	}
	private void makeTransaction(Player p){
		this.getMan().makeTransaction(p, "30 Days of " + this.getName(),this.c , cost, back, new Transactions(){

			@Override
			public boolean onComplete(Player p) {
				addTimes(p, 0, 30);
				return true;
			}
			
		});
	}
	public ClickEvent getClick(){
		return this.e;
	}
	
	private ClickEvent getClickEvent(){
		return new ClickEvent(){

			@Override
			public void onClick(Player p, Stats s, boolean r, boolean l) {
				if(levelmin > s.getLevel()){
					p.sendMessage(ChatColor.RED + "This perk requires level " + ChatColor.DARK_RED + levelmin + ChatColor.RED + " to purchase");
					return;
				}
				if(c.equals(Currency.BLIPS)){
					if(s.getBlips() >= cost){
						makeTransaction(p);
						return;
					}else{
						p.sendMessage(ChatColor.RED + "You need more Blips for that!");
						return;
					}
				}else{
					if(s.getCredits() >= cost){
						makeTransaction(p);
						return;
					}else{
						p.sendMessage(ChatColor.RED + "You need more Credits for that!");
						return;
					}
				}
				
				
			}
		};
	}
	public Currency getCurrency(){
		return this.c;
	}
}
