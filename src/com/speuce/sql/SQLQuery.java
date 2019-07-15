package com.speuce.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.zaxxer.hikari.HikariDataSource;

public abstract class SQLQuery extends BukkitRunnable {
	HikariDataSource data;
	public void Query(HikariDataSource data, Plugin p){
		this.data = data;
		this.runTaskAsynchronously(p);
	}
	protected void close(Connection conn) {
        if (conn != null) try { conn.close(); } catch (SQLException ignored) {}
	}
	protected void close(ResultSet ps) {
		if (ps != null) try { ps.close(); } catch (SQLException ignored) {}
	}
	protected void close(PreparedStatement ps) {
		if (ps != null) try { ps.close(); } catch (SQLException ignored) {}
	}
}
