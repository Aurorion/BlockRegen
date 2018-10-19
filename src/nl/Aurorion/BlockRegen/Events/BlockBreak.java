package nl.Aurorion.BlockRegen.Events;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import com.palmergames.bukkit.towny.object.TownyUniverse;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.CuboidRegion;

import nl.Aurorion.BlockRegen.Main;
import nl.Aurorion.BlockRegen.Utils;

@SuppressWarnings("deprecation")
public class BlockBreak implements Listener {
	
	Main main;
	
	public BlockBreak(Main main){
		this.main = main;
	}
	
	private ArrayList<Location> regenBlocks = new ArrayList<Location>();
	
	public static Block block;
	
	@EventHandler (priority = EventPriority.MONITOR)
	public void onBreak(BlockBreakEvent event){
		Player player = event.getPlayer();
		if(Utils.bypass.contains(player.getName())){
			return;
		}
		
		block = event.getBlock();
		
		if(regenBlocks.contains(block.getLocation())){
			event.setCancelled(true);
			return;
		}
		
		String blockname = Utils.blockToString(block);
		
		if(Utils.itemcheck.contains(player.getName())){
			event.setCancelled(true);
			player.sendMessage(main.getMessages().datacheck.replace("%block%", blockname));
			return;
		}
		
		FileConfiguration settings = main.getFiles().getSettings();
		
		if(settings.getBoolean("Towny-Support")){
			if(TownyUniverse.getTownBlock(block.getLocation()) != null){
				if(TownyUniverse.getTownBlock(block.getLocation()).hasTown()){
					return;
				}
			}
		}
		
		FileConfiguration blocklist = main.getFiles().getBlocklist();
		ConfigurationSection blocks = blocklist.getConfigurationSection("Blocks");
		Set<String> setblocks = blocks.getKeys(false);
		
		String worldname = player.getWorld().getName();
		List<String> worlds = settings.getStringList("Worlds-Enabled");
		boolean isinone = false;
		for(String world : worlds){
			if(world.equalsIgnoreCase(worldname)){
				isinone = true;
			}
		}
		
		if(isinone){
			
			World bworld = block.getWorld();
			
			boolean useregions		= settings.getBoolean("Use-Regions");
			boolean disablebreak	= settings.getBoolean("Disable-Other-Break");
			boolean disablebreakr	= settings.getBoolean("Disable-Other-Break-Region");
			
			boolean isinregion		= false;
			
			if(useregions && main.getWorldEdit() != null){
				ConfigurationSection regionsection = main.getFiles().getRegions().getConfigurationSection("Regions");
				Set<String> regionset = regionsection.getKeys(false);
				for(String regionloop : regionset){
					World world = Bukkit.getWorld(main.getFiles().getRegions().getString("Regions." + regionloop + ".World"));
					if(world == bworld) {
						Vector locA = Utils.stringToVector(main.getFiles().getRegions().getString("Regions." + regionloop + ".Max"));
						Vector locB = Utils.stringToVector(main.getFiles().getRegions().getString("Regions." + regionloop + ".Min"));
						CuboidRegion selection = new CuboidRegion(locA, locB);
						Vector vec = new Vector(Double.valueOf(block.getX()), Double.valueOf(block.getY()), Double.valueOf(block.getZ()));
						if(selection.contains(vec)){
							isinregion = true;
							break;
						}
					}
				}
			}
			
			if(setblocks.contains(blockname)){
				
				int expToDrop = event.getExpToDrop();
				
				if(isinregion){
					if(blocklist.getString("Blocks." + blockname + ".tool-required") != null){
						Material tool = Material.valueOf(blocklist.getString("Blocks." + blockname + ".tool-required").toUpperCase());
						if(player.getInventory().getItemInMainHand().getType() != tool){
							event.setCancelled(true);
							player.sendMessage(main.getMessages().toolRequired.replace("%tool%", tool.toString().toLowerCase().replace("_", " ")));
							return;
						}
					}
					event.setDropItems(false);
					event.setExpToDrop(0);
					this.blockBreak(player, block, blockname, bworld, expToDrop);
				}else{
					if(useregions){
						return;
					}else{
						if(blocklist.getString("Blocks." + blockname + ".tool-required") != null){
							Material tool = Material.valueOf(blocklist.getString("Blocks." + blockname + ".tool-required").toUpperCase());
							if(player.getInventory().getItemInMainHand().getType() != tool){
								event.setCancelled(true);
								player.sendMessage(main.getMessages().toolRequired.replace("%tool%", tool.toString().toLowerCase().replace("_", " ")));
								return;
							}
						}
						event.setDropItems(false);
						event.setExpToDrop(0);
						this.blockBreak(player, block, blockname, bworld, event.getExpToDrop());
					}
				}				
			}else{
				if(isinregion){
					if(disablebreakr){
						event.setCancelled(true);
					}else if(disablebreak){
						event.setCancelled(true);
					}else{
						return;
					}
				}
				if(disablebreak){
					event.setCancelled(true);
				}
			}
		}
	}
	
	
	private void blockBreak(Player player, Block block, String blockname, World bworld, Integer exptodrop){
		FileConfiguration blocklist = main.getFiles().getBlocklist();
		BlockState state = block.getState();
		Location loc = block.getLocation();
		
		//Events  ---------------------------------------------------------------------------------------------
		String eventName;
		boolean doubleDrops = false;
		boolean doubleExp = false;
		ItemStack eventItem = null;
		boolean dropEventItem = false;
		int rarity = 0;
		
		if(blocklist.getString("Blocks." + blockname + ".event.event-name") != null){
			eventName = blocklist.getString("Blocks." + blockname + ".event.event-name");
			if(Utils.events.get(eventName) == true){
				doubleDrops = blocklist.getBoolean("Blocks." + blockname + ".event.double-drops");
				doubleExp = blocklist.getBoolean("Blocks." + blockname + ".event.double-exp");
				if(blocklist.getBoolean("Blocks." + blockname + ".event.custom-item.enabled") == true && blocklist.getString("Blocks." + blockname + ".event.custom-item.material") != null){
					eventItem = new ItemStack(Material.valueOf(blocklist.getString("Blocks." + blockname + ".event.custom-item.material").toUpperCase()), 1);
					ItemMeta meta = eventItem.getItemMeta();
					ArrayList<String> lores = new ArrayList<String>();
					if(blocklist.getString("Blocks." + blockname + ".event.custom-item.name") != null){
						meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', blocklist.getString("Blocks." + blockname + ".event.custom-item.name")));
						for(String lorelist : blocklist.getStringList("Blocks." + blockname + ".event.custom-item.lores")){
							lores.add(ChatColor.translateAlternateColorCodes('&', lorelist));
						}
					}else{
						meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&4&l[&cError&4&l]"));
						lores.add(" ");
						lores.add(ChatColor.translateAlternateColorCodes('&', "&aYou forgot to name this item"));
						lores.add(ChatColor.translateAlternateColorCodes('&', "&aPlease specify a name in your Blocklist.yml"));
						lores.add("  ");
					}
					meta.setLore(lores);
					eventItem.setItemMeta(meta);
					dropEventItem = blocklist.getBoolean("Blocks." + blockname + ".event.custom-item.drop-naturally"); 
					rarity = blocklist.getInt("Blocks." + blockname + ".event.custom-item.rarity");
				}
			}
		}
		
		//Drop Section-----------------------------------------------------------------------------------------
		ItemStack item = player.getInventory().getItemInMainHand();
		boolean naturalbreak;
		if(blocklist.get("Blocks." + blockname + ".natural-break") == null){
			naturalbreak = true;
		}else{
			naturalbreak = blocklist.getBoolean("Blocks." + blockname + ".natural-break");
		}
		
		if(naturalbreak){
			for(ItemStack drops : block.getDrops()){
				Material mat = drops.getType();
				int amount;
				if(doubleDrops){
					amount = drops.getAmount() * 2;
				}else{
					amount = drops.getAmount();
				}
				ItemStack dropStack = new ItemStack(mat, amount);
				bworld.dropItemNaturally(block.getLocation(), dropStack);
			}
			if(doubleExp){
				((ExperienceOrb)bworld.spawn(loc, ExperienceOrb.class)).setExperience(exptodrop * 2);
			}else{
				((ExperienceOrb)bworld.spawn(loc, ExperienceOrb.class)).setExperience(exptodrop);
			}
		}else{
			if(blocklist.getString("Blocks." + blockname + ".drop-item.material") == null){
				if(!player.hasPermission("blockregen.admin")){
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l[&3BlockRegen&6&l] &cThere is something wrong with BlockRegen. Please contact an admin to report this issue."));
				}else{
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l[&3BlockRegen&6&l] &cThe drop-item section of the block is not propperly set up."));
				}
			}else{
				Material dropmaterial = Material.valueOf(blocklist.getString("Blocks." + blockname + ".drop-item.material"));
				
				String itemname	= blocklist.getString("Blocks." + blockname + ".drop-item.name");
				if(itemname == null){
					itemname = ChatColor.translateAlternateColorCodes('&', "&4&l[&cError&4&l]");
				}else{
					itemname = blocklist.getString("Blocks." + blockname + ".drop-item.name");
				}
				
				ArrayList<String> lores = new ArrayList<String>();
				if(itemname.equalsIgnoreCase("§4§l[§cError§4§l]")){
					lores.add(" ");
					lores.add(ChatColor.translateAlternateColorCodes('&', "&aYou forgot to name this item"));
					lores.add(ChatColor.translateAlternateColorCodes('&', "&aPlease specify a name in your Blocklist.yml"));
					lores.add("  ");
				}else{
					for(String lorelist : blocklist.getStringList("Blocks." + blockname + ".drop-item.lores")){
						lores.add(ChatColor.translateAlternateColorCodes('&', lorelist));
					}
				}
				
				boolean dropnaturally 	= blocklist.getBoolean("Blocks." + blockname + ".drop-item.drop-naturally");
				boolean expdrop			= blocklist.getBoolean("Blocks." + blockname + ".drop-item.exp.drop-naturally");
				int expamount			= blocklist.getInt("Blocks." + blockname + ".drop-item.exp.amount");
				int amounthigh			= blocklist.getInt("Blocks." + blockname + ".drop-item.amount.high");
				int amountlow			= blocklist.getInt("Blocks." + blockname + ".drop-item.amount.low");
				Random random = new Random();
				int amount = random.nextInt((amounthigh - amountlow) + 1) + amountlow;
				
				if(amount > 0){
					if(doubleDrops){
						amount = amount * 2;
					}
					
					if(item.hasItemMeta() && item.getItemMeta().hasEnchant(Enchantment.LOOT_BONUS_BLOCKS)){
						int enchantLevel = item.getItemMeta().getEnchantLevel(Enchantment.LOOT_BONUS_BLOCKS);
						amount = amount + enchantLevel;
					}
					ItemStack dropitem = new ItemStack(dropmaterial, amount);
					ItemMeta dropmeta = dropitem.getItemMeta();
					dropmeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', itemname));
					dropmeta.setLore(lores);
					dropitem.setItemMeta(dropmeta);
					
					if(dropnaturally){
						bworld.dropItem(loc, dropitem);
					}else{
						player.getInventory().addItem(dropitem);
						player.updateInventory();
					}
				}
				
				if(expamount > 0){
					if(expdrop){
						((ExperienceOrb)bworld.spawn(loc, ExperienceOrb.class)).setExperience(expamount);
					}else{
						player.giveExp(expamount);
					}
				}
			}	
		}
		
		if(eventItem != null){
			int random = new Random().nextInt(rarity);
			if(random == 1){
				if(dropEventItem){
					bworld.dropItemNaturally(loc, eventItem);
				}else{
					player.getInventory().addItem(eventItem);
				}
			}
		}
		
		//Vault money -----------------------------------------------------------------------------------------
		boolean usevault 	= main.getFiles().getSettings().getBoolean("Use-Economy");
		int money			= 0;
		if(blocklist.get("Blocks." + blockname + ".money") != null){
			money = blocklist.getInt("Blocks." + blockname + ".money");
		}
		if(main.getEconomy() != null && usevault && money > 0){
				main.getEconomy().depositPlayer(player, money);
		}
		
		//Command execution -----------------------------------------------------------------------------------
		String consoleCommand = blocklist.getString("Blocks." + blockname + ".console-command");
		if(consoleCommand != null){
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), consoleCommand.replace("%player%", player.getName()));
		}
		
		String playerCommand = blocklist.getString("Blocks." + blockname + ".player-command");
		if(playerCommand != null){
			Bukkit.dispatchCommand(player, playerCommand.replace("%player%", player.getName()));
		}
		
		//Tool damage -----------------------------------------------------------------------------------------
		int tooldamage	    = blocklist.getInt("Blocks." + blockname + ".tool-damage");
		int durability		= item.getDurability();
		int maxdurability	= item.getType().getMaxDurability();
		int diff			= maxdurability - durability;
		int diff2			= diff - tooldamage;
		
		if(tooldamage != 0){
			if(diff2 > 0){
				item.setDurability((short) (durability + tooldamage));
			}else{
				player.getInventory().remove(item);
			}	
			player.updateInventory();
		}
		
		//Particles  ------------------------------------------------------------------------------------------
		//Disabled ATM
		/*if(blocklist.getString("Blocks." + blockname + ".particle-effect") != null){
			String particleName = blocklist.getString("Blocks." + blockname + ".particle-effect");
			main.getParticles().check(particleName);
		}*/
		
		//Replacing the block ---------------------------------------------------------------------------------
		
		new BukkitRunnable(){

			@Override
			public void run() {
				String replaceName = blocklist.getString("Blocks." + blockname + ".replace-block");
				block.setType(Material.valueOf(replaceName));		
			}
			
		}.runTaskLater(main, 2l);
		
		regenBlocks.add(loc);
		
		//Actual Regening -------------------------------------------------------------------------------------
		int regendelay = 3;
		if(blocklist.get("Blocks." + blockname + ".regen-delay") != null){
			regendelay = blocklist.getInt("Blocks." + blockname + ".regen-delay");
		}
		new BukkitRunnable(){

			@Override
			public void run() {
				state.update(true);
				regenBlocks.remove(loc);
			}
			
		}.runTaskLater(main, regendelay * 20);
		return;
	}

}
