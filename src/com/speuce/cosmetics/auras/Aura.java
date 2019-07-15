package com.speuce.cosmetics.auras;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.speuce.cosmetics.Cosmetic;
import com.speuce.cosmetics.CosmeticManager;
import com.speuce.cosmetics.CosmeticProperty;
import com.speuce.cosmetics.CosmeticType;
import com.speuce.cosmetics.extra.Timeable;

public abstract class Aura extends Cosmetic implements Timeable{
	public List<Player> players;
	int i = 0;
	public Aura(String name, CosmeticManager cosman) {
		super(name, cosman);
		this.addProperty(CosmeticProperty.BOOLEAN);
		this.players = new ArrayList<Player>();
	}

	@Override
	public CosmeticType getCosmeticType() {
		return CosmeticType.AURA;
	}
	public void off(final Player p) {
		BukkitRunnable br = new BukkitRunnable(){

			@Override
			public void run() {
				if(players.contains(p)){
					players.remove(p);
				}
				
			}
			
		};
		this.getCosmeticManager().runTaskSync(br);
	}
//	@Override
//	public ClickEvent getClick(){
//		return new ClickEvent(){
//
//			@Override
//			public void onClick(Shop store, Player p, Stats s, boolean right, boolean shift ) {
//				CosmeticUseResult res = canUse(p);
//				if(getCosmetic() instanceof Buyable && right && !isDisabled() && !getCosmetic().canUse(p).getResult()){
//					Buyable b = (Buyable) getCosmetic();
//					b.makeTransaction(store.getTransactionManager(),p, getCosmetic().getBackHandler());
//				}else{
//					if(res.getResult() || ChatColor.stripColor(res.getText()).contains("second")){
//						onSelect(p);
//					}else{
//						p.sendMessage(ChatColor.RED + res.getText());
//					}
//				}
//
//				
//			}
//			
//		};
//	}
	@Override
	public void Tick(){
		this.Tock();
		i++;
		if(i >= 50){
			i = 0;
			if(this.isDisabled() && !this.players.isEmpty()){
				for(Player p: this.players){
					p.sendMessage(ChatColor.RED + "This Cosmetic has been disabled!");
					this.off(p);
				}
			}
		}

	}
	
	@Override
	public void onSelect(Player p) {
		Cosmetic c = this.getCosmeticManager().getRecentCosmeticsManager().getRecentCosmetic(p, this.getCosmeticType());
		if(c != null && !c.getName().equals(this.getName())){
			if(c instanceof Aura){
				Aura a = (Aura) c;
				p.sendMessage(ChatColor.RED + "Deselected: " + a.getName());
				a.off(p);
			}
		}
		if(!this.players.contains(p)){
			p.sendMessage(ChatColor.GREEN + "Selected: " + this.getName());
			this.setRecent(p);
			this.players.add(p);
			p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 2F, 1F);
			p.closeInventory();
		}else{
			this.players.remove(p);
			this.setNotRecent(p);
			p.sendMessage(ChatColor.RED + "Deselected: " + this.getName());
			p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 2F, 1F);
			p.closeInventory();
		}

	}
	public abstract void Tock();
}
