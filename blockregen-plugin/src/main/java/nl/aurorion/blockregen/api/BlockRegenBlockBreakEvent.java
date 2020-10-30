package nl.aurorion.blockregen.api;

import lombok.Getter;
import lombok.Setter;
import nl.aurorion.blockregen.system.preset.struct.BlockPreset;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.BlockBreakEvent;

/**
 * Fired after the original BlockBreakEvent.
 * Cancelling this event causes BlockRegen not to do any action after the block is broken. It does not cancel BlockBreakEvent itself.
 */
public class BlockRegenBlockBreakEvent extends BlockRegenBlockEvent implements Cancellable {

    /**
     * Original BLockBreakEvent which caused BlockRegen to take action.
     */
    @Getter
    private final BlockBreakEvent blockBreakEvent;

    @Getter
    @Setter
    private boolean cancelled = false;

    public BlockRegenBlockBreakEvent(BlockBreakEvent blockBreakEvent, BlockPreset blockPreset) {
        super(blockBreakEvent.getBlock(), blockPreset);
        this.blockBreakEvent = blockBreakEvent;
    }
}