package nl.aurorion.blockregen.listeners;

import com.bekvon.bukkit.residence.api.ResidenceApi;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.ResidencePermissions;
import com.palmergames.bukkit.towny.TownyAPI;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.regions.CuboidRegion;
import nl.aurorion.blockregen.BlockRegen;
import nl.aurorion.blockregen.Message;
import nl.aurorion.blockregen.Utils;
import nl.aurorion.blockregen.system.Getters;
import nl.aurorion.blockregen.system.preset.BlockPreset;
import nl.aurorion.blockregen.system.preset.ExperienceDrop;
import nl.aurorion.blockregen.system.preset.ItemDrop;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
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

public class BlockBreak implements Listener {

    private final BlockRegen plugin;

    public BlockBreak(BlockRegen plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBreak(BlockBreakEvent event) {

        Player player = event.getPlayer();
        Block block = event.getBlock();

        String blockName = block.getType().name().toUpperCase();
        BlockPreset preset = plugin.getPresetManager().getPreset(blockName);

        if (event.isCancelled()) {
            return;
        }

        // Check bypass
        if (Utils.bypass.contains(player.getName())) {
            return;
        }

        // Check if the block is regenerating already
        if (Utils.regenBlocks.contains(block.getLocation())) {
            event.setCancelled(true);
            plugin.getConsoleOutput().debug("Cancelled.");
            return;
        }

        // Block data check
        if (Utils.dataCheck.contains(player.getName())) {
            // Make sure the event gets cancelled.
            event.setCancelled(true);
            return;
        }

        FileConfiguration settings = plugin.getFiles().getSettings().getFileConfiguration();

        // Towny
        if (plugin.getGetters().useTowny() && plugin.getServer().getPluginManager().getPlugin("Towny") != null) {
            if (TownyAPI.getInstance().getTownBlock(block.getLocation()) != null)
                if (TownyAPI.getInstance().getTownBlock(block.getLocation()).hasTown())
                    return;
        }

        // Grief Prevention
        if (plugin.getGetters().useGriefPrevention() && plugin.getGriefPrevention() != null) {
            String noBuildReason = plugin.getGriefPrevention().allowBreak(player, block, block.getLocation(), event);
            if (noBuildReason != null) return;
        }

        // WorldGuard
        if (plugin.getGetters().useWorldGuard() && plugin.getWorldGuardProvider() != null) {
            if (!plugin.getWorldGuardProvider().canBreak(player, block.getLocation())) return;
        }

        // Residence
        if (plugin.getGetters().useResidence() && plugin.getResidence() != null) {
            ClaimedResidence residence = ResidenceApi.getResidenceManager().getByLoc(block.getLocation());

            if (residence != null) {
                ResidencePermissions permissions = residence.getPermissions();
                if (!permissions.playerHas(player, Flags.build, true)) return;
            }
        }

        World world = block.getWorld();

        boolean isInWorld = settings.getStringList("Worlds-Enabled").contains(world.getName());

        boolean useRegions = plugin.getGetters().useRegions();

        if (useRegions) {

            // Regions
            // TODO: Cache regions / move to a manager

            if (plugin.getWorldEditProvider() == null) return;

            boolean isInRegion = false;

            ConfigurationSection regionSection = plugin.getFiles().getRegions().getFileConfiguration().getConfigurationSection("Regions");

            List<String> regions = regionSection == null ? new ArrayList<>() : new ArrayList<>(regionSection.getKeys(false));

            for (String region : regions) {
                String max = plugin.getFiles().getRegions().getFileConfiguration().getString("Regions." + region + ".Max");
                String min = plugin.getFiles().getRegions().getFileConfiguration().getString("Regions." + region + ".Min");

                if (min == null || max == null)
                    continue;

                Location locA = Utils.stringToLocation(max);
                Location locB = Utils.stringToLocation(min);

                if (locA.getWorld() == null || !locA.getWorld().equals(world))
                    continue;

                CuboidRegion selection = new CuboidRegion(BukkitAdapter.asBlockVector(locA), BukkitAdapter.asBlockVector(locB));

                if (selection.contains(BukkitAdapter.asBlockVector(block.getLocation()))) {
                    isInRegion = true;
                    break;
                }
            }

            if (isInRegion) {
                if (preset != null) {
                    process(event);
                } else {
                    if (BlockRegen.getInstance().getGetters().disableOtherBreakRegion())
                        event.setCancelled(true);
                }
            }
        } else {
            // Worlds
            if (isInWorld) {
                if (preset != null) {
                    process(event);
                } else {
                    if (BlockRegen.getInstance().getGetters().disableOtherBreak())
                        event.setCancelled(true);
                }
            }
        }
    }

    private void process(BlockBreakEvent event) {

        int expToDrop = event.getExpToDrop();

        // TODO: Might want to get rid of this part later on, useless
        event.setDropItems(false);
        event.setExpToDrop(0);

        Getters getters = plugin.getGetters();

        Player player = event.getPlayer();

        Block block = event.getBlock();
        BlockState state = block.getState();
        Location location = block.getLocation();
        World world = block.getWorld();

        String blockName = block.getType().name();
        BlockPreset preset = plugin.getPresetManager().getPreset(blockName);

        // Check permissions and conditions
        if (!player.hasPermission("blockregen.block." + blockName) && !player.hasPermission("blockregen.block.*") && !player.isOp()) {
            player.sendMessage(Message.PERMISSION_BLOCK_ERROR.get());
            event.setCancelled(true);
            return;
        }

        if (!preset.getConditions().check(player)) {
            event.setCancelled(true);
            return;
        }

        List<ItemStack> drops = new ArrayList<>();

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
                    int amount = getters.eventItemAmount(blockName, player);

                    if (amount > 0) {
                        eventItem = new ItemStack(getters.eventItemMaterial(blockName), amount);
                        ItemMeta meta = eventItem.getItemMeta();

                        if (meta != null) {
                            if (getters.eventItemName(blockName, player) != null)
                                meta.setDisplayName(getters.eventItemName(blockName, player));

                            if (!getters.eventItemLore(blockName, player).isEmpty())
                                meta.setLore(getters.eventItemLore(blockName, player));
                        }

                        eventItem.setItemMeta(meta);
                        dropEventItem = getters.eventItemDropNaturally(blockName);
                        rarity = getters.eventItemRarity(blockName);
                    }
                }
            }

        // Drop Section-----------------------------------------------------------------------------------------
        if (preset.isNaturalBreak()) {
            plugin.getConsoleOutput().debug("Natural drops: " + block.getDrops(player.getInventory().getItemInMainHand()).size());

            for (ItemStack drop : block.getDrops(player.getInventory().getItemInMainHand())) {
                Material mat = drop.getType();
                int amount = drop.getAmount();

                if (doubleDrops)
                    amount *= 2;

                ItemStack dropItem = new ItemStack(mat, amount);

                BlockRegen.getInstance().consoleOutput.debug("Dropping item " + dropItem.getType().toString() + "x" + dropItem.getAmount());
                drops.add(dropItem);
            }

            // TODO: Get rid of exp section, add drop-exp-naturally to the main Preset section -- simplifies some stuff
            if (expToDrop > 0) {
                if (doubleExp) expToDrop *= 2;
                world.spawn(location, ExperienceOrb.class).setExperience(expToDrop);
            }
        } else {
            for (ItemDrop drop : preset.getRewards().getDrops()) {
                ItemStack itemStack = drop.toItemStack();

                if (itemStack == null) continue;

                if (preset.isApplyFortune())
                    itemStack.setAmount(Utils.applyFortune(block.getType(), player.getInventory().getItemInMainHand()) + itemStack.getAmount());

                if (doubleDrops)
                    itemStack.setAmount(itemStack.getAmount() * 2);

                drops.add(itemStack);
                BlockRegen.getInstance().consoleOutput.debug("Dropping item " + itemStack.getType().toString() + "x" + itemStack.getAmount());

                if (drop.getExperienceDrop() == null) continue;

                ExperienceDrop experienceDrop = drop.getExperienceDrop();

                int expAmount = experienceDrop.getAmount().getInt();

                if (expAmount <= 0) continue;

                if (doubleExp) expAmount *= 2;

                if (experienceDrop.isDropNaturally())
                    world.spawn(location, ExperienceOrb.class).setExperience(expAmount);
                else player.giveExp(expAmount);
            }
        }

        // Event item
        if (eventItem != null &&
                dropEventItem &&
                (plugin.getRandom().nextInt((rarity - 1) + 1) + 1) == 1) {
            drops.add(eventItem);
        }

        for (ItemStack drop : drops) {
            if (preset.isDropNaturally())
                world.dropItemNaturally(location, drop);
            else player.getInventory().addItem(drop);
        }

        // Trigger Jobs Break if enabled -----------------------------------------------------------------------
        if (plugin.getGetters().useJobsRewards() && plugin.getJobsProvider() != null) {
            plugin.getJobsProvider().triggerBlockBreakAction(player, block);
        }

        // Rewards ---------------------------------------------------------------------------------------------
        preset.getRewards().give(player);

        // Particles -------------------------------------------------------------------------------------------
        if (preset.getParticle() != null)
            plugin.getParticleManager().displayParticle(preset.getParticle(), block);

        // Data Recovery ---------------------------------------------------------------------------------------
        // TODO: Get this sh*t out of here
        FileConfiguration data = plugin.getFiles().getData().getFileConfiguration();

        if (getters.dataRecovery()) {
            List<String> dataLocs = new ArrayList<>();

            if (data.contains(blockName))
                dataLocs = data.getStringList(blockName);

            dataLocs.add(Utils.locationToString(location));
            data.set(blockName, dataLocs);
            plugin.getFiles().getData().save();
        } else
            Utils.persist.put(location, block.getType());

        // Replacing the block ---------------------------------------------------------------------------------
        Material replaceMaterial = preset.getReplaceMaterial().get();

        new BukkitRunnable() {
            @Override
            public void run() {
                block.setType(replaceMaterial);
                BlockRegen.getInstance().consoleOutput.debug("Replaced block with " + replaceMaterial.toString());
            }
        }.runTaskLater(plugin, 2L);

        Utils.regenBlocks.add(location);

        // Actual Regeneration -------------------------------------------------------------------------------------

        int regenDelay = preset.getDelay().getInt();
        regenDelay = Math.max(1, regenDelay);
        plugin.getConsoleOutput().debug("Regen Delay: " + regenDelay);

        Material regenerateInto = preset.getRegenMaterial().get();

        if (regenerateInto != state.getType()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    state.setType(regenerateInto);
                    plugin.getConsoleOutput().debug("Regenerate into: " + regenerateInto.toString());
                }
            }.runTaskLater(plugin, 2L);
        }

        BukkitTask task = new BukkitRunnable() {
            public void run() {
                regenerate(state, location, data, blockName);
            }
        }.runTaskLater(plugin, regenDelay * 20);

        Utils.tasks.put(location, task);
    }

    private void regenerate(BlockState state, Location location, FileConfiguration data, String blockName) {
        state.update(true);
        BlockRegen.getInstance().consoleOutput.debug("Regenerated block " + blockName);

        Utils.persist.remove(location);
        Utils.regenBlocks.remove(location);
        Utils.tasks.remove(location);

        if (data != null && data.contains(blockName)) {
            List<String> dataLocs = data.getStringList(blockName);

            if (!dataLocs.isEmpty()) {
                dataLocs.remove(Utils.locationToString(location));
                data.set(blockName, dataLocs);
                plugin.getFiles().getData().save();
            }
        }
    }
}