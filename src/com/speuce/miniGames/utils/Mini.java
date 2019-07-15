package com.speuce.miniGames.utils;



import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;


public class Mini {
	static Random ran = new Random();
	public static void reload(){
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "reload");
	}
	public static void setSpec(Player p){
		p.setGameMode(GameMode.SPECTATOR);
		p.sendMessage(ChatColor.AQUA.toString() + ChatColor.BOLD.toString() + "--------------------------");
		p.sendMessage(ChatColor.RED + "   You are now in Spectator mode!");
		p.sendMessage(ChatColor.GOLD + "    Type /hub to exit the game!");
		p.sendMessage(ChatColor.AQUA.toString() + ChatColor.BOLD.toString() + "--------------------------");
		for(Player pl: Bukkit.getOnlinePlayers()){
			if(pl != p){
				pl.hidePlayer(p);
			}
		}
	}
	public static void sendActionBar(Player player, String message) {
//		CraftPlayer p = (CraftPlayer) player;
//		IChatBaseComponent cbc = ChatSerializer.a("{\"text\": \"" + message + "\"}");
//		PacketPlayOutChat ppoc = new PacketPlayOutChat(cbc, (byte) 2);
//		p.getHandle().playerConnection.sendPacket(ppoc);
		player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
	}
	public static void spawnRandomFirework(Location l){
		Firework fw = (Firework) l.getWorld().spawnEntity(l, EntityType.FIREWORK);
		FireworkMeta fm = fw.getFireworkMeta();
		FireworkEffect ef = null;
		int i = ran.nextInt(5);
		if(i == 0){
			ef = FireworkEffect.builder().
			flicker(false).trail(true).with(Type.BURST).withColor(Color.RED).build();
		}else if(i == 1){
			ef =FireworkEffect.builder().
			flicker(false).trail(true).with(Type.BURST).withColor(Color.AQUA).build();
		}else if(i == 2){
			ef =FireworkEffect.builder().
			flicker(false).trail(true).with(Type.BURST).withColor(Color.PURPLE).build();
		}else if(i == 3){
			ef = FireworkEffect.builder().
			flicker(false).trail(true).with(Type.BURST).withColor(Color.LIME).build();
		}else{
			ef = FireworkEffect.builder().
			flicker(false).trail(true).with(Type.BURST).withColor(Color.ORANGE).build();
		}
		fm.addEffect(ef);
		fw.setFireworkMeta(fm);
	}
	
	public static Location getLocationRelativeWorldSpawn(int relative, World w){
		int xChange = ran.nextInt(relative * 2)-relative;
		int yChange = ran.nextInt(relative);
		int zChange = ran.nextInt(relative * 2)-relative;
		return w.getSpawnLocation().add(xChange, yChange, zChange);
	}
}
