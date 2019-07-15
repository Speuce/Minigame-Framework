package com.speuce.miniGames.utils;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.speuce.miniGames.minigame.MiniGame;
import com.speuce.stats.StatsManager;

public class Mod implements CommandExecutor, Listener{
	MiniGame m;
	StatsManager man;
	public Mod(MiniGame m, StatsManager man){
		this.m = m;
		this.man = man;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String arg2,
			String[] arg3) {
		if(cmd.getName().equalsIgnoreCase("mod")){
			if(sender instanceof Player){
				Player p = (Player) sender;
				if(man.hasPlayerStats(p.getUniqueId())){
					if(man.getPlayerStats(p.getUniqueId()).getStaffRank().getPower() >= 3){
						p.openInventory(this.getInventory());
						p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.5F, 1.5F);
						return true;
					}else{
						p.sendMessage(ChatColor.RED + "You aren't a mod+!");
						return true;
					}
				}else{
					p.sendMessage(ChatColor.RED + "Couldn't find your stats!");
					return true;
				}
			}
		}
		return false;
	}
	@EventHandler
	public void onInventoryClick(InventoryClickEvent e){
		if(e.getCurrentItem() == null || e.getCurrentItem().getType().equals(Material.AIR)){
			return;
		}
		if(e.getView().getTitle().contains("Mod Menu")){
			e.setCancelled(true);
			if(e.getCurrentItem().getType().equals(Material.REDSTONE)){
				((Player) e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 2F, 2F);
				this.m.setAdminMode((Player) e.getWhoClicked());
			}else{
				((Player) e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 2F, 0F);
			}
		}
	}
	private Inventory getInventory(){
		Inventory i = Bukkit.createInventory(null, 27, ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD.toString() + "Mod Menu");
		ItemStack admin = new ItemStack(Material.REDSTONE);
		ItemMeta m = admin.getItemMeta();
		m.setDisplayName(ChatColor.RED.toString() + ChatColor.BOLD.toString() + "Toggle Admin mode");
		List<String> lore = new ArrayList<String>();
		lore.add(ChatColor.BLUE.toString() + "Admin mode sets you to creative");
		lore.add(ChatColor.BLUE.toString() + "Hides you from all players");
		lore.add(ChatColor.BLUE.toString() + "and makes it look like you left.");
		m.setLore(lore);
		admin.setItemMeta(m);
		i.setItem(10, admin);
		return i;
	}
	
}
