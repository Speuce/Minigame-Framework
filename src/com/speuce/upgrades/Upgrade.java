package com.speuce.upgrades;

import java.util.List;

import org.bukkit.Material;

import com.speuce.store.ShopItem;

public abstract class Upgrade extends ShopItem{
	private UpgradeType UpgradeType;
	private UpgradesManager man;
	public Upgrade(Material m, String name, List<String> lore, UpgradeType type, UpgradesManager man) {
		super(m, name, lore);
		this.UpgradeType = type;
		this.man = man;
	}
	public UpgradeType getUpgradeType(){
		return this.UpgradeType;
	}
	public UpgradesManager getMan(){
		return this.man;
	}

}
