package com.speuce.cosmetics;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.speuce.cosmetics.auras.Halo;
import com.speuce.cosmetics.auras.Musicality;
import com.speuce.cosmetics.auras.RedLight;
import com.speuce.cosmetics.auras.TripleTwirl;
import com.speuce.cosmetics.auras.Twirl;
import com.speuce.cosmetics.extra.Timeable;
import com.speuce.cosmetics.recent.RecentCosmeticsManager;
import com.speuce.sql.ColumnAdd;
import com.speuce.sql.ColumnCheck;
import com.speuce.sql.DataType;
import com.speuce.sql.SQLManager;
import com.speuce.sql.TableCheck;
import com.speuce.sql.booleanQuery;
import com.speuce.stats.StatsManager;

public class CosmeticManager implements Listener, CommandExecutor{

	private SQLManager man;
	private StatsManager stats;
	private JavaPlugin plugin;
	private Map<String, Cosmetic> registeredCosmetics;
	private List<Timeable> registeredTimeables;
	private Map<Player, Long> coolDowns;
	private final String title = "cosmetics";
	private BukkitRunnable timer, otherTimer;
	private RecentCosmeticsManager recent;
	public CosmeticManager(SQLManager man, StatsManager stats, JavaPlugin plugin){
		this.coolDowns = new HashMap<Player, Long>();
		this.registeredTimeables = new ArrayList<Timeable>();
		this.man = man;
		this.stats = stats;
		this.plugin = plugin;
		this.registeredCosmetics = new HashMap<String, Cosmetic>();
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		this.checkTable();
		this.timer = this.getTimer();
		this.timer.runTaskTimer(plugin, 20L, 20L);
		this.otherTimer = this.getOtherTimer();
		this.otherTimer.runTaskTimerAsynchronously(plugin, 2L, 2L);
		this.recent = new RecentCosmeticsManager(man, this);
		this.registerAuras();
	}
	private void registerAuras(){
		this.registerCosmetic(new Halo(this));
		this.registerCosmetic(new Twirl(this));
		this.registerCosmetic(new TripleTwirl(this));
		this.registerCosmetic(new Musicality(this));
		this.registerCosmetic(new RedLight(this));
		//this.registerCosmetic(new MiniWaters(this));
	}
	public JavaPlugin getPlugin(){
		return this.plugin;
	}
	public RecentCosmeticsManager getRecentCosmeticsManager(){
		return this.recent;
	}
	public StatsManager getStatsManager(){
		return this.stats;
	}
	public Cosmetic getCosmetic(String s){
		if(this.registeredCosmetics.containsKey(s)){
			return this.registeredCosmetics.get(s);
		}else{
			return null;
		}
	}
	public void runTaskTimerAsync(BukkitRunnable br, long tick){
		br.runTaskTimerAsynchronously(plugin, 2L, tick);
	}
	public void runTaskSync(BukkitRunnable br){
		br.runTask(plugin);
	}
	public void runTaskLater(BukkitRunnable br, long delay){
		br.runTaskLater(plugin, delay);
	}
	public void runTaskAsync(BukkitRunnable br){
		br.runTaskAsynchronously(this.plugin);
	}
	private void checkTable(){
		man.Query(new TableCheck(title, new booleanQuery(){

			@Override
			public void onReturn(boolean b) {
				if(!b){
					makeTable();
				}
			}
		}));
	}
	public void disable(){
		this.otherTimer.cancel();
		this.timer.cancel();
		for(Player p: Bukkit.getOnlinePlayers()){
			this.unsafeSaveData(p.getUniqueId());
			p.getInventory().setItem(6, new ItemStack(Material.AIR));
		}
	}
	private BukkitRunnable getTimer(){
		return new BukkitRunnable(){


			@Override
			public void run() {
				Iterator<Player> it = coolDowns.keySet().iterator();
				Player p;
				while(it.hasNext()){
					p = it.next();
					Long l = coolDowns.get(p);
					if(l <= 1){
						coolDowns.remove(p);
					}else{
						coolDowns.put(p, l-1);
					}
				}
				
			}
		};
	}
	private BukkitRunnable getOtherTimer(){
		return new BukkitRunnable(){


			@Override
			public void run() {
				for(Timeable t : registeredTimeables){
					t.Tick();
				}
				
			}
		};
	}
	public void cooldown(Player p, Long l){
		this.coolDowns.put(p, l);
	}
	public boolean cooldownContains(Player p){
		return this.coolDowns.containsKey(p);
	}
	public Long getCooldown(Player p){
		if(this.coolDowns.containsKey(p)){
			return this.coolDowns.get(p);
		}else{
			return 0L;
		}
	}
	private void makeTable(){
		Map<String, DataType> columns = new HashMap<String, DataType>();
		columns.put("uuid", DataType.UUID);
		this.man.CreateTable(title, columns, "uuid");
	}
	public void registerCosmetic(final Cosmetic c){
		if(c.hasProperty(CosmeticProperty.QUANTIFIED)){
			this.man.Query(new ColumnCheck(title, c.getName()+"amt", getQuer(title, c.getName()+"amt", DataType.INT)));
		}
		if(c.hasProperty(CosmeticProperty.BOOLEAN)){
			this.man.Query(new ColumnCheck(title, c.getName(), getQuer(title, c.getName(), DataType.BOOLEAN)));
		}
		if(c instanceof Timeable){
			Timeable t = (Timeable) c;
			this.registeredTimeables.add(t);
		}
		if(c instanceof Listener){
			Listener l = (Listener) c;
			Bukkit.getServer().getPluginManager().registerEvents(l, this.plugin);
		}
		this.registeredCosmetics.put(c.getName(), c);
		
	}
	private void loadData(final UUID uuid){
		BukkitRunnable br = new BukkitRunnable(){

			@Override
			public void run() {
				Connection co = null;
				PreparedStatement ps = null;
				ResultSet rs = null;
				try{
					co = man.getConnection();
					ps = co.prepareStatement("SELECT * FROM " + title + " WHERE uuid=?");
					ps.setString(1, uuid.toString());
					rs = ps.executeQuery();
					if(!rs.next()){
						newPlayer(uuid);
						return;
					}else{
						for(Cosmetic c: registeredCosmetics.values()){
							CosmeticPlayerInfo cs = new CosmeticPlayerInfo(c);
							if(c.hasProperty(CosmeticProperty.BOOLEAN)){
								boolean b = rs.getBoolean(c.getName());
								cs.setUse(b);
							}
							if(c.hasProperty(CosmeticProperty.QUANTIFIED)){
								cs.setUse(true);
								cs.setNeedsAmount(true);
								int i = rs.getInt(c.getName() + "amt");
								cs.setAmount(i);
							}
							c.addInfo(uuid, cs);
						}
					}
				System.out.println(ps.toString());
				}catch(SQLException e){
					e.printStackTrace();
				}finally{
					man.close(co);
					man.close(ps);
					man.close(rs);
					this.cancel();
				}
				
			}
			
		};
		br.runTaskAsynchronously(this.plugin);
	}
	private void newPlayer(final UUID uuid){
		BukkitRunnable br = new BukkitRunnable(){

			@Override
			public void run() {
				Connection conn = null;
				PreparedStatement ps = null;
				try {
					conn = man.getConnection();
					ps = conn.prepareStatement("INSERT INTO " + title + " (uuid) VALUES (?)");
					ps.setString(1, uuid.toString());
					ps.executeUpdate();
				} catch (SQLException e) {
					e.printStackTrace();
				} finally {
					man.close(ps);
					man.close(conn);
				}

				
			}
			
		};
		br.runTaskAsynchronously(this.plugin);
		for(Cosmetic c: this.registeredCosmetics.values()){
			CosmeticPlayerInfo it = new CosmeticPlayerInfo(c, 0, false);
			if(c.hasProperty(CosmeticProperty.QUANTIFIED)){
				it.setNeedsAmount(true);
			}
			c.addInfo(uuid, it);
		}
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e){
		this.loadData(e.getPlayer().getUniqueId());
	}
	@EventHandler
	public void onQuit(PlayerQuitEvent e){
		this.saveData(e.getPlayer().getUniqueId(), true);
		e.getPlayer().getInventory().setItem(6, new ItemStack(Material.AIR));
	}
	
	private void unsafeSaveData(final UUID uuid){
		PreparedStatement ps = null;
		Connection conn = null;

		Queue<Object> thing= new LinkedList<Object>();
		Queue<String> col = new LinkedList<String>();
		for(Cosmetic c: registeredCosmetics.values()){
			if(c.hasInfo(uuid)){
				CosmeticPlayerInfo it = c.getInfo(uuid);
				if(c.hasProperty(CosmeticProperty.BOOLEAN)){
					thing.offer(it.canUse());
					col.offer(c.getName());
				}
				if(c.hasProperty(CosmeticProperty.QUANTIFIED)){
					thing.offer(it.getAmount());
					col.offer(c.getName() + "amt");
				}
			}
		}
		String quer = "UPDATE " + title + " SET ";
		
		String temp1;
		while((temp1 = col.poll()) != null){
			if(col.peek() != null){
				quer += temp1 + "=?, ";
			}else{
				quer += temp1 + "=? ";
			}
		}
		quer += "WHERE uuid=?";
		
		try{
			conn = man.getConnection();
			ps = conn.prepareStatement(quer);
			
			Object o;
			int i = 1;
			while((o=thing.poll())!= null){
				ps.setObject(i, o);
				i++;
			}
			ps.setString(i, uuid.toString());
			ps.executeUpdate();
		}catch(SQLException e){
			e.printStackTrace();
		}finally{
			man.close(ps);
			man.close(conn);
		}
	}
	
	
	public void saveData(final UUID uuid, final boolean clear){
		BukkitRunnable br = new BukkitRunnable(){

			@Override
			public void run() {
				PreparedStatement ps = null;
				Connection conn = null;

				Queue<Object> thing= new LinkedList<Object>();
				Queue<String> col = new LinkedList<String>();
				for(Cosmetic c: registeredCosmetics.values()){
					if(c.hasInfo(uuid)){
						CosmeticPlayerInfo it = c.getInfo(uuid);
						if(c.hasProperty(CosmeticProperty.BOOLEAN)){
							thing.offer(it.canUse());
							col.offer(c.getName());
						}
						if(c.hasProperty(CosmeticProperty.QUANTIFIED)){
							thing.offer(it.getAmount());
							col.offer(c.getName() + "amt");
						}
					}
				}
				String quer = "UPDATE " + title + " SET ";
				
				String temp1;
				while((temp1 = col.poll()) != null){
					if(col.peek() != null){
						quer += temp1 + "=?, ";
					}else{
						quer += temp1 + "=? ";
					}
				}
				quer += "WHERE uuid=?";
				
				try{
					conn = man.getConnection();
					ps = conn.prepareStatement(quer);
					
					Object o;
					int i = 1;
					while((o=thing.poll())!= null){
						ps.setObject(i, o);
						i++;
					}
					ps.setString(i, uuid.toString());
					ps.executeUpdate();
					if(clear){
						clearData(uuid);
					}
				}catch(SQLException e){
					e.printStackTrace();
				}finally{
					man.close(ps);
					man.close(conn);
					this.cancel();
				}
				
			}
			
		};
		br.runTaskAsynchronously(this.plugin);
	}
	private void clearData(UUID uuid){
		for(Cosmetic c: registeredCosmetics.values()){
			if(c.hasInfo(uuid)){
				c.deleteInfo(uuid);
			}
		}
	}
	
	private booleanQuery getQuer(final String title,final String table,final DataType i){
		return new booleanQuery(){

			@Override
			public void onReturn(boolean b) {
				if(!b){
					man.Query(new ColumnAdd(title, table, i));
				}
			}
		};
	}
		
		
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String arg2,
			String[] args) {
		if(cmd.getName().equalsIgnoreCase("cosmetic") || cmd.getName().equalsIgnoreCase("cos")){
			if(sender instanceof Player){
				Player p = (Player) sender;
				if(this.stats.hasPlayerStats(p.getUniqueId())){
					if(this.stats.getStaffRank(p.getUniqueId()).getPower() < 8){
						p.sendMessage(ChatColor.RED + "LOL U THINK UR FUNNY??");
						return true;
					}
				}else{
					p.sendMessage(ChatColor.RED + "Your Stats are not loaded!");
					return true;
				}
			}
			if(args.length < 2){
				return false;
			}
			if(args[0].equalsIgnoreCase("disable") || args[0].equalsIgnoreCase("dis")){
				if(this.registeredCosmetics.containsKey(args[1])){
					this.registeredCosmetics.get(args[1]).setDisabled(true);
					sender.sendMessage(ChatColor.GREEN + "Cosmetic: " + args[1] + " disabled.");
					return true;
				}else{
					sender.sendMessage(ChatColor.RED + "Cosmetic: " + args[1] + " not found!");
					return true;
				}
			}else if(args[0].equalsIgnoreCase("enable") || args[0].equalsIgnoreCase("en")){
				if(this.registeredCosmetics.containsKey(args[1])){
					this.registeredCosmetics.get(args[1]).setDisabled(false);
					sender.sendMessage(ChatColor.GREEN + "Cosmetic: " + args[1] + " enabled.");
					return true;
				}else{
					sender.sendMessage(ChatColor.RED + "Cosmetic: " + args[1] + " not found!");
					return true;
				}
			}else{
				return false;
			}
		}
		return false;
	}
}
