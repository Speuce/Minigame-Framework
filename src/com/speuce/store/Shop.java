package com.speuce.store;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import com.speuce.stats.Stats;
import com.speuce.stats.StatsManager;
import com.speuce.upgrades.Perk;
import com.speuce.upgrades.TieredUpgrade;

public abstract class Shop implements Listener {
	private String name;
	private Map<String, ShopItem> lookup = new LinkedHashMap<String, ShopItem>();
	private Map<String, Category> registeredCategories = new HashMap<String, Category>();
	private StatsManager m;
	public Shop(JavaPlugin p, String name, StatsManager m){
		this.name = name;
		this.m = m;
		p.getServer().getPluginManager().registerEvents(this, p);
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent e){
		if(e.getCurrentItem() == null || e.getCurrentItem().getType().equals(Material.AIR)){
			return;
		}
		if(e.getView().getTitle().equals(this.name)){
			e.setCancelled(true);
			String look = ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName());
			if(this.lookup.containsKey(look)){
				Player p = (Player) e.getWhoClicked();
				if(!m.hasPlayerStats(p.getUniqueId())){
					p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0F, 1.0F);
					p.sendMessage("Sorry, your stats aren't loaded yet. Try again later.");
					return;
				}else{
					this.lookup.get(look).getClick().onClick(p, m.getPlayerStats(p.getUniqueId()), e.isRightClick(), e.isShiftClick());
					p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1.0F, 1.0F);
				}
			}
		}else if(this.registeredCategories.containsKey(e.getView().getTitle())){
			e.setCancelled(true);
			Player p = (Player) e.getWhoClicked();
			if(!m.hasPlayerStats(p.getUniqueId())){
				p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0F, 1.0F);
				p.sendMessage("Sorry, your stats aren't loaded yet. Try again later."); 
				return;
			}else{
				Stats s = m.getPlayerStats(p.getUniqueId());
				Category c = this.registeredCategories.get(e.getView().getTitle());
				c.onCategoryClickedIn(e.getCurrentItem().getItemMeta().getDisplayName(),p, s);
				p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1.0F, 1.0F);
			}

		}
		
	}
	
	private Inventory ReSort(Player p){
		int size = lookup.size();
		Inventory it = Bukkit.createInventory(null, (size <= 9) ? 9 : (size <= 18) ? 18 : 27, this.name);
		int in = 0;
		for(ShopItem s : this.lookup.values()){
			if(s instanceof TieredUpgrade){
				TieredUpgrade d = (TieredUpgrade) s;
				ItemStack stck = d.getDisplay();
				ItemMeta meta = stck.getItemMeta();
				int i = d.getLevel(p);
				LinkedList<String> lore = new LinkedList<String>();

				if(i < 0 || i >= d.getMaxLevel()){
					if(lore.size() >= 1){
						lore.set(0,ChatColor.RED + "Cannot Purchase.");
					}else{
						lore.add(ChatColor.RED + "Cannot Purchase.");
					}

					meta.addEnchant(Enchantment.DAMAGE_ALL, 1, true);
					meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
				}else{
					if(lore.size() >= 1){
						lore.set(0,ChatColor.AQUA + "Buy Level " + ChatColor.GOLD.toString() + StaticShop.RomanNumerals(i + 1) 
								+ ChatColor.AQUA.toString() + " for " + ChatColor.GREEN + d.getCost(i + 1) + " " + d.getCurrency().getName());
					}else{
						lore.add(ChatColor.AQUA + "Buy Level " + ChatColor.GOLD.toString() + StaticShop.RomanNumerals(i + 1) 
								+ ChatColor.AQUA.toString() + " for " + ChatColor.GREEN + d.getCost(i + 1) + " " + d.getCurrency().getName());
					}
				}
				lore.addAll(meta.getLore());
				meta.setLore(lore);
				stck.setItemMeta(meta);
				it.setItem(in, stck);
			}else if(s instanceof Perk){
				Perk d = (Perk) s;
				ItemStack stck = new ItemStack(d.getType());
				ItemMeta meta = stck.getItemMeta();
				long secs = d.getTimeleftSeconds(p);
				long days = secs/(60 * 60 * 24);
				long hours = (secs/(3600)) % 24;
				LinkedList<String> lore = new LinkedList<String>();
					if(d.canUse(p)){
						meta.addEnchant(Enchantment.DAMAGE_ALL, 1, true);
						meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
					}
					
				lore.add(ChatColor.GOLD + "Time left: " + ChatColor.GREEN + days + "d, " + hours + "h");
				lore.add(ChatColor.AQUA + "Buy " + ChatColor.RED.toString() + "30d " + ChatColor.AQUA + "for " + ChatColor.GREEN.toString() + d.getCost() + " " + d.getCurrency().getName());
				lore.add(ChatColor.GOLD.toString() + "Requires " + ChatColor.YELLOW.toString() + "Level " + d.getLevelmin() + ChatColor.GOLD.toString() + " to Purchase.");
				lore.add(" ");
				meta.setDisplayName(ChatColor.RESET.toString() + StaticShop.getRandomColor() + d.getName());
				lore.addAll(d.getLore());
				meta.setLore(lore);
				stck.setItemMeta(meta);
				it.setItem(in, stck);
			}else if(s instanceof GroupBuy){
				GroupBuy g = (GroupBuy) s;
				if(g.isBought()){
					ItemStack stck = new ItemStack( g.getType());
					ItemMeta meta = stck.getItemMeta();
					meta.setDisplayName(g.getName());
					List<String> lore = new ArrayList<String>();
					lore.add(ChatColor.DARK_PURPLE.toString() + "Already activated by: "+ g.getActivator().getDisplayName());
					lore.add(" ");
					lore.addAll(g.getLore());
					meta.setLore(lore);
					meta.addEnchant(Enchantment.DAMAGE_ALL, 1, true);
					meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
					stck.setItemMeta(meta);
					it.setItem(in, stck);
				}else{
					ItemStack stck = new ItemStack( g.getType());
					ItemMeta meta = stck.getItemMeta();
					meta.setDisplayName(g.getName());
					List<String> lore = new ArrayList<String>();
					lore.add(ChatColor.AQUA + "Buy for " + ChatColor.GREEN +g.getCost() + " Credits.");
					lore.add(" ");
					lore.addAll(g.getLore());
					meta.setLore(lore);
					stck.setItemMeta(meta);
					it.setItem(in, stck);
				}
			}else{
				it.setItem(in, s.getDisplay());
			}
			in++;
		}
		return it;
	}
	public String getName(){
		return this.name;
	}
	public void open(Player p){
		p.openInventory(this.ReSort(p));
	}
	public void addItem(ShopItem e){
		lookup.put(e.getName(), e);
		Bukkit.broadcastMessage("added item: " + e.getName());
	}
	public void registerCategory(Category c){
		this.registeredCategories.put(c.getName(), c);
	}
	public BackHandler getDefaultBackHandler(){
		return new BackHandler(){

			@Override
			public void onBack(Player p) {
				open(p);
			}
		};
	}
}
