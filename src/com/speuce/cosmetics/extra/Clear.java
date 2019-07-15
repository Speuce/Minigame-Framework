package com.speuce.cosmetics.extra;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.speuce.cosmetics.Cosmetic;
import com.speuce.cosmetics.CosmeticType;
import com.speuce.cosmetics.auras.Aura;
import com.speuce.cosmetics.recent.RecentCosmeticsManager;
import com.speuce.stats.Stats;
import com.speuce.store.BackHandler;
import com.speuce.store.ClickEvent;
import com.speuce.store.ShopItem;

public class Clear extends ShopItem{
	private ClickEvent ev;
	private List<String> lore;
	public Clear(final BackHandler back,final  CosmeticType type, final RecentCosmeticsManager rm) {
		super(Material.GLASS, ChatColor.RED + "Clear");
		this.lore = new ArrayList<String>();
		this.ev = new ClickEvent(){

			@Override
			public void onClick(Player p, Stats s, boolean right, boolean shift) {
				Cosmetic c = rm.getRecentCosmetic(p, type);
				if(c != null){
					p.sendMessage(ChatColor.RED + "Deselected: " + c.getName());
					rm.setRecentCosmetic(p, type, null);
					if(c.getCosmeticType() == CosmeticType.GADGET){
						p.getInventory().setItem(6, null);
					}else if(c instanceof Aura){
						Aura a = (Aura) c;
						a.off(p);
					}
				}else{
					p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1F, 0F);
				}
				
			}
			
		};
	}
	@Override
	public ClickEvent getClick() {
		return this.ev;
	}
	@Override
	public List<String> getLore() {
		return this.lore;
	}

}
