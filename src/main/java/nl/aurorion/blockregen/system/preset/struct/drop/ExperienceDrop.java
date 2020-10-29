package nl.aurorion.blockregen.system.preset.struct.drop;

import lombok.Data;
import lombok.NoArgsConstructor;
import nl.aurorion.blockregen.system.preset.struct.Amount;

@Data
@NoArgsConstructor
public class ExperienceDrop {

    private boolean dropNaturally = true;

    private Amount amount = new Amount(1);
}