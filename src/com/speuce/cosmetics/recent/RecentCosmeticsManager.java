package com.speuce.cosmetics.recent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.speuce.cosmetics.Cosmetic;
import com.speuce.cosmetics.CosmeticManager;
import com.speuce.cosmetics.CosmeticType;
import com.speuce.cosmetics.CosmeticUseResult;
import com.speuce.sql.ColumnAdd;
import com.speuce.sql.ColumnCheck;
import com.speuce.sql.DataType;
import com.speuce.sql.SQLManager;
import com.speuce.sql.TableCheck;
import com.speuce.sql.booleanQuery;
import com.speuce.stats.Stats;
import com.speuce.stats.StatsJoin;

public class RecentCosmeticsManager implements Listener{
	private SQLManager sql;
	private CosmeticManager cosman;
	private final String title = "recentcosmetics";
	private Map<Player, RecentCosmetics> recent;
	public RecentCosmeticsManager(SQLManager sql, CosmeticManager cosman){
		this.sql = sql;
		this.cosman = cosman;
		cosman.getPlugin().getServer().getPluginManager().registerEvents(this, cosman.getPlugin());
		this.recent = new HashMap<Player, RecentCosmetics>();
		this.checkTable();
		this.addStatsJoin();
	}
	private void addStatsJoin(){
		this.cosman.getStatsManager().addStatsJoiner(new StatsJoin(){

			@Override
			public void onJoin(Player p, Stats s) {
				loadData(p);
				
			}
			
		});
	}
	public Cosmetic getRecentCosmetic(Player p, CosmeticType ty){
		if(this.recent.containsKey(p)){
			return this.recent.get(p).getRecentCosmetic(ty);
		}else{
			return null;
		}
	}
	public void setRecentCosmetic(Player p, CosmeticType ty, Cosmetic c){
		if(this.recent.containsKey(p)){
			this.recent.get(p).setRecentCosmetic(ty, c);
		}else{
			RecentCosmetics st = new RecentCosmetics();
			st.setRecentCosmetic(ty, c);
			this.recent.put(p, st);
		}
	}
	private void checkTable(){
		sql.Query(new TableCheck(title, new booleanQuery(){

			@Override
			public void onReturn(boolean b) {
				if(!b){
					makeTable();
				}else{
					enable();
				}
			}
		}));
	}
	@EventHandler
	public void onPQuit(PlayerQuitEvent e){
		this.saveData(e.getPlayer());
	}
	private void giveRecentCosmetics(Player p){
		if(p != null && p.isOnline()){
			Map<CosmeticType, Cosmetic> rec = this.recent.get(p).getRecent();
			if(!rec.isEmpty()){
				for(Cosmetic c: rec.values()){
					if(c != null){
						CosmeticUseResult cosrt = c.canUse(p);
						if(cosrt.getResult()){
							c.use(p);
						}else{
							p.sendMessage(ChatColor.RED + "Error loading recent cosmetic: " + c.getName() + " '" + cosrt.getText() + ChatColor.RED + "'");
						}
					}
				}
			}
		}
	}
	private void saveData(final Player p){
		BukkitRunnable br = new BukkitRunnable(){

			@Override
			public void run() {
				PreparedStatement ps = null;
				Connection conn = null;

				Queue<Object> thing= new LinkedList<Object>();
				Queue<String> col = new LinkedList<String>();
				Map<CosmeticType, Cosmetic> map = recent.get(p).getRecent();
				if(map.isEmpty()){
					recent.remove(p);
					return;
				}
				for(CosmeticType ct: map.keySet()){
					Cosmetic cosm = map.get(ct);
					String cosmetic;
					if(cosm != null){
						cosmetic = map.get(ct).getName();
					}else{
						cosmetic = "null";
					}
					thing.offer(cosmetic);
					col.offer(ct.getTitle());
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
					conn = sql.getConnection();
					ps = conn.prepareStatement(quer);
					
					Object o;
					int i = 1;
					while((o=thing.poll())!= null){
						ps.setObject(i, o);
						i++;
					}
					ps.setString(i, p.getUniqueId().toString());
					ps.executeUpdate();
					recent.remove(p);
				}catch(SQLException e){
					e.printStackTrace();
				}finally{
					sql.close(ps);
					sql.close(conn);
					this.cancel();
				}
				
			}
			
		};
		br.runTaskAsynchronously(this.cosman.getPlugin());
	}
	private void loadData(final Player p){
		BukkitRunnable br = new BukkitRunnable(){

			@Override
			public void run() {
				Connection co = null;
				PreparedStatement ps = null;
				ResultSet rs = null;
				try{
					co = sql.getConnection();
					ps = co.prepareStatement("SELECT * FROM " + title + " WHERE uuid=?");
					ps.setString(1, p.getUniqueId().toString());
					rs = ps.executeQuery();
					if(!rs.next()){
						newPlayer(p);
						return;
					}else{
						RecentCosmetics rc = new RecentCosmetics();
						for(CosmeticType ct: CosmeticType.values()){
							String s = rs.getString(ct.getTitle());
							if(s == null || s.equalsIgnoreCase("null")){
								rc.setRecentCosmetic(ct, null);
							}else{
								Cosmetic cos = cosman.getCosmetic(s);
								if(cos == null){
									p.sendMessage(ChatColor.RED + "Could not Load recent Cosmetic: " + s + ". Cosmetic not found!");
									rc.setRecentCosmetic(ct, null);
								}else{
									rc.setRecentCosmetic(ct, cos);
								}
							}
							recent.put(p, rc);
						}
						giveRecentCosmetics(p);
					}
				System.out.println(ps.toString());
				}catch(SQLException e){
					e.printStackTrace();
				}finally{
					sql.close(co);
					sql.close(ps);
					sql.close(rs);
					this.cancel();
				}
				
			}
			
		};
		br.runTaskAsynchronously(this.cosman.getPlugin());
	}
	private void newPlayer(final Player p){
		BukkitRunnable br = new BukkitRunnable(){

			@Override
			public void run() {
				Connection conn = null;
				PreparedStatement ps = null;
				try {
					conn = sql.getConnection();
					ps = conn.prepareStatement("INSERT INTO " + title + " (uuid) VALUES (?)");
					ps.setString(1, p.getUniqueId().toString());
					ps.executeUpdate();
				} catch (SQLException e) {
					e.printStackTrace();
				} finally {
					sql.close(ps);
					sql.close(conn);
				}

				
			}
			
		};
		br.runTaskAsynchronously(this.cosman.getPlugin());
		this.recent.put(p, new RecentCosmetics());
	}
	private void enable(){
		for(CosmeticType ct: CosmeticType.values()){
			this.sql.Query(new ColumnCheck(title, ct.getTitle(), 
					getQuer(title, ct.getTitle(), DataType.TEXT)));
		}
	}
	private void makeTable(){
		Map<String, DataType> columns = new HashMap<String, DataType>();
		columns.put("uuid", DataType.UUID);
		this.sql.CreateTable(title, columns, "uuid");
		enable();
	}
	private booleanQuery getQuer(final String title,final String table,final DataType i){
		return new booleanQuery(){

			@Override
			public void onReturn(boolean b) {
				if(!b){
					sql.Query(new ColumnAdd(title, table, i));
				}
			}
		};
	}
}
