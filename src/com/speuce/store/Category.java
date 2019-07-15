package com.speuce.store;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.speuce.stats.Stats;
import com.speuce.upgrades.Perk;
import com.speuce.upgrades.TieredUpgrade;

public class Category extends ShopItem{
	private BackHandler b;
	private ClickEvent e;
	private Map<String, ShopItem> lookup = new HashMap<String, ShopItem>();
	public Category(Material m, String name, BackHandler b, List<String> lore) {
		super(m, name, lore);
		this.b = b;
		this.e = this.getClickEvent();
	}
	public ClickEvent getClick(){
		return this.e;
	}
	public ClickEvent getClickEvent(){
		return new ClickEvent(){

			@Override
			public void onClick(Player p, Stats s, boolean t, boolean tt) {
				open(p);
				
			}

			
			
		};
	}
	public void onCategoryClickedIn(String itemName, Player p, Stats s){
		if(ChatColor.stripColor(itemName).equalsIgnoreCase("Go Back")){
			this.b.onBack(p);
			return;
		}else{
			if(lookup.containsKey(ChatColor.stripColor(itemName))){
				ShopItem got = lookup.get(ChatColor.stripColor(itemName));
				ClickEvent e = got.getClick();
				e.onClick(p, s, false ,false);
				return;
			}else{
				p.sendMessage(ChatColor.RED + "Something weird Occured. Please notidy an admin about this. Error code: 701.");
				return;
			}
		}
	}
	public BackHandler getBackHandler(){
		return new BackHandler(){

			@Override
			public void onBack(Player p) {
				open(p);
				
			}
		};
	}
	public void addItem(ShopItem e){
		lookup.put(e.getName(), e);
	}
	
	private Inventory ReSort(Player p){
		int size = lookup.size();
		Inventory it = Bukkit.createInventory(p, (size <= 8) ? 9 : (size <= 16) ? 18 : 27, this.getName());
		int in = 0;
		for(ShopItem s : this.lookup.values()){
			if(in > 0 && in % 9 == 0){
				it.setItem(in, StaticShop.getBack());
				in++;
			}
			if(s instanceof TieredUpgrade){
				TieredUpgrade d = (TieredUpgrade) s;
				ItemStack stck = new ItemStack(d.getType());
				ItemMeta meta = stck.getItemMeta();
				int i = d.getLevel(p);
				LinkedList<String> lore = new LinkedList<String>();
				
				if(i < 0 || i >= d.getMaxLevel()){
					lore.add(ChatColor.RED + "Cannot Purchase.");
					meta.addEnchant(Enchantment.DAMAGE_ALL, 1, true);
					meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
				}else{
						lore.add(ChatColor.AQUA + "Buy Level " + ChatColor.GOLD.toString() + StaticShop.RomanNumerals(i + 1) 
								+ ChatColor.AQUA.toString() + " for " + ChatColor.GREEN + d.getCost(i) + " " + d.getCurrency().getName());
						
						lore.add(ChatColor.GREEN.toString() + "Requires " + 
						ChatColor.DARK_GREEN.toString() + "Level " + 
						d.getLevelminforUpgrade(d.getLevel(p)) + ChatColor.GREEN.toString() 
						+ " to upgrade.");
				}
				lore.add(" ");
				meta.setDisplayName(ChatColor.RESET.toString() + StaticShop.getRandomColor() + d.getName());
				lore.addAll(d.getLore());
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
					List<String> lore = new ArrayList<String>();
					lore.add(ChatColor.AQUA + "Buy for " + ChatColor.GREEN +g.getCost() + " Credits.");
					lore.add(" ");
					lore.addAll(g.getLore());
					meta.setLore(lore);
					meta.addEnchant(Enchantment.DAMAGE_ALL, 1, true);
					meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
					stck.setItemMeta(meta);
					it.setItem(in, stck);
				}

			}else{
				it.setItem(in, s.getDisplay());
			}
			in++;

		}
		if(in <= 9){
			it.setItem(8, StaticShop.getBack());
		}
		return it;
	}
	public void open(Player p){
		p.openInventory(this.ReSort(p));
	}
}
