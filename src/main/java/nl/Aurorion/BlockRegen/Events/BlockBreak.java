package nl.Aurorion.BlockRegen.Events;

import com.gamingmesh.jobs.Jobs;
import com.gamingmesh.jobs.container.JobProgression;
import com.palmergames.bukkit.towny.TownyAPI;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.regions.CuboidRegion;
import nl.Aurorion.BlockRegen.Main;
import nl.Aurorion.BlockRegen.Message;
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
        if (Utils.dataCheck.contains(player.getName())) {
            event.setCancelled(true);
            player.sendMessage(Message.DATA_CHECK.get().replace("%block%", blockName));
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

            boolean useRegions = plugin.getGetters().useRegions();

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
                if ((isInRegion && Main.getInstance().getGetters().disableOtherBreakRegion()) ||
                        Main.getInstance().getGetters().disableOtherBreak())

                    event.setCancelled(true);
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
        player.sendMessage(Message.TOOL_REQUIRED_ERROR.get().replace("%tool%", string.toLowerCase().replace("_", " ")));
        return false;
    }

    private boolean enchantCheck(String string, Player player) {
        String[] enchants = string.split(", ");
        boolean check = false;
        for (String all : enchants) {
            Enchantment enchant = Enchantment.getByKey(NamespacedKey.minecraft(all.toLowerCase()));
            if (enchant == null)
                continue;

            ItemMeta meta = player.getInventory().getItemInMainHand().getItemMeta();
            if (meta != null) {
                if (meta.hasEnchant(enchant)) {
                    check = true;
                    break;
                }
            } else check = true;
        }

        if (check)
            return true;

        player.sendMessage(Message.ENCHANT_REQUIRED_ERROR.get().replace("%enchant%", string.toLowerCase().replace("_", " ")));
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
            player.sendMessage(Message.JOBS_REQUIRED_ERROR.get().replace("%job%", jobCheckString[0]).replace("%level%", jobCheckString[1]));
            return false;
        } else if (level < Integer.parseInt(jobCheckString[1])) {
            player.sendMessage(Message.JOBS_REQUIRED_ERROR.get().replace("%job%", jobCheckString[0]).replace("%level%", jobCheckString[1]));
            return false;
        } else {
            return true;
        }
    }

    private void blockBreak(Player player, Block block, String blockName, World world, int expToDrop) {
        Getters getters = plugin.getGetters();
        BlockState state = block.getState();
        Location location = block.getLocation();

        // Events ---------------------------------------------------------------------------------------------
        boolean doubleDrops = false;
        boolean doubleExp = false;
        ItemStack eventItem = null;
        boolean dropEventItem = false;
        int rarity = 0;

        if (getters.eventName(blockName) != null && Utils.events.containsKey(getters.eventName(blockName)))
            if (Utils.events.get(getters.eventName(blockName))) {

                doubleDrops = getters.eventDoubleDrops(blockName);
                doubleExp = getters.eventDoubleExp(blockName);

                if (getters.eventItemMaterial(blockName) != null) {
                    eventItem = new ItemStack(getters.eventItemMaterial(blockName), 1);
                    ItemMeta meta = eventItem.getItemMeta();

                    if (meta != null) {
                        if (getters.eventItemName(blockName) != null)
                            meta.setDisplayName(getters.eventItemName(blockName));

                        if (!getters.eventItemLores(blockName).isEmpty())
                            meta.setLore(getters.eventItemLores(blockName));
                    }

                    eventItem.setItemMeta(meta);
                    dropEventItem = getters.eventItemDropNaturally(blockName);
                    rarity = getters.eventItemRarity(blockName);
                }
            }

        // Drop Section-----------------------------------------------------------------------------------------
        Material dropMaterial = getters.dropItemMaterial(blockName);

        if (getters.naturalBreak(blockName)) {
            for (ItemStack drops : block.getDrops()) {
                Material mat = drops.getType();
                int amount;
                if (doubleDrops) {
                    amount = drops.getAmount() * 2;
                } else {
                    amount = drops.getAmount();
                }
                ItemStack dropStack = new ItemStack(mat, amount);
                world.dropItemNaturally(block.getLocation(), dropStack);
            }
            if (expToDrop > 0) {
                if (doubleExp) {
                    world.spawn(location, ExperienceOrb.class).setExperience(expToDrop * 2);
                } else {
                    world.spawn(location, ExperienceOrb.class).setExperience(expToDrop);
                }
            }
        } else if (dropMaterial != null) {
            if (dropMaterial != Material.AIR) {
                int itemAmount = getters.dropItemAmount(blockName, player);

                if (doubleDrops) {
                    itemAmount = itemAmount * 2;
                }

                ItemStack dropItem = new ItemStack(dropMaterial, itemAmount);
                ItemMeta dropMeta = dropItem.getItemMeta();

                if (dropMeta != null) {
                    if (getters.dropItemName(blockName) != null) {
                        dropMeta.setDisplayName(getters.dropItemName(blockName));
                    }

                    if (!getters.dropItemLores(blockName).isEmpty()) {
                        dropMeta.setLore(getters.dropItemLores(blockName));
                    }

                    dropItem.setItemMeta(dropMeta);
                }

                if (itemAmount > 0)
                    if (getters.dropItemDropNaturally(blockName)) {
                        world.dropItemNaturally(location, dropItem);
                    } else {
                        player.getInventory().addItem(dropItem);
                        player.updateInventory();
                    }
            }

            int expAmount = getters.dropItemExpAmount(blockName);

            if (expAmount > 0) {
                if (doubleExp)
                    expAmount = expAmount * 2;

                if (getters.dropItemExpDrop(blockName)) {
                    world.spawn(location, ExperienceOrb.class).setExperience(expAmount);
                } else {
                    player.giveExp(expAmount);
                }
            }
        }

        if (eventItem != null) {
            if ((plugin.getRandom().nextInt((rarity - 1) + 1) + 1) == 1) {
                if (dropEventItem) {
                    world.dropItemNaturally(location, eventItem);
                } else {
                    player.getInventory().addItem(eventItem);
                }
            }
        }

        // Vault money -----------------------------------------------------------------------------------------
        if (plugin.getEconomy() != null) {
            int money = getters.money(blockName);
            if (money > 0)
                plugin.getEconomy().depositPlayer(player, money);
        }

        // Commands execution -----------------------------------------------------------------------------------
        if (getters.consoleCommands(blockName) != null)
            getters.consoleCommands(blockName).forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), Utils.parse(command, player)));

        if (getters.playerCommands(blockName) != null)
            getters.playerCommands(blockName).forEach(command -> Bukkit.dispatchCommand(player, Utils.parse(command, player)));

        // Particles -------------------------------------------------------------------------------------------
        if (getters.particleCheck(blockName) != null)
            plugin.getParticleUtil().run(getters.particleCheck(blockName).toLowerCase(), block);

        // Data Recovery ---------------------------------------------------------------------------------------
        FileConfiguration data = plugin.getFiles().getData();

        if (getters.dataRecovery()) {
            List<String> dataLocs = new ArrayList<>();

            if (data.contains(blockName))
                dataLocs = data.getStringList(blockName);

            dataLocs.add(Utils.locationToString(location));
            data.set(blockName, dataLocs);
            plugin.getFiles().saveData();
        } else
            Utils.persist.put(location, block.getType());

        // Replacing the block ---------------------------------------------------------------------------------
        new BukkitRunnable() {
            @Override
            public void run() {
                block.setType(getters.replaceBlock(blockName));
                Main.getInstance().cO.debug("Replaced block");
            }
        }.runTaskLater(plugin, 2L);

        Utils.regenBlocks.add(location);

        // Actual Regeneration -------------------------------------------------------------------------------------
        int regenDelay = getters.replaceDelay(blockName);
        regenDelay = regenDelay == 0 ? 1 : regenDelay;
        Main.getInstance().cO.debug("Regen Delay: " + regenDelay);

        BukkitTask task = new BukkitRunnable() {
            public void run() {
                regen(state, location, data, blockName);
            }
        }.runTaskLater(plugin, regenDelay * 20);

        Utils.tasks.put(location, task);
    }

    private void regen(BlockState state, Location location, FileConfiguration data, String blockName) {
        state.update(true);
        Main.getInstance().cO.debug("Regen");

        Utils.persist.remove(location);
        Utils.regenBlocks.remove(location);
        Utils.tasks.remove(location);

        if (data != null && data.contains(blockName)) {
            List<String> dataLocs = data.getStringList(blockName);

            if (!dataLocs.isEmpty()) {
                dataLocs.remove(Utils.locationToString(location));
                data.set(blockName, dataLocs);
                plugin.getFiles().saveData();
            }
        }
    }
}