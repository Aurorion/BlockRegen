package nl.aurorion.blockregen.listeners;

import com.bekvon.bukkit.residence.api.ResidenceApi;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.ResidencePermissions;
import com.palmergames.bukkit.towny.TownyAPI;
import nl.aurorion.blockregen.BlockRegen;
import nl.aurorion.blockregen.Message;
import nl.aurorion.blockregen.util.Utils;
import nl.aurorion.blockregen.api.BlockRegenBlockBreakEvent;
import nl.aurorion.blockregen.system.event.struct.PresetEvent;
import nl.aurorion.blockregen.system.preset.struct.BlockPreset;
import nl.aurorion.blockregen.system.preset.struct.drop.ExperienceDrop;
import nl.aurorion.blockregen.system.preset.struct.drop.ItemDrop;
import nl.aurorion.blockregen.system.regeneration.struct.RegenerationProcess;
import nl.aurorion.blockregen.system.region.struct.RegenerationRegion;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class BlockListener implements Listener {

    private final BlockRegen plugin;

    public BlockListener(BlockRegen plugin) {
        this.plugin = plugin;
    }

    private boolean hasBypass(Player player) {
        return Utils.bypass.contains(player.getUniqueId()) || (plugin.getConfig().getBoolean("Bypass-In-Creative", false) && player.getGameMode() == GameMode.CREATIVE);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBreak(BlockBreakEvent event) {

        Player player = event.getPlayer();
        Block block = event.getBlock();

        BlockPreset preset = plugin.getPresetManager().getPresetByBlock(block).orElse(null);

        if (event.isCancelled()) {
            return;
        }

        // Check if the block is regenerating already
        if (plugin.getRegenerationManager().isRegenerating(block.getLocation())) {

            // Remove the process
            if (hasBypass(player)) {
                plugin.getRegenerationManager().removeProcess(block.getLocation());
                return;
            }

            plugin.getConsoleOutput().debug("Block is regenerating...");
            event.setCancelled(true);
            return;
        }

        // Check bypass
        if (hasBypass(player)) {
            return;
        }

        // Block data check
        if (Utils.dataCheck.contains(player.getUniqueId())) {
            event.setCancelled(true);
            return;
        }

        // Towny
        if (plugin.getConfig().getBoolean("Towny-Support", true) && plugin.getServer().getPluginManager().getPlugin("Towny") != null) {
            if (TownyAPI.getInstance().getTownBlock(block.getLocation()) != null && TownyAPI.getInstance().getTownBlock(block.getLocation()).hasTown())
                return;
        }

        // Grief Prevention
        if (plugin.getConfig().getBoolean("GriefPrevention-Support", true) && plugin.getGriefPrevention() != null) {
            String noBuildReason = plugin.getGriefPrevention().allowBreak(player, block, block.getLocation(), event);
            if (noBuildReason != null) return;
        }

        // WorldGuard
        if (plugin.getConfig().getBoolean("WorldGuard-Support", true) && plugin.getVersionManager().getWorldGuardProvider() != null) {
            if (!plugin.getVersionManager().getWorldGuardProvider().canBreak(player, block.getLocation())) return;
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

        boolean isInWorld = plugin.getConfig().getStringList("Worlds-Enabled").contains(world.getName());

        boolean useRegions = plugin.getConfig().getBoolean("Use-Regions", false);

        if (useRegions) {

            if (plugin.getVersionManager().getWorldEditProvider() == null)
                return;

            RegenerationRegion region = plugin.getRegionManager().getRegion(block.getLocation());

            boolean isInRegion = region != null;

            if (isInRegion) {
                if (preset != null) {
                    process(plugin.getRegenerationManager().createProcess(block, preset, region.getName()), preset, event);
                } else {
                    if (plugin.getConfig().getBoolean("Disable-Other-Break-Region"))
                        event.setCancelled(true);
                }
            }
        } else {
            if (isInWorld) {
                if (preset != null) {
                    process(plugin.getRegenerationManager().createProcess(block, preset), preset, event);
                } else {
                    if (plugin.getConfig().getBoolean("Disable-Other-Break", false))
                        event.setCancelled(true);
                }
            }
        }
    }

    private void process(RegenerationProcess process, BlockPreset preset, BlockBreakEvent event) {

        final AtomicInteger expToDrop = new AtomicInteger(event.getExpToDrop());

        event.setDropItems(false);
        event.setExpToDrop(0);

        Player player = event.getPlayer();

        Block block = event.getBlock();
        Location location = block.getLocation();
        World world = block.getWorld();

        String blockName = block.getType().name();

        if (preset == null)
            return;

        // Check permissions and conditions
        if (!player.hasPermission("blockregen.block." + blockName) && !player.hasPermission("blockregen.block.*") && !player.isOp()) {
            Message.PERMISSION_BLOCK_ERROR.send(event.getPlayer());
            event.setCancelled(true);
            return;
        }

        if (!preset.getConditions().check(player)) {
            event.setCancelled(true);
            return;
        }

        // Event API
        BlockRegenBlockBreakEvent blockRegenBlockBreakEvent = new BlockRegenBlockBreakEvent(event, preset);
        Bukkit.getServer().getPluginManager().callEvent(blockRegenBlockBreakEvent);

        if (blockRegenBlockBreakEvent.isCancelled())
            return;

        List<ItemStack> vanillaDrops = new ArrayList<>(block.getDrops(player.getInventory().getItemInMainHand()));

        // Start regeneration
        process.start();

        // Run rewards async
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {

            List<ItemStack> drops = new ArrayList<>();

            // Events ---------------------------------------------------------------------------------------------

            boolean doubleDrops = false;
            boolean doubleExp = false;

            PresetEvent presetEvent = plugin.getEventManager().getEvent(preset.getName());

            if (presetEvent != null && presetEvent.isEnabled()) {
                doubleDrops = presetEvent.isDoubleDrops();
                doubleExp = presetEvent.isDoubleExperience();

                if (presetEvent.getItem() != null) {
                    ItemStack eventDrop = presetEvent.getItem().toItemStack(player);

                    if (eventDrop != null && (plugin.getRandom().nextInt((presetEvent.getItemRarity().getInt() - 1) + 1) + 1) == 1)
                        drops.add(eventDrop);
                }
            }

            // Drop Section -----------------------------------------------------------------------------------------
            if (preset.isNaturalBreak()) {

                for (ItemStack drop : vanillaDrops) {
                    Material mat = drop.getType();
                    int amount = drop.getAmount();

                    if (doubleDrops)
                        amount *= 2;

                    ItemStack dropItem = new ItemStack(mat, amount);

                    plugin.getConsoleOutput().debug("Dropping item " + dropItem.getType().toString() + "x" + dropItem.getAmount(), player);
                    drops.add(dropItem);
                }

                // TODO: Get rid of exp section, add drop-exp-naturally to the main Preset section -- simplifies some stuff
                if (expToDrop.get() > 0) {
                    if (doubleExp)
                        expToDrop.set(expToDrop.get() * 2);
                    Bukkit.getScheduler().runTask(plugin, () -> world.spawn(location, ExperienceOrb.class).setExperience(expToDrop.get()));
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
                    plugin.getConsoleOutput().debug("Dropping item " + itemStack.getType().toString() + "x" + itemStack.getAmount(), player);

                    if (drop.getExperienceDrop() == null) continue;

                    ExperienceDrop experienceDrop = drop.getExperienceDrop();

                    AtomicInteger expAmount = new AtomicInteger(experienceDrop.getAmount().getInt());

                    if (expAmount.get() <= 0) continue;

                    if (doubleExp)
                        expAmount.set(expAmount.get() * 2);

                    plugin.getConsoleOutput().debug("Exp: " + expAmount, player);

                    if (experienceDrop.isDropNaturally())
                        Bukkit.getScheduler().runTask(plugin, () -> world.spawn(location, ExperienceOrb.class).setExperience(expAmount.get()));
                    else player.giveExp(expAmount.get());
                }
            }

            if (presetEvent != null) {
                // Add items from presetEvent
                for (ItemDrop drop : presetEvent.getRewards().getDrops()) {
                    ItemStack item = drop.toItemStack(player);
                    if (item != null)
                        drops.add(item);
                }

                // Fire rewards
                presetEvent.getRewards().give(player);
            }

            for (ItemStack drop : drops) {
                if (preset.isDropNaturally()) {
                    Bukkit.getScheduler().runTask(plugin, () -> world.dropItemNaturally(location, drop));
                    plugin.getConsoleOutput().debug("Dropping item " + drop.getType().name() + "x" + drop.getAmount());
                } else {
                    player.getInventory().addItem(drop);
                    plugin.getConsoleOutput().debug("Adding item " + drop.getType().name() + "x" + drop.getAmount() + " to inventory.");
                }
            }

            // Trigger Jobs Break if enabled -----------------------------------------------------------------------
            if (plugin.getConfig().getBoolean("Jobs-Rewards", false) && plugin.getJobsProvider() != null)
                Bukkit.getScheduler().runTask(plugin, () -> plugin.getJobsProvider().triggerBlockBreakAction(player, block));

            // Rewards ---------------------------------------------------------------------------------------------
            preset.getRewards().give(player);

            // Block Break Sound ---------------------------------------------------------------------------------------------
            if (preset.getSound() != null)
                preset.getSound().play(block.getLocation());

            // Particles -------------------------------------------------------------------------------------------
            if (preset.getParticle() != null)
                Bukkit.getScheduler().runTask(plugin, () -> plugin.getParticleManager().displayParticle(preset.getParticle(), block));
        });
    }
}
