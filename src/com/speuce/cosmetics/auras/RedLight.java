package com.speuce.cosmetics.auras;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.util.Vector;

import com.speuce.cosmetics.CosmeticManager;
import com.speuce.cosmetics.extra.Rarity;

public class RedLight extends Aura implements Listener{
	double t = 0;
	final double r = 1.5;
	double time = 0;
	private List<Player> cooldowns;
	public RedLight(CosmeticManager cosman) {
		super("RedLight", cosman);
		cooldowns = new ArrayList<Player>();
	}


	@Override
	public Rarity getRarity() {
		return Rarity.KRYPTED;
	}
	@Override
	public void Tock() {
		time += 1;
		if(time >= 40){
			time = 0;
			if(!this.cooldowns.isEmpty()){
				this.cooldowns.clear();
			}
		}
		if(!this.players.isEmpty()){
			t += 3.14/16;
			double x = r * Math.sin(t);
			double z = r * Math.cos(t);
			double y = 2.2;
			
			for(Player p: this.players){
				if(!p.isOnline()){
					this.off(p);
				}
				this.ShootParticle(p.getLocation().add(x, y, z));
			}
			if(t >= 2*Math.PI){
				t = 0;
			}
	
		}
	}
	@EventHandler
	public void onShift(PlayerToggleSneakEvent e){
		if(!this.players.contains(e.getPlayer())){
			return;
		}
		if(!this.cooldowns.contains(e.getPlayer()) && e.isSneaking()){
			this.cooldowns.add(e.getPlayer());
			for(double theta = 0; theta < 2*Math.PI; theta += Math.PI/4){
				double x = r * Math.sin(theta);
				double z = r * Math.cos(theta);
				this.ShootAway(e.getPlayer().getLocation().add(x,0,z), e.getPlayer());
			}
			e.getPlayer().getWorld().playSound(e.getPlayer().getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 2F, 2F);
		}
	}
	private void ShootAway(Location s, Player p){
		Location l = p.getLocation();
		Vector dir = p.getLocation().toVector().subtract(s.toVector());
		dir.multiply(-1);
		dir.add(new Vector(0, 1, 0));
		double x,y,z;
		for(double i = 1; i <= 2; i += 0.25){
			x = dir.getX() * i;
			y = dir.getY() * i + 1;
			z = dir.getZ() * i;
			l.add(x,y,z);
			this.ShootParticle(l);
			l.subtract(x,y,z);
		}
	}
	
	private void ShootParticle(Location l){
		l.getWorld().spawnParticle(Particle.REDSTONE, l.getX(), l.getY(), l.getZ(), 25, 0.1, 0.1, 0.1, 0);
	}

}
