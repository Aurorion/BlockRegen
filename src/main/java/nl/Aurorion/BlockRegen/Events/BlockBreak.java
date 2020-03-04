package nl.Aurorion.BlockRegen.Events;

import com.gamingmesh.jobs.Jobs;
import com.gamingmesh.jobs.container.JobProgression;
import com.palmergames.bukkit.towny.TownyAPI;
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

public class BlockBreak implements Listener {

    private final Main plugin;

    public BlockBreak(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBreak(BlockBreakEvent event) {

        Player player = event.getPlayer();
        Block block = event.getBlock();

        // Check bypass
        if (Utils.bypass.contains(player.getName()))
            return;

        // Check regen
        if (Utils.regenBlocks.contains(block.getLocation())) {
            event.setCancelled(true);
            return;
        }

        String blockName = block.getType().name();

        // Block data check
        if (Utils.itemcheck.contains(player.getName())) {
            event.setCancelled(true);
            player.sendMessage(plugin.getMessages().datacheck.replace("%block%", blockName));
            return;
        }

        FileConfiguration settings = plugin.getFiles().getSettings();

        // Towny
        if (plugin.getGetters().useTowny()) {
            if (TownyAPI.getInstance().getTownBlock(block.getLocation()) != null)
                if (TownyAPI.getInstance().getTownBlock(block.getLocation()).hasTown())
                    return;
        }

        // Grief Prevention
        if (plugin.getGetters().useGP()) {
            String noBuildReason = plugin.getGriefPrevention().allowBreak(player, block, block.getLocation(), event);
            if (noBuildReason != null)
                return;
        }

        FileConfiguration blockList = plugin.getFiles().getBlocklist();

        // Check worlds
        String worldName = player.getWorld().getName();
        List<String> worlds = settings.getStringList("Worlds-Enabled");

        boolean isInWorld = false;
        for (String world : worlds) {
            if (world.equalsIgnoreCase(worldName)) {
                isInWorld = true;
                break;
            }
        }

        // Load blocks
        ConfigurationSection blockSection = blockList.getConfigurationSection("Blocks");

        List<String> blocks = blockSection == null ? new ArrayList<>() : new ArrayList<>(blockSection.getKeys(false));

        if (isInWorld) {

            World bworld = block.getWorld();

            boolean useRegions = settings.getBoolean("Use-Regions");
            boolean disableBreak = settings.getBoolean("Disable-Other-Break");
            boolean disableBreakRegions = settings.getBoolean("Disable-Other-Break-Region");

            boolean isInRegion = false;

            if (useRegions && plugin.getWorldEdit() != null) {
                ConfigurationSection regionSection = plugin.getFiles().getRegions().getConfigurationSection("Regions");

                List<String> regions = regionSection == null ? new ArrayList<>() : new ArrayList<>(regionSection.getKeys(false));

                for (String region : regions) {
                    String max = plugin.getFiles().getRegions().getString("Regions." + region + ".Max");
                    String min = plugin.getFiles().getRegions().getString("Regions." + region + ".Min");

                    if (min == null || max == null)
                        continue;

                    Location locA = Utils.stringToLocation(max);
                    Location locB = Utils.stringToLocation(min);

                    CuboidRegion selection = new CuboidRegion(BukkitAdapter.asBlockVector(locA), BukkitAdapter.asBlockVector(locB));

                    if (selection.contains(BukkitAdapter.asBlockVector(block.getLocation()))) {
                        isInRegion = true;
                        break;
                    }
                }
            }

            if (blocks.contains(blockName)) {

                int expToDrop = event.getExpToDrop();

                if (isInRegion) {
                    if ((plugin.getGetters().toolRequired(blockName) != null) && (!toolCheck(plugin.getGetters().toolRequired(blockName), player))) {
                        event.setCancelled(true);
                        return;
                    }

                    if ((plugin.getGetters().enchantRequired(blockName) != null) && (!enchantCheck(plugin.getGetters().enchantRequired(blockName), player))) {
                        event.setCancelled(true);
                        return;
                    }

                    if (plugin.getGetters().jobsCheck(blockName) != null) {
                        if (!jobsCheck(plugin.getGetters().jobsCheck(blockName), player)) {
                            event.setCancelled(true);
                            return;
                        }
                    }

                    event.setDropItems(false);
                    event.setExpToDrop(0);
                    this.blockBreak(player, block, blockName, bworld, expToDrop);
                } else {
                    if (!useRegions) {
                        if ((plugin.getGetters().toolRequired(blockName) != null) && (!toolCheck(plugin.getGetters().toolRequired(blockName), player))) {
                            event.setCancelled(true);
                            return;
                        }

                        if ((plugin.getGetters().enchantRequired(blockName) != null) && (!enchantCheck(plugin.getGetters().enchantRequired(blockName), player))) {
                            event.setCancelled(true);
                            return;
                        }

                        if (plugin.getGetters().jobsCheck(blockName) != null) {
                            if (!jobsCheck(plugin.getGetters().jobsCheck(blockName), player)) {
                                event.setCancelled(true);
                                return;
                            }
                        }
                        event.setDropItems(false);
                        event.setExpToDrop(0);
                        this.blockBreak(player, block, blockName, bworld, expToDrop);
                    }
                }
            } else {
                if ((isInRegion && disableBreakRegions) || disableBreak) event.setCancelled(true);
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
        player.sendMessage(this.plugin.getMessages().toolRequired.replace("%tool%", string.toLowerCase().replace("_", " ")));
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
        player.sendMessage(this.plugin.getMessages().enchantRequired.replace("%enchant%", string.toLowerCase().replace("_", " ")));
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
            player.sendMessage(plugin.getMessages().jobsError.replace("%job%", jobCheckString[0]).replace("%level%", jobCheckString[1]));
            return false;
        } else if (level < Integer.valueOf(jobCheckString[1])) {
            player.sendMessage(plugin.getMessages().jobsError.replace("%job%", jobCheckString[0]).replace("%level%", jobCheckString[1]));
            return false;
        } else {
            return true;
        }
    }

    private void blockBreak(Player player, Block block, String blockname, World bworld, Integer exptodrop) {
        Getters getters = plugin.getGetters();
        BlockState state = block.getState();
        Location loc = block.getLocation();

        // Events ---------------------------------------------------------------------------------------------
        boolean doubleDrops = false;
        boolean doubleExp = false;
        ItemStack eventItem = null;
        boolean dropEventItem = false;
        int rarity = 0;

        if (getters.eventName(blockname) != null && Utils.events.containsKey(blockname))
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
            if ((plugin.getRandom().nextInt((rarity - 1) + 1) + 1) == 1) {
                if (dropEventItem) {
                    bworld.dropItemNaturally(loc, eventItem);
                } else {
                    player.getInventory().addItem(eventItem);
                }
            }
        }

        // Vault money -----------------------------------------------------------------------------------------
        if (plugin.getEconomy() != null && getters.money(blockname) != null && getters.money(blockname) > 0)
            plugin.getEconomy().depositPlayer(player, getters.money(blockname));

        // Commands execution -----------------------------------------------------------------------------------
        if (getters.consoleCommands(blockname) != null)
            getters.consoleCommands(blockname).forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), Utils.parse(command, player)));

        if (getters.playerCommands(blockname) != null)
            getters.playerCommands(blockname).forEach(command -> Bukkit.dispatchCommand(player, Utils.parse(command, player)));

        // Particles -------------------------------------------------------------------------------------------
        if (getters.particleCheck(blockname) != null)
            plugin.getParticles().run(getters.particleCheck(blockname).toLowerCase(), block);

        // Data Recovery ---------------------------------------------------------------------------------------
        FileConfiguration data = plugin.getFiles().getData();

        if (getters.dataRecovery()) {
            List<String> dataLocs = new ArrayList<>();

            if (data.contains(blockname))
                dataLocs = data.getStringList(blockname);

            dataLocs.add(Utils.locationToString(loc));
            data.set(blockname, dataLocs);
            plugin.getFiles().saveData();
        } else
            Utils.persist.put(loc, block.getType());

        // Replacing the block ---------------------------------------------------------------------------------
        new BukkitRunnable() {
            @Override
            public void run() {
                block.setType(getters.replaceBlock(blockname));
            }
        }.runTaskLater(plugin, 2L);

        Utils.regenBlocks.add(loc);

        // Actual Regeneration -------------------------------------------------------------------------------------
        int regendelay = 3;

        if (getters.replaceDelay(blockname) != null)
            regendelay = getters.replaceDelay(blockname);

        BukkitTask task = new BukkitRunnable() {
            public void run() {
                state.update(true);
                Utils.persist.remove(loc);
                Utils.regenBlocks.remove(loc);
                Utils.tasks.remove(loc);

                if (data != null && data.contains(blockname)) {
                    List<String> dataLocs = data.getStringList(blockname);

                    if (!dataLocs.isEmpty()) {
                        dataLocs.remove(Utils.locationToString(loc));
                        data.set(blockname, dataLocs);
                        plugin.getFiles().saveData();
                    }
                }
            }
        }.runTaskLater(plugin, regendelay * 20);

        Utils.tasks.put(loc, task);
    }
}