package nl.aurorion.blockregen.system.preset.struct;

import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import com.gamingmesh.jobs.Jobs;
import com.gamingmesh.jobs.container.Job;
import com.gamingmesh.jobs.container.JobsPlayer;
import com.google.common.base.Strings;
import lombok.NoArgsConstructor;
import nl.aurorion.blockregen.BlockRegen;
import nl.aurorion.blockregen.ConsoleOutput;
import nl.aurorion.blockregen.Message;
import nl.aurorion.blockregen.util.ParseUtil;
import nl.aurorion.blockregen.util.TextUtil;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

@NoArgsConstructor
public class PresetConditions {

    private final List<XMaterial> toolsRequired = new ArrayList<>();

    private final Map<XEnchantment, Integer> enchantsRequired = new HashMap<>();

    private final Map<Job, Integer> jobsRequired = new HashMap<>();

    public boolean check(Player player) {
        return checkTools(player) && checkEnchants(player) && checkJobs(player);
    }

    public boolean checkTools(Player player) {

        if (toolsRequired.isEmpty()) return true;

        for (XMaterial material : toolsRequired) {
            if (XMaterial.matchXMaterial(player.getInventory().getItemInMainHand()) == material)
                return true;
        }

        player.sendMessage(Message.TOOL_REQUIRED_ERROR.get(player).replace("%tool%", composeToolRequirements()));
        return false;
    }

    private String composeToolRequirements() {
        return toolsRequired.stream()
                .map(xMaterial -> TextUtil.capitalize(xMaterial.toString()
                        .toLowerCase()
                        .replace("_", " ")))
                .collect(Collectors.joining(", "));
    }

    public boolean checkEnchants(Player player) {

        if (enchantsRequired.isEmpty()) return true;

        ItemStack tool = player.getInventory().getItemInMainHand();

        ItemMeta meta = tool.getItemMeta();

        if (meta != null && player.getInventory().getItemInMainHand().getType() != Material.AIR) {
            for (Map.Entry<XEnchantment, Integer> entry : enchantsRequired.entrySet()) {

                Enchantment enchantment = entry.getKey().parseEnchantment();

                if (enchantment == null)
                    continue;

                if (meta.hasEnchant(enchantment) && meta.getEnchantLevel(enchantment) >= entry.getValue())
                    return true;
            }
        }

        player.sendMessage(Message.ENCHANT_REQUIRED_ERROR.get(player)
                .replace("%enchant%", compressEnchantRequirements()));
        return false;
    }

    private String compressEnchantRequirements() {
        return enchantsRequired.entrySet().stream()
                .map(e -> String.format("%s (%d)", TextUtil.capitalize(e.getKey().name().replace("_", " ")), e.getValue()))
                .collect(Collectors.joining(", "));
    }

    public boolean checkJobs(Player player) {

        if (BlockRegen.getInstance().getJobsProvider() == null || jobsRequired.isEmpty()) return true;

        JobsPlayer jobsPlayer = Jobs.getPlayerManager().getJobsPlayer(player);

        for (Map.Entry<Job, Integer> entry : jobsRequired.entrySet()) {
            if (Jobs.getPlayerManager().getJobsPlayer(player).isInJob(entry.getKey()) && jobsPlayer.getJobProgression(entry.getKey()).getLevel() >= entry.getValue()) {
                return true;
            }
        }

        player.sendMessage(Message.JOBS_REQUIRED_ERROR.get(player)
                .replace("%job%", compressJobRequirements()));
        return false;
    }

    private String compressJobRequirements() {
        return jobsRequired.entrySet().stream()
                .map(e -> String.format("%s (%d)", e.getKey().getName(), e.getValue()))
                .collect(Collectors.joining(", "));
    }

    public void setToolsRequired(@Nullable String input) {

        if (Strings.isNullOrEmpty(input))
            return;

        String[] arr = input.split(", ");

        toolsRequired.clear();
        for (String loop : arr) {
            XMaterial material = ParseUtil.parseMaterial(loop);
            if (material == null) {
                ConsoleOutput.getInstance().warn("Could not parse tool material " + loop);
                continue;
            }
            toolsRequired.add(material);
        }
    }

    public void setEnchantsRequired(@Nullable String input) {

        if (Strings.isNullOrEmpty(input)) return;

        String[] arr = input.split(", ");

        enchantsRequired.clear();
        for (String loop : arr) {

            String enchantmentName = loop.split(";")[0];
            XEnchantment enchantment = ParseUtil.parseEnchantment(enchantmentName);
            if (enchantment == null) {
                ConsoleOutput.getInstance().warn("Could not parse enchantment " + enchantmentName + " in " + input);
                continue;
            }

            int level = 1;
            if (loop.contains(";")) {
                level = Integer.parseInt(loop.split(";")[1]);

                if (level < 0) {
                    ConsoleOutput.getInstance().warn("Could not parse an enchantment level in " + input);
                    continue;
                }
            }

            enchantsRequired.put(enchantment, level);
        }
    }

    public void setJobsRequired(@Nullable String input) {

        if (Strings.isNullOrEmpty(input)) return;

        String[] arr = input.split(", ");

        jobsRequired.clear();
        for (String loop : arr) {
            Job job;
            int level = 1;

            if (loop.contains(";")) {
                job = Jobs.getJob(loop.split(";")[0]);
                level = Integer.parseInt(loop.split(";")[1]);
            } else {
                job = Jobs.getJob(loop);
            }

            jobsRequired.put(job, level);
        }
    }
}