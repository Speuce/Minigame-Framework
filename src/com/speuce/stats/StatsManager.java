package com.speuce.stats;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.speuce.sql.ASyncQuery;
import com.speuce.sql.DataType;
import com.speuce.sql.Query;
import com.speuce.sql.Rank;
import com.speuce.sql.SQLManager;
import com.speuce.sql.TableCheck;
import com.speuce.sql.UUIDConverter;
import com.speuce.sql.ValueCheck;
import com.speuce.sql.booleanQuery;



@SuppressWarnings("deprecation")
public class StatsManager implements Listener,CommandExecutor{
	private SQLManager sql;
	private JavaPlugin p;
	private Map<UUID, Stats> playerStats;
	List<StatsJoin> toCall;
	public StatsManager(SQLManager m , JavaPlugin p){
		this.p = p;
		this.sql = m;
		this.playerStats = new HashMap<UUID, Stats>();
		this.toCall = new ArrayList<StatsJoin>();
		p.getServer().getPluginManager().registerEvents(this, p);
		m.Query(new TableCheck("stats", new booleanQuery(){

			@Override
			public void onReturn(boolean b) {
				if(b == false){
					createStatsTable();
				}
				
			}}));
	}
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e){
		if(this.playerStats.containsKey(e.getPlayer().getUniqueId())){
			this.saveData(e.getPlayer().getUniqueId(), this.playerStats.get(e.getPlayer().getUniqueId()));
			this.playerStats.remove(e.getPlayer().getUniqueId());
			
		}
	}
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e){
		e.setJoinMessage("");
		this.loadPlayerData(e.getPlayer().getUniqueId());
	}
	public void addStatsJoiner(StatsJoin s){
		this.toCall.add(s);
	}
	
	@EventHandler
	public void onChat(AsyncPlayerChatEvent e){
		Player p = e.getPlayer();
		if(this.playerStats.containsKey(p.getUniqueId())){
			Stats s = this.playerStats.get(p.getUniqueId());
			if(s.getRank() != Rank.DEFAULT){
				if(s.getStaffRank() != StaffRank.NONE){
					e.setFormat(s.getRank().getPrefix() + " " + ChatColor.GRAY + p.getDisplayName()  + " "+ s.getStaffRank().getTag() + ChatColor.RED + " » " + ChatColor.GRAY.toString() + e.getMessage());
					return;
				}
				e.setFormat(s.getRank().getPrefix()  + " " + ChatColor.GRAY +  p.getDisplayName() + ChatColor.RED + " » " + ChatColor.GRAY.toString() + e.getMessage());
				return;
			}else{
				if(s.getStaffRank() != StaffRank.NONE){
					e.setFormat( ChatColor.GRAY + p.getDisplayName() + s.getStaffRank().getTag()  + ChatColor.RED + " » " + ChatColor.GRAY.toString() + e.getMessage());
					return;
				}
				
				e.setFormat(ChatColor.GRAY + p.getDisplayName()+ ChatColor.RED + " » " + ChatColor.GRAY.toString() + e.getMessage());
				return;
			}
		}else{
			e.setFormat(ChatColor.GRAY + p.getDisplayName() + ChatColor.RED + " » " + ChatColor.GRAY.toString() + e.getMessage());
		}
	}
	
	public Stats getPlayerStats(UUID uuid){
		if(this.playerStats.containsKey(uuid)){
			return this.playerStats.get(uuid);
		}else{
			return null;
		}
	}
	public StaffRank getStaffRank(UUID uuid){
		if(this.playerStats.containsKey(uuid)){
			return this.playerStats.get(uuid).getStaffRank();
		}else{
			return StaffRank.NONE;
		}
	}
	public int getLevel(UUID uuid){
		if(this.playerStats.containsKey(uuid)){
			return this.playerStats.get(uuid).getLevel();
		}else{
			return 0;
		}
	}
	public boolean hasPlayerStats(UUID uuid){
		return this.playerStats.containsKey(uuid);
	}
	private void saveData(final UUID uuid, final Stats s){
		BukkitRunnable br = new BukkitRunnable(){

			@Override
			public void run() {
				Connection c = null;
				PreparedStatement st = null;
				try{
					c = sql.getConnection();
					st = c.prepareStatement("UPDATE stats SET blips=?, credits=?, plus=?, staffrank=?, level=?, xp=? WHERE uuid=?");
					st.setInt(1, s.getBlips());
					st.setInt(2,  s.getCredits());
					st.setLong(3, s.getPlusEpoch());
					st.setString(4, s.getStaffRank().getName());
					st.setInt(5, s.getLevel());
					st.setInt(6, s.getXp());
					st.setString(7, uuid.toString());
					st.executeUpdate();
				}catch(SQLException e){
					e.printStackTrace();
				}finally{
					sql.close(c);
					sql.close(st);
					this.cancel();
				}
				
			}
			
		};
		br.runTaskAsynchronously(this.p);
	}
	private void call(final Player p,final Stats s){
		BukkitRunnable br = new BukkitRunnable(){

			@Override
			public void run() {
				for(StatsJoin j : toCall){
					j.onJoin(p,s);
				}
				this.cancel();
				
			}
			
		};
		br.runTask(this.p);
	}
	
	private void newPlayer(final UUID p){
		BukkitRunnable br = new BukkitRunnable(){

			@Override
			public void run() {
				Connection c = null;
				PreparedStatement st = null;
				try{
					Timestamp s = new Timestamp(Stats.getCurrentTime());
					c = sql.getConnection();
					st = c.prepareStatement("INSERT INTO stats (uuid, blips, credits, plus, staffrank, joined, level, xp) VALUES(?, ?, ?, ?, ?, ?, ?, ?)");
					st.setString(1, p.toString());
					st.setInt(2, 100);
					st.setInt(3,  100);
					st.setLong(4, 100L);
					st.setString(5, StaffRank.NONE.getName());
					st.setTimestamp(6, s);
					st.setInt(7, 1);
					st.setInt(8, 0);
					st.executeUpdate();
					Player pl = Bukkit.getPlayer(p);
					Bukkit.broadcastMessage(pl.getDisplayName() + " Has joined for the first time!");
					Stats stm = new Stats(100, 100L, StaffRank.NONE, 100, s, 1, 0);
					playerStats.put(p, stm);
					call(pl, stm);
				}catch(SQLException e){
					e.printStackTrace();
				}finally{
					sql.close(c);
					sql.close(st);
					this.cancel();
				}
				
			}
			
		};
		br.runTaskAsynchronously(this.p);
	}
	private void loadPlayerData(final UUID p){
		ASyncQuery q = new ASyncQuery("SELECT * FROM stats WHERE uuid='" + p + "';", new Query(){

			@Override
			public void onReturn(ResultSet s) {
				try {
					if(!s.next()){
						newPlayer(p);
						return;
					}else{
						int blips = s.getInt("blips");
						Long plus = s.getLong("plus");
						StaffRank sr = StaffRank.fromString(s.getString("staffrank"));
						int credits = s.getInt("credits");
						Timestamp joined = s.getTimestamp("joined");
						int level = s.getInt("level");
						int xp = s.getInt("xp");
						Stats sta = new Stats(blips, plus, sr, credits, joined, level, xp);
						call(Bukkit.getPlayer(p), sta);
						playerStats.put(p, sta);
					}
					
				} catch (SQLException e) {
					e.printStackTrace();
				}finally{
					sql.close(s);
				}
				
			}
			
		} );
		this.sql.Query(q);
	}
	
	private void createStatsTable(){
		p.getLogger().log(Level.SEVERE, "Couldn't find stats table, creating one...");
		Map<String, DataType> toPut = new HashMap<String, DataType>();
		toPut.put("uuid", DataType.UUID);
		toPut.put("blips", DataType.INT);
		toPut.put("plus", DataType.BIGINT);
		toPut.put("staffrank", DataType.TINYTEXT);
		toPut.put("credits", DataType.INT);
		toPut.put("joined", DataType.DATETIME);
		toPut.put("level", DataType.INT);
		toPut.put("xp", DataType.INT);
		this.sql.CreateTable("stats", toPut, "uuid");
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		if(cmd.getName().equalsIgnoreCase("set")){
			if(args.length >= 3){
				if(sender instanceof Player){
					Player p = (Player) sender;
					if(this.playerStats.get(p.getUniqueId()).getStaffRank().getPower() >= 8){
						doSet(sender, args);
						return true;
					}else{
						sender.sendMessage(ChatColor.RED + "Insufficient privileges!");
						return true;
					}
				}else{
					doSet(sender, args);
					return true;
				}
			}else{
				sender.sendMessage(ChatColor.RED + "Invalid arguments!");
				return true;
			}
		}else if(cmd.getName().equalsIgnoreCase("stats")){
			if(sender instanceof Player){
				Player p = (Player) sender;
				if(this.playerStats.containsKey(p.getUniqueId())){
					Stats stats = this.playerStats.get(p.getUniqueId());
					p.sendMessage(ChatColor.AQUA + "You have " + stats.getBlips() + " Blips.");
					p.sendMessage(ChatColor.GREEN + "You have " + stats.getCredits() + " Credits.");
					p.sendMessage(ChatColor.GOLD + "You are level " + stats.getLevel() + ".");
					p.sendMessage(ChatColor.BLUE + "You have " + stats.getXp() + ChatColor.RED + "/" + ChatColor.DARK_PURPLE + getLevelUpXp(stats.getLevel()) + "XP");
					if(!stats.getRank().equals(Rank.DEFAULT)){
						p.sendMessage(ChatColor.LIGHT_PURPLE + "You have " + ChatColor.DARK_PURPLE +
								stats.getPlusHours()/24 + ChatColor.LIGHT_PURPLE + " days, " +
								ChatColor.DARK_PURPLE + stats.getPlusHours()%24 + 
								ChatColor.LIGHT_PURPLE + " hours, " +  ChatColor.DARK_PURPLE +(stats.getPlusMinutes()-(stats.getPlusHours()*60)) + ChatColor.LIGHT_PURPLE +" minutes left of" + ChatColor.DARK_PURPLE + 
								" Plus"+ ChatColor.LIGHT_PURPLE+".");
					}
					return true;
				}
			}
		}
		return false;
	}
	
	private void doSet(final CommandSender sender, String[] args){
		if(args[0].equalsIgnoreCase("blips")){
			if(args[1].equalsIgnoreCase("add")){
				final int i = Integer.parseInt(args[3]);
				if(i == 0){
					sender.sendMessage(ChatColor.RED + "Invalid bumber!");
					return;
				}
				final UUID uuid = UUIDConverter.getUUIDFromNameAsUUID(args[2], true);
				if(uuid == null){
					sender.sendMessage(ChatColor.RED + "Invalid player, not in mojangs auth!");
					return;
				}else{
					BukkitRunnable br = new BukkitRunnable(){
						
						@Override
						public void run() {
							Connection c = null;
							PreparedStatement stmt = null;
							ResultSet s = null;
							try{
								c = sql.getConnection();
								stmt = c.prepareStatement("SELECT blips FROM stats WHERE uuid=?");
								stmt.setString(1, uuid.toString());
								s = stmt.executeQuery();
								if(s.next()){
									int it = s.getInt("blips");
									setBlips(uuid, it + i);
									sender.sendMessage(ChatColor.GREEN + "Attempting to do that..");
								}else{
									sender.sendMessage(ChatColor.RED + "Player doesn't exsist in db");
									return;
								}
							}catch(SQLException e){
								e.printStackTrace();
							}finally{
								sql.close(c);
								sql.close(stmt);
								sql.close(s);
								this.cancel();
							}
							
						}
						
					};
					br.runTaskAsynchronously(p);
				}
			}else if(args[1].equalsIgnoreCase("rem") || args[1].equalsIgnoreCase("remove")){
				final int i = Integer.parseInt(args[3]);
				if(i == 0){
					sender.sendMessage(ChatColor.RED + "Invalid bumber!");
					return;
				}
				final UUID uuid = UUIDConverter.getUUIDFromNameAsUUID(args[2], true);
				if(uuid == null){
					sender.sendMessage(ChatColor.RED + "Invalid player, not in mojangs auth!");
					return;
				}else{
					BukkitRunnable br = new BukkitRunnable(){
						
						@Override
						public void run() {
							Connection c = null;
							PreparedStatement stmt = null;
							ResultSet s = null;
							try{
								c = sql.getConnection();
								stmt = c.prepareStatement("SELECT blips FROM stats WHERE uuid=?");
								stmt.setString(1, uuid.toString());
								s = stmt.executeQuery();
								if(s.next()){
									int it = s.getInt("blips");
									setBlips(uuid, it - i);
									sender.sendMessage(ChatColor.GREEN + "Attempting to do that..");
								}else{
									sender.sendMessage(ChatColor.RED + "Player doesn't exsist in db");
									return;
								}
							}catch(SQLException e){
								e.printStackTrace();
							}finally{
								sql.close(c);
								sql.close(stmt);
								sql.close(s);
								this.cancel();
							}
							
						}
						
					};
					br.runTaskAsynchronously(p);
					}
			}else{
				sender.sendMessage(ChatColor.RED + "Invalid args!");
				return;
			}
		}else if(args[0].equalsIgnoreCase("credits")){
			if(args[1].equalsIgnoreCase("add")){
				final int i = Integer.parseInt(args[3]);
				if(i == 0){
					sender.sendMessage(ChatColor.RED + "Invalid bumber!");
					return;
				}
				final UUID uuids = UUIDConverter.getUUIDFromNameAsUUID(args[2], true);
				if(uuids == null){
					sender.sendMessage(ChatColor.RED + "Invalid player, not in mojangs auth!");
					return;
				}else{
					BukkitRunnable br = new BukkitRunnable(){
						
						@Override
						public void run() {
							Connection c = null;
							PreparedStatement stmt = null;
							ResultSet s = null;
							try{
								c = sql.getConnection();
								stmt = c.prepareStatement("SELECT credits FROM stats WHERE uuid=?");
								stmt.setString(1, uuids.toString());
								s = stmt.executeQuery();
								System.out.println(stmt.toString());
								if(s.next()){
									int it = s.getInt("credits");
									setCredits(uuids, it + i);
									sender.sendMessage(ChatColor.GREEN + "Attempting to do that..");
								}else{
									System.out.println("ones");
									sender.sendMessage(ChatColor.RED + "Player doesn't exsist in db");
									return;
								}
							}catch(SQLException e){
								e.printStackTrace();
							}finally{
								sql.close(c);
								sql.close(stmt);
								sql.close(s);
								this.cancel();
							}
							
						}
						
					};
					br.runTaskAsynchronously(p);
					}
			}else if(args[1].equalsIgnoreCase("rem") || args[1].equalsIgnoreCase("remove")){
				final int i = Integer.parseInt(args[3]);
				if(i == 0){
					sender.sendMessage(ChatColor.RED + "Invalid bumber!");
					return;
				}
				final UUID uuid = UUIDConverter.getUUIDFromNameAsUUID(args[2], true);
				if(uuid == null){
					sender.sendMessage(ChatColor.RED + "Invalid player, not in mojangs auth!");
					return;
				}else{
					BukkitRunnable br = new BukkitRunnable(){
						
						@Override
						public void run() {
							Connection c = null;
							PreparedStatement stmt = null;
							ResultSet s = null;
							try{
								c = sql.getConnection();
								stmt = c.prepareStatement("SELECT credits FROM stats WHERE uuid=?");
								stmt.setString(1, uuid.toString());
								s = stmt.executeQuery();
								if(s.next()){
									int it = s.getInt("credits");
									setCredits(uuid, it - i);
									sender.sendMessage(ChatColor.GREEN + "Attempting to do that..");
								}else{
									sender.sendMessage(ChatColor.RED + "Player doesn't exsist in db");
									return;
								}
							}catch(SQLException e){
								e.printStackTrace();
							}finally{
								sql.close(c);
								sql.close(stmt);
								sql.close(s);
								this.cancel();
							}
							
						}
						
					};
					br.runTaskAsynchronously(p);
					}
			}else{
				sender.sendMessage(ChatColor.RED + "Invalid args!");
				return;
			}
			
		}else if(args[0].equalsIgnoreCase("rank")){
			final Rank r = Rank.fromString(args[2]);
			if(r == null){
				sender.sendMessage(ChatColor.RED + "Invalid rank!");
				return;
			}
			final UUID uuid = UUIDConverter.getUUIDFromNameAsUUID(args[1], true);
			int hours = 0;
			int days = 0;
			if(uuid == null){
				sender.sendMessage(ChatColor.RED + "Invalid player, not in mojangs auth!");
				return;
			}else if(args.length < 4){
				sender.sendMessage(ChatColor.RED + "Usage: /set rank <name> <rank> <#d> (<#h>)");
				return;
				
			}else{
			try{
				if(args.length == 5){
					String stri = args[4];
					if(stri.endsWith("d")){
						days = Integer.parseInt(stri.split("d")[0]);
					}else if(stri.endsWith("h")){
						hours = Integer.parseInt(stri.split("h")[0]);
					}else{
						sender.sendMessage(ChatColor.RED + "wtf is " + stri);
						return;
					}
				}
				String strin = args[3];
				if(strin.endsWith("d")){
					days = Integer.parseInt(strin.split("d")[0]);
				}else if(strin.endsWith("h")){
					hours = Integer.parseInt(strin.split("h")[0]);
				}else{
					sender.sendMessage(ChatColor.RED + "wtf is " + strin);
					return;
				}
			}catch(ArrayIndexOutOfBoundsException | NumberFormatException e){
				sender.sendMessage(ChatColor.RED + "Errorr you lil..");
				return;
			}
			final int ho = hours;
			final int da = days;
			this.sql.Query(new ValueCheck("stats", "uuid", uuid.toString(), new booleanQuery(){

					@Override
					public void onReturn(boolean b) {
						if(b == false){
							sender.sendMessage(ChatColor.RED + "Player isn't in db!");
							return;
						}else{
							addRank(uuid, r, ho, da);
							sender.sendMessage(ChatColor.GREEN + "Attempting to do that..");
						}
						
					}
					
				}));
			}
			
		}else if(args[0].equalsIgnoreCase("staff")){
			final StaffRank r = StaffRank.fromString(args[2]);
			if(r.equals(null)){
				sender.sendMessage(ChatColor.RED + "Invalid rank!");
				return;
			}
			final UUID uuid = UUIDConverter.getUUIDFromNameAsUUID(args[1], true);
			if(uuid == null){
				sender.sendMessage(ChatColor.RED + "Invalid player, not in mojangs auth!");
				return;
			}else{
				this.sql.Query(new ValueCheck("stats", "uuid", uuid.toString(), new booleanQuery(){

					@Override
					public void onReturn(boolean b) {
						if(b == false){
							sender.sendMessage(ChatColor.RED + "Player isn't in db!");
							return;
						}else{
							setStaffRank(uuid, r);
							sender.sendMessage(ChatColor.GREEN + "Attempting to do that..");
						}
						
					}
					
				}));
			}
			
		}else{
			sender.sendMessage(ChatColor.RED + "Invalid args!");
			return;
		}
		
	}
	private void addRank(final UUID uuid,final Rank r,final int hours,final int days){
		if(this.playerStats.containsKey(uuid)){
			if(r.equals(Rank.PLUS)){
				Stats s = this.playerStats.get(uuid);
				long pls = s.getPlusEpoch();
				long now = Stats.getCurrentTime();
				if(pls >= now){
					Long sta = Stats.addTime(pls, hours, days);
					s.setPlusExpiry(sta);
					this.addRankpt2(sta, uuid);
				}else{
					Long sta = Stats.addTime(now, hours, days);
					s.setPlusExpiry(sta);
					this.addRankpt2(sta, uuid);
				}
				this.playerStats.put(uuid, s);
			}
			return;
		}else{
			
			BukkitRunnable bro;
			if(r.equals(Rank.PLUS)){
				bro = new BukkitRunnable(){

					@Override
					public void run() {
						Connection c = null;
						PreparedStatement st = null;
						ResultSet rs = null;
						try{
							c = sql.getConnection();
							st = c.prepareStatement("SELECT plus FROM stats WHERE uuid=?");
							st.setString(1, uuid.toString());
							rs = st.executeQuery();
							if(rs.next()){
								addRankpt2(Stats.addTime(rs.getLong("plus"), hours, days), uuid);
							}
						}catch(SQLException e){
							e.printStackTrace();
						}finally{
							sql.close(rs);
							sql.close(c);
							sql.close(st);
							this.cancel();
						}
					}
					
				};
			bro.runTaskAsynchronously(this.p);
			}else{
				return;
			}
		}


	}
	private void addRankpt2(final long l,final UUID uuid){
		BukkitRunnable br = new BukkitRunnable(){

			@Override
			public void run() {
				Connection c = null;
				PreparedStatement st = null;
				try{
					c = sql.getConnection();
					st = c.prepareStatement("UPDATE stats SET plus=? WHERE uuid=?");
					st.setLong(1, l);
					st.setString(2,  uuid.toString());
					st.executeUpdate();
				}catch(SQLException e){
					e.printStackTrace();
				}finally{
					sql.close(c);
					sql.close(st);
					this.cancel();
				}
				
			}
			
		};
		br.runTaskAsynchronously(this.p);
	}
	private void setStaffRank(final UUID uuid,final StaffRank r){
		if(this.playerStats.containsKey(uuid)){
			Stats s = this.playerStats.get(uuid);
			s.setStaffRank(r);
			this.playerStats.put(uuid, s);
		}
		BukkitRunnable br = new BukkitRunnable(){

			@Override
			public void run() {
				Connection c = null;
				PreparedStatement st = null;
				try{
					c = sql.getConnection();
					st = c.prepareStatement("UPDATE stats SET staffrank=? WHERE uuid=?");
					st.setString(1, r.getName());
					st.setString(2,  uuid.toString());
					st.executeUpdate();
				}catch(SQLException e){
					e.printStackTrace();
				}finally{
					sql.close(c);
					sql.close(st);
					this.cancel();
				}
				
			}
			
		};
		br.runTaskAsynchronously(this.p);
	}
	public void giveExp(UUID uuid, int amount){
		if(this.playerStats.containsKey(uuid)){
			Stats s = this.getPlayerStats(uuid);
			int level = s.getLevel();
			int cx = s.getXp() + amount;
			int needed = getLevelUpXp(level);
			while(needed <= cx){
				cx -= needed;
				level++;
				needed = getLevelUpXp(level);
			}
			this.setLevelXP(level, cx, uuid);
		}
	}
	
	private void setLevelXP(final int level,final int xp,final UUID uuid){
		if(this.playerStats.containsKey(uuid)){
			Stats s = this.playerStats.get(uuid);
			s.setXp(xp);
			s.setLevel(level);
			this.playerStats.put(uuid, s);
		}
		BukkitRunnable br = new BukkitRunnable(){

			@Override
			public void run() {
				Connection c = null;
				PreparedStatement st = null;
				try{
					c = sql.getConnection();
					st = c.prepareStatement("UPDATE stats SET level=?,xp=?  WHERE uuid=?");
					st.setInt(1, level);
					st.setInt(2,  xp);
					st.setString(3, uuid.toString());
					st.executeUpdate();
				}catch(SQLException e){
					e.printStackTrace();
				}finally{
					sql.close(c);
					sql.close(st);
					this.cancel();
				}
				
			}
			
		};
		br.runTaskAsynchronously(this.p);
	}
	
	public static int getLevelUpXp(int level){
		return (int) Math.pow((level + 5), 2);
	}
	public void setBlips(final UUID uuid, final int blips){
		if(this.playerStats.containsKey(uuid)){
			Stats s = this.playerStats.get(uuid);
			s.setBlips(blips);
			this.playerStats.put(uuid, s);
		}
		BukkitRunnable br = new BukkitRunnable(){

			@Override
			public void run() {
				Connection c = null;
				PreparedStatement st = null;
				try{
					c = sql.getConnection();
					st = c.prepareStatement("UPDATE stats SET blips=? WHERE uuid=?");
					st.setInt(1, blips);
					st.setString(2,  uuid.toString());
					p.getLogger().log(Level.INFO, st.toString());
					st.executeUpdate();
				}catch(SQLException e){
					e.printStackTrace();
				}finally{
					sql.close(c);
					sql.close(st);
					this.cancel();
				}
				
			}
			
		};
		br.runTaskAsynchronously(this.p);
	}
	public void setCredits(final UUID uuid,final int credits){
		if(this.playerStats.containsKey(uuid)){
			Stats s = this.playerStats.get(uuid);
			s.setCredits(credits);
			this.playerStats.put(uuid, s);
		}
		BukkitRunnable br = new BukkitRunnable(){

			@Override
			public void run() {
				Connection c = null;
				PreparedStatement st = null;
				try{
					c = sql.getConnection();
					st = c.prepareStatement("UPDATE stats SET credits=? WHERE uuid=?");
					st.setInt(1, credits);
					st.setString(2,  uuid.toString());
					st.executeUpdate();
				}catch(SQLException e){
					e.printStackTrace();
				}finally{
					sql.close(c);
					sql.close(st);
					this.cancel();
				}
				
			}
			
		};
		br.runTaskAsynchronously(this.p);
	}
	
	
}
