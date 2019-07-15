package com.speuce.miniGames.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;

import com.speuce.miniGames.minigame.MiniGame;
import com.speuce.miniGames.utils.Reward;
import com.speuce.sql.SQLManager;
import com.speuce.stats.StatsManager;

public class GamesLoader {
	
	private MiniGameMain p;
	private File folder;
	private List<String> games;
	private Debug d;
	private MiniGame ms;
	private SQLManager m;
	private StatsManager stats;
	private Reward r;
	public GamesLoader(MiniGameMain p, SQLManager m, StatsManager stats, Reward r){
		this.p = p;
		this.m = m;
		this.r = r;
		this.stats = stats;
		this.games = new ArrayList<String>();
		this.d = p.getDebug();
		if(!p.getDataFolder().exists()){
			p.getDataFolder().mkdir();
		}
		this.folder = new File(p.getDataFolder().getAbsolutePath() + File.separator + "games");
		if(!this.folder.exists()){
			this.folder.mkdir();
			p.getLogger().log(Level.SEVERE, "folder didn't exsist, serverhalted.");
			System.exit(0);
			return;
		}else{
			if(!(this.folder.list().length > 0)){
				p.getLogger().log(Level.SEVERE, "folder is empty, server halted.");
				System.exit(0);
				return;
			}else{
				File[] files = this.folder.listFiles();
				for(File f : files){
					if(f.getName().endsWith(".jar")){
						String[] tt = f.getName().split("\\.");
						games.add(tt[0]);
					}
				}
			}
		}
	}
	public void disable(){
		this.ms = null;
		this.folder = null;
	}
	
	public void removeGame(String s){
		if(this.games.contains(s)){
			this.games.remove(s);
			if(this.games.isEmpty()){
				p.getLogger().log(Level.SEVERE, "Ran out of Playable Games. server halted.");
				System.exit(0);
			}
		}
	}
	
	public List<String> getGames(){
		return this.games;
	}
	public String getGameString(){
		String toRet = new String();
		for(String s : this.games){
			toRet += s + ":";
		}
		return toRet;
	}
	public MiniGame getGame(){
		return this.ms;
	}
	
	public boolean PlayGame(String s){
		if(this.games.contains(s)){
			JarFile j;
			try {
				j = new JarFile(this.folder + File.separator + s + ".jar");
				
			} catch (IOException e) {
				p.getLogger().log(Level.SEVERE, "Couldn't Load game: " + s);
				e.printStackTrace();
				return false;
			}
			JarEntry ent = j.getJarEntry("game.txt");
			if(ent == null){
				p.getLogger().log(Level.SEVERE, "Game: " + s + " doesn't contain game.txt");
				try {
					j.close();
				} catch (IOException e) {
					d.log("COULDNT CLOSE J");
					e.printStackTrace();
				}
				return false;
			}
			try {
				j.close();
			} catch (IOException e1) {
				d.log("COULDNT CLOSE J");
				e1.printStackTrace();
				return false;
			}
			
			
			
			URL url;
			try {
				url = new URL("jar:file:" + this.folder + File.separator + s + ".jar!/" + "game.txt");

			} catch (MalformedURLException e) {
				d.severeLog("MALFORMED URL 1");
				e.printStackTrace();
				return false;
			}
			BufferedReader reader;
			try {
				reader = new BufferedReader(new InputStreamReader(url.openStream()));
			} catch (IOException e) {
				d.severeLog("IO EXCEPTION WHILST CREATING A BUFFERED READER FROM URL: " + url.toString());
				e.printStackTrace();
				return false;
			}
			
			
			
			String mainClassPath = null;
			String name = null;
			int min = 0;
			int max = 0;
			int count = 0;
			try {
				String line;
				while((line = reader.readLine()) != null){
					String[] temp1 = line.split(":");
					if(temp1.length == 2){
						if(line.startsWith("main")){
							mainClassPath = temp1[1];
						}else if(line.startsWith("name")){	
							name = temp1[1];
						}else if(line.startsWith("min")){		
							min = Integer.parseInt(temp1[1]);
						}else if(line.startsWith("max")){	
							max = Integer.parseInt(temp1[1]);
						}else if(line.startsWith("count")){	
							count = Integer.parseInt(temp1[1]);
						}else{
							d.log("tag " + line + "not reconized");
						}
					}else{
						d.log("wrong args length in: " + line);
					}
				}
			} catch (IOException e) {
				d.severeLog("IOEXCEPTION whilst going through the buffered reader");
				e.printStackTrace();
				return false;
			}
			if(mainClassPath == null || name == null || max == 0 || min == 0 || count == 0){
				p.getLogger().log(Level.INFO, "Couln't Locate the one or more tags in game.txt :(");
				return false;
			}
			
			
			
			URL[] url2 = new URL[1];
			try {
				p.getLogger().log(Level.INFO, "jar:file:" + this.folder + File.separator + s + ".jar!/");
				url2[0] = new URL("jar:file:" + this.folder + File.separator + s + ".jar!/");
			} catch (MalformedURLException e) {
				d.severeLog("MALFORMED URL 2");
				e.printStackTrace();
				return false;
			}
			
			
			
			URLClassLoader loader = URLClassLoader.newInstance(url2, this.getClass().getClassLoader());
			Class<?> cl;
			try {
				cl = loader.loadClass(mainClassPath);
			} catch (ClassNotFoundException e) {
				p.getLogger().log(Level.SEVERE, "COULD NOT FIND CLASS: " + mainClassPath);
				e.printStackTrace();
				return false;
			}
			Class<? extends MiniGame> c;
			if(MiniGame.class.isAssignableFrom(cl)){
				try {
					c = cl.asSubclass(MiniGame.class);
				} catch (ClassCastException ex) {
					p.getLogger().log(Level.SEVERE, "Class: " + mainClassPath + " does not extend MiniGame!");
					ex.printStackTrace();
					return false;
				}
				try {
					this.ms = c.newInstance();
				} catch (InstantiationException | IllegalAccessException e) {
					p.getLogger().log(Level.SEVERE, "Class: " + mainClassPath + " cant be instantiated!");
					e.printStackTrace();
					return false;
				}
				
				
				
				this.ms.enable(this.p, min, max, name, count, this.m, this.stats, r);
				p.getLogger().log(Level.INFO, "enabled game: " + name + " !!! :D");
				
				
				
				
				
				try {
					//Closing & setting everything to null, just incase..
					reader.close();
					j = null;
					ent = null;
					url = null;
					reader = null;
					url2 = null;
					cl = null;
					c = null;
				} catch (IOException ignored) {
					
				}
				return true;

			}
			try {
				j.close();
			} catch (IOException e) {
				p.getLogger().log(Level.SEVERE, "couldn't close j");
				e.printStackTrace();
				return false;
			}
			
			
			
		}else{
			d.log("game: " + s + " not found!");
			return false;
		}
		return true;
	}
	
	
	
}
