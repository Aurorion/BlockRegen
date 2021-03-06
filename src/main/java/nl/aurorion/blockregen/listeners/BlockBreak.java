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
import nl.aurorion.blockregen.api.BlockRegenBlockBreakEvent;
import nl.aurorion.blockregen.system.preset.struct.BlockPreset;
import nl.aurorion.blockregen.system.preset.struct.ExperienceDrop;
import nl.aurorion.blockregen.system.preset.struct.ItemDrop;
import nl.aurorion.blockregen.system.regeneration.struct.RegenerationProcess;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

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
        BlockPreset preset = plugin.getPresetManager().getPresetByTarget(blockName).orElse(null);

        if (event.isCancelled()) {
            return;
        }

        // Check bypass
        if (Utils.bypass.contains(player.getName())) {
            return;
        }

        // Check if the block is regenerating already
        if (plugin.getRegenerationManager().isRegenerating(block.getLocation())) {
            plugin.getConsoleOutput().debug("Block is regenerating...");
            event.setCancelled(true);
            return;
        }

        // Block data check
        if (Utils.dataCheck.contains(player.getName())) {
            event.setCancelled(true);
            return;
        }

        FileConfiguration settings = plugin.getFiles().getSettings().getFileConfiguration();

        // Towny
        if (plugin.getConfig().getBoolean("Towny-Support", true) && plugin.getServer().getPluginManager().getPlugin("Towny") != null) {
            if (TownyAPI.getInstance().getTownBlock(block.getLocation()) != null)
                if (TownyAPI.getInstance().getTownBlock(block.getLocation()).hasTown())
                    return;
        }

        // Grief Prevention
        if (plugin.getConfig().getBoolean("GriefPrevention-Support", true) && plugin.getGriefPrevention() != null) {
            String noBuildReason = plugin.getGriefPrevention().allowBreak(player, block, block.getLocation(), event);
            if (noBuildReason != null) return;
        }

        // WorldGuard
        if (plugin.getConfig().getBoolean("WorldGuard-Support", true) && plugin.getWorldGuardProvider() != null) {
            if (!plugin.getWorldGuardProvider().canBreak(player, block.getLocation())) return;
        }

        // Residence
        if (plugin.getConfig().getBoolean("Residence-Support", true) && plugin.getResidence() != null) {
            ClaimedResidence residence = ResidenceApi.getResidenceManager().getByLoc(block.getLocation());

            if (residence != null) {
                ResidencePermissions permissions = residence.getPermissions();
                if (!permissions.playerHas(player, Flags.build, true)) return;
            }
        }

        World world = block.getWorld();

        boolean isInWorld = settings.getStringList("Worlds-Enabled").contains(world.getName());

        boolean useRegions = plugin.getConfig().getBoolean("Use-Regions", false);

        if (useRegions) {

            // Regions
            // TODO: Cache regions / move to a manager

            if (plugin.getWorldEditProvider() == null) return;

            boolean isInRegion = false;
            String regionName = null;

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
                    regionName = region;
                    break;
                }
            }

            if (isInRegion) {
                if (preset != null) {
                    process(plugin.getRegenerationManager().createProcess(block, preset, regionName), event);
                } else {
                    if (plugin.getConfig().getBoolean("Disable-Other-Break-Region"))
                        event.setCancelled(true);
                }
            }
        } else {
            // Worlds
            if (isInWorld) {
                if (preset != null) {
                    process(plugin.getRegenerationManager().createProcess(block, preset), event);
                } else {
                    if (plugin.getConfig().getBoolean("Disable-Other-Break", false))
                        event.setCancelled(true);
                }
            }
        }
    }

    private void process(RegenerationProcess process, BlockBreakEvent event) {

        int expToDrop = event.getExpToDrop();

        // TODO: Might want to get rid of this part later on, useless
        event.setDropItems(false);
        event.setExpToDrop(0);

        Player player = event.getPlayer();

        Block block = event.getBlock();
        Location location = block.getLocation();
        World world = block.getWorld();

        String blockName = block.getType().name();
        BlockPreset preset = plugin.getPresetManager().getPresetByTarget(blockName).orElse(null);

        // Check permissions and conditions
        if (!player.hasPermission("blockregen.block." + blockName) && !player.hasPermission("blockregen.block.*") && !player.isOp()) {
            player.sendMessage(Message.PERMISSION_BLOCK_ERROR.get(event.getPlayer()));
            event.setCancelled(true);
            plugin.getRegenerationManager().removeProcess(process);
            return;
        }

        if (!preset.getConditions().check(player)) {
            event.setCancelled(true);
            plugin.getRegenerationManager().removeProcess(process);
            return;
        }

        // Event API
        BlockRegenBlockBreakEvent blockRegenBlockBreakEvent = new BlockRegenBlockBreakEvent(event, preset);
        Bukkit.getServer().getPluginManager().callEvent(blockRegenBlockBreakEvent);

        if (blockRegenBlockBreakEvent.isCancelled())
            return;

        // Start regeneration

        process.start();

        // Rewards...

        List<ItemStack> drops = new ArrayList<>();

        // Events ---------------------------------------------------------------------------------------------

        boolean doubleDrops = false;
        boolean doubleExp = false;

        if (preset.getEvent() != null && Utils.events.containsKey(preset.getEvent().getName())) {
            if (Utils.events.get(preset.getEvent().getName())) {

                doubleDrops = preset.getEvent().isDoubleDrops();
                doubleExp = preset.getEvent().isDoubleExperience();

                if (preset.getEvent().getItem() != null) {
                    ItemStack eventDrop = preset.getEvent().getItem().toItemStack(player);

                    // Event item
                    if (eventDrop != null && (plugin.getRandom().nextInt((preset.getEvent().getItemRarity().getInt() - 1) + 1) + 1) == 1) {
                        drops.add(eventDrop);
                    }
                }
            }
        }

        // Drop Section-----------------------------------------------------------------------------------------
        if (preset.isNaturalBreak()) {

            for (ItemStack drop : block.getDrops(player.getInventory().getItemInMainHand())) {
                Material mat = drop.getType();
                int amount = drop.getAmount();

                if (doubleDrops)
                    amount *= 2;

                ItemStack dropItem = new ItemStack(mat, amount);

                BlockRegen.getInstance().consoleOutput.debug("Dropping item " + dropItem.getType().toString() + "x" + dropItem.getAmount(), player);
                drops.add(dropItem);
            }

            // TODO: Get rid of exp section, add drop-exp-naturally to the main Preset section -- simplifies some stuff
            if (expToDrop > 0) {
                if (doubleExp) expToDrop *= 2;
                world.spawn(location, ExperienceOrb.class).setExperience(expToDrop);
            }
        } else {
            for (ItemDrop drop : preset.getRewards().getDrops()) {
                ItemStack itemStack = drop.toItemStack(player);

                if (itemStack == null) continue;

                if (preset.isApplyFortune())
                    itemStack.setAmount(Utils.applyFortune(block.getType(), player.getInventory().getItemInMainHand()) + itemStack.getAmount());

                if (doubleDrops)
                    itemStack.setAmount(itemStack.getAmount() * 2);

                drops.add(itemStack);
                BlockRegen.getInstance().consoleOutput.debug("Dropping item " + itemStack.getType().toString() + "x" + itemStack.getAmount(), player);

                if (drop.getExperienceDrop() == null) continue;

                ExperienceDrop experienceDrop = drop.getExperienceDrop();

                int expAmount = experienceDrop.getAmount().getInt();

                if (expAmount <= 0) continue;

                if (doubleExp) expAmount *= 2;

                plugin.getConsoleOutput().debug("Exp: " + expAmount, player);

                if (experienceDrop.isDropNaturally())
                    world.spawn(location, ExperienceOrb.class).setExperience(expAmount);
                else player.giveExp(expAmount);
            }
        }

        for (ItemStack drop : drops) {
            if (preset.isDropNaturally())
                world.dropItemNaturally(location, drop);
            else player.getInventory().addItem(drop);
        }

        // Trigger Jobs Break if enabled -----------------------------------------------------------------------
        if (plugin.getConfig().getBoolean("Jobs-Rewards", false) && plugin.getJobsProvider() != null) {
            plugin.getJobsProvider().triggerBlockBreakAction(player, block);
        }

        // Rewards ---------------------------------------------------------------------------------------------
        preset.getRewards().give(player);

        // Particles -------------------------------------------------------------------------------------------
        if (preset.getParticle() != null)
            plugin.getParticleManager().displayParticle(preset.getParticle(), block);
    }
}