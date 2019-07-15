package com.speuce.store;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.speuce.miniGames.minigame.MiniGame;
import com.speuce.stats.Stats;

public class GroupBuy extends ShopItem{
	
	private boolean bought;
	private int cost;
	private TransactionManager man;
	private BackHandler back;
	private MiniGame game;
	private ClickEvent e;
	private Player activator = null;
	public GroupBuy(Material m, String name, List<String> lore,TransactionManager man, BackHandler back,MiniGame game, int cost) {
		super(m, name, lore);
		this.cost = cost;
		this.man = man;
		this.game = game;
		this.back = back;
		this.e = getC();
	}
	
	public boolean isBought(){
		return this.bought;
	}
	
	@Override
	public ClickEvent getClick() {
		return this.e;
	}
	public int getCost(){
		return this.cost;
	}
	private ClickEvent getC(){
		return new ClickEvent(){

			@Override
			public void onClick(Player p, Stats s, boolean ex, boolean ext) {
				if(s.getCredits() >= cost && !bought){
					makeTransaction(p);
				}else{
					p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_FALL, 3F, 0F);
				}
				
			}
			
		};
	}
	public Player getActivator(){
		return this.activator;
	}
	private void makeTransaction(Player p){
		this.man.createTransaction(p, this.getName(), Currency.CREDITS, this.cost, new Transactions(){

			@Override
			public boolean onComplete(Player p) {
				if(bought){
					return false;
				}else{
					bought = true;
					activator = p;
					game.bcast(ChatColor.GOLD.toString() + ChatColor.BOLD.toString() + p.getDisplayName() + ChatColor.AQUA.toString() + " has activated " + getName() + "!");
					return true;
				}
				
			}}, this.back);
	}

}
