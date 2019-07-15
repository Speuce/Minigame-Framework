package com.speuce.cosmetics.auras;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import com.speuce.cosmetics.CosmeticManager;
import com.speuce.cosmetics.extra.Legacy;
import com.speuce.cosmetics.extra.Rarity;

public class Twirl extends Aura implements Legacy{
	double t = 0;
	double y;
	public Twirl(CosmeticManager cosman) {
		super("Twirl", cosman);
	}

	@Override
	public void Tock() {
		if(!this.players.isEmpty()){
			t += Math.PI/10;
			double x = 1.5 * Math.cos(t);
			double z = 1.5 * Math.sin(t);
			y = t*0.23;
			for(Player p: this.players){
				if(!p.isOnline()){
					this.off(p);
				}
				this.ShootParticle(p.getLocation().add(x, y, z));
			}
			if(t >= 4* Math.PI){
				t = Math.PI/10;
			}
		}

	}
	private void ShootParticle(Location l){
		l.getWorld().spawnParticle(Particle.FLAME, l.getX(), l.getY(), l.getZ(), 1, 0, 0, 0, 0);
	}

	@Override
	public Rarity getRarity() {
		return Rarity.RARE;
	}


	@Override
	public String getRetireDate() {
		return "Nov 28, 2016";
	}

}
