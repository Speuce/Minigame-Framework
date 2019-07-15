package com.speuce.miniGames.main;

import java.util.List;
import java.util.logging.Level;

import org.bukkit.plugin.java.JavaPlugin;

import com.speuce.miniGames.connect.Client;
import com.speuce.miniGames.connect.Client.ClientIt;
import com.speuce.miniGames.minigame.MiniGame;
import com.speuce.miniGames.minigame.WorldManager;
import com.speuce.miniGames.utils.Reward;
import com.speuce.sql.SQLManager;
import com.speuce.stats.StatsManager;
import com.speuce.upgrades.ExampleStore;



public class MiniGameMain extends JavaPlugin{
	private Client c;
	List<String> pickList;
	MiniGame currentGame;
	Debug d;
	GamesLoader load;
	SQLManager sm;
	StatsManager st;
	Reward r;
	ExampleStore ex;
	//TODO remove ex
	@Override
	public void onEnable(){
		d = new Debug(this);
		sm = new SQLManager(this);
		st = new StatsManager(sm, this);
		this.r = new Reward(st, this);
		load = new GamesLoader(this, sm, st, this.r);
		this.c = new Client(this, "127.0.0.1", 3557);
		WorldManager w = new WorldManager();
		this.getCommand("w").setExecutor(w);
		this.getCommand("set").setExecutor(st);
		this.getCommand("stats").setExecutor(st);

		this.ex = new ExampleStore(this, this.st, this.sm);
		this.getCommand("reward").setExecutor(this.r);
		this.getCommand("exp").setExecutor(this.r);

		this.doOne();


	}
	@Override
	public void onDisable(){
		this.c.disable();
		this.currentGame.Disable();
		this.currentGame.onDisable();
		this.load.disable();
	}
	public Client getClient(){
		return this.c;
	}
	public Debug getDebug(){
		return this.d;
	}
	public StatsManager getStats(){
		return this.st;
	}
	private void log(Level l, String s){
		this.getLogger().log(l, s);
	}
	private void doOne(){
		this.c.sendStringNoIDs("gamepick;" + this.load.getGameString(), new ClientIt(){

			@Override
			public void onServerRecieve(String s) {
				if(s == null){
					log(Level.SEVERE, " SERVER RETURNED NULL");
				}else{
					doRest(s);
				}
				
			}
			
		});
	}
	
	private void doRest(String s){
		this.log(Level.INFO, "server returned: " + s);
		if(this.load.PlayGame(s)){
			this.currentGame = this.load.getGame();
			this.c.enable(s);
		}else{
			this.load.removeGame(s);
			this.doOne();
		}
	}
}
