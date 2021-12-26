package ru.ckateptb.tablecloth.temporary.block;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import ru.ckateptb.tablecloth.temporary.AbstractTemporary;
import ru.ckateptb.tablecloth.temporary.TemporaryUpdateState;

import java.util.*;

@Getter
@Setter
public class TemporaryBlock extends AbstractTemporary {
    private static final Map<Location, LinkedList<TemporaryBlock>> instances = new HashMap<>();
    private final List<TemporaryBlock> attached = new ArrayList<>();
    private final Location location;
    private final BlockData blockData;
    private final long duration;
    private final long startTime;
    private BlockState original;
    public TemporaryBlock(Location location, BlockData blockData, long duration) {
        this.location = location;
        this.blockData = blockData;
        this.duration = duration;
        this.startTime = System.currentTimeMillis();
        this.original = location.getBlock().getState();
        instances.computeIfAbsent(location, key -> new LinkedList<>()).add(this);
        this.register();
    }

    public static boolean isTemporaryBlock(Location location) {
        return instances.containsKey(location) && instances.get(location).size() > 0;
    }

    public static Map<Location, LinkedList<TemporaryBlock>> getTemporaryBlocks() {
        return new HashMap<>(instances);
    }

    public static List<TemporaryBlock> getTemporaryBlocks(Location location) {
        return new LinkedList<>(instances.computeIfAbsent(location, key -> new LinkedList<>()));
    }

    @SneakyThrows
    @Override
    public void init() {
        location.getBlock().setBlockData(blockData, false);
    }

    @Override
    public TemporaryUpdateState update() {
        if (System.currentTimeMillis() >= this.startTime + duration) {
            if (instances.get(location).getLast() == this) {
                return TemporaryUpdateState.REVERT;
            }
        }
        return TemporaryUpdateState.CONTINUE;
    }

    @Override
    public void setRevertTime(long revertTime) {
    }

    @Override
    public void revert() {
        LinkedList<TemporaryBlock> temporaryBlocks = instances.get(location);
        if (temporaryBlocks.getLast() != this) {
            TemporaryBlock nextTemporaryBlock = temporaryBlocks.get(temporaryBlocks.indexOf(this) + 1);
            nextTemporaryBlock.setOriginal(this.original);
        } else {
            this.location.getBlock().setBlockData(original.getBlockData(), false);
            this.original.update();
        }
        temporaryBlocks.remove(this);
    }
}

