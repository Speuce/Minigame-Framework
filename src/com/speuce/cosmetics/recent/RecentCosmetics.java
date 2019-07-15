package com.speuce.cosmetics.recent;

import java.util.HashMap;
import java.util.Map;

import com.speuce.cosmetics.Cosmetic;
import com.speuce.cosmetics.CosmeticType;

public class RecentCosmetics {
	private Map<CosmeticType, Cosmetic> recent;
	
	public RecentCosmetics(){
		this.recent = new HashMap<CosmeticType, Cosmetic>();
	}
	public void setRecentCosmetic(CosmeticType s, Cosmetic c){
		recent.put(s, c);
	}
	public Map<CosmeticType, Cosmetic> getRecent(){
		return this.recent;
	}
	public Cosmetic getRecentCosmetic(CosmeticType s){
		if(this.recent.containsKey(s)){
			return this.recent.get(s);
		}else{
			return null;
		}
	}
}
