package ru.ckateptb.tablecloth.temporary.fallingblock;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.FallingBlock;
import org.bukkit.metadata.FixedMetadataValue;
import ru.ckateptb.tablecloth.Tablecloth;
import ru.ckateptb.tablecloth.temporary.AbstractTemporary;
import ru.ckateptb.tablecloth.temporary.TemporaryUpdateState;

@Setter
@Getter
public class TemporaryFallingBlock extends AbstractTemporary {
    private final Tablecloth plugin;
    private final Location location;
    private final BlockData blockData;
    private final double maxDistance;
    private final boolean glowing;
    private final boolean gravity;
    private FallingBlock fallingBlock;

    public TemporaryFallingBlock(Location location, BlockData blockData, double maxDistance, boolean glowing, boolean gravity) {
        this.location = location;
        this.blockData = blockData;
        this.maxDistance = maxDistance;
        this.glowing = glowing;
        this.gravity = gravity;
        this.plugin = Tablecloth.getInstance();
        World world = location.getWorld();
        if (world == null) return;
        this.fallingBlock = world.spawnFallingBlock(location, blockData);
        this.fallingBlock.setDropItem(false);
        this.fallingBlock.setHurtEntities(false);
        this.fallingBlock.setTicksLived(100);
        this.fallingBlock.setGravity(gravity);
        this.fallingBlock.setGlowing(glowing);
        this.register();
    }


    @Override
    public void init() {
        this.fallingBlock.setMetadata("tablecloth:fallingblock", new FixedMetadataValue(this.plugin, this));
    }

    @Override
    public TemporaryUpdateState update() {
        return maxDistance > 0 && location.distance(this.fallingBlock.getLocation()) > maxDistance ? TemporaryUpdateState.REVERT : TemporaryUpdateState.CONTINUE;
    }

    @Override
    public void revert() {
        this.fallingBlock.removeMetadata("tablecloth:fallingblock", this.plugin);
        this.fallingBlock.remove();
    }
}
