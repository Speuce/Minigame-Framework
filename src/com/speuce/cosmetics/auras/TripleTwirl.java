package com.speuce.cosmetics.auras;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.speuce.cosmetics.CosmeticManager;
import com.speuce.cosmetics.extra.Rarity;

public class TripleTwirl extends Aura{
	double rr = 2.0, t = 0;
	public TripleTwirl(CosmeticManager cosman) {
		super("TripleTwirl", cosman);
	}
	@Override
	public void Tock() {
		double y;
		if(!players.isEmpty()){
			t += Math.PI/10;
			y = t*0.3;
			double r = rr - (t * 0.15);
			//double r = rr;
			double x1 =r * Math.sin(t);
			double z1 =r* Math.cos(t);
			
			double x2 =r* Math.sin(t + 2.09);
			double z2 =r* Math.cos(t + 2.09);
			
			double x3 = r*Math.sin(t + 4.18);
			double z3 = r*Math.cos(t + 4.18);
			for(Player p: this.players){
				if(!p.isOnline()){
					this.off(p);
				}
				this.ShootParticle(p.getLocation().add(x1, y, z1));
				this.ShootParticle(p.getLocation().add(x2, y, z2));
				this.ShootParticle(p.getLocation().add(x3, y, z3));
			}

			if(t >= 4* Math.PI){
				t = 0;
				if(!this.players.isEmpty()){
					for(double i = 0; i <= 2 * Math.PI; i += Math.PI/4){
						double x = Math.sin(i);
						double yt = 0.8;
						double z = Math.cos(i);
						Vector v = new Vector(x, yt, z).normalize();
						for(Player p: this.players){
							Location l = p.getLocation();
							l.getWorld().spawnParticle(Particle.FLAME, l.getX(), l.getY() + y, l.getZ(), 0, v.getX(), v.getY(), v.getZ());
						}
						
					}
				}

			}
		}
		
		
	}
	private void ShootParticle(Location l){
		l.getWorld().spawnParticle(Particle.FLAME, l.getX(), l.getY(), l.getZ(), 1, 0, 0, 0, 0);
	}
	@Override
	public Rarity getRarity() {
		return Rarity.LEGENDARY;
	}
}
