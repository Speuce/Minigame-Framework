package com.speuce.store;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public abstract class ShopItem {
	private ItemStack display;
	private String name;
	private Material type;
	private List<String> lore;
	public ShopItem(Material m, String name, List<String> lore){
		this.type = m;
		this.name = name;
		ItemStack i = new ItemStack(m);
		ItemMeta a = i.getItemMeta();
		a.setDisplayName(name);
		a.setLore(lore);
		i.setItemMeta(a);
		this.display = i;
		this.lore = lore;
	}
	public ShopItem(Material m, String name){
		this.type = m;
		this.name = name;
		ItemStack i = new ItemStack(m);
		ItemMeta a = i.getItemMeta();
		a.setDisplayName(name);
		a.setLore(lore);
		i.setItemMeta(a);
		this.display = i;
		this.lore = new ArrayList<String>();
	}
	public abstract ClickEvent getClick();
	public Material getType(){
		return this.type;
	}
	public String getName(){
		return name;
	}
	public ItemStack getDisplay(){
		return this.display;
	}
	public List<String> getLore(){
		return this.lore;
	}
}
