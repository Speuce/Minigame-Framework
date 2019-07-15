package com.speuce.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ValueCheck extends SQLQuery{
	String table;
	String columnName;
	String value;
	booleanQuery query;
	public ValueCheck(String table, String column, String value, booleanQuery quer){
		this.table = table;
		this.columnName = column;
		this.value = value;
		this.query = quer;
	}
	
	@Override
	public void run() {
		PreparedStatement st = null;
		Connection c = null;
		ResultSet rs = null;
		try{
			c = this.data.getConnection();
			st = c.prepareStatement("SELECT " + this.columnName + " FROM " + this.table + " WHERE " + this.columnName + "='"+ this.value+"';");
			//st.setString(1, this.value);
			rs = st.executeQuery();
			if(rs.next()){
				query.onReturn(true);
			}else{
				query.onReturn(false);
			}
		}catch(SQLException e){
			e.printStackTrace();
			query.onReturn(false);
		}finally{
			close(c);
			close(st);
			close(rs);
			this.cancel();
		}
		
	}

}
