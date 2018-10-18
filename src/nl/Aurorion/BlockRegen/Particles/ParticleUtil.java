package nl.Aurorion.BlockRegen.Particles;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitTask;

import nl.Aurorion.BlockRegen.Main;
import nl.Aurorion.BlockRegen.Events.BlockBreak;
import nl.Aurorion.BlockRegen.Particles.breaking.FireWorks;
import nl.Aurorion.BlockRegen.Particles.breaking.FlameCrown;

public class ParticleUtil {
	
	Main main;
	
	public ParticleUtil(Main instance){
		this.main = instance;
	}
	
	private HashMap<Block, BukkitTask> tasks = new HashMap<Block, BukkitTask>();
	
	public void check(String particleName){
		Block block = BlockBreak.block;
		if(tasks.containsKey(block)){
			Bukkit.getScheduler().cancelTask(tasks.get(block).getTaskId());
		}
		
		BukkitTask task = null;
		
		if(particleName.equalsIgnoreCase("flame_crown")){
			task = Bukkit.getScheduler().runTaskTimerAsynchronously(main, new FlameCrown(), 0l, 20l);
		}
		if(particleName.equalsIgnoreCase("fireworks")){
			task = Bukkit.getScheduler().runTask(main, new FireWorks(main));
		}
		
		tasks.put(block, task);
	}

}
