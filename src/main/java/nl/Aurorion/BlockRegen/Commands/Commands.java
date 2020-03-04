package nl.Aurorion.BlockRegen.Commands;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.regions.Region;
import nl.Aurorion.BlockRegen.Main;
import nl.Aurorion.BlockRegen.Messages;
import nl.Aurorion.BlockRegen.Utils;
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

import java.util.Set;

public class Commands implements CommandExecutor, Listener {

    private Main main;

    // Todo rework, add TabCompleter?

    public Commands(Main main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("blockregen")) {
            if (args.length == 0) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&m-----&r &3&lBlockRegen &6&m-----"
                        + "\n&3/" + label + " reload &7: Reload the Settings.yml, Messages.yml and Blocklist.yml, also generates Recovery.yml if needed."
                        + "\n&3/" + label + " bypass &7: Bypass the events."
                        + "\n&3/" + label + " check &7: Check the name + data of the block to put in the blocklist."
                        + "\n&3/" + label + " region &7: All the info to set a region."
                        + "\n&3/" + label + " events &7: Check all your events."
                        + "\nCurrently using BlockRegen v" + main.getDescription().getVersion()
                        + "\n&6&m-----------------------"));
                return true;
            } else {
                if (args[0].equalsIgnoreCase("reload")) {
                    if (!sender.hasPermission("blockregen.admin")) {
                        sender.sendMessage(main.getMessages().noperm);
                        return true;
                    }
                    main.getFiles().reloadSettings();
                    main.getFiles().reloadMessages();
                    new Messages(main.getFiles());
                    main.getFiles().reloadBlocklist();
                    main.cO.setDebug(main.getFiles().getSettings().getBoolean("Debug-Enabled"));
                    main.getFiles().generateRecoveryFile(main);
                    Utils.events.clear();
                    main.fillEvents();
                    Utils.bars.clear();
                    sender.sendMessage(main.getMessages().reload);
                    return true;
                }
                if (args[0].equalsIgnoreCase("bypass")) {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(main.getMessages().noplayer);
                        return true;
                    }
                    Player player = (Player) sender;
                    if (!player.hasPermission("blockregen.bypass")) {
                        player.sendMessage(main.getMessages().noperm);
                        return true;
                    }
                    if (!Utils.bypass.contains(player.getName())) {
                        Utils.bypass.add(player.getName());
                        player.sendMessage(main.getMessages().bypasson);
                    } else {
                        Utils.bypass.remove(player.getName());
                        player.sendMessage(main.getMessages().bypassoff);
                    }
                    return true;
                }
                if (args[0].equalsIgnoreCase("check")) {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(main.getMessages().noplayer);
                        return true;
                    }
                    Player player = (Player) sender;
                    if (!player.hasPermission("blockregen.datacheck")) {
                        player.sendMessage(main.getMessages().noperm);
                        return true;
                    }
                    if (!Utils.itemcheck.contains(player.getName())) {
                        Utils.itemcheck.add(player.getName());
                        player.sendMessage(main.getMessages().datacheckon);
                    } else {
                        Utils.itemcheck.remove(player.getName());
                        player.sendMessage(main.getMessages().datacheckoff);
                    }
                    return true;
                }
                if (args[0].equalsIgnoreCase("convert")) {
                    this.convert();
                    sender.sendMessage(main.getMessages().prefix + ChatColor.translateAlternateColorCodes('&', "&a&lConverted your regions to BlockRegen 3.4.0 compatibility!"));
                }
                if (args[0].equalsIgnoreCase("region")) {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(main.getMessages().noplayer);
                        return true;
                    }
                    Player player = (Player) sender;
                    if (!player.hasPermission("blockregen.admin")) {
                        player.sendMessage(main.getMessages().noperm);
                        return true;
                    }
                    if (args.length == 1 || args.length > 3) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&m-----&r &3&lBlockRegen &6&m-----"
                                + "\n&3/" + label + "  region set <name> &7: set a region."
                                + "\n&3/" + label + "  region remove <name> &7: remove a region."
                                + "\n&3/" + label + "  region list &7: a list of all your regions."
                                + "\n&6&m-----------------------"));
                        return true;
                    }
                    if (args.length == 2) {
                        if (args[1].equalsIgnoreCase("list")) {
                            ConfigurationSection regions = main.getFiles().getRegions().getConfigurationSection("Regions");
                            Set<String> setregions = regions.getKeys(false);
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

                            Region s = null;
                            try {
                                s = main.getWorldEdit().getSession(player).getSelection(BukkitAdapter.adapt(player.getWorld()));
                            } catch (IncompleteRegionException e) {
                                player.sendMessage(main.getMessages().noregion);
                                e.printStackTrace();
                            }

                            if (main.getFiles().getRegions().getString("Regions") == null) {
                                main.getFiles().getRegions().set("Regions." + args[2] + ".Min", Utils.locationToString(BukkitAdapter.adapt(player.getWorld(), s.getMinimumPoint())));
                                main.getFiles().getRegions().set("Regions." + args[2] + ".Max", Utils.locationToString(BukkitAdapter.adapt(player.getWorld(), s.getMaximumPoint())));
                                main.getFiles().saveRegions();
                                player.sendMessage(main.getMessages().setregion);
                            } else {
                                ConfigurationSection regions = main.getFiles().getRegions().getConfigurationSection("Regions");
                                Set<String> setregions = regions.getKeys(false);
                                if (setregions.contains(args[2])) {
                                    player.sendMessage(main.getMessages().dupregion);
                                } else {
                                    main.getFiles().getRegions().set("Regions." + args[2] + ".Min", Utils.locationToString(BukkitAdapter.adapt(player.getWorld(), s.getMinimumPoint())));
                                    main.getFiles().getRegions().set("Regions." + args[2] + ".Max", Utils.locationToString(BukkitAdapter.adapt(player.getWorld(), s.getMaximumPoint())));
                                    main.getFiles().saveRegions();
                                    player.sendMessage(main.getMessages().setregion);
                                }
                                return true;
                            }
                            return true;
                        }
                        if (args[1].equalsIgnoreCase("remove")) {
                            if (main.getFiles().getRegions().getString("Regions") == null) {
                                player.sendMessage(main.getMessages().unknownregion);
                            } else {
                                ConfigurationSection regions = main.getFiles().getRegions().getConfigurationSection("Regions");
                                Set<String> setregions = regions.getKeys(false);
                                if (setregions.contains(args[2])) {
                                    main.getFiles().getRegions().set("Regions." + args[2], null);
                                    main.getFiles().saveRegions();
                                    player.sendMessage(main.getMessages().removeregion);
                                } else {
                                    player.sendMessage(main.getMessages().unknownregion);
                                }
                                return true;
                            }
                            return true;
                        }
                    }
                }
                if (args[0].equalsIgnoreCase("events")) {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(main.getMessages().noplayer);
                        return true;
                    }
                    Player player = (Player) sender;
                    if (!player.hasPermission("blockregen.admin")) {
                        player.sendMessage(main.getMessages().noperm);
                        return true;
                    }
                    if (args.length < 3) {
                        if (Utils.events.isEmpty()) {
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&m-----&r &3&lBlockRegen &6&m-----"
                                    + "\n&eYou haven't yet made any events. Make some to up your servers game!"
                                    + "\n&6&m-----------------------"));
                        } else {
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&m-----&r &3&lBlockRegen &6&m-----"));
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&eYou have the following events ready to be activated."));
                            player.sendMessage(" ");
                            for (String events : Utils.events.keySet()) {
                                String state;
                                if (Utils.events.get(events) == false) {
                                    state = ChatColor.RED + "(inactive)";
                                } else {
                                    state = ChatColor.GREEN + "(active)";
                                }
                                player.sendMessage(ChatColor.AQUA + "- " + events + " " + state);
                            }
                            player.sendMessage(" ");
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&eUse &3/" + label + "  events activate <event name> &eto activate it."));
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&eUse &3/" + label + "  events deactivate <event name> &eto de-activate it."));
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&m-----------------------"));
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
                                if (Utils.events.get(allArgs) == false) {
                                    Utils.events.put(allArgs, true);
                                    player.sendMessage(main.getMessages().activateEvent.replace("%event%", allArgs));
                                    String barName = null;
                                    BarColor barColor = BarColor.BLUE;
                                    FileConfiguration blocklist = main.getFiles().getBlocklist();
                                    ConfigurationSection blocks = blocklist.getConfigurationSection("Blocks");
                                    Set<String> setblocks = blocks.getKeys(false);
                                    for (String loopBlocks : setblocks) {
                                        String eventName = blocklist.getString("Blocks." + loopBlocks + ".event.event-name");
                                        if (eventName.equalsIgnoreCase(allArgs)) {
                                            if (blocklist.getString("Blocks." + loopBlocks + ".event.bossbar.name") == null) {
                                                barName = "Event " + allArgs + " is now active!";
                                            } else {
                                                barName = blocklist.getString("Blocks." + loopBlocks + ".event.bossbar.name");
                                            }
                                            if (blocklist.getString("Blocks." + loopBlocks + ".event.bossbar.color") == null) {
                                                barColor = BarColor.YELLOW;
                                            } else {
                                                barColor = BarColor.valueOf(blocklist.getString("Blocks." + loopBlocks + ".event.bossbar.color").toUpperCase());
                                            }

                                            break;
                                        } else {
                                            continue;
                                        }
                                    }
                                    BossBar bossbar = Bukkit.createBossBar(null, BarColor.BLUE, BarStyle.SOLID);
                                    Utils.bars.put(allArgs, bossbar);
                                    bossbar.setTitle(ChatColor.translateAlternateColorCodes('&', barName));
                                    bossbar.setColor(barColor);
                                    for (Player online : Bukkit.getOnlinePlayers()) {
                                        bossbar.addPlayer(online);
                                    }
                                } else {
                                    player.sendMessage(main.getMessages().eventActive);
                                }
                            } else {
                                player.sendMessage(main.getMessages().eventNotFound);
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
                                if (Utils.events.get(allArgs) == true) {
                                    Utils.events.put(allArgs, false);
                                    player.sendMessage(main.getMessages().deActivateEvent.replace("%event%", allArgs));
                                    BossBar bossbar = Utils.bars.get(allArgs);
                                    bossbar.removeAll();
                                    Utils.bars.remove(allArgs);
                                } else {
                                    player.sendMessage(main.getMessages().eventNotActive);
                                }
                            } else {
                                player.sendMessage(main.getMessages().eventNotFound);
                            }
                            return true;
                        }
                    }
                    return true;
                }

            }
        }
        return false;
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
        FileConfiguration regions = main.getFiles().getRegions();

        String[] locA;
        String[] locB;
        String world;

        ConfigurationSection regionsection = regions.getConfigurationSection("Regions");
        Set<String> regionset = regionsection.getKeys(false);
        for (String regionloop : regionset) {
            if (regions.get("Regions." + regionloop + ".World") != null) {
                locA = regions.getString("Regions." + regionloop + ".Max").split(";");
                locB = regions.getString("Regions." + regionloop + ".Min").split(";");
                world = regions.getString("Regions." + regionloop + ".World");
                regions.set("Regions." + regionloop + ".Max", world + ";" + locA[0] + ";" + locA[1] + ";" + locA[2]);
                regions.set("Regions." + regionloop + ".Min", world + ";" + locB[0] + ";" + locB[1] + ";" + locB[2]);
                regions.set("Regions." + regionloop + ".World", null);
            }
        }

        main.getFiles().saveRegions();

    }

}
