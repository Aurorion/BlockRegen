package nl.aurorion.blockregen.system.preset.struct.drop;

import lombok.Data;
import lombok.NoArgsConstructor;
import nl.aurorion.blockregen.system.preset.struct.Amount;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

@Data
@NoArgsConstructor
public class ExperienceDrop {

    private boolean dropNaturally = true;

    private Amount amount = new Amount(1);

    @Nullable
    public static ExperienceDrop load(@Nullable ConfigurationSection section) {

        if (section == null)
            return null;

        ExperienceDrop drop = new ExperienceDrop();
        drop.setAmount(Amount.load(section, "amount", 0));
        drop.setDropNaturally(section.getBoolean("drop-naturally", false));
        return drop;
    }
}