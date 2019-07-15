package com.speuce.cosmetics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.speuce.cosmetics.extra.Rarity;

public abstract class Cosmetic{
	private List<CosmeticProperty> properties;
	private Map<UUID, CosmeticPlayerInfo> info;
	private boolean disabled = false;
	private String name;
	private boolean autoDeduct = true;
	private CosmeticManager cm;
	public Cosmetic(String name, CosmeticManager cm){
		this.info = new HashMap<UUID, CosmeticPlayerInfo>();
		this.properties = new ArrayList<CosmeticProperty>();
		this.name = name;
		this.cm = cm;
	}
	public void setAutoDeduct(boolean deduct){
		this.autoDeduct = deduct;
	}
	public void setRecent(Player p){
		this.getCosmeticManager().getRecentCosmeticsManager().setRecentCosmetic(p, this.getCosmeticType(), this);
	}
	public void setNotRecent(Player p){
		this.getCosmeticManager().getRecentCosmeticsManager().setRecentCosmetic(p, this.getCosmeticType(), null);
	}
	public boolean isRecent(Player p){
		Cosmetic c = this.getCosmeticManager().getRecentCosmeticsManager().getRecentCosmetic(p, this.getCosmeticType());
		if(c == null){
			return false;
		}else{
			return c.getName().equalsIgnoreCase(this.getName());
		}
	}
	public CosmeticManager getCosmeticManager(){
		return this.cm;
	}
	public void setDisabled(boolean disable){
		this.disabled = disable;
	}
	public List<CosmeticProperty> getProperties(){
		return this.properties;
	}
	public boolean hasProperty(CosmeticProperty p){
		return this.properties.contains(p);
	}
	public void addProperty(CosmeticProperty p){
		if(!this.properties.contains(p)){
			this.properties.add(p);
		}
	}
	public boolean isDisabled(){
		return this.disabled;
	}
	public abstract CosmeticType getCosmeticType();
	public void clearProperties(){
		this.properties.clear();
	}
	public String getName(){
		return this.name;
	}
	public CosmeticUseResult canUse(Player p){
		if(this.disabled){
			return new CosmeticUseResult(false, ChatColor.RED + "This Cosmetic is disabled!");
		}
		if(!this.info.containsKey(p.getUniqueId())){
			return new CosmeticUseResult(false, ChatColor.RED + "You do not have data for this Cosmetic!");
		}
		if(this.getCosmeticManager().cooldownContains(p)){
			return new CosmeticUseResult(false, ChatColor.RED + "Please wait " + this.getCosmeticManager().getCooldown(p) + " second(s) for your Cosmetic Cooldown!");
		}
		if(!this.info.get(p.getUniqueId()).canUse()){
			return new CosmeticUseResult(false, ChatColor.RED + "You cannot use this Cosmetic!");
		}
		return new CosmeticUseResult(true, "");
	}
	public boolean infoCanuse(Player p){
		return this.info.get(p.getUniqueId()).canUse();
	}
	public void use(Player p){
		if(this.hasProperty(CosmeticProperty.QUANTIFIED) && this.autoDeduct){
			if(this.info.containsKey(p.getUniqueId())){
				this.info.get(p.getUniqueId()).useOne();
			}
		}
		this.onSelect(p);
	}
	public boolean hasInfo(UUID uuid){
		return this.info.containsKey(uuid);
	}
	public void addInfo(UUID uuid, CosmeticPlayerInfo c){
		this.info.put(uuid, c);
	}
	public void deleteInfo(UUID uuid){
		if(this.info.containsKey(uuid)){
			this.info.remove(uuid);
		}
	}
//	@Override
//	public ClickEvent getClick(){
//		return new ClickEvent(){
//
//			@Override
//			public void onClick(Shop store, Player p, Stats s, boolean right, boolean shift ) {
//				CosmeticUseResult res = canUse(p);
//				if(getCosmetic() instanceof Buyable && right && !isDisabled() && !(getCosmetic() instanceof Legacy)){
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
	public Cosmetic getCosmetic(){
		return this;
	}
	public CosmeticPlayerInfo getInfo(UUID uuid){
		if(this.info.containsKey(uuid)){
			return this.info.get(uuid);
		}else{
			return new CosmeticPlayerInfo(this, 0, false);

		}
	}
	public abstract void onSelect(Player p);
	public abstract Rarity getRarity();
}
