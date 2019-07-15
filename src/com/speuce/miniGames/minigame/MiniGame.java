package com.speuce.miniGames.minigame;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.speuce.cosmetics.CosmeticManager;
import com.speuce.miniGames.connect.Client.GameStatus;
import com.speuce.miniGames.main.MiniGameMain;
import com.speuce.miniGames.utils.AniFinish;
import com.speuce.miniGames.utils.Mini;
import com.speuce.miniGames.utils.Mod;
import com.speuce.miniGames.utils.Reward;
import com.speuce.sql.SQLManager;
import com.speuce.stats.StatsManager;

public abstract class MiniGame implements Listener, CommandExecutor{
	
	private MiniGameMain p;
	private int minPlayers;
	private int maxPlayers;
	private int currentPlayers = 0;
	private int countdown;
	private boolean isCounting;
	private String name;
	private boolean isStarted = false;
	private boolean freezeAll = false;
	private boolean forceStart = false;
	private int counter;
	private YamlConfiguration conf;
	private File path;
	private List<Player> participants;
	private List<Player> admins;
	private RewardManager rew;
	private boolean willFreeze = true;
	private StatsManager stats;
	private File gameFolder;
	private Mod modcmd;
	private MapManager mapman;
	private CosmeticManager cosman;
	public void enable(MiniGameMain p, int minPlayers, int maxPlayers, String name, int count, 
			SQLManager sm, StatsManager stats, Reward r){
		this.p = p;
		this.cosman = new CosmeticManager(sm, stats, p);
		this.minPlayers = minPlayers;
		this.maxPlayers = maxPlayers;
		this.name = name;
		this.rew = new RewardManager(stats, r, this, p);
		this.stats = stats;
		this.setCountdown(count);
		this.loadConf();
		this.gameFolder = new File(p.getDataFolder() + File.separator + name);
		this.modcmd = new Mod(this, stats);
		p.getCommand("mod").setExecutor(this.modcmd);
		p.getCommand("fstart").setExecutor(this);

		p.getServer().getPluginManager().registerEvents(this.modcmd, p);
		register();
		Bukkit.getServer().getPluginManager().registerEvents(this, this.p);
		this.onEnable(sm);
		this.getDelayedEnable().runTaskLater(p, 50L);
		this.mapman = new MapManager(this, p);
		this.mapman.load();
		p.getCommand("map").setExecutor(this.mapman);
	}
	public abstract List<String> getInfo();
	public abstract void onEnable(SQLManager sq);
	public abstract void start();
	
	//Empty Methods
	public void Disable(){
		// TODO this.sql.close();
	}
	public void onLeave(LeaveEvent e){}
	public void onJoin(JoinEvent e){}
	public void preStart(){}
	public void onDisable(){}
	public void delayedEnable(){}
	public void onMapChange(Map from, Map to){}

	public void registerCommand(String cmd, BukkitCommand claz){
		try {
			final Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
			bukkitCommandMap.setAccessible(true);
			CommandMap commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());
			commandMap.register(cmd, claz);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	public void RegisterListener(Listener l){
		this.p.getServer().getPluginManager().registerEvents(l, this.p);
	}
	public final void endGame(String winner){
		for(Player p: this.getParticipants()){
			p.setGameMode(GameMode.SPECTATOR);
			p.sendMessage(winner + ChatColor.GOLD.toString() + "has won the game!!");
		}
		this.cosman.disable();
		BukkitRunnable br = new BukkitRunnable(){
			int i = 0;
			@Override
			public void run() {
				i++;
				
				if(i == 10){
					for(Player p: getParticipants()){
						rew.giveRewards(p);
					}
				}
				World world = getMapMan().getCurrentMap().getWorld();
				Mini.spawnRandomFirework(Mini.getLocationRelativeWorldSpawn(30, world));
				Mini.spawnRandomFirework(Mini.getLocationRelativeWorldSpawn(30, world));
				Mini.spawnRandomFirework(Mini.getLocationRelativeWorldSpawn(30, world));
				if(i >= 240){
					this.cancel();
					p.getServer().shutdown();
				}				
			}
			
		};
		br.runTaskTimer(p, 40L, 10L);
	}
	public MapManager getMapMan(){
		return this.mapman;
	}
	public AniFinish getF(final Player p){
		return new AniFinish(){
			@Override
			public void onFinish() {
				BukkitRunnable br = new BukkitRunnable(){

					@Override
					public void run() {
						MiniGame.this.p.getClient().sendLobby(p);
						
					}
					
				};
				br.runTaskLater(MiniGame.this.p, 40L);
			}
		};
	}
	public StatsManager getStats(){
		return this.stats;
	}
	public void runTaskTimer(BukkitRunnable br, Long delay, Long Period){
		br.runTaskTimer(this.p, delay, Period);
	}
	public void runTaskLater(BukkitRunnable br, Long delay){
		br.runTaskLater(this.p, delay);
	}
	public void runTaskAsynchronously(BukkitRunnable br){
		br.runTaskAsynchronously(this.p);
	}
	
	private void loadConf(){
				if(!p.getDataFolder().exists()){
					p.getDataFolder().mkdir();
					p.getLogger().log(Level.INFO, "Created Plugin data folder.");
				}
				path = new File(p.getDataFolder().getPath(),name + ".yml");
				conf = YamlConfiguration.loadConfiguration(path);
				p.getLogger().log(Level.INFO, "Data file loaded!");
	}

	private boolean staffCheck(Player p, int power){
		if(this.stats.hasPlayerStats(p.getUniqueId())){
			return this.stats.getStaffRank(p.getUniqueId()).getPower() >= power;
		}else{
			p.sendMessage(ChatColor.RED + "Sorry, Data is not loaded yet!");
			return false;
		}
	}
	public void saveConfig(){
		try {
			this.conf.save(this.path);
			this.p.getLogger().log(Level.INFO, "Saving data..");
		} catch (IOException e) {
			this.p.getLogger().log(Level.SEVERE, "");
			this.p.getLogger().log(Level.SEVERE, " -----> COULD NOT SAVE DATA <----");
			this.p.getLogger().log(Level.SEVERE, "");
			e.printStackTrace();
		}
	}
	

	private void ccast(int counter){
		bcast(ChatColor.DARK_PURPLE.toString() + "Game starting in " + ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD.toString() + counter + ChatColor.RESET.toString() + ChatColor.DARK_PURPLE.toString() + " seconds!"  );
	}
	
	public void bcast(String s){
		Bukkit.broadcastMessage(ChatColor.AQUA.toString() + ChatColor.BOLD.toString() + this.name + ChatColor.RESET.toString() + ChatColor.RED.toString() + " » " + s);
	}
	
	public void pcast(Float f, Sound s){
		for(Player p : this.getParticipants()){
			p.playSound(p.getLocation(), s, 2.0F, f);
		}
	}
	
	private void register(){
		this.p.getCommand("list").setExecutor(this);
		this.participants = new ArrayList<Player>();
		this.admins = new ArrayList<Player>();
		Collection<? extends Player> c = Bukkit.getOnlinePlayers();
		for(Player p: c){
			this.participants.add(p);
		}
		//this.lateEnable();
		
	}
	
	@EventHandler
	public void onLogin(PlayerLoginEvent e){
		if(this.currentPlayers == this.maxPlayers){
			if(!e.getPlayer().isOp()){
				e.setKickMessage("Game Full!");
				e.setResult(Result.KICK_FULL);
				return;
			}
		if(this.isStarted){
			if(!e.getPlayer().isOp()){
					e.setKickMessage("Game Already Started!");
					e.setResult(Result.KICK_OTHER);
					return;
				}
			}
		}

	}

	@EventHandler
	public void onMove(PlayerMoveEvent e){
		if(this.freezeAll && this.participants.contains(e.getPlayer())){
			e.setCancelled(true);
		}
	}
	@EventHandler
	public void onDamage(EntityDamageEvent e){
		if(!this.isStarted){
			e.setCancelled(true);
			return;
		}
	}
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e){
		e.setJoinMessage("");
		this.join(e.getPlayer());
		this.onJoin(new JoinEvent(e.getPlayer()));
	}
	private void join(Player p){
		p.setHealth(p.getMaxHealth());
		p.setFoodLevel(20);
		p.teleport(this.getCurrentMap().getWorld().getSpawnLocation());
		if(this.isStarted){
			this.currentPlayers += 1;
			Mini.setSpec(p);
			return;
		}
		p.setGameMode(GameMode.SURVIVAL);
		p.setAllowFlight(true);
		this.participants.add(p);
		this.currentPlayers += 1;
		if(this.currentPlayers >= this.getMinPlayers()){
			this.bcast(ChatColor.GOLD.toString() + "Player " + ChatColor.YELLOW.toString() + 
					p.getDisplayName() + ChatColor.GOLD.toString() + " has joined! " +
					ChatColor.GREEN.toString() + this.getCurrentPlayers() + ChatColor.AQUA.toString() +
					"/" + ChatColor.YELLOW.toString() + this.maxPlayers);
			if(!this.isCounting && !this.isStarted){
				this.startCountDown(this.countdown);
			}
		}else{
			this.bcast(ChatColor.GOLD.toString() + "Player " + ChatColor.YELLOW.toString() + 
					p.getDisplayName() + ChatColor.GOLD.toString() + " has joined! " +
					ChatColor.RED.toString() + this.getCurrentPlayers() + ChatColor.AQUA.toString() +
					"/" + ChatColor.YELLOW.toString() + this.maxPlayers);
		}
	}
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent e){
		e.setQuitMessage("");
		this.leave(e.getPlayer());
		this.onLeave(new LeaveEvent(e.getPlayer()));
	}
	private void leave(Player p){
		rew.out(p);
		if(!this.participants.contains(p)){
			return;
		}
		this.participants.remove(p);
		this.currentPlayers -= 1;
		if(this.currentPlayers < this.minPlayers){
			if(this.isCounting && (!this.forceStart || this.currentPlayers == 0)){
				this.bcast(ChatColor.RED.toString() + "Player " +
			ChatColor.DARK_RED.toString() + p.getDisplayName() + 
			ChatColor.RED.toString() + " Has left! " + ChatColor.DARK_RED.toString() + 
			"Countdown Stopped! " + ChatColor.RED.toString() + this.getCurrentPlayers() + ChatColor.AQUA.toString() +
			"/" + ChatColor.YELLOW.toString() + this.maxPlayers);
			}else{
				this.bcast(ChatColor.RED.toString() + "Player " +
			ChatColor.DARK_RED.toString() + p.getDisplayName() + 
			ChatColor.RED.toString() + " Has left! " 
			+ ChatColor.RED.toString() + this.getCurrentPlayers() + ChatColor.AQUA.toString() +
			"/" + ChatColor.YELLOW.toString() + this.maxPlayers);
			}
			this.isCounting = false;
			this.forceStart = false;
		}else{
			this.bcast(ChatColor.RED.toString() + "Player " +
		ChatColor.DARK_RED.toString() + p.getDisplayName() + 
		ChatColor.RED.toString() + " Has left! " + ChatColor.GREEN.toString() + this.getCurrentPlayers() + ChatColor.AQUA.toString() +
		"/" + ChatColor.YELLOW.toString() + this.maxPlayers);
		}
		World wo = p.getWorld();

		if(wo.getPlayers().isEmpty()){
			Bukkit.getServer().unloadWorld(wo, true);
		}
		if(Bukkit.getOnlinePlayers().size() - 1 == 0 && this.isStarted){
			Bukkit.getServer().shutdown();
		}
	}
	public boolean getAdminMode(Player p){
		return admins.contains(p);
	}
	public void setAdminMode(Player p){
		if(this.admins.contains(p)){
			this.admins.remove(p);
			p.sendMessage(ChatColor.GREEN.toString() + ChatColor.BOLD.toString() + "Admin Mode disabled!");
			for(Player pl: this.getParticipants()){
				pl.showPlayer(p);
			}
			this.join(p);
		}else{
			this.admins.add(p);
			p.sendMessage(ChatColor.GREEN.toString() + ChatColor.BOLD.toString() + "Admin Mode enabled");
			p.setGameMode(GameMode.CREATIVE);
			for(Player pl: this.getParticipants()){
				pl.hidePlayer(p);
			}
			this.leave(p);
		}
	}
	public void stopCountDown(){
		this.isCounting = false;
	}
	public boolean IsCounting(){
		return this.isCounting;
	}

	private void startCountDown(final int count){
		this.counter = count;
		this.isCounting = true;
		BukkitRunnable r = new BukkitRunnable(){
			@Override
			public void run() {
				if(counter != 0){
					if(!isCounting){
						this.cancel();
					}

					if(counter <= 5){
						ccast(counter);
						pcast(0.7F, Sound.BLOCK_NOTE_BLOCK_PLING);
					}else if(counter % 10 == 0){
						ccast(counter);
						pcast(0.7F, Sound.BLOCK_NOTE_BLOCK_PLING);
					}
					counter -= 1;
				}else{
	
					isStarted = true;
					pcast(1.5F, Sound.ENTITY_PLAYER_LEVELUP);
					this.cancel();
					setGameStatus(GameStatus.START);
					for(Player p: getParticipants()){
						p.setAllowFlight(false);
						p.setHealth(p.getMaxHealth());
						p.setFoodLevel(20);
					}
					preStart();
					if(willFreeze){
						freeze();
					}else{
						for(Player p: getParticipants()){
							Mini.sendActionBar(p, ChatColor.RED.toString() + "Game Started!");
							p.setHealth(p.getMaxHealth());
							p.setFoodLevel(20);
							p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 5F, 2F);
							for(Player pl : getParticipants()){
								if(pl != p){
									p.showPlayer(pl);
								}
							}
						}
						setFreezeAll(false);
						setGameStatus(GameStatus.INGAME);
						broadcastInfo();
						start();
	
					}
					this.cancel();
					stopCountDown();
				}

			}			
		};
		r.runTaskTimer(this.p, 20L, 20L);
	}
	private void broadcastInfo(){
		for(String s: this.getInfo()){
			Bukkit.broadcastMessage(s);
		}
	}
	private void freeze(){
		this.setFreezeAll(true);
		broadcastInfo();
		BukkitRunnable freezer = new BukkitRunnable(){
			int i = 6;
			@Override
			public void run() {
				if(i > 0){
					String s = ChatColor.GREEN.toString() + i;
					for(Player p: getParticipants()){
						Mini.sendActionBar(p, s);
						p.playSound(p.getLocation(), Sound.BLOCK_DISPENSER_FAIL, 5F, 2F);
					}
					i--;
				}else{
					for(Player p: getParticipants()){
						Mini.sendActionBar(p, ChatColor.RED.toString() + "Game Started!");
						p.setHealth(p.getMaxHealth());
						p.setFoodLevel(20);
						p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 5F, 2F);
						for(Player pl : getParticipants()){
							if(pl != p){
								p.showPlayer(pl);
							}
						}

					}
					setFreezeAll(false);
					setGameStatus(GameStatus.INGAME);
					start();
					this.cancel();
				}
				
			}
			
		};
		freezer.runTaskTimer(this.p, 20L, 20L);
		
	}	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if(cmd.getName().equalsIgnoreCase("list")){
			//TODO
		}else if(cmd.getName().equalsIgnoreCase("setmap")){
			if(this.isStarted){
				sender.sendMessage(ChatColor.RED + "Game already started.");
				return true;
			}
			if(args.length != 1){
				sender.sendMessage(ChatColor.RED + "Usage: /map <mapname>");
				return true;
			}
		}else if(cmd.getName().equalsIgnoreCase("fstart")){
			if(this.isStarted){
				sender.sendMessage(ChatColor.RED + "Game already started.");
				return true;
			}
			if(sender instanceof Player){
				Player p = (Player) sender;
				if(staffCheck(p, 6)){
					if(args.length == 1){
						if(this.currentPlayers == 0){
							p.sendMessage(ChatColor.RED + "C'mon you need SOME players");
							return true;
						}
						Integer i;
						try{
							i = Integer.parseInt(args[0]);
						}catch(NumberFormatException e){
							p.sendMessage(ChatColor.RED + "ERROR: Number Format Exception.");
							return true;
						}
						if(i > 0){
							Bukkit.broadcastMessage(ChatColor.GOLD.toString() + ChatColor.BOLD.toString() + p.getDisplayName() + ChatColor.GREEN.toString() + " has set the timer to: " + ChatColor.AQUA.toString() + ChatColor.BOLD.toString() + i);
							if(this.isCounting){
								this.counter = i;
								return true;
							}else{
								this.startCountDown(i);
								this.forceStart = true;
								return true;
							}

						}else{
							p.sendMessage(ChatColor.RED + "Countdown Cannot be zero.");
							return true;
						}
					}else{
						return false;
					}
				}else{
					p.sendMessage(ChatColor.RED + "Insufficient Privalages");
					return true;
				}
			}else{
				sender.sendMessage(ChatColor.RED + "You aint no playa!");
				return true;
			}
		}
		return false;
	}
	
	//Setters
	public void setWillfreeze(boolean willFreeze){
		this.willFreeze = willFreeze;
	}
	public void setParticipants(List<Player> participants) {
		this.participants = participants;
	}
	public List<Player> getParticipants() {
		return participants;
	}
	public void setCountdown(int countdown) {
		this.countdown = countdown;
	}
	public void setMaxPlayers(int maxPlayers){
		this.maxPlayers = maxPlayers;
	}
	public void setMinPlayers(int minPlayers){
		this.minPlayers = minPlayers;
	}
	private void setGameStatus(GameStatus g){
		this.p.getClient().sendString("status:" + g.getName());
	}
	public void setConf(YamlConfiguration f){
		this.conf = f;
	}
	public void setFreezeAll(boolean b){
		this.freezeAll = b;
	}
	
	//Getters
	public RewardManager getRewardManager(){
		return this.rew;
	}
	public JavaPlugin getPlugin(){
		return this.p;
	}
	public String getName(){
		return this.name;
	}
	public File getConfFolder(){
		return this.gameFolder;
	}
	public int getCurrentPlayers(){
		return this.currentPlayers;
	}
	public int getCountdown() {
		return countdown;
	}
	public int getMinPlayers(){
		return this.minPlayers;
	}
	public int getMaxPlayers(){
		return this.maxPlayers;
	}
	public YamlConfiguration getConf(){
		return this.conf;
	}
	public com.speuce.miniGames.minigame.Map getCurrentMap(){
		return this.getMapMan().getCurrentMap();
	}
	private BukkitRunnable getDelayedEnable(){
		return new BukkitRunnable(){

			@Override
			public void run() {
				delayedEnable();
			}
			
		};
	}
	public boolean getFreezeAll(){
		return this.freezeAll;
	}
	public World getCurrentMapWorld(){
		return this.getCurrentMap().getWorld();
	}
	
	//Nested Classes
	public class JoinEvent{
		Player pl;
		public JoinEvent(Player pl){
			this.pl = pl;
		}
		public Player getPlayer(){
			return pl;
		}
		
	}
	public class LeaveEvent{
		Player pl;
		public LeaveEvent(Player pl){
			this.pl = pl;
		}
		public Player getPlayer(){
			return pl;
		}		
	}

}
