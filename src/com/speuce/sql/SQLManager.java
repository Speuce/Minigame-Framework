package com.speuce.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.plugin.java.JavaPlugin;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;


public class SQLManager {
	JavaPlugin p;
	private HikariDataSource hik;
	private String dbName = "Engadine";
	private String userName = "matt";
	private String password = "PWrd$123";
	private int port = 3306;
	private String address = "142.44.210.76";
	
	public SQLManager(JavaPlugin p){
		this.p = p;
		this.setupPool();
	}
    private void setupPool() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(
                "jdbc:mysql://" +
                        address +
                        ":" +
                        port +
                        "/" +
                        dbName
        );
        config.setUsername(userName);
        config.setPassword(password);
        config.setMinimumIdle(2);
        config.setMaximumPoolSize(5);
        config.setConnectionTimeout(30000);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        //config.setConnectionTestQuery("show tables");
        hik = new HikariDataSource(config);
        p.getLogger().log(Level.INFO, "Connected to db :D");
    }
 
    public Connection getConnection() throws SQLException {
        return hik.getConnection();
    }
 
    public void close(Connection conn, PreparedStatement ps, ResultSet res) {
        if (conn != null){
        	try{ conn.close(); 
        	}catch(SQLException ignored) {}
        }

        if (ps != null){
          	try{ ps.close(); 
        	}catch(SQLException ignored) {}
        }
  
        if (res != null){
        	try { res.close(); 
        	}catch(SQLException ignored) {}
        }

    }
	public void CreateTable(String name, Map<String, DataType> objects, String key){
		if(name == null || objects == null || objects.isEmpty() || key == null){
			throw new NullPointerException("Name or objects or key is null or empty");
		}
		if(!objects.containsKey(key)){
			throw new NullPointerException("Specified key: '" + key + "' does not exsist in objects");
		}
		String arg = " ( ";
		Iterator<String> i = objects.keySet().iterator();
		while(i.hasNext()){
			String s = i.next();
			arg += s + " " + objects.get(s).getName() + ", ";
		}

		arg += "PRIMARY KEY (" + key + "))";
		p.getLogger().log(Level.INFO, "Creating table: " + name);
		p.getLogger().log(Level.INFO, "CREATE TABLE IF NOT EXISTS " + name + arg + ";");
		Connection c = null;
		PreparedStatement s = null;
		try{
			c = hik.getConnection();
			s = c.prepareStatement("CREATE TABLE IF NOT EXISTS " + name + arg + ";");
			s.executeUpdate();
		}catch(SQLException e){
			e.printStackTrace();
		}finally{
			close(c);
			close(s);
		}
		
		
		
	}
	public void Query(SQLQuery s){
		s.Query(this.hik, this.p);
	}
	public void closeHik(){
		this.hik.close();
	}
	public void close(Connection conn) {
        if (conn != null) try { conn.close(); } catch (SQLException ignored) {}
	}
	public void close(PreparedStatement ps) {
		if (ps != null) try { ps.close(); } catch (SQLException ignored) {}
	}
	public void close(ResultSet conn) {
        if (conn != null) try { conn.close(); } catch (SQLException ignored) {}
	}
}

