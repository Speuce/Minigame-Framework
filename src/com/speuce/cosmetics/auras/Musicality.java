package com.speuce.cosmetics.auras;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.speuce.cosmetics.CosmeticManager;
import com.speuce.cosmetics.extra.Chord;
import com.speuce.cosmetics.extra.Rarity;

public class Musicality extends Aura{
	double t = 0;
	double c = 0;
	float y = 0;
	boolean up = true;
	int count = 0;
	int note = 0;
	int chord = 0;
	public Musicality(CosmeticManager cosman) {
		super("Musicality", cosman);
	}

	@Override
	public void Tock() {
		if(!this.players.isEmpty()){
			t += Math.PI/15;
			c += 0.005;
			count ++;
			double x = 1.5 * Math.cos(t);
			double z = 1.5 * Math.sin(t);
			
			if(up){
				y += 0.039;
			}else{
				y -= 0.039;
			}
			double x2 =1.5* Math.sin(t + 3.14);
			double z2 =1.5* Math.cos(t + 3.14);
			Float f = 0F;
			if(count == 4){
				f = this.getNote();
			}

			
			for(Player p: this.players){
				if(!p.isOnline()){
					this.off(p);
				}
				this.ShootParticle(p.getLocation().add(x, y, z), c);
				this.ShootParticle(p.getLocation().add(x2, y, z2), c);

				if(count == 4){
					p.getWorld().playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 0.5F, f);
					count = 0;
				}
			}
			if(t >= 4* Math.PI){
				t = 0;
				if(up){
					up = false;
				}else{
					up = true;
				}
			}
		}

	}
	private void ShootParticle(Location l, double t){
		l.getWorld().spawnParticle(Particle.NOTE, l.getX(), l.getY(), l.getZ(), 0, t, 0, 0, 1);
	}

	@Override
	public Rarity getRarity() {
		return Rarity.SPECIAL;
	}
	private Float getNote(){
		note++;
		if(note > 3){
			note = 0;
			chord++;
			if(chord > 7){
				chord = 0;
			}
		}
		Chord c = Chord.C_MAJOR;
		switch(chord){
		case 0:
			c = Chord.C_MAJOR;
			break;
		case 1:
			c = Chord.C_MAJOR;
			break;
		case 2:
			c = Chord.A_MINOR;
			break;
		case 3:
			c = Chord.A_MINOR;
			break;
		case 4:
			c = Chord.F_MAJOR;
			break;
		case 5:
			c = Chord.F_MAJOR;
			break;
		case 6:
			c = Chord.G_MAJOR;
			break;
		case 7:
			c = Chord.G_MAJOR;
			break;
		}
		switch(note){
		case 0:
			return c.getFirst();
		case 1:
			return c.getThird();
		case 2:
			return c.getFifth();
		case 3:
			return c.getThird();
		}
		return Chord.C_MAJOR.getFirst();
	}

}
