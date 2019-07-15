package com.speuce.upgrades;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.speuce.sql.ColumnAdd;
import com.speuce.sql.ColumnCheck;
import com.speuce.sql.DataType;
import com.speuce.sql.SQLManager;
import com.speuce.sql.TableCheck;
import com.speuce.sql.booleanQuery;
import com.speuce.stats.Stats;
import com.speuce.store.BackHandler;
import com.speuce.store.Currency;
import com.speuce.store.Shop;
import com.speuce.store.TransactionManager;
import com.speuce.store.Transactions;

public class UpgradesManager implements Listener{
	private Shop shop;
	private SQLManager m;
	private Map<String, DataType> columns;
	private Plugin p;
	private Map<UUID, Map<String, Object>> data;
	private TransactionManager tm;
	public UpgradesManager(Shop shop, SQLManager m, Plugin p, TransactionManager tm){
		TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of("Z")));
		p.getServer().getPluginManager().registerEvents(this, p);
		this.shop = shop;
		this.m = m;
		this.p = p;
		this.tm = tm;
		this.data = new HashMap<UUID, Map<String, Object>>();
		this.columns = new HashMap<String, DataType>();
		this.m.Query(new TableCheck(shop.getName(), new booleanQuery(){

			
			@Override
			public void onReturn(boolean b) {
				if(!b){
					makeTable();
				}else{
					checkTable();
				}
			}
		}));
	}
	@EventHandler
	public void Join(PlayerJoinEvent e){
		this.onLoginf(e.getPlayer().getUniqueId());
	}
	@EventHandler
	public void Quit(PlayerQuitEvent e){
		this.onLogoffs(e.getPlayer().getUniqueId());
	}
	

	public void onLoginf(final UUID uuid){
		BukkitRunnable br = new BukkitRunnable(){

			@Override
			public void run() {
				Connection c = null;
				PreparedStatement st = null;
				ResultSet rs = null;
				try{
					c = m.getConnection();
					st = c.prepareStatement("SELECT * FROM " + shop.getName() + " WHERE uuid=?");
					st.setString(1, uuid.toString());
					rs = st.executeQuery();
					if(!rs.next()){
						newPlayer(uuid);
					}else{
						Map<String, Object> playerdata = new HashMap<String, Object>();
						for(String s: columns.keySet()){
							if(!s.equals("uuid")){
								try{
									playerdata.put(s, rs.getObject(s));
								}catch(SQLException e){
									if(columns.get(s).equals(DataType.BIGINT)){
										playerdata.put(s, Stats.getCurrentTime() - 2L);
									}else if(columns.get(s).equals(DataType.INT)){
										playerdata.put(s, 0);
									}else{
										playerdata.put(s, null);
									}
								}

							}
						}
						 data.put(uuid, playerdata);
					}
				}catch(SQLException e){
					e.printStackTrace();
				}finally{
					m.close(c);
					m.close(st);
					m.close(rs);
					this.cancel();
				}
				
			}
			
		};
		br.runTaskAsynchronously(this.p);
		
	}
	private void newPlayer(final UUID uuid){


		String prep = new String();
		String prep2 = new String();
		Iterator<String> i = columns.keySet().iterator();
		while(i.hasNext()){
			String st= i.next();
			prep += "?";
			prep2 += st;
			if(i.hasNext()){
				prep+=", ";
				prep2+=", ";
			}
		}
		final String quer = new String("INSERT INTO " + shop.getName() + " (" + prep2 + ") VALUES(" + prep + ")");
		BukkitRunnable br = new BukkitRunnable(){
			@Override
			public void run() {
				Connection c = null;
				PreparedStatement ps = null;
				Map<String, Object> playerdata = new HashMap<String, Object>();
				try{
					c = m.getConnection();
					ps = c.prepareStatement(quer);
					int it = 1;
					for(String s: columns.keySet()){
						DataType o = columns.get(s);
						if(o.equals(DataType.BIGINT)){
							Long st = Stats.getCurrentTime();
							ps.setLong(it, st);
							playerdata.put(s,st);
						}else if(o.equals(DataType.INT)){
							ps.setInt(it, 0);
							playerdata.put(s, 0);
						}else if(s.equalsIgnoreCase("uuid")){
							ps.setString(it, uuid.toString());
							playerdata.put(s, uuid.toString());
						}else{
							ps.setString(it, "null");
							playerdata.put(s, null);
						}
						it++;
					}
					data.put(uuid, playerdata);
					ps.executeUpdate();
				}catch(SQLException e){
					e.printStackTrace();
				}finally{
					m.close(c);
					m.close(ps);
				}
				
			}
			
		};
		br.runTaskAsynchronously(this.p);

	}
	public void onLogoffs(final UUID uuid){
		BukkitRunnable br = new BukkitRunnable(){

			@Override
			public void run() {
				if(data.containsKey(uuid)){
					Map<String, Object> pd = data.get(uuid);
					String quer = new String("UPDATE "+ shop.getName() + " SET ");
					Iterator<String> i = pd.keySet().iterator();
					while(i.hasNext()){
						String st = i.next();
						if(i.hasNext()){
							quer += st+"=?, ";
						}else{
							quer += st+"=? ";
						}
					}
					quer += "WHERE uuid=?";
					Connection c = null;
					PreparedStatement ps = null;
					try{
						c = m.getConnection();
						ps = c.prepareStatement(quer);
						int in = 1;
						for(String s: pd.keySet()){
							Object o = pd.get(s);
							if(o instanceof Long){
								ps.setLong(in,(Long) o);
							}else if(o instanceof Integer){
								ps.setInt(in, (Integer) o);
							}else{
								ps.setObject(in, o);
							}
							in++;
						}
						data.remove(uuid);
						//TODO watch line above, may cause eerrrroor
						ps.setString(in, uuid.toString());
						ps.executeUpdate();
					}catch(SQLException e){
						e.printStackTrace();
					}finally{
						m.close(ps);
						m.close(c);
						this.cancel();
						data.remove(uuid);
					}
				}
				
			}
			
		};
		br.runTaskAsynchronously(this.p);
	}
	private void makeTable(){
		Map<String, DataType> data = new HashMap<String, DataType>();
		data.put("uuid", DataType.UUID);
		m.CreateTable(shop.getName(), data, "uuid" );
		columns.put("uuid", DataType.UUID);
	}
	private void checkTable(){
		m.Query(new ColumnCheck(shop.getName(), "uuid", new booleanQuery(){

			@Override
			public void onReturn(boolean b) {
				if(b){
					columns.put("uuid", DataType.UUID);
				}else{
					addColumn("uuid", DataType.UUID);
					columns.put("uuid", DataType.UUID);
				}
				
			}
			
		}));
	}
	private void addColumn(String name, DataType type){
		m.Query(new ColumnAdd(shop.getName(), name, type));
	}
	public void registerUpgrade(final Upgrade g){
		m.Query(new ColumnCheck(shop.getName(), g.getName(), new booleanQuery(){
			
			@Override
			public void onReturn(boolean b) {
				if(b){
					columns.put(g.getName(), g.getUpgradeType().getType());
				}else{
					addColumn(g.getName(), g.getUpgradeType().getType());
					columns.put(g.getName(), g.getUpgradeType().getType());
				}
			}			
		}));
	}

	public Object getData(UUID p, String column){
		if(this.data.containsKey(p)){
			if(this.data.get(p).containsKey(column)){
				return this.data.get(p).get(column);
			}
		}
		return null;
	}
	public void setData(final UUID p, final String column,final Object o){
		if(this.data.containsKey(p)){
			Map<String, Object> temp = this.data.get(p);
			temp.put(column, o);
			this.data.put(p, temp);
			BukkitRunnable br = new BukkitRunnable(){

				@Override
				public void run() {
					PreparedStatement ps = null;
					Connection c = null;
					try{
						c = m.getConnection();
						ps = c.prepareStatement("UPDATE " + shop.getName() + " SET " + column + "=? WHERE uuid=?");
						if(o instanceof Long){
							ps.setLong(1, (Long) o);
						}else if(o instanceof Integer){
							ps.setInt(1, (int) o);
						}else{
							ps.setObject(1, o);
						}
						ps.setString(2, p.toString());
						ps.executeUpdate();
					}catch(SQLException e){
						e.printStackTrace();
					}finally{
						m.close(c);
						m.close(ps);
					}
					
				}
				
			};
			br.runTaskAsynchronously(this.p);
		}
	}
	public void makeTransaction(Player p, String title, Currency c, int amount, BackHandler back, Transactions trans){
		this.tm.createTransaction(p, title, c, amount, trans, back);
	}
}
