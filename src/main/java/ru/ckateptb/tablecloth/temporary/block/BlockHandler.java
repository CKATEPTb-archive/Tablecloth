package ru.ckateptb.tablecloth.temporary.block;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import ru.ckateptb.tablecloth.ioc.annotation.Component;

@Component
public class BlockHandler implements Listener {
    @EventHandler
    public void on(BlockGrowEvent event) {
        if (TemporaryBlock.isTemporaryBlock(event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void on(BlockPhysicsEvent event) {
        Block block = event.getBlock();
        BlockFace[] relatives = new BlockFace[]{BlockFace.SELF, BlockFace.DOWN};
        for (BlockFace relative : relatives) {
            if (TemporaryBlock.isTemporaryBlock(block.getRelative(relative).getLocation())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void on(BlockPlaceEvent event) {
        if (TemporaryBlock.isTemporaryBlock(event.getBlock().getLocation())) {
            event.setCancelled(true);
            event.getBlock().setBlockData(event.getBlockPlaced().getBlockData());
        }
    }

    @EventHandler
    public void on(BlockBreakEvent event) {
        if (TemporaryBlock.isTemporaryBlock(event.getBlock().getLocation())) {
            event.setDropItems(false);
        }
    }

    @EventHandler
    public void on(BlockFromToEvent event) {
        if (TemporaryBlock.isTemporaryBlock(event.getBlock().getLocation()) || TemporaryBlock.isTemporaryBlock(event.getToBlock().getLocation())) {
            event.setCancelled(true);
        }
    }
}
