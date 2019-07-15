package com.speuce.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
@Deprecated
public class ASyncQuery extends SQLQuery{
	String query;
	Query ret;
	public ASyncQuery(String query, Query query2){
		this.query = query;
		this.ret = query2;
	}
	
	@Override
	public void run() {
		Connection c = null;
		PreparedStatement stmt = null;
			try {
				c = this.data.getConnection();
				stmt = c.prepareStatement(query);
				ret.onReturn(stmt.executeQuery());
			} catch (SQLException e) {
				ret.onReturn(null);
				e.printStackTrace();
			}finally{
				close(c);
				close(stmt);
				query = null;
				ret = null;
				data = null;
				this.cancel();
			}

	}


}
