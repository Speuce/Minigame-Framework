package com.speuce.store;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.speuce.stats.StatsManager;

public class TransactionManager implements Listener{
	StatsManager m;
	JavaPlugin p;
	Map<String, Transaction> PendingTransactions;
	public TransactionManager(JavaPlugin p, StatsManager m){
		this.p = p;
		this.m = m;
		this.p.getServer().getPluginManager().registerEvents(this, p);
		this.PendingTransactions = new HashMap<String, Transaction>();
	}
	@EventHandler
	public void onClose(InventoryCloseEvent e){
		if(this.PendingTransactions.containsKey(ChatColor.stripColor(e.getView().getTitle()))){
			this.PendingTransactions.get(ChatColor.stripColor(e.getView().getTitle())).close();
		}
	}
	@EventHandler
	public void onClick(InventoryClickEvent e){
		if(this.PendingTransactions.containsKey(ChatColor.stripColor(e.getView().getTitle())) && !e.getCurrentItem().getType().equals(Material.AIR)){
			this.PendingTransactions.get(ChatColor.stripColor(e.getView().getTitle())).onClick(e.getCurrentItem().getItemMeta().getDisplayName());
			e.setCancelled(true);
			
		}
	}
	public void tryRemove(UUID uuid){
		if(this.PendingTransactions.containsKey(uuid.toString())){
			this.PendingTransactions.remove(uuid.toString());
		}
	}
	public void createTransaction(Player p, String title, Currency c, int amount, Transactions trans, BackHandler back){
		Transaction t = new Transaction(p, c, amount, title, trans, back, this.m, this);
		t.open();
		this.PendingTransactions.put(t.getID().toString(), t);
	}
	
	
}
