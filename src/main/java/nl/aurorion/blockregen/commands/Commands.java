package nl.aurorion.blockregen.commands;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.regions.Region;
import nl.aurorion.blockregen.BlockRegen;
import nl.aurorion.blockregen.Message;
import nl.aurorion.blockregen.Utils;
import nl.aurorion.blockregen.system.preset.struct.BlockPreset;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class Commands implements CommandExecutor, Listener {

    private final BlockRegen plugin;

    public Commands(BlockRegen plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Utils.color("&8&m        &r &3BlockRegen &f" + plugin.getDescription().getVersion() + " &8&m        &r"
                    + "\n&3/" + label + " reload &8- &7Reload the plugin."
                    + "\n&3/" + label + " bypass &8- &7Bypass block breaking."
                    + "\n&3/" + label + " check &8- &7Check the name + data of the block to put in the blocklist."
                    + "\n&3/" + label + " region &8- &7All the info to set a region."
                    + "\n&3/" + label + " events &8- &7Check all your events."
                    + "\n&3/" + label + " discord &8- &7Print BlockRegen discord invite."
                    + "\n&3/" + label + " debug (all) &8- &7Enable player debug channel."));
            return true;
        }

        Player player;

        switch (args[0].toLowerCase()) {
            case "reload":
                if (!sender.hasPermission("blockregen.admin")) {
                    sender.sendMessage(Message.NO_PERM.get());
                    return true;
                }

                plugin.reload(sender);
                break;
            case "bypass":
                if (checkConsole(sender))
                    return true;

                player = (Player) sender;

                if (!player.hasPermission("blockregen.bypass")) {
                    player.sendMessage(Message.NO_PERM.get(player));
                    return true;
                }

                if (!Utils.bypass.contains(player.getName())) {
                    Utils.bypass.add(player.getName());
                    player.sendMessage(Message.BYPASS_ON.get(player));
                } else {
                    Utils.bypass.remove(player.getName());
                    player.sendMessage(Message.BYPASS_OFF.get(player));
                }
                break;
            case "check":
                if (checkConsole(sender))
                    return true;

                player = (Player) sender;

                if (!player.hasPermission("blockregen.datacheck")) {
                    player.sendMessage(Message.NO_PERM.get(player));
                    return true;
                }

                if (!Utils.dataCheck.contains(player.getName())) {
                    Utils.dataCheck.add(player.getName());
                    player.sendMessage(Message.DATA_CHECK_ON.get(player));
                } else {
                    Utils.dataCheck.remove(player.getName());
                    player.sendMessage(Message.DATA_CHECK_OFF.get(player));
                }
                break;
            case "convert":
                this.convert();
                sender.sendMessage(Message.PREFIX.get() + Utils.color("&a&lConverted your regions to BlockRegen 3.4.0 compatibility!"));
                break;
            case "region":
                if (checkConsole(sender))
                    return true;

                player = (Player) sender;

                if (!player.hasPermission("blockregen.admin")) {
                    player.sendMessage(Message.NO_PERM.get(player));
                    return true;
                }

                if (args.length == 1 || args.length > 3) {
                    player.sendMessage(Utils.color("&6&m-----&r &3&lBlockRegen &6&m-----"
                            + "\n&3/" + label + "  region set <name> &7: set a region."
                            + "\n&3/" + label + "  region remove <name> &7: remove a region."
                            + "\n&3/" + label + "  region list &7: a list of all your regions."
                            + "\n&6&m-----------------------"));
                    return true;
                }

                if (args.length == 2) {
                    if (args[1].equalsIgnoreCase("list")) {
                        ConfigurationSection regions = plugin.getFiles().getRegions().getFileConfiguration().getConfigurationSection("Regions");
                        Set<String> setregions = Objects.requireNonNull(regions).getKeys(false);
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&m-----&r &3&lBlockRegen &6&m-----"));
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&eHere is a list of all your regions."));
                        player.sendMessage(" ");
                        for (String checkregions : setregions) {
                            player.sendMessage(ChatColor.AQUA + "- " + checkregions);
                        }
                        player.sendMessage(" ");
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&m-----------------------"));
                        return true;
                    }
                }

                if (args.length == 3) {
                    if (args[1].equalsIgnoreCase("set")) {

                        if (plugin.getWorldEditProvider() == null) {
                            player.sendMessage(Message.WORLD_EDIT_NOT_INSTALLED.get(player));
                            return true;
                        }

                        Region selection = plugin.getWorldEditProvider().getSelection(player);

                        if (selection == null) {
                            return true;
                        }

                        List<String> regions = new ArrayList<>();

                        if (plugin.getFiles().getRegions().getFileConfiguration().getString("Regions") != null) {
                            ConfigurationSection regionSection = plugin.getFiles().getRegions().getFileConfiguration().getConfigurationSection("Regions");
                            regions = new ArrayList<>(Objects.requireNonNull(regionSection).getKeys(false));
                        }

                        if (regions.contains(args[2])) {
                            player.sendMessage(Message.DUPLICATED_REGION.get(player));
                            return true;
                        }

                        plugin.getFiles().getRegions().getFileConfiguration().set("Regions." + args[2] + ".Min", Utils.locationToString(BukkitAdapter.adapt(player.getWorld(), selection.getMinimumPoint())));
                        plugin.getFiles().getRegions().getFileConfiguration().set("Regions." + args[2] + ".Max", Utils.locationToString(BukkitAdapter.adapt(player.getWorld(), selection.getMaximumPoint())));
                        plugin.getFiles().getRegions().save();
                        player.sendMessage(Message.SET_REGION.get(player));
                        return true;
                    }

                    if (args[1].equalsIgnoreCase("remove")) {
                        if (plugin.getFiles().getRegions().getFileConfiguration().getString("Regions") == null) {
                            player.sendMessage(Message.UNKNOWN_REGION.get(player));
                        } else {
                            ConfigurationSection regions = plugin.getFiles().getRegions().getFileConfiguration().getConfigurationSection("Regions");
                            Set<String> setregions = Objects.requireNonNull(regions).getKeys(false);

                            if (setregions.contains(args[2])) {
                                plugin.getFiles().getRegions().getFileConfiguration().set("Regions." + args[2], null);
                                plugin.getFiles().getRegions().save();
                                player.sendMessage(Message.REMOVE_REGION.get(player));
                            } else {
                                player.sendMessage(Message.UNKNOWN_REGION.get(player));
                            }

                            return true;
                        }
                        return true;
                    }
                }
                break;
            case "debug":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(Message.ONLY_PLAYERS.get());
                    return true;
                }

                player = (Player) sender;

                if (args.length > 1 && args[1].equalsIgnoreCase("all")) {
                    if (plugin.getConsoleOutput().getListeners().contains(sender)) {
                        plugin.getConsoleOutput().removeListener(sender);
                        sender.sendMessage(Utils.color(Message.PREFIX.getValue() + " &cYou are no longer listening."));
                    } else {
                        plugin.getConsoleOutput().addListener(sender);
                        sender.sendMessage(Utils.color(Message.PREFIX.getValue() + " &aYou are listening to everything."));
                    }
                    return false;
                }

                if (plugin.getConsoleOutput().switchPersonalDebug(sender))
                    sender.sendMessage(Message.DEBUG_ON.get(player));
                else
                    sender.sendMessage(Message.DEBUG_OFF.get(player));
                break;
            case "discord":
                sender.sendMessage(Utils.color("&8&m      &3 BlockRegen Discord Server" +
                        "\n&6>> &7https://discord.gg/ZCxMca5"));
                break;
            case "events":
                if (!sender.hasPermission("blockregen.admin")) {
                    sender.sendMessage(Message.NO_PERM.get());
                    return true;
                }

                if (args.length < 3) {
                    if (Utils.events.isEmpty()) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&m-----&r &3&lBlockRegen &6&m-----"
                                + "\n&eYou haven't yet made any events. Make some to up your servers game!"
                                + "\n&6&m-----------------------"));
                    } else {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&m-----&r &3&lBlockRegen &6&m-----"));
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&eYou have the following events ready to be activated."));
                        sender.sendMessage(" ");
                        for (String events : Utils.events.keySet()) {
                            String state;
                            if (!Utils.events.get(events)) {
                                state = ChatColor.RED + "(inactive)";
                            } else {
                                state = ChatColor.GREEN + "(active)";
                            }
                            sender.sendMessage(ChatColor.AQUA + "- " + events + " " + state);
                        }
                        sender.sendMessage(" ");
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&eUse &3/" + label + "  events activate <event name> &eto activate it."));
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&eUse &3/" + label + "  events deactivate <event name> &eto de-activate it."));
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&m-----------------------"));
                    }
                } else {
                    if (args[1].equalsIgnoreCase("activate")) {
                        String allArgs = args[2];
                        if (args.length > 3) {
                            StringBuilder sb = new StringBuilder();
                            for (int i = 2; i < args.length; i++) {
                                sb.append(args[i]).append(" ");
                            }
                            allArgs = sb.toString().trim();
                        }

                        if (Utils.events.containsKey(allArgs)) {
                            if (!Utils.events.get(allArgs)) {
                                Utils.events.put(allArgs, true);
                                sender.sendMessage(Message.ACTIVATE_EVENT.get().replace("%event%", allArgs));

                                String barName = null;
                                BarColor barColor = BarColor.BLUE;

                                for (BlockPreset preset : plugin.getPresetManager().getPresets().values()) {
                                    if (preset.getEvent() == null || !preset.getEvent().getName().equalsIgnoreCase(allArgs))
                                        continue;

                                    if (preset.getEvent().getBossBar() == null)
                                        return false;

                                    barName = preset.getEvent().getBossBar().getText();
                                    barColor = preset.getEvent().getBossBar().getColor();
                                }

                                BossBar bossbar = Bukkit.createBossBar(Utils.color(Utils.parse(barName)), barColor, BarStyle.SOLID);

                                Utils.bars.put(allArgs, bossbar);

                                for (Player online : Bukkit.getOnlinePlayers()) {
                                    bossbar.addPlayer(online);
                                }
                            } else {
                                sender.sendMessage(Message.EVENT_ALREADY_ACTIVE.get());
                            }
                        } else {
                            sender.sendMessage(Message.EVENT_NOT_FOUND.get());
                        }
                        return true;
                    }

                    if (args[1].equalsIgnoreCase("deactivate")) {
                        String allArgs = args[2];
                        if (args.length > 3) {
                            StringBuilder sb = new StringBuilder();
                            for (int i = 2; i < args.length; i++) {
                                sb.append(args[i]).append(" ");
                            }
                            allArgs = sb.toString().trim();
                        }

                        if (Utils.events.containsKey(allArgs)) {
                            if (Utils.events.get(allArgs)) {
                                Utils.events.put(allArgs, false);
                                sender.sendMessage(Message.DEACTIVATE_EVENT.get().replace("%event%", allArgs));
                                BossBar bossbar = Utils.bars.get(allArgs);
                                bossbar.removeAll();
                                Utils.bars.remove(allArgs);
                            } else {
                                sender.sendMessage(Message.EVENT_NOT_ACTIVE.get());
                            }
                        } else {
                            sender.sendMessage(Message.EVENT_NOT_FOUND.get());
                        }

                        return true;
                    }
                }
                break;
        }
        return true;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!Utils.bars.isEmpty()) {
            for (String bars : Utils.bars.keySet()) {
                BossBar bar = Utils.bars.get(bars);
                bar.addPlayer(player);
            }
        }
    }

    private void convert() {
        FileConfiguration regions = plugin.getFiles().getRegions().getFileConfiguration();

        String[] locA;
        String[] locB;
        String world;

        ConfigurationSection regionSection = regions.getConfigurationSection("Regions");
        Set<String> regionSet = Objects.requireNonNull(regionSection).getKeys(false);

        for (String region : regionSet) {
            if (regions.get("Regions." + region + ".World") != null) {
                locA = Objects.requireNonNull(regions.getString("Regions." + region + ".Max")).split(";");
                locB = Objects.requireNonNull(regions.getString("Regions." + region + ".Min")).split(";");
                world = regions.getString("Regions." + region + ".World");
                regions.set("Regions." + region + ".Max", world + ";" + locA[0] + ";" + locA[1] + ";" + locA[2]);
                regions.set("Regions." + region + ".Min", world + ";" + locB[0] + ";" + locB[1] + ";" + locB[2]);
                regions.set("Regions." + region + ".World", null);
            }
        }

        plugin.getFiles().getRegions().save();
    }

    private boolean checkConsole(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Message.ONLY_PLAYERS.get());
            return true;
        }

        return false;
    }
}