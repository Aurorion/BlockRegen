package nl.aurorion.blockregen.commands;

import nl.aurorion.blockregen.BlockRegen;
import nl.aurorion.blockregen.Message;
import nl.aurorion.blockregen.StringUtil;
import nl.aurorion.blockregen.util.Utils;
import nl.aurorion.blockregen.system.event.struct.PresetEvent;
import nl.aurorion.blockregen.system.region.struct.RegenerationRegion;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Commands implements CommandExecutor {

    private final BlockRegen plugin;

    public Commands(BlockRegen plugin) {
        this.plugin = plugin;
    }

    private void sendHelp(CommandSender sender, String label) {
        sender.sendMessage(StringUtil.color("&8&m        &r &3BlockRegen &f" + plugin.getDescription().getVersion() + " &8&m        &r"
                + "\n&3/" + label + " reload &8- &7Reload the plugin."
                + "\n&3/" + label + " bypass &8- &7Bypass block regeneration."
                + "\n&3/" + label + " check &8- &7Check the correct material name to use. Just hit a block."
                + "\n&3/" + label + " region &8- &7Region management."
                + "\n&3/" + label + " events &8- &7Event management."
                + "\n&3/" + label + " discord &8- &7BlockRegen discord invite. Ask for support here."));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sendHelp(sender, label);
            return false;
        }

        Player player;

        switch (args[0].toLowerCase()) {
            case "reload":
                if (!sender.hasPermission("blockregen.admin")) {
                    Message.NO_PERM.send(sender);
                    return false;
                }

                plugin.reload(sender);
                break;
            case "bypass":
                if (checkConsole(sender))
                    return false;

                player = (Player) sender;

                if (!player.hasPermission("blockregen.bypass")) {
                    Message.NO_PERM.send(player);
                    return false;
                }

                if (!Utils.bypass.contains(player.getUniqueId())) {
                    Utils.bypass.add(player.getUniqueId());
                    Message.BYPASS_ON.send(player);
                } else {
                    Utils.bypass.remove(player.getUniqueId());
                    Message.BYPASS_OFF.send(player);
                }
                break;
            case "check":
                if (checkConsole(sender))
                    return false;

                player = (Player) sender;

                if (!player.hasPermission("blockregen.datacheck")) {
                    Message.NO_PERM.send(player);
                    return false;
                }

                if (!Utils.dataCheck.contains(player.getUniqueId())) {
                    Utils.dataCheck.add(player.getUniqueId());
                    Message.DATA_CHECK_ON.send(player);
                } else {
                    Utils.dataCheck.remove(player.getUniqueId());
                    Message.DATA_CHECK_OFF.send(player);
                }
                break;
            case "region":

                if (checkConsole(sender))
                    return false;

                player = (Player) sender;

                if (!player.hasPermission("blockregen.admin")) {
                    player.sendMessage(Message.NO_PERM.get(player));
                    return false;
                }

                if (args.length == 1) {
                    player.sendMessage(StringUtil.color("&8&m     &r &3BlockRegen Regions &8&m     "
                            + "\n&3/" + label + " region set <name> &8- &7Create a regeneration region."
                            + "\n&3/" + label + " region remove <name> &8- &7Remove a region."
                            + "\n&3/" + label + " region list &8- &7List your regions."));
                    return false;
                }

                if (args[1].equalsIgnoreCase("list")) {

                    if (args.length > 2) {
                        sender.sendMessage(Message.TOO_MANY_ARGS.get(player)
                                .replace("%help%", String.format("/%s region list", label)));
                        return false;
                    }

                    StringBuilder message = new StringBuilder("&8&m    &3 BlockRegen Regions &8&m    &r");
                    for (String name : plugin.getRegionManager().getLoadedRegions().keySet()) {
                        message.append("\n&8  - &f").append(name);
                    }
                    message.append("\n");
                    sender.sendMessage(StringUtil.color(message.toString()));
                    return false;
                } else if (args[1].equalsIgnoreCase("set")) {

                    if (args.length > 3) {
                        sender.sendMessage(Message.TOO_MANY_ARGS.get(player)
                                .replace("%help%", String.format("/%s region set <name>", label)));
                        return false;
                    } else if (args.length < 3) {
                        sender.sendMessage(Message.NOT_ENOUGH_ARGS.get(player)
                                .replace("%help%", String.format("/%s region set <name>", label)));
                        return false;
                    }

                    if (plugin.getRegionManager().exists(args[2])) {
                        Message.DUPLICATED_REGION.send(player);
                        return false;
                    }

                    if (plugin.getVersionManager().getWorldEditProvider() == null) {
                        Message.WORLD_EDIT_NOT_INSTALLED.send(player);
                        return false;
                    }

                    RegenerationRegion region = plugin.getVersionManager().getWorldEditProvider().createFromSelection(args[2], player);

                    if (region == null) {
                        Message.NO_SELECTION.send(player);
                        return false;
                    }

                    plugin.getRegionManager().addRegion(region);
                    Message.SET_REGION.send(player);
                    return false;
                } else if (args[1].equalsIgnoreCase("remove")) {

                    if (args.length > 3) {
                        sender.sendMessage(Message.TOO_MANY_ARGS.get(player)
                                .replace("%help%", String.format("/%s region remove <name>", label)));
                        return false;
                    } else if (args.length < 3) {
                        sender.sendMessage(Message.NOT_ENOUGH_ARGS.get(player)
                                .replace("%help%", String.format("/%s region remove <name>", label)));
                        return false;
                    }

                    if (!plugin.getRegionManager().exists(args[2])) {
                        Message.UNKNOWN_REGION.send(player);
                        return false;
                    }

                    plugin.getRegionManager().removeRegion(args[2]);
                    Message.REMOVE_REGION.send(player);
                    return false;
                } else {
                    player.sendMessage(StringUtil.color("&8&m     &r &3BlockRegen Regions &8&m     "
                            + "\n&3/" + label + " region set <name> &8- &7Create a regeneration region."
                            + "\n&3/" + label + " region remove <name> &8- &7Remove a region."
                            + "\n&3/" + label + " region list &8- &7List your regions."));
                }
                break;
            case "debug":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(Message.ONLY_PLAYERS.get());
                    return false;
                }

                player = (Player) sender;

                if (plugin.getConsoleHandler().getListeners().contains(sender)) {
                    plugin.getConsoleHandler().removeListener(sender);
                    sender.sendMessage(Message.DEBUG_OFF.get(player));
                } else {
                    plugin.getConsoleHandler().addListener(sender);
                    sender.sendMessage(Message.DEBUG_ON.get(player));
                }
                break;
            case "discord":
                sender.sendMessage(StringUtil.color("&8&m      &3 BlockRegen Discord Server" +
                        "\n&6>> &7https://discord.gg/ZCxMca5"));
                break;
            case "events":
                if (!sender.hasPermission("blockregen.admin")) {
                    sender.sendMessage(Message.NO_PERM.get());
                    return false;
                }

                if (args.length < 3) {

                    if (plugin.getEventManager().getLoadedEvents().isEmpty()) {
                        sender.sendMessage(StringUtil.color("&8&m     &r &3BlockRegen Events &8&m     "
                                + "\n&cYou haven't made any events yet."
                                + "\n&8&m                       "));
                        return false;
                    }

                    StringBuilder list = new StringBuilder("&8&m     &r &3BlockRegen Events &8&m     \n" +
                            "&7You have the following events loaded:").append("\n&r ");

                    for (PresetEvent event : plugin.getEventManager().getEvents(e -> true)) {
                        list.append("\n&8 - &r").append(event.getDisplayName()).append(" &7(Name: &f").append(event.getName()).append("&7) ")
                                .append(event.isEnabled() ? " &a(active)&r" : " &c(inactive)&r");
                    }

                    list.append("\n&r \n&7Use &3/").append(label).append(" events activate <name> &7to activate it.\n")
                            .append("&7Use &3/").append(label).append(" events deactivate <name> &7to de-activate it.");
                    sender.sendMessage(StringUtil.color(list.toString()));
                } else {
                    if (args[1].equalsIgnoreCase("activate")) {
                        String name = args[2];

                        PresetEvent event = plugin.getEventManager().getEvent(name);

                        if (event == null) {
                            sender.sendMessage(Message.EVENT_NOT_FOUND.get());
                            return false;
                        }

                        if (event.isEnabled()) {
                            sender.sendMessage(Message.EVENT_ALREADY_ACTIVE.get());
                            return false;
                        }

                        plugin.getEventManager().enableEvent(event);
                        sender.sendMessage(StringUtil.color(Message.ACTIVATE_EVENT.get().replace("%event%", event.getDisplayName())));
                        return false;
                    }

                    if (args[1].equalsIgnoreCase("deactivate")) {
                        String name = args[2];

                        PresetEvent event = plugin.getEventManager().getEvent(name);

                        if (event == null) {
                            sender.sendMessage(Message.EVENT_NOT_FOUND.get());
                            return false;
                        }

                        if (!event.isEnabled()) {
                            sender.sendMessage(Message.EVENT_NOT_ACTIVE.get());
                            return false;
                        }

                        plugin.getEventManager().disableEvent(event);
                        sender.sendMessage(StringUtil.color(Message.DEACTIVATE_EVENT.get().replace("%event%", event.getDisplayName())));
                        return false;
                    }
                }
                break;
            default:
                sendHelp(sender, label);
        }
        return false;
    }

    private boolean checkConsole(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Message.ONLY_PLAYERS.get());
            return true;
        }

        return false;
    }
}