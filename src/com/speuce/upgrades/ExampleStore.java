package com.speuce.upgrades;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.speuce.sql.SQLManager;
import com.speuce.stats.StatsManager;
import com.speuce.store.Category;
import com.speuce.store.Currency;
import com.speuce.store.Shop;
import com.speuce.store.TransactionManager;

public class ExampleStore extends Shop implements Listener{
	Category cat1;
	Category cat1n1, cat1n2;
	TieredUpgrade item1;
	UpgradesManager upman;
	TransactionManager tman;
	Perk perk;
	public ExampleStore(JavaPlugin p, StatsManager m, SQLManager sql) {
		super(p, "example", m);
		this.tman = new TransactionManager(p, m);
		this.upman = new UpgradesManager(this, sql, p, this.tman);
		List<String> sampleLore = new ArrayList<String>();
		sampleLore.add(ChatColor.AQUA.toString() + "Example Text");
		int[] pricez = {1,2,3,4,5};
		int[] levelz = {0, 5, 10, 15, 20};
		
		this.cat1 = new Category(Material.WOODEN_AXE, "ex", getDefaultBackHandler(), sampleLore);
		this.cat1n1 = new Category(Material.DIAMOND_HORSE_ARMOR, "Other", this.cat1.getBackHandler(), sampleLore);
		this.cat1n2 = new Category(Material.SNOWBALL, "Catty", this.cat1.getBackHandler(), sampleLore);
		this.item1 = new TieredUpgrade(Material.DIAMOND, "Itemm", sampleLore,pricez,Currency.BLIPS,this.upman, this.cat1n1.getBackHandler(), levelz );
		this.perk = new Perk(Material.EMERALD, "Perky", sampleLore, 5, Currency.BLIPS, this.upman, this.cat1n2.getBackHandler(), 5);
		
		this.addItem(cat1);
		
		this.cat1.addItem(this.cat1n1);
		this.cat1.addItem(this.cat1n2);
		
		this.cat1n1.addItem(this.item1);
		
		this.cat1n2.addItem(this.perk);
		
		this.registerCategory(cat1n1);
		this.registerCategory(cat1n2);
		this.registerCategory(cat1);
		
		this.upman.registerUpgrade(this.item1);
		this.upman.registerUpgrade(this.perk);
	}
	@EventHandler
	public void onInteract(PlayerInteractEvent e){
		if(e.getAction().equals(Action.RIGHT_CLICK_AIR) && e.getItem().getType().equals(Material.DIAMOND)){
			open(e.getPlayer());
		}
	}
	

}
