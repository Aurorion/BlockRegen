package nl.aurorion.blockregen.system.preset.struct;

import nl.aurorion.blockregen.BlockRegen;
import com.google.common.base.Strings;
import lombok.Getter;
import lombok.Setter;
import nl.aurorion.blockregen.ConsoleOutput;
import nl.aurorion.blockregen.util.ParseUtil;
import org.bukkit.configuration.ConfigurationSection;

public class Amount {

    @Getter @Setter private double lowValue;
    @Getter @Setter private double highValue;

    @Getter @Setter private double fixedValue;

    @Getter @Setter private boolean fixed;

    public Amount(double low, double high) {
        fixed = false;

        lowValue = Math.min(low, high);
        highValue = Math.max(low, high);
    }

    public Amount(double fixedValue) {
        fixed = true;

        this.fixedValue = fixedValue;
    }

    // Load Amount from yaml
    public static Amount load(ConfigurationSection root, String path, double defaultValue) {

        if (root == null)
            return new Amount(defaultValue);

        // low & high section syntax
        if (root.isConfigurationSection(path)) {

            ConfigurationSection section = root.getConfigurationSection(path);

            if (section == null || !section.contains("high") || section.contains("low"))
                return new Amount(defaultValue);

            if (!(section.get("low") instanceof Number) || !(section.get("high") instanceof Number))
                return new Amount(defaultValue);

            double low = section.getDouble("low");
            double high = section.getDouble("high");

            return new Amount(low, high);
        } else {
            String data = root.getString(path);

            if (Strings.isNullOrEmpty(data))
                return new Amount(defaultValue);

            // low-high syntax
            if (data.contains("-")) {
                double low = ParseUtil.nullOrDefault(() -> Double.parseDouble(data.split("-")[0]),
                        defaultValue,
                        e -> ConsoleOutput.getInstance().warn("Could not parse low amount at " + root.getCurrentPath() + "." + path)
                );

                double high = ParseUtil.nullOrDefault(() -> Double.parseDouble(data.split("-")[1]),
                        defaultValue,
                        e -> ConsoleOutput.getInstance().warn("Could not parse high amount at " + root.getCurrentPath() + "." + path)
                );

                return new Amount(low, high);
            }

            // Fixed value syntax
            double fixed = ParseUtil.nullOrDefault(() -> root.getDouble(path),
                    defaultValue,
                    e -> ConsoleOutput.getInstance().warn("Could not parse fixed value amount at " + root.getCurrentPath() + "." + path)
            );
            return new Amount(fixed);
        }
    }

    public int getInt() {
        return fixed ? (int) fixedValue : Math.max(BlockRegen.getInstance().getRandom().nextInt((int) highValue + 1), (int) lowValue);
    }

    public double getDouble() {
        return fixed ? fixedValue : Math.max(BlockRegen.getInstance().getRandom().nextDouble() * highValue, lowValue);
    }

    public String toString() {
        return fixed ? String.valueOf(fixedValue) : lowValue + " - " + highValue;
    }
}