package com.speuce.store;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Wool;

import com.speuce.stats.StatsManager;

public class Transaction {
	private Transactions complete;
	private BackHandler back;
	private Player p;
	private Inventory i;
	private Currency c;
	private int amount;
	private UUID id;
	private StatsManager m;
	private TransactionManager man;
	public Transaction(Player p, Currency c, int amount, String title, 
			Transactions complete, BackHandler back, StatsManager m, TransactionManager man){
		this.p = p;
		this.c = c;
		this.m = m;
		this.complete = complete;
		this.amount = amount;
		this.man = man;
		this.id = UUID.randomUUID();
		this.i = Bukkit.createInventory(null, 9, ChatColor.GRAY + id.toString());
		this.back = back;
		ItemStack red = new Wool(DyeColor.RED).toItemStack(1);
		ItemMeta redmeta = red.getItemMeta();
		redmeta.setDisplayName(ChatColor.RED + "Deny");
		red.setItemMeta(redmeta);
		ItemStack green = new Wool(DyeColor.LIME).toItemStack(1);
		ItemMeta greenmeta = red.getItemMeta();
		greenmeta.setDisplayName(ChatColor.GREEN + "Buy " + title + ChatColor.GREEN + " for " + ChatColor.AQUA + amount + " " + c.getName());
		green.setItemMeta(greenmeta);
		this.i.setItem(2, red);
		this.i.setItem(6, green);
		
	}
	public void open(){
		this.p.openInventory(this.i);
	}
	public void onClick(String s){
		if(s.contains("Deny")){
			this.deny();
		}else if(s.contains("Buy")){
			this.complete();
		}
	}
	private void complete(){
		if(this.c.equals(Currency.BLIPS)){
			int bal = m.getPlayerStats(p.getUniqueId()).getBlips();
			if(bal >= this.amount){
				if(this.complete.onComplete(this.p)){
					m.setBlips(p.getUniqueId(), bal - this.amount);
					this.p.playSound(this.p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1.0F, 1.0F);
					p.sendMessage(ChatColor.GREEN + "Transaction Completed.");
					this.man.tryRemove(this.id);
					
					this.back.onBack(p);
					this.destroy();
				}else{
					this.deny();
				}

			}else{
				this.p.playSound(this.p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0F, 1.0F);
				this.p.sendMessage(ChatColor.RED + "Insufficient funds.");
				this.deny();
			}
		}else{
			int bal = m.getPlayerStats(p.getUniqueId()).getCredits();
			if(bal >= this.amount){
				if(this.complete.onComplete(this.p)){
					m.setCredits(p.getUniqueId(), bal - this.amount);
					this.man.tryRemove(this.id);
					this.p.playSound(this.p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1.0F, 1.0F);
					p.sendMessage(ChatColor.GREEN + "Transaction Completed.");
					this.back.onBack(p);
					this.destroy();
				}else{
					this.deny();
				}

			}else{
				this.p.playSound(this.p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0F, 1.0F);
				this.p.sendMessage(ChatColor.RED + "Insufficient funds.");
				this.deny();
			}
		}
	}
	public void deny(){
		this.p.playSound(this.p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0F, 1.0F);
		this.p.sendMessage(ChatColor.RED + "Transaction Denied.");
		this.man.tryRemove(this.id);
		this.p.closeInventory();
		this.back.onBack(this.p);
		this.destroy();
	}
	public void close(){
		this.p.sendMessage(ChatColor.RED + "Transaction Cancelled");
		this.man.tryRemove(this.id);
		this.destroy();
	}
	public UUID getID(){
		return this.id;
	}
	public void destroy(){
		this.complete = null;
		this.p = null;
		this.i = null;
	}
}
