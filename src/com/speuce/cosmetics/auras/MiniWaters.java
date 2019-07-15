package com.speuce.cosmetics.auras;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import com.speuce.cosmetics.CosmeticManager;
import com.speuce.cosmetics.extra.Rarity;

public class MiniWaters extends Aura{
	List<String> lore;
	double t = 0;
	final double r = 2.5D;
	final double min = Math.PI/16;
	public MiniWaters(CosmeticManager cosman) {
		super("WaterCircle", cosman);
		this.lore = new LinkedList<String>();
		this.lore.add("A Water Circle that Circles you.");
	}

	@Override
	public void Tock() {
		if(this.players.isEmpty()){
			return;
		}
		double x1 = Math.sin(t);
		double y = 1.3, y1 = 1.3;
		double z1 = Math.cos(t);
		for(double thet = r; thet > r - 0.75; thet -= 0.15){
			double x = x1 * thet;
			double z = z1 * thet;
			if(thet >= r - 0.3 ){
				if(thet >= r - 0.3){
					y -= 0.1125;
					y1 += 0.1125;
				}else{
					y -= 0.15;
					y1 += 0.15;
				}

			}else{
				y += 0.15;
				y1 -= 0.15;
			}
			if(r == thet || thet == (r-0.6)){
				for(Player p: this.players){
					if(!p.isOnline()){
						this.off(p);
					}else{
						this.ShootParticle(p.getLocation().add(x, 1.3, z));
					}

				}
			}else{
				for(Player p: this.players){
					if(!p.isOnline()){
						this.off(p);
					}else{
						this.ShootParticle(p.getLocation().add(x, y, z));
						this.ShootParticle(p.getLocation().add(x, y1, z));
					}

				}
			}

			
		}
		t += min;
		if(t >= 6.28){
			t = 0;
		}
		
	}

	private void ShootParticle(Location l){
		l.getWorld().spawnParticle(Particle.WATER_DROP, l.getX(), l.getY(), l.getZ(), 3, 0, 0, 0, 0);
	}


	@Override
	public Rarity getRarity() {
		return Rarity.RARE;
	}


}
