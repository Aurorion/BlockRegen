package nl.Aurorion.BlockRegen.Events;

import com.gamingmesh.jobs.Jobs;
import com.gamingmesh.jobs.container.JobProgression;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.CuboidRegion;
import nl.Aurorion.BlockRegen.Main;
import nl.Aurorion.BlockRegen.System.Getters;
import nl.Aurorion.BlockRegen.Utils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BlockBreak implements Listener {

    public static Block block;
    private Main main;

    public BlockBreak(Main main) {
        this.main = main;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        block = event.getBlock();

        if (Utils.bypass.contains(player.getName()))
            return;

        if (Utils.regenBlocks.contains(block.getLocation())) {
            event.setCancelled(true);
            return;
        }

        String blockType = Utils.blockToString(block);

        // Find out blockNames by material
        List<String> blockNames = new ArrayList<>();
        for (String loopString : main.getGetters().blockNames()) {
            if (main.getGetters().blockMaterial(loopString).equals(block.getType())) {
                if (main.getGetters().blockData(loopString) == block.getData()) {
                    blockNames.add(loopString);
                }
            }
        }

        if (Utils.itemcheck.contains(player.getName())) {
            event.setCancelled(true);
            player.sendMessage(main.getMessages().datacheck.replace("%block%", blockType));
            return;
        }

        FileConfiguration settings = main.getFiles().getSettings();

        // Region and world stuff

        if (settings.getStringList("Worlds-Enabled").contains(player.getWorld().getName())) {

            World world = block.getWorld();

            boolean useRegions = settings.getBoolean("Use-Regions");
            boolean disableBreak = settings.getBoolean("Disable-Other-Break");
            boolean disableBreakRegions = settings.getBoolean("Disable-Other-Break-Region");

            boolean isInRegion = false;

            // Is in region or not?

            // Which one?
            String regionName = null;

            if (useRegions && main.getWorldEdit() != null) {

                ConfigurationSection regionSection = main.getFiles().getRegions().getConfigurationSection("Regions");
                Set<String> regionset = regionSection.getKeys(false);

                for (String regionloop : regionset) {
                    World regionWorld = Bukkit.getWorld(main.getFiles().getRegions().getString("Regions." + regionloop + ".World"));

                    if (world == regionWorld) {

                        Vector locA = Utils.stringToVector(main.getFiles().getRegions().getString("Regions." + regionloop + ".Max"));
                        Vector locB = Utils.stringToVector(main.getFiles().getRegions().getString("Regions." + regionloop + ".Min"));

                        CuboidRegion selection = new CuboidRegion(locA, locB);

                        Vector vec = new Vector(Double.valueOf(block.getX()), Double.valueOf(block.getY()), Double.valueOf(block.getZ()));

                        if (selection.contains(vec)) {
                            isInRegion = true;
                            regionName = regionloop;
                            break;
                        }
                    }
                }
            }

            List<String> list1 = new ArrayList<>();

            if (isInRegion) {
                for (String blockName : blockNames) {
                    if (!main.getGetters().regionBlocks(regionName).contains(blockName)) {
                        list1.add(blockName);
                    }
                }
            }
            blockNames.removeAll(list1);

            String blockName;

            if (blockNames.size() > 1) {
                main.getLogger().warning("There are multiple options for this block, unable to take action.");
                return;
            } else if (blockNames.size() == 1)
                blockName = blockNames.get(0);
            else {
                // Block Break disable, if enabled.
                if (isInRegion) {
                    if (disableBreakRegions)
                        event.setCancelled(true);
                    else if (disableBreak)
                        event.setCancelled(true);
                    else {
                        if (!Utils.restorer.containsKey(block.getLocation()) && main.getGetters().useRestorer())
                            Utils.restorer.put(block.getLocation(), block.getType());
                        return;
                    }
                }
                if (disableBreak)
                    event.setCancelled(true);
                else {
                    if (!Utils.restorer.containsKey(block.getLocation()) && main.getGetters().useRestorer())
                        Utils.restorer.put(block.getLocation(), block.getType());
                }
                return;
            }

            int expToDrop = event.getExpToDrop();

            if (isInRegion) {
                // Tool check
                if ((main.getFiles().getBlocklist().getConfigurationSection("Blocks." + blockName + ".tool-required") != null) && (!toolCheck(blockName, player))) {
                    event.setCancelled(true);
                    return;
                }
                // Jobs require check
                if (main.getGetters().jobsCheck(blockName) != null) {
                    if (!jobsCheck(main.getGetters().jobsCheck(blockName), player)) {
                        event.setCancelled(true);
                        return;
                    }
                }
                // Proceed
                event.setDropItems(false);
                event.setExpToDrop(0);
                this.blockBreak(player, block, blockName, world, expToDrop);
            } else {
                // Out of an region, if enabled.
                if (useRegions)
                    return;
                else {
                    if ((main.getFiles().getBlocklist().getConfigurationSection("Blocks." + blockName + ".tool-required") != null) && (!toolCheck(blockName, player))) {
                        event.setCancelled(true);
                        return;
                    }
                    if (main.getGetters().jobsCheck(blockName) != null) {
                        if (!jobsCheck(main.getGetters().jobsCheck(blockName), player)) {
                            event.setCancelled(true);
                            return;
                        }
                    }
                    event.setDropItems(false);
                    event.setExpToDrop(0);
                    this.blockBreak(player, block, blockName, world, expToDrop);
                }
            }
        }
    }

    private boolean toolCheck(String blockName, Player player) {

        ItemStack tool = player.getInventory().getItemInHand();

        // Type
        if (main.getFiles().getBlocklist().getConfigurationSection("Blocks." + blockName + ".tool-required") != null) {
            if (!tool.getType().equals(main.getGetters().toolMaterial(blockName)))
                return false;
            if (tool.getAmount() != main.getGetters().toolAmount(blockName))
                return false;
            if (!tool.getItemMeta().getItemFlags().equals(main.getGetters().toolItemFlags(blockName)))
                return false;
            if (!(Utils.enchantmentsToEnchants(tool.getItemMeta().getEnchants()).contains(main.getGetters().toolEnchants(blockName))))
                return false;
            if (!tool.getItemMeta().getDisplayName().equals(main.getGetters().toolName(blockName)))
                return false;
            if (!tool.getItemMeta().getLore().equals(main.getGetters().toolLores(blockName)))
                return false;
        }
        return true;
    }

    private boolean jobsCheck(String string, Player player) {
        String job = null;
        int level = 0;
        String[] jobCheckString = string.split(";");
        List<JobProgression> jobs = Jobs.getPlayerManager().getJobsPlayer(player).getJobProgression();
        for (JobProgression OneJob : jobs) {
            if (OneJob.getJob().equals(Jobs.getJob(jobCheckString[0]))) {
                job = OneJob.getJob().getName();
                level = OneJob.getLevel();
                break;
            }
        }
        if (job == null || !job.equals(jobCheckString[0])) {
            player.sendMessage(main.getMessages().jobsError.replace("%job%", jobCheckString[0]).replace("%level%", jobCheckString[1]));
            return false;
        } else if (level < Integer.valueOf(jobCheckString[1])) {
            player.sendMessage(main.getMessages().jobsError.replace("%job%", jobCheckString[0]).replace("%level%", jobCheckString[1]));
            return false;
        } else
            return true;
    }

    private void blockBreak(Player player, Block block, String blockname, World bworld, Integer exptodrop) {
        Getters getters = main.getGetters();
        BlockState state = block.getState();
        Location loc = block.getLocation();

        // Events ---------------------------------------------------------------------------------------------
        boolean doubleDrops = false;
        boolean doubleExp = false;
        ItemStack eventItem = null;
        boolean dropEventItem = false;
        int rarity = 0;

        if (getters.eventName(blockname) != null) {
            if (Utils.events.get(getters.eventName(blockname))) {
                doubleDrops = getters.eventDoubleDrops(blockname);
                doubleExp = getters.eventDoubleExp(blockname);
                if (getters.eventItemMaterial(blockname) != null) {
                    eventItem = new ItemStack(getters.eventItemMaterial(blockname), 1);
                    ItemMeta meta = eventItem.getItemMeta();
                    if (getters.eventItemName(blockname) != null) {
                        meta.setDisplayName(getters.eventItemName(blockname));
                    }
                    if (!getters.eventItemLores(blockname).isEmpty()) {
                        meta.setLore(getters.eventItemLores(blockname));
                    }
                    eventItem.setItemMeta(meta);
                    dropEventItem = getters.eventItemDropNaturally(blockname);
                    rarity = getters.eventItemRarity(blockname);
                }
            }
        }

        // Drop Section-----------------------------------------------------------------------------------------
        if (!player.getGameMode().equals(GameMode.CREATIVE)) {
            if (getters.naturalBreak(blockname)) {
                for (ItemStack drop : block.getDrops()) {
                    Material dropMaterial = drop.getType();
                    int dropAmount;
                    if (doubleDrops)
                        dropAmount = drop.getAmount() * 2;
                    else
                        dropAmount = drop.getAmount();

                    ItemStack dropStack = new ItemStack(dropMaterial, dropAmount);
                    bworld.dropItemNaturally(block.getLocation(), dropStack);
                }
                if (doubleExp)
                    bworld.spawn(loc, ExperienceOrb.class).setExperience(exptodrop * 2);
                else
                    bworld.spawn(loc, ExperienceOrb.class).setExperience(exptodrop);
            }
        } else if (getters.dropItemMaterial(blockname) != null) {
            if (getters.dropItemAmount(blockname, player) > 0) {
                if (!player.getGameMode().equals(GameMode.SURVIVAL)) {

                    // CUSTOM DROP ITEMSTACK -----------------------------------------------------------

                    int dropAmount = getters.dropItemAmount(blockname, player);

                    if (doubleDrops)
                        dropAmount = dropAmount * 2;

                    ItemStack dropItemStack = new ItemStack(getters.dropItemMaterial(blockname), dropAmount, getters.dropItemData(blockname));
                    ItemMeta dropMeta = dropItemStack.getItemMeta();

                    if (getters.dropItemName(blockname) != null)
                        dropMeta.setDisplayName(getters.dropItemName(blockname));

                    if (!getters.dropItemLores(blockname).isEmpty())
                        dropMeta.setLore(getters.dropItemLores(blockname));

                    if (!getters.dropEnchants(blockname).isEmpty())
                        getters.dropEnchants(blockname).forEach(enchant -> dropMeta.addEnchant(enchant.getEnchantment(), enchant.getLevel(), true));

                    if (!getters.dropFlags(blockname).isEmpty())
                        getters.dropFlags(blockname).forEach(flag -> dropMeta.addItemFlags(flag));

                    dropItemStack.setItemMeta(dropMeta);

                    if (getters.dropItemDropNaturally(blockname))
                        bworld.dropItem(loc, dropItemStack);
                    else {
                        player.getInventory().addItem(dropItemStack);
                        player.updateInventory();
                    }
                }
            }

            // EXP -------------------------------------------

            if (getters.dropItemExpAmount(blockname) > 0) {
                if (!player.getGameMode().equals(GameMode.CREATIVE)) {
                    int expAmount = getters.dropItemExpAmount(blockname);

                    if (doubleExp)
                        expAmount = expAmount * 2;

                    if (getters.dropItemExpDrop(blockname))
                        bworld.spawn(loc, ExperienceOrb.class).setExperience(expAmount);
                    else
                        player.giveExp(expAmount);
                }
            }
        }

        if (!player.getGameMode().equals(GameMode.CREATIVE)) {
            if (eventItem != null) {
                if (main.getRandom().nextInt(rarity) == 1) {
                    if (dropEventItem)
                        bworld.dropItemNaturally(loc, eventItem);
                    else
                        player.getInventory().addItem(eventItem);
                }
            }
        }

        // Vault money -----------------------------------------------------------------------------------------
        if (main.getEconomy() != null && getters.money(blockname) > 0)
            main.getEconomy().depositPlayer(player, getters.money(blockname));

        // Command execution -----------------------------------------------------------------------------------
        if (getters.consoleCommands(blockname) != null)
            main.getGetters().consoleCommands(blockname).forEach(consoleCommand -> Bukkit.dispatchCommand(player, consoleCommand.replace("%player%", player.getName())));

        if (getters.playerCommands(blockname) != null)
            main.getGetters().playerCommands(blockname).forEach(playerCommand -> Bukkit.dispatchCommand(player, playerCommand.replace("%player%", player.getName())));

        // Particles ------------------------------------------------------------------------------------------
        // Disabled ATM - Will be in an particle update
		/*if (blocklist.getString("Blocks." + blockname + ".particle-effect") != null) {
			String particleName = blocklist.getString("Blocks." + blockname + ".particle-effect");
			main.getParticles().check(particleName);
		}*/

        // Replacing the block ---------------------------------------------------------------------------------
        Utils.persist.put(loc, block.getType());
        new BukkitRunnable() {

            @Override
            public void run() {
                block.setType(getters.replaceBlockMaterial(blockname));
                block.setData(getters.replaceBlockData(blockname));
            }

        }.runTaskLater(main, 2l);

        Utils.regenBlocks.add(loc);

        // Actual Regening -------------------------------------------------------------------------------------
        int regendelay = 3;
        if (getters.replaceDelay(blockname) != null) {
            regendelay = getters.replaceDelay(blockname);
        }

        BukkitTask task = new BukkitRunnable() {
            public void run() {
                state.update(true);
                Utils.persist.remove(loc);
                Utils.regenBlocks.remove(loc);
                Utils.tasks.remove(loc);
            }
        }.runTaskLater(main, regendelay * 20);

        Utils.tasks.put(loc, task);
        return;
    }

}
