package com.speuce.miniGames.connect;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.speuce.miniGames.main.Debug;
import com.speuce.miniGames.main.MiniGameMain;



public class Client implements PluginMessageListener, Listener{

	private MiniGameMain p;
	private int sendPort;
	private int id;
	private String host;
	private String name;
	public boolean nameRequested = false;
	private ClientManager m;
	private BukkitRunnable updater;
	private String game;
	private File path;
	private Debug d;
	private FileConfiguration conf;
	private boolean updaterIsRunning = false;
	public Client(MiniGameMain p,String host, int sendPort){
		this.name = new String(); 
		this.p = p;
		this.d = p.getDebug();
		this.sendPort = sendPort;
		this.id = 0;
		this.host = host;
		this.updater = this.getUpdater();
		this.m = new ClientManager(p, this);




	}
	public void enable(String game){
		this.game = game;
		p.getServer().getPluginManager().registerEvents(this, p);
		p.getServer().getMessenger().registerOutgoingPluginChannel(p, "BungeeCord");
		p.getServer().getMessenger().registerIncomingPluginChannel(p, "BungeeCord", this);
		if(!this.p.getDataFolder().exists()){
			this.p.getDataFolder().mkdir();
		}
		this.path = new File(p.getDataFolder().getAbsolutePath() + File.separator + "server.yml");
		if(!this.path.exists()){
			try {
				this.path.createNewFile();
			} catch (IOException e) {
				
			}
		}
		this.conf = YamlConfiguration.loadConfiguration(this.path);
		if(!conf.contains("name")){
			conf.createSection("name");
		}else{
			String s = conf.getString("name");
			if(s != "" && s != null){
				this.name = s;
				this.nameRequested = true;
				this.p.getLogger().log(Level.INFO, "got name from file:" + s);
				 sendString("update players:" + "0");
			}
		}
	}
	
	public String getGame(){
		return this.game;
	}
	
	
	public void startUpdater(){
		this.d.log("started updating thread..");
		if(!this.updaterIsRunning){
			this.updater.runTaskTimer(this.p, 10L, 200L);
			this.updaterIsRunning = true;
		}

	}
	
	
	public void delayedSend(final String send, long wait){
		BukkitRunnable br = new BukkitRunnable(){

			@Override
			public void run() {
				sendString(send);
				
			}
			
		};
		br.runTaskLater(this.p, wait);
	}
	@EventHandler
	public void onLeave(PlayerQuitEvent e){
		e.setQuitMessage("");
		this.updatePlayers();
	}
	private void updatePlayers(){
		this.sendString("update players:" + (Bukkit.getOnlinePlayers().size() - 1));
	}
	private void sendStringNoReturn(final String s){
		Socket server = null;
		try{
			server = new Socket(host, sendPort);
			DataOutputStream out = new DataOutputStream(server.getOutputStream());
			p.getLogger().log(Level.INFO, "sent: " + s);
			out.writeUTF(id + ";" + s);
		}catch(IOException e){
			p.getLogger().log(Level.SEVERE, "Could not find server.. ;-;");
			return;
		}finally{
			try {
				if(server != null){
					server.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
	public void sendStringNoIDs(final String st, final ClientIt receive){
		BukkitRunnable run = new BukkitRunnable(){
			@Override
			public void run() {
				Socket server = null;
				try{
					server = new Socket(host, sendPort);
				}catch(IOException e){
					d.severeLog("Could not find server.. ;-;");
					return;
				}
				try {
					DataOutputStream out = new DataOutputStream(server.getOutputStream());
					out.writeUTF(st);
					DataInputStream input = new DataInputStream(server.getInputStream());
					String in = input.readUTF();
					receive.onServerRecieve(in);
					out.close();
					input.close();
					server.close();
					this.cancel();
				} catch (IOException e) {
					p.getLogger().log(Level.SEVERE, "error connecting to server..");
					this.cancel();
				}
				
			}

		};
		run.runTaskAsynchronously(this.p);
	}
	private void save(){
		if(this.nameRequested){
			this.conf.set("name", this.name);
		}
		try {
			this.conf.save(this.path);
		} catch (IOException e) {
			this.p.getLogger().log(Level.INFO, "Couldn't save config");
			e.printStackTrace();
		}


	}
	
	public void disable(){
		this.save();
		this.updater.cancel();
		this.sendStringNoReturn("off");
	}
	public void disableUpdate(){
		this.p.getLogger().log(Level.INFO, "Closed updating thread..");
		this.updaterIsRunning = false;
		this.updater.cancel();
	}
	public void setGame(String s){
			this.game = s;
			this.sendString("game " + s);
		
	}
	@EventHandler
	public void onJoin(final PlayerJoinEvent e){
		if(!this.nameRequested){
			BukkitRunnable br = new BukkitRunnable(){

				@Override
				public void run() {
					requestName(e.getPlayer());
				}
				
			};
			br.runTaskLater(this.p, 2L);
		}else{
			sendString("update players:" + Bukkit.getOnlinePlayers().size());
		}
	}
	public void sendString(String st){
		this.sendString(st, this.m);
	}
	
	private BukkitRunnable getUpdater(){
		return new BukkitRunnable(){

			@Override
			public void run() {
				sendString("packet");
			}
			
		};
	}
	
	
	
	public void sendString(String st, final ClientIt receive){
		final String s = st.replace(";", "");
		BukkitRunnable run = new BukkitRunnable(){
			@Override
			public void run() {
				Socket server = null;
				try{
					server = new Socket(host, sendPort);
				}catch(IOException e){
					p.getLogger().log(Level.SEVERE, "Could not find server.. ;-;");
					return;
				}
				try {
					DataOutputStream out = new DataOutputStream(server.getOutputStream());
					out.writeUTF(id + ";" + s);
					DataInputStream input = new DataInputStream(server.getInputStream());
					String[] in = input.readUTF().split(";");
					int newId = Integer.parseInt(in[0]);
					if(newId != id){
						setId(newId);
						p.getLogger().log(Level.INFO, "Got new id: " + newId);
					}
					receive.onServerRecieve(in[1]);
					out.close();
					input.close();
					server.close();
					this.cancel();
				} catch (IOException e) {
					p.getLogger().log(Level.SEVERE, "");
					p.getLogger().log(Level.SEVERE, "error connecting to server..");
					p.getLogger().log(Level.SEVERE, "");
					e.printStackTrace();
					this.cancel();
				}
				
			}

		};
		run.runTaskAsynchronously(this.p);
	}
	public void setId(int id){
		this.id = id;
	}
	
	public ClientManager getManager(){
		return this.m;
	}
	
	private void requestName(Player p){
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		  out.writeUTF("GetServer");
		  p.sendPluginMessage(this.p, "BungeeCord", out.toByteArray());
		  this.p.getLogger().log(Level.INFO, "Requested client name..");
		

		
	}
	public String getName(){
		return this.name;
	}
	
	public interface ClientIt{
		void onServerRecieve(String s);
		
	}
	
	

	@Override
	public void onPluginMessageReceived(String channel, Player p, byte[] msg) {
	    if (!channel.equals("BungeeCord")) {
	        return;
	      }
	    ByteArrayDataInput in = ByteStreams.newDataInput(msg);
	    String subchannel = in.readUTF();
	    if(subchannel.equals("GetServer")){
	    	String ss = in.readUTF();
	    	this.name = ss;
			this.nameRequested = true;
	    	this.p.getLogger().log(Level.INFO, "Name Data received: " + ss);
			this.delayedSend("update players:" + Bukkit.getOnlinePlayers().size(), 20L);
	    }
	    
		
	}
	public void connect(Player p, String server){
		p.sendMessage(ChatColor.GREEN + "Sending you to server: " + server);
		  ByteArrayDataOutput out = ByteStreams.newDataOutput();
		  out.writeUTF("Connect");
		  out.writeUTF(server);
		  p.sendPluginMessage(this.p, "BungeeCord", out.toByteArray());

	}
	public void sendLobby(final Player p){
		this.sendString("findlobby", new ClientIt(){

			@Override
			public void onServerRecieve(String s) {
				if(s == null || s.equals("null")){
					p.kickPlayer(ChatColor.RED + "Could not find a lobby server for you to connect to");
					return;
				}else{
					connect(p, s);
				}
				
			}});
	}
	public enum ClientStatus {
		GOOD("good"),
		SLOW("slow"),
		POOR("poor"),
		DEAD("dead");
		String s;
		private ClientStatus(String s){
			this.s = s;
		}
		@Override
		public String toString(){
			return s;
		}
		
		public static ClientStatus fromString(String n){
			for(ClientStatus g : ClientStatus.values()){
				if(g.toString().equalsIgnoreCase(n)){
					return g;
				}
			}
			return null;
		}
	}
	public enum GameStatus {
		LOBBY("lobby"),
		START("start"),
		INGAME("ingame"),
		CLEANUP("cleanup"),
		RELOAD("reload");
		String name;
		private GameStatus(String name){
			this.name = name;
		}
		public String getName(){
			return this.name;
		}
		
		public static GameStatus fromString(String n){
			for(GameStatus g : GameStatus.values()){
				if(g.getName().equalsIgnoreCase(n)){
					return g;
				}
			}
			return null;
		}
	}

}
