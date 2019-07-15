package com.speuce.store;

public enum Currency {
	BLIPS("Blips"),
	CREDITS("Credits");
	private String name;
	private Currency(String name){
		this.name = name;
	}
	public String getName(){
		return name;
	}
}
