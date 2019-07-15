package com.speuce.sql;

import java.sql.ResultSet;

public interface Query{
	public void onReturn(ResultSet s);
}
