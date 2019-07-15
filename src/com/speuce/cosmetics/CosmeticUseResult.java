package com.speuce.cosmetics;

public class CosmeticUseResult {
	private boolean result;
	private String text;
	
	public CosmeticUseResult(boolean Result, String text){
		this.result = Result;
		this.text = text;
	}

	public boolean getResult() {
		return result;
	}

	public String getText() {
		return text;
	}
}
