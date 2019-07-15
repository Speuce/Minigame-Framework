package com.speuce.stats;

import java.sql.Timestamp;

import com.speuce.sql.Rank;

public class Stats {
	private int blips = 0;
	private StaffRank staffRank;
	private int credits = 0;
	private Timestamp dateJoined;
	private int level;
	private int xp;
	private long plusExpireSeconds;
	public Stats(int blips,long plusExpireSeconds, StaffRank staffRank, int credits, Timestamp dateJoined, int level, int xp) {
		this.blips = blips;
		this.plusExpireSeconds = plusExpireSeconds;
		this.staffRank = staffRank;
		this.credits = credits;
		this.dateJoined = dateJoined;
		this.level = level;
		this.xp = xp;
	}
	public int getBlips() {
		return blips;
	}
	public long getPlusEpoch(){
		return this.plusExpireSeconds;
	}
	public void setPlusExpiry(long epoch){
		this.plusExpireSeconds = epoch;
	}
	public int getPlusHours(){
		 return (int) getHoursLeft(plusExpireSeconds);
	}
	public StaffRank getStaffRank() {
		return staffRank;
	}
	public int getCredits(){
		return this.credits;
	}
	public Timestamp getDateJoined(){
		return this.dateJoined;
	}
	public void setBlips(int blips) {
		this.blips = blips;
	}
	public void setStaffRank(StaffRank staffRank) {
		this.staffRank = staffRank;
	}
	public void setCredits(int credits) {
		this.credits = credits;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public int getXp() {
		return xp;
	}
	public void setXp(int xp) {
		this.xp = xp;
	}
	public static long getCurrentTime(){
		return System.currentTimeMillis()/1000L;
	}
	public Rank getRank(){
		if(this.plusExpireSeconds > getCurrentTime()){
			return Rank.PLUS;
		}else{
			return Rank.DEFAULT;
		}
	}
	public static long getHoursLeft(long epoch){
		long less = getCurrentTime();
		return (epoch-less) <= 0 ? 0L : (epoch-less)/3600L;
	}
	public static long getDifferrenceHours(long more, long less){
		return (more-less) <= 0 ? 0L : (more-less)/3600L;
	}
	public static long getDifferenceMinutes(long more, long less){
		return (more-less) <= 0 ? 0L : (more-less)/60L;
	}
	public static long addTime(long epoch, int hours, int days){
		long ep = epoch;
		ep += (hours * 3600);
		ep += (days * 24 * 3600);
		return ep;
	}
	public long getPlusMinutes(){
		return getDifferenceMinutes(this.getPlusEpoch(), getCurrentTime());
	}
	
	
}
