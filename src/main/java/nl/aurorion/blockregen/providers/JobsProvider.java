package nl.aurorion.blockregen.providers;

import com.gamingmesh.jobs.Jobs;
import com.gamingmesh.jobs.actions.BlockActionInfo;
import com.gamingmesh.jobs.container.ActionType;
import com.gamingmesh.jobs.container.JobsPlayer;
import lombok.NoArgsConstructor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

@NoArgsConstructor
public class JobsProvider {

    public void triggerBlockBreakAction(Player player, Block block) {
        JobsPlayer jobsPlayer = Jobs.getPlayerManager().getJobsPlayer(player);
        Jobs.action(jobsPlayer, new BlockActionInfo(block, ActionType.BREAK), block);
    }
}