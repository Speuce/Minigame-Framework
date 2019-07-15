package com.speuce.cosmetics;

public enum CosmeticType {
	GADGET("GADGET"),
	AURA("AURA");
	
	
	private String title;
	private CosmeticType(String title){
		this.title = title;
	}
	public String getTitle(){
		return this.title;
	}
	public static CosmeticType getFromTitle(String title){
		for(CosmeticType c: CosmeticType.values()){
			if(c.getTitle().equalsIgnoreCase(title)){
				return c;
			}
		}
		return null;
	}
}
