package com.speuce.cosmetics;





public class CosmeticPlayerInfo {
	private int amount = 0;
	private boolean use = false;
	private boolean needsamount = false;
	private Cosmetic c;
	public CosmeticPlayerInfo(Cosmetic c){
		this.c = c;
		this.use = true;
		this.amount = 1;
	}
	public CosmeticPlayerInfo(Cosmetic c, int amount){
		this.c = c;
		this.amount = amount;
		this.use = true;
	}
	public CosmeticPlayerInfo(Cosmetic c, boolean use){
		this.c = c;
		this.use = use;
	}
	public CosmeticPlayerInfo(Cosmetic c, int amount, boolean use){
		this.c = c;
		this.amount = amount;
		this.use = use;
	}
	public int getAmount() {
		return amount;
	}
	public void setNeedsAmount(boolean nd){
		this.needsamount = nd;
	}
	public void setAmount(int amount) {
		this.amount = amount;
	}
	public void giveAmount(int amo){
		this.amount = this.amount + amo;
	}
	public boolean canUse() {
		boolean b;
		if(this.needsamount){
			b = this.amount > 0;
		}else{
			b = this.use;
		}
			
		return b;
	}
	public void setUse(boolean use) {
		this.use = use;
	}
	public Cosmetic getCosmetic() {
		return c;
	}
	public void useOne(){
		this.amount--;
	}
}
