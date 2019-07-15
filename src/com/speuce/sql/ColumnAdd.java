package com.speuce.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ColumnAdd extends SQLQuery{
	private String table;
	private String column;
	private DataType type;
	
	public ColumnAdd(String table, String column, DataType type){
		this.table = table;
		this.column = column;
		this.type = type;
	}
	
	@Override
	public void run() {
		PreparedStatement st = null;
		Connection c = null;
		try{
			c = this.data.getConnection();
			st = c.prepareStatement("ALTER TABLE " + table + " ADD "+ column + " " + this.type.getName());
			st.executeUpdate();
		}catch(SQLException e){
			e.printStackTrace();
		}finally{
			close(c);
			close(st);
			this.cancel();
		}
		
	}

}
