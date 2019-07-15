package com.speuce.sql;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TableCheck extends SQLQuery{
	String table;
	booleanQuery ret;
	
	public TableCheck(String name, booleanQuery ret){
		this.table = name;
		this.ret = ret;
	}
	
	@Override
	public void run() {
		Connection c = null;
		DatabaseMetaData dbm = null;  
		ResultSet r = null;
		try{
			c = this.data.getConnection();
			dbm = c.getMetaData();
			r = dbm.getTables(null, null,table, null);
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
