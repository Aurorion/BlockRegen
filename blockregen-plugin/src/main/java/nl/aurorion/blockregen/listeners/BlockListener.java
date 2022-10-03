package nl.aurorion.blockregen.listeners;

import com.bekvon.bukkit.residence.api.ResidenceApi;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.ResidencePermissions;
import com.cryptomorin.xseries.XMaterial;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.TownBlock;
import lombok.extern.java.Log;
import nl.aurorion.blockregen.BlockRegen;
import nl.aurorion.blockregen.Message;
import nl.aurorion.blockregen.api.BlockRegenBlockBreakEvent;
import nl.aurorion.blockregen.system.event.struct.PresetEvent;
import nl.aurorion.blockregen.system.preset.struct.BlockPreset;
import nl.aurorion.blockregen.system.preset.struct.drop.ExperienceDrop;
import nl.aurorion.blockregen.system.preset.struct.drop.ItemDrop;
import nl.aurorion.blockregen.system.regeneration.struct.RegenerationProcess;
import nl.aurorion.blockregen.system.region.struct.RegenerationRegion;
import nl.aurorion.blockregen.util.ItemUtil;
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

@Log
public class BlockListener implements Listener {

    private final BlockRegen plugin;

    public BlockListener(BlockRegen plugin) {
        this.plugin = plugin;
    }

    private boolean hasBypass(Player player) {
        return plugin.getRegenerationManager().hasBypass(player)
                || (plugin.getConfig().getBoolean("Bypass-In-Creative", false)
                && player.getGameMode() == GameMode.CREATIVE);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBreak(BlockBreakEvent event) {

        // Respect cancels on higher priorities
        if (event.isCancelled()) {
            log.fine("Event already cancelled.");
            return;
        }

        Player player = event.getPlayer();
        Block block = event.getBlock();

        boolean useRegions = plugin.getConfig().getBoolean("Use-Regions", false);

        BlockPreset preset;

        RegenerationRegion region = plugin.getRegionManager().getRegion(block.getLocation());

        if (useRegions && region != null) {
            preset = plugin.getPresetManager().getPreset(block, region);
        } else {
            preset = plugin.getPresetManager().getPreset(block);
        }

        RegenerationProcess process = plugin.getRegenerationManager().getProcess(block);

        // Check if the block is regenerating already
        if (process != null) {

            // Remove the process
            if (hasBypass(player)) {
                plugin.getRegenerationManager().removeProcess(process);
                log.fine("Removed process in bypass.");
                return;
            }

            if (process.getRegenerationTime() > System.currentTimeMillis()) {
                log.fine(String.format("Block is regenerating. Process: %s", process));
                event.setCancelled(true);
                return;
            }
        }

        // Check bypass
        if (hasBypass(player)) {
            log.fine("Player has bypass.");
            return;
        }

        // Block data check
        if (plugin.getRegenerationManager().hasDataCheck(player)) {
            event.setCancelled(true);
            log.fine("Player has block check.");
            return;
        }

        // Towny
        if (plugin.getConfig().getBoolean("Towny-Support", true)
                && plugin.getServer().getPluginManager().getPlugin("Towny") != null) {

            TownBlock townBlock = TownyAPI.getInstance().getTownBlock(block.getLocation());

            if (townBlock != null && townBlock.hasTown()) {
                log.fine("Let Towny handle this.");
                return;
            }
        }

        // Grief Prevention
        if (plugin.getConfig().getBoolean("GriefPrevention-Support", true) && plugin.getGriefPrevention() != null) {
            String noBuildReason = plugin.getGriefPrevention().allowBreak(player, block, block.getLocation(), event);

            if (noBuildReason != null) {
                log.fine("Let GriefPrevention handle this.");
                return;
            }
        }

        // WorldGuard
        if (plugin.getConfig().getBoolean("WorldGuard-Support", true)
                && plugin.getVersionManager().getWorldGuardProvider() != null) {

            if (!plugin.getVersionManager().getWorldGuardProvider().canBreak(player, block.getLocation())) {
                log.fine("Let WorldGuard handle this.");
                return;
            }
        }

        // Residence
        if (plugin.getConfig().getBoolean("Residence-Support", true) && plugin.getResidence() != null) {
            ClaimedResidence residence = ResidenceApi.getResidenceManager().getByLoc(block.getLocation());

            if (residence != null) {
                ResidencePermissions permissions = residence.getPermissions();

                if (!permissions.playerHas(player, Flags.build, true)) {
                    log.fine("Let Residence handle this.");
                    return;
                }
            }
        }

        World world = block.getWorld();

        boolean isInWorld = plugin.getConfig().getStringList("Worlds-Enabled").contains(world.getName());

        if (useRegions) {
            if (region != null) {
                if (preset != null && region.hasPreset(preset.getName())) {
                    process(plugin.getRegenerationManager().createProcess(block, preset, region.getName()), preset,
                            event);
                } else {
                    if (preset != null && !region.hasPreset(preset.getName())) {
                        log.fine(String.format("Region %s doesn't have preset %s added.", region.getName(), preset.getName()));
                    }

                    if (plugin.getConfig().getBoolean("Disable-Other-Break-Region")) {
                        event.setCancelled(true);
                        log.fine("Not a valid preset. Denied BlockBreak.");
                        return;
                    }
                    log.fine("Not a valid preset.");
                }
            } else {
                log.fine("Not in region.");
            }
        } else {
            if (isInWorld) {
                if (preset != null) {
                    process(plugin.getRegenerationManager().createProcess(block, preset), preset, event);
                } else {
                    if (plugin.getConfig().getBoolean("Disable-Other-Break", false)) {
                        event.setCancelled(true);
                        log.fine("Not a valid preset. Denied BlockBreak.");
                        return;
                    }
                    log.fine("Not a valid preset.");
                }
            } else {
                log.fine(String.format("Not in world. World: %s, enabled: %s", world.getName(),
                        plugin.getConfig().getStringList("Worlds-Enabled")));
            }
        }
    }

    private void process(RegenerationProcess process, BlockPreset preset, BlockBreakEvent event) {
        Player player = event.getPlayer();

        Block block = event.getBlock();
        String blockName = block.getType().name();

        // Check permissions
        if (!player.hasPermission("blockregen.block." + blockName) &&
                !player.hasPermission("blockregen.block.*") &&
                !player.isOp()) {
            Message.PERMISSION_BLOCK_ERROR.send(event.getPlayer());
            event.setCancelled(true);
            log.fine("Player doesn't have permissions.");
            return;
        }

        // Check conditions
        if (!preset.getConditions().check(player)) {
            event.setCancelled(true);
            log.info("Player doesn't meet conditions.");
            return;
        }

        // Event API
        BlockRegenBlockBreakEvent blockRegenBlockBreakEvent = new BlockRegenBlockBreakEvent(event, preset);
        Bukkit.getServer().getPluginManager().callEvent(blockRegenBlockBreakEvent);

        if (blockRegenBlockBreakEvent.isCancelled()) {
            log.fine("BlockRegenBreakEvent got cancelled.");
            return;
        }

        final AtomicInteger expToDrop = new AtomicInteger(event.getExpToDrop());

        if (plugin.getVersionManager().isCurrentAbove("1.8", false))
            event.setDropItems(false);

        event.setExpToDrop(0);

        List<ItemStack> vanillaDrops = new ArrayList<>(
                block.getDrops(plugin.getVersionManager().getMethods().getItemInMainHand(player)));

        if (plugin.getVersionManager().isCurrentBelow("1.8", true)) {
            block.setType(Material.AIR);
        }

        // Start regeneration
        process.start();

        // Run rewards async
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {

            // Events
            // ---------------------------------------------------------------------------------------------

            boolean doubleDrops = false;
            boolean doubleExp = false;

            PresetEvent presetEvent = plugin.getEventManager().getEvent(preset.getName());

            if (presetEvent != null && presetEvent.isEnabled()) {
                doubleDrops = presetEvent.isDoubleDrops();
                doubleExp = presetEvent.isDoubleExperience();
            }

            // Drop Section
            // -----------------------------------------------------------------------------------------
            if (preset.isNaturalBreak()) {

                for (ItemStack drop : vanillaDrops) {
                    XMaterial mat = XMaterial.matchXMaterial(drop);
                    int amount = drop.getAmount();

                    ItemStack item = mat.parseItem();

                    if (item == null) {
                        log.severe(String.format("Material %s not supported on this version.", mat));
                        continue;
                    }

                    item.setAmount(doubleDrops ? amount * 2 : amount);

                    giveItem(item, player, block, preset.isDropNaturally());
                }

                if (expToDrop.get() > 0) {
                    giveExp(block.getLocation(), player, doubleExp ? expToDrop.get() * 2 : expToDrop.get(),
                            preset.isDropNaturally());
                }
            } else {
                for (ItemDrop drop : preset.getRewards().getDrops()) {
                    ItemStack itemStack = drop.toItemStack(player);

                    if (itemStack == null)
                        continue;

                    if (preset.isApplyFortune())
                        itemStack.setAmount(ItemUtil.applyFortune(block.getType(),
                                plugin.getVersionManager().getMethods().getItemInMainHand(player))
                                + itemStack.getAmount());

                    if (doubleDrops)
                        itemStack.setAmount(itemStack.getAmount() * 2);

                    // Drop/Give the item.

                    giveItem(itemStack, player, block, drop.isDropNaturally());

                    if (drop.getExperienceDrop() == null)
                        continue;

                    ExperienceDrop experienceDrop = drop.getExperienceDrop();

                    AtomicInteger expAmount = new AtomicInteger(experienceDrop.getAmount().getInt());

                    if (expAmount.get() <= 0)
                        continue;

                    if (doubleExp)
                        expAmount.set(expAmount.get() * 2);

                    // Drop/Give the exp.

                    giveExp(block.getLocation(), player, expAmount.get(), experienceDrop.isDropNaturally());
                }
            }

            if (presetEvent != null && presetEvent.isEnabled()) {

                // Fire rewards
                if (plugin.getRandom().nextInt(presetEvent.getItemRarity().getInt()) == 0) {

                    // Event item
                    if (presetEvent.getItem() != null) {
                        ItemDrop eventDrop = presetEvent.getItem();

                        if (eventDrop != null) {
                            ItemStack eventStack = eventDrop.toItemStack(player);
                            giveItem(eventStack, player, block, eventDrop.isDropNaturally());
                        }
                    }

                    // Add items from presetEvent
                    for (ItemDrop drop : presetEvent.getRewards().getDrops()) {
                        ItemStack item = drop.toItemStack(player);
                        giveItem(item, player, block, drop.isDropNaturally());
                    }

                    presetEvent.getRewards().give(player);
                }
            }

            // Trigger Jobs Break if enabled
            // -----------------------------------------------------------------------
            if (plugin.getConfig().getBoolean("Jobs-Rewards", false) && plugin.getJobsProvider() != null)
                Bukkit.getScheduler().runTask(plugin,
                        () -> plugin.getJobsProvider().triggerBlockBreakAction(player, block));

            // Rewards
            // ---------------------------------------------------------------------------------------------
            preset.getRewards().give(player);

            // Block Break Sound
            // ---------------------------------------------------------------------------------------------
            if (preset.getSound() != null)
                preset.getSound().play(block.getLocation());

            // Particles
            // -------------------------------------------------------------------------------------------
            // TODO: Make particles work on 1.8 with it's effect API.
            if (preset.getParticle() != null && plugin.getVersionManager().isCurrentAbove("1.8", false))
                Bukkit.getScheduler().runTask(plugin,
                        () -> plugin.getParticleManager().displayParticle(preset.getParticle(), block));
        });
    }

    private void spawnExp(Location location, int amount) {
        if (location.getWorld() == null)
            return;

        Bukkit.getScheduler().runTask(plugin,
                () -> location.getWorld().spawn(location, ExperienceOrb.class).setExperience(amount));
        log.fine(String.format("Spawning xp (%d).", amount));
    }

    private void giveExp(Location location, Player player, int amount, boolean naturally) {
        if (naturally)
            spawnExp(location, amount);
        else
            player.giveExp(amount);
    }

    private void giveItem(ItemStack item, Player player, Block block, boolean naturally) {
        if (item == null)
            return;

        if (naturally) {
            dropItem(item, block);
        } else {
            giveItem(item, player);
        }
    }

    private void dropItem(ItemStack item, Block block) {
        Bukkit.getScheduler().runTask(plugin, () -> block.getWorld().dropItemNaturally(block.getLocation(), item));
        log.fine("Dropping item " + item.getType() + "x" + item.getAmount());
    }

    private void giveItem(ItemStack item, Player player) {
        Bukkit.getScheduler().runTask(plugin, () -> player.getInventory().addItem(item));
        log.fine("Giving item " + item.getType() + "x" + item.getAmount());
    }
}
