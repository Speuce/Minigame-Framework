package com.speuce.miniGames.connect;

import com.speuce.miniGames.connect.Client.ClientStatus;
import com.speuce.miniGames.connect.Client.GameStatus;

public class ClientInstance {
	private String name;
	private int PlayerCount;
	private int MaxPlayers;
	private GameStatus status;

	private ClientStatus connectionStatus;
	public ClientInstance(String name, int playerCount, int maxPlayers) {
		this.name = name;
		PlayerCount = playerCount;
		MaxPlayers = maxPlayers;
	}
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	public int getPlayerCount() {
		return PlayerCount;
	}
	public void setPlayerCount(int playerCount) {
		PlayerCount = playerCount;
	}
	public int getMaxPlayers() {
		return MaxPlayers;
	}
	public void setMaxPlayers(int maxPlayers) {
		MaxPlayers = maxPlayers;
	}
	public GameStatus getStatus() {
		return status;
	}
	public void setStatus(GameStatus status) {
		this.status = status;
	}
	public ClientStatus getConnectionStatus() {
		return connectionStatus;
	}
	public void setConnectionStatus(ClientStatus connectionStatus) {
		this.connectionStatus = connectionStatus;
	}
	
	
	
}