package nl.aurorion.blockregen.system.preset;

import com.gamingmesh.jobs.Jobs;
import com.gamingmesh.jobs.container.Job;
import com.gamingmesh.jobs.container.JobsPlayer;
import com.google.common.base.Strings;
import nl.aurorion.blockregen.BlockRegen;
import nl.aurorion.blockregen.Message;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PresetConditions {

    private List<Material> toolsRequired = new ArrayList<>();

    private Map<Enchantment, Integer> enchantsRequired = new HashMap<>();

    private Map<Job, Integer> jobsRequired = new HashMap<>();

    private final BlockRegen plugin;

    public PresetConditions() {
        this.plugin = BlockRegen.getInstance();
    }

    public boolean check(Player player) {
        return checkTools(player) && checkEnchants(player) && checkJobs(player);
    }

    public boolean checkTools(Player player) {

        if (toolsRequired.isEmpty()) return true;

        for (Material material : toolsRequired) {
            if (player.getInventory().getItemInMainHand().getType() == material) {
                return true;
            }
        }

        player.sendMessage(Message.TOOL_REQUIRED_ERROR.get().replace("%tool%", toolsRequired.stream()
                .map(Enum::toString)
                .collect(Collectors.joining(", "))
                .replace("_", " ")
                .toLowerCase()));
        return false;
    }

    public boolean checkEnchants(Player player) {

        if (enchantsRequired.isEmpty()) return true;

        ItemStack tool = player.getInventory().getItemInMainHand();

        ItemMeta meta = tool.getItemMeta();

        if (meta != null && player.getInventory().getItemInMainHand().getType() != Material.AIR) {
            for (Map.Entry<Enchantment, Integer> entry : enchantsRequired.entrySet()) {

                if (meta.hasEnchant(entry.getKey()) && meta.getEnchantLevel(entry.getKey()) >= entry.getValue()) {
                    return true;
                }
            }
        }

        // TODO: Make sure first letter is big in materials. (regex)
        player.sendMessage(Message.ENCHANT_REQUIRED_ERROR.get().replace("%enchant%", compressEnchantRequirements(enchantsRequired)));
        return false;
    }

    private String compressEnchantRequirements(Map<Enchantment, Integer> input) {
        List<String> out = new ArrayList<>();

        for (Map.Entry<Enchantment, Integer> enchant : input.entrySet()) {
            out.add(enchant.getKey().getKey().getKey() + " (" + enchant.getValue().toString() + ")");
        }

        return String.join(", ", out);
    }

    private String compressJobRequirements(Map<Job, Integer> input) {
        List<String> out = new ArrayList<>();

        for (Map.Entry<Job, Integer> job : input.entrySet()) {
            out.add(job.getKey().getName() + " (" + job.getValue().toString() + ")");
        }

        return String.join(", ", out);
    }

    public boolean checkJobs(Player player) {

        if (BlockRegen.getInstance().getJobsProvider() == null || jobsRequired.isEmpty()) return true;

        JobsPlayer jobsPlayer = Jobs.getPlayerManager().getJobsPlayer(player);

        for (Map.Entry<Job, Integer> entry : jobsRequired.entrySet()) {
            if (Jobs.getPlayerManager().getJobsPlayer(player).isInJob(entry.getKey()) && jobsPlayer.getJobProgression(entry.getKey()).getLevel() >= entry.getValue()) {
                return true;
            }
        }

        player.sendMessage(Message.JOBS_REQUIRED_ERROR.get().replace("%job%", compressJobRequirements(jobsRequired)));
        return false;
    }

    public void setToolsRequired(@Nullable String input) {

        if (Strings.isNullOrEmpty(input)) return;

        String[] arr = input.split(", ");

        toolsRequired.clear();
        for (String loop : arr) {
            Material material;
            try {
                material = Material.valueOf(loop.toUpperCase());
            } catch (IllegalArgumentException e) {
                return;
            }
            toolsRequired.add(material);
        }
    }

    public void setEnchantsRequired(@Nullable String input) {

        if (Strings.isNullOrEmpty(input)) return;

        String[] arr = input.split(", ");

        enchantsRequired.clear();
        for (String loop : arr) {
            Enchantment enchantment;
            int level = 1;

            // TODO: Exc handle for enchants
            if (loop.contains(";")) {
                enchantment = Enchantment.getByKey(NamespacedKey.minecraft(loop.split(";")[0].toLowerCase()));
                level = Integer.parseInt(loop.split(";")[1]);
            } else {
                enchantment = Enchantment.getByKey(NamespacedKey.minecraft(loop.toLowerCase()));
            }

            if (enchantment == null) continue;

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