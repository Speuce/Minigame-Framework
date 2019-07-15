package com.speuce.sql;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.plugin.Plugin;

import com.zaxxer.hikari.HikariDataSource;

public class ColumnCheck extends SQLQuery{
	private String table;
	private String column;
	booleanQuery ret;
	
	public ColumnCheck(String table, String column, booleanQuery ret){
		this.table = table;
		this.column = column;
		this.ret = ret;
	}
	
	public void Query(HikariDataSource data, Plugin p){
		this.data = data;
		this.runTaskAsynchronously(p);
	}
	
	@Override
	public void run() {
		Connection c = null;
		DatabaseMetaData dbm = null;  
		ResultSet r = null;
		try{
			c = this.data.getConnection();
			dbm = c.getMetaData();
			r = dbm.getColumns(null, null, table, column);
			if(r.next()){
				ret.onReturn(true);		
			}else{
				ret.onReturn(false);
			}
		}catch(SQLException e){
			e.printStackTrace();
			ret.onReturn(false);
		}finally{
			close(c);
			close(r);
			c = null;
			dbm = null;
			r = null;
			this.cancel();
		}
		
	}
}
