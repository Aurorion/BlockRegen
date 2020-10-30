package nl.aurorion.blockregen.api;

import lombok.Getter;
import nl.aurorion.blockregen.system.preset.struct.BlockPreset;
import org.bukkit.block.Block;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Base class for Events regarding BlockRegenBlock actions.
 */
public class BlockRegenBlockEvent extends BlockEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    @Getter
    private final BlockPreset blockPreset;

    public BlockRegenBlockEvent(Block block, BlockPreset blockPreset) {
        super(block);
        this.blockPreset = blockPreset;
    }
}