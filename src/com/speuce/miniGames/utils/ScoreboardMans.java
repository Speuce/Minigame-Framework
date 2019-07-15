package com.speuce.miniGames.utils;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class ScoreboardMans {
	
	Map<Integer, String> places;
	String title;
	Scoreboard sc;
	Objective o;
	Objective buffer;
	boolean buff = false;
	public ScoreboardMans(String title){
		this.title = title;
		this.places = new HashMap<Integer, String>();
		this.sc = Bukkit.getScoreboardManager().getNewScoreboard();
		this.buffer.setDisplayName(title);
		o.setDisplaySlot(DisplaySlot.SIDEBAR);
		
	}
	public void set(int place, String thing){
		this.places.put(place, thing);
		if(!this.buff){
			this.buffer = sc.registerNewObjective(title, "dummy");
			this.buffer.setDisplayName(title);
			for(int i: places.keySet()){
				String s = places.get(i);
				buffer.getScore(s).setScore(i);
			}
			buffer.setDisplaySlot(DisplaySlot.SIDEBAR);
			this.buff = true;
		}else{
			this.o = sc.registerNewObjective(title, "dummy");
			this.o.setDisplayName(title);
			for(int i: places.keySet()){
				String s = places.get(i);
				o.getScore(s).setScore(i);
			}
			o.setDisplaySlot(DisplaySlot.SIDEBAR);
			this.buff = false;
		}

		
		
		
	}
	
	public String getTitle(){
		return this.title;
	}
	public void sendScoreboardAll(){
		for(Player p: Bukkit.getOnlinePlayers()){
			p.setScoreboard(sc);
		}
	}
	public void sendScoreboard(Player p){
		p.setScoreboard(sc);
	}
}
