package com.speuce.cosmetics.auras;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import com.speuce.cosmetics.CosmeticManager;
import com.speuce.cosmetics.extra.Rarity;

public class Halo extends Aura{
	double t = 0;
	final double r = 0.5;
	public Halo(CosmeticManager cosman) {
		super("Halo", cosman);
		this.players = new ArrayList<Player>();
		
	}

	@Override
	public void Tock() {
		if(!this.players.isEmpty()){
			t += 3.14/6;
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
	private void ShootParticle(Location l){
		l.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, l.getX(), l.getY(), l.getZ(), 1, 0, 0, 0, 0);
		
		
//		PacketContainer pct = this.pm.createPacket(PacketType.Play.Server.SPAWN_ENTITY);
//		int i = this.ran.nextInt(10000) * 2;
//		pct.getDoubles()
//		.write(0, l.getX())
//		.write(1, l.getY())
//		.write(2, l.getZ());
//		pct.getIntegers().write(0, i).write(1, 2);
//		//say hi
//		//pct.getShorts().write(0, (short) 0).write(1, (short) 0).write(2,(short) 0);
//		PacketContainer pci = this.pm.createPacket(PacketType.Play.Server.ENTITY_METADATA);
//		pci.getIntegers().write(0, i);
//		WrappedDataWatcher wr = new WrappedDataWatcher();
//		Serializer serializer2 = WrappedDataWatcher.Registry.getItemStackSerializer(true);
//		WrappedDataWatcherObject object = new WrappedDataWatcher.WrappedDataWatcherObject(6, serializer2);
//		wr.setObject(object, Optional.of(new ItemStack(Material.GOLD_NUGGET)));
//		pci.getWatchableCollectionModifier().write(0, wr.getWatchableObjects());
//		for(Player p: Bukkit.getOnlinePlayers()){
//			try {
//			    pm.sendServerPacket(p, pct);
//			    pm.sendServerPacket(p, pci);
//			} catch (InvocationTargetException e) {
//				p.sendMessage(ChatColor.RED + "Error loading a Fake Object. Please notify admins!");
//			    e.printStackTrace();
//			}
//		}
	}

	@Override
	public Rarity getRarity() {
		return Rarity.RARE;
	}
}
