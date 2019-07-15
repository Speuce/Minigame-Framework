package com.speuce.store;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class StaticShop {
	public static ItemStack getBack(){
		ItemStack i = new ItemStack(Material.BARRIER);
		ItemMeta m = i.getItemMeta();
		m.setDisplayName(ChatColor.RED + "Go Back");
		i.setItemMeta(m);
		return i;
	}

	public static ChatColor getRandomColor(){
		Random ran = new Random();
		List<ChatColor> l = new ArrayList<ChatColor>();
		l.add(ChatColor.RED);
		l.add(ChatColor.AQUA);
		l.add(ChatColor.GREEN);
		l.add(ChatColor.LIGHT_PURPLE);
		l.add(ChatColor.GOLD);
		return l.get(ran.nextInt(l.size()));
	}
	
	  public static String RomanNumerals(int Int) {
		    LinkedHashMap<String, Integer> roman_numerals = new LinkedHashMap<String, Integer>();
		    roman_numerals.put("M", 1000);
		    roman_numerals.put("CM", 900);
		    roman_numerals.put("D", 500);
		    roman_numerals.put("CD", 400);
		    roman_numerals.put("C", 100);
		    roman_numerals.put("XC", 90);
		    roman_numerals.put("L", 50);
		    roman_numerals.put("XL", 40);
		    roman_numerals.put("X", 10);
		    roman_numerals.put("IX", 9);
		    roman_numerals.put("V", 5);
		    roman_numerals.put("IV", 4);
		    roman_numerals.put("I", 1);
		    String res = "";
		    for(Map.Entry<String, Integer> entry : roman_numerals.entrySet()){
		      int matches = Int/entry.getValue();
		      res += repeat(entry.getKey(), matches);
		      Int = Int % entry.getValue();
		    }
		    return res;
		  }
		  public static String repeat(String s, int n) {
		    if(s == null) {
		        return null;
		    }
		    final StringBuilder sb = new StringBuilder();
		    for(int i = 0; i < n; i++) {
		        sb.append(s);
		    }
		    return sb.toString();
		  }
}
