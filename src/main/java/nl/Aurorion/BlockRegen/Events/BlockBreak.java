package nl.Aurorion.BlockRegen.Events;

import com.gamingmesh.jobs.Jobs;
import com.gamingmesh.jobs.container.JobProgression;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.regions.CuboidRegion;
import nl.Aurorion.BlockRegen.Main;
import nl.Aurorion.BlockRegen.System.Getters;
import nl.Aurorion.BlockRegen.Utils;
import org.bukkit.*;
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
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BlockBreak implements Listener {

    private Main main;

    public BlockBreak(Main main) {
        this.main = main;
    }

    public static Block block;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        block = event.getBlock();
        if (Utils.bypass.contains(player.getName())) {
            return;
        }

        if (Utils.regenBlocks.contains(block.getLocation())) {
            event.setCancelled(true);
            return;
        }

        String blockname = block.getType().name();

        if (Utils.itemcheck.contains(player.getName())) {
            event.setCancelled(true);
            player.sendMessage(main.getMessages().datacheck.replace("%block%", blockname));
            return;
        }

        FileConfiguration settings = main.getFiles().getSettings();

        if (main.getGetters().useTowny()) {
            if (TownyUniverse.getTownBlock(block.getLocation()) != null) {
                if (TownyUniverse.getTownBlock(block.getLocation()).hasTown()) {
                    return;
                }
            }
        }

        if (main.getGetters().useGP()) {
            String noBuildReason = main.getGriefPrevention().allowBreak(player, block, block.getLocation(), event);
            if (noBuildReason != null) {
                return;
            }
        }

        FileConfiguration blocklist = main.getFiles().getBlocklist();
        ConfigurationSection blocks = blocklist.getConfigurationSection("Blocks");
        Set<String> setblocks = blocks.getKeys(false);

        String worldname = player.getWorld().getName();
        List<String> worlds = settings.getStringList("Worlds-Enabled");
        boolean isinone = false;
        for (String world : worlds) {
            if (world.equalsIgnoreCase(worldname)) {
                isinone = true;
            }
        }

        if (isinone) {

            World bworld = block.getWorld();

            boolean useregions = settings.getBoolean("Use-Regions");
            boolean disablebreak = settings.getBoolean("Disable-Other-Break");
            boolean disablebreakr = settings.getBoolean("Disable-Other-Break-Region");

            boolean isinregion = false;

            if (useregions && main.getWorldEdit() != null) {
                ConfigurationSection regionsection = main.getFiles().getRegions().getConfigurationSection("Regions");
                Set<String> regionset = regionsection.getKeys(false);
                for (String regionloop : regionset) {
                    Location locA = Utils.stringToLocation(main.getFiles().getRegions().getString("Regions." + regionloop + ".Max"));
                    Location locB = Utils.stringToLocation(main.getFiles().getRegions().getString("Regions." + regionloop + ".Min"));
                    CuboidRegion selection = new CuboidRegion(BukkitAdapter.asBlockVector(locA), BukkitAdapter.asBlockVector(locB));
                    if (selection.contains(BukkitAdapter.asBlockVector(block.getLocation()))) {
                        isinregion = true;
                        break;
                    }
                }
            }

            if (setblocks.contains(blockname)) {

                int expToDrop = event.getExpToDrop();

                if (isinregion) {
                    if ((main.getGetters().toolRequired(blockname) != null) && (!toolCheck(main.getGetters().toolRequired(blockname), player))) {
                        event.setCancelled(true);
                        return;
                    }
                    if ((main.getGetters().enchantRequired(blockname) != null) && (!enchantCheck(main.getGetters().enchantRequired(blockname), player))) {
                        event.setCancelled(true);
                        return;
                    }
                    if (main.getGetters().jobsCheck(blockname) != null) {
                        if (!jobsCheck(main.getGetters().jobsCheck(blockname), player)) {
                            event.setCancelled(true);
                            return;
                        }
                    }
                    event.setDropItems(false);
                    event.setExpToDrop(0);
                    this.blockBreak(player, block, blockname, bworld, expToDrop);
                } else {
                    if (useregions) {
                        return;
                    } else {
                        if ((main.getGetters().toolRequired(blockname) != null) && (!toolCheck(main.getGetters().toolRequired(blockname), player))) {
                            event.setCancelled(true);
                            return;
                        }
                        if ((main.getGetters().enchantRequired(blockname) != null) && (!enchantCheck(main.getGetters().enchantRequired(blockname), player))) {
                            event.setCancelled(true);
                            return;
                        }
                        if (main.getGetters().jobsCheck(blockname) != null) {
                            if (!jobsCheck(main.getGetters().jobsCheck(blockname), player)) {
                                event.setCancelled(true);
                                return;
                            }
                        }
                        event.setDropItems(false);
                        event.setExpToDrop(0);
                        this.blockBreak(player, block, blockname, bworld, expToDrop);
                    }
                }
            } else {
                if (isinregion) {
                    if (disablebreakr) {
                        event.setCancelled(true);
                    } else if (disablebreak) {
                        event.setCancelled(true);
                    }
                }
                if (disablebreak) {
                    event.setCancelled(true);
                }
            }
        }
    }

    private boolean toolCheck(String string, Player player) {
        String[] tools = string.split(", ");
        boolean check = false;
        for (String all : tools) {
            Material tool = Material.valueOf(all.toUpperCase());
            if (player.getInventory().getItemInMainHand().getType() == tool) {
                check = true;
                break;
            }
        }
        if (check) {
            return true;
        }
        player.sendMessage(this.main.getMessages().toolRequired.replace("%tool%", string.toLowerCase().replace("_", " ")));
        return false;
    }

    private boolean enchantCheck(String string, Player player) {
        String[] enchants = string.split(", ");
        boolean check = false;
        for (String all : enchants) {
            Enchantment enchant = Enchantment.getByKey(NamespacedKey.minecraft(all.toLowerCase()));
            ItemMeta meta = player.getInventory().getItemInMainHand().getItemMeta();
            if (meta.hasEnchant(enchant)) {
                check = true;
                break;
            }
        }
        if (check) {
            return true;
        }
        player.sendMessage(this.main.getMessages().enchantRequired.replace("%enchant%", string.toLowerCase().replace("_", " ")));
        return false;
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
        } else {
            return true;
        }
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
        if (getters.naturalBreak(blockname)) {
            for (ItemStack drops : block.getDrops()) {
                Material mat = drops.getType();
                int amount;
                if (doubleDrops) {
                    amount = drops.getAmount() * 2;
                } else {
                    amount = drops.getAmount();
                }
                ItemStack dropStack = new ItemStack(mat, amount);
                bworld.dropItemNaturally(block.getLocation(), dropStack);
            }
            if (exptodrop > 0) {
                if (doubleExp) {
                    ((ExperienceOrb) bworld.spawn(loc, ExperienceOrb.class)).setExperience(exptodrop * 2);
                } else {
                    ((ExperienceOrb) bworld.spawn(loc, ExperienceOrb.class)).setExperience(exptodrop);
                }
            }
        } else if (getters.dropItemMaterial(blockname) != null) {
            if (getters.dropItemAmount(blockname, player) > 0) {
                int itemAmount = getters.dropItemAmount(blockname, player);
                if (doubleDrops) {
                    itemAmount = itemAmount * 2;
                }
                ItemStack dropitem = new ItemStack(getters.dropItemMaterial(blockname), itemAmount);
                ItemMeta dropmeta = dropitem.getItemMeta();
                if (getters.dropItemName(blockname) != null) {
                    dropmeta.setDisplayName(getters.dropItemName(blockname));
                }
                if (!getters.dropItemLores(blockname).isEmpty()) {
                    dropmeta.setLore(getters.dropItemLores(blockname));
                }
                dropitem.setItemMeta(dropmeta);

                if (getters.dropItemDropNaturally(blockname)) {
                    bworld.dropItem(loc, dropitem);
                } else {
                    player.getInventory().addItem(dropitem);
                    player.updateInventory();
                }
            }

            if (getters.dropItemExpAmount(blockname) > 0) {
                int expAmount = getters.dropItemExpAmount(blockname);
                if (doubleExp) {
                    expAmount = expAmount * 2;
                }
                if (getters.dropItemExpDrop(blockname)) {
                    ((ExperienceOrb) bworld.spawn(loc, ExperienceOrb.class)).setExperience(expAmount);
                } else {
                    player.giveExp(expAmount);
                }
            }
        }

        if (eventItem != null) {
            if ((main.getRandom().nextInt((rarity - 1) + 1) + 1) == 1) {
                if (dropEventItem) {
                    bworld.dropItemNaturally(loc, eventItem);
                } else {
                    player.getInventory().addItem(eventItem);
                }
            }
        }

        // Vault money -----------------------------------------------------------------------------------------
        if (main.getEconomy() != null && getters.money(blockname) != null && getters.money(blockname) > 0)
            main.getEconomy().depositPlayer(player, getters.money(blockname));

        // Commands execution -----------------------------------------------------------------------------------
        if (getters.consoleCommands(blockname) != null)
            getters.consoleCommands(blockname).forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), Utils.parse(command, player)));

        if (getters.playerCommands(blockname) != null)
            getters.playerCommands(blockname).forEach(command -> Bukkit.dispatchCommand(player, Utils.parse(command, player)));

        // Particles -------------------------------------------------------------------------------------------
        if (getters.particleCheck(blockname) != null)
            main.getParticles().check(getters.particleCheck(blockname).toLowerCase());

        // Data Recovery
        FileConfiguration data = main.getFiles().getData();
        if (getters.dataRecovery()) {
            List<String> dataLocs = new ArrayList<>();
            if (data.contains(blockname))
                dataLocs = data.getStringList(blockname);
            dataLocs.add(Utils.locationToString(loc));
            data.set(blockname, dataLocs);
            main.getFiles().saveData();
        } else {
            Utils.persist.put(loc, block.getType());
        }

        // Replacing the block ---------------------------------------------------------------------------------
        new BukkitRunnable() {

            @Override
            public void run() {
                block.setType(getters.replaceBlock(blockname));
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
                if (data != null && data.contains(blockname)) {
                    List<String> dataLocs = data.getStringList(blockname);
                    if (dataLocs != null && !dataLocs.isEmpty()) {
                        dataLocs.remove(Utils.locationToString(loc));
                        data.set(blockname, dataLocs);
                        main.getFiles().saveData();
                    }
                }
            }
        }.runTaskLater(main, regendelay * 20);

        Utils.tasks.put(loc, task);
        return;
    }

}
