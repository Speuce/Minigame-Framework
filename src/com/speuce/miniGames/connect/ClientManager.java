package com.speuce.miniGames.connect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.speuce.miniGames.connect.Client;
import com.speuce.miniGames.connect.Client.ClientIt;
import com.speuce.miniGames.connect.Client.ClientStatus;
import com.speuce.miniGames.connect.Client.GameStatus;

public class ClientManager implements ClientIt{
	Client c;
	JavaPlugin p;
	Map<Integer, ClientInstance> lobby;
	Map<Integer, ClientInstance> mini;
	Map<String, List<Integer>> games;
	private boolean update = false;
	public ClientManager(JavaPlugin p,Client c){
		this.c = c;
		this.p = p;
		this.lobby = new HashMap<Integer, ClientInstance>();
		this.mini = new HashMap<Integer, ClientInstance>();
		this.games = new HashMap<String, List<Integer>>();
	}
	public Map<Integer, ClientInstance> getLobbies(){
		return this.lobby;
	}
	public Map<Integer, ClientInstance> getMinis(){
		return this.mini;
	}
	
	
	@Override
	public void onServerRecieve(String s) {
		if(s.startsWith("error")){
			p.getLogger().log(Level.SEVERE, s);
			return;
		}else if(s.startsWith("success")){
			//this.p.getLogger().log(Level.INFO, "success");
			return;
		}else if(s.equalsIgnoreCase("new connection")){
			if(update){
				c.disableUpdate();
			}
			p.getLogger().log(Level.INFO, "Server did not reconize connection, sending new connection!");
			this.c.sendString("new client:" + c.getName() + ":" + Bukkit.getOnlinePlayers().size() + ":" + "5"+ ":" + "mini" + ":" + c.getGame());
			this.c.startUpdater();
			update = true;
			return;

			
		}else if(s.startsWith("lobby:")){	
			this.lobby = new HashMap<Integer, ClientInstance>();
			String[] args = s.split(":");
			for(int i = 1; i >= args.length - 1; i++){
				String s1 = args[i];
				String[] ss = s1.split("-");
				ClientStatus g = ClientStatus.fromString(ss[2]);
				GameStatus gs = GameStatus.fromString(ss[5]);
				if((!g.equals(null)) && (gs.equals(GameStatus.LOBBY))){
					ClientInstance ci = new ClientInstance(ss[1], Integer.parseInt(args[3]), Integer.parseInt(args[4]));
					int i1 = Integer.parseInt(ss[0]);
					ci.setStatus(gs);
					ci.setConnectionStatus(g);
					lobby.put(i1, ci);
				}else{
					p.getLogger().log(Level.INFO, "Error adding lobby server to cache: clientstatus not reconized: " + ss[1] + " or game status not reconized: " + ss[5]);
				}

			}
		}else if(s.startsWith("mini:")){	
			this.mini = new HashMap<Integer, ClientInstance>();
			String[] args = s.split(":");
			for(int i = 1; i >= args.length - 1; i++){
				String s1 = args[i];
				String[] ss = s1.split("-");
				ClientStatus g = ClientStatus.fromString(ss[2]);
				GameStatus gs = GameStatus.fromString(ss[5]);
				
				if((!g.equals(null)) && (gs.equals(GameStatus.LOBBY))){
					ClientInstance ci = new ClientInstance(ss[1], Integer.parseInt(args[3]), Integer.parseInt(args[4]));
					int i1 = Integer.parseInt(ss[0]);
					ci.setStatus(gs);
					ci.setConnectionStatus(g);
					mini.put(i1, ci);
					if(this.games.containsKey(args[6])){
						List<Integer> toadd = this.games.get(args[6]);
						toadd.add(i1);
						this.games.put(args[6], toadd);
					}else{
						List<Integer> toadd = new ArrayList<Integer>();
						toadd.add(i1);
						this.games.put(args[6], toadd);
					}
				}else{
					p.getLogger().log(Level.INFO, "Error adding mini server to cache: clientstatus not reconized: " + ss[1]);
				}

			}
		}else{
			p.getLogger().log(Level.SEVERE, "server sent unknown response. : " + s);
			return;	
		}
		
	}
}
