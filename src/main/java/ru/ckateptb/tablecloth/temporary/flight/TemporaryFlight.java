package ru.ckateptb.tablecloth.temporary.flight;

import lombok.Getter;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import ru.ckateptb.tablecloth.Tablecloth;
import ru.ckateptb.tablecloth.ioc.IoC;
import ru.ckateptb.tablecloth.temporary.AbstractTemporary;
import ru.ckateptb.tablecloth.temporary.Temporary;
import ru.ckateptb.tablecloth.temporary.TemporaryService;
import ru.ckateptb.tablecloth.temporary.TemporaryUpdateState;
import ru.ckateptb.tablecloth.util.WorldUtils;

import java.util.List;

@Getter
public class TemporaryFlight extends AbstractTemporary {
    private final TemporaryService temporaryService;
    private final Tablecloth plugin;

    private final LivingEntity livingEntity;
    private final boolean preventFallDamage;
    private final boolean revertIfOnGround;
    private final boolean enableFly;
    private boolean notOnGround;
    private boolean originalAllowFlight;
    private boolean originalIsFlying;

    public TemporaryFlight(LivingEntity livingEntity, long duration, boolean preventFallDamage, boolean revertIfOnGround, boolean enableFly) {
        this.temporaryService = IoC.get(TemporaryService.class);
        this.plugin = Tablecloth.getInstance();

        this.livingEntity = livingEntity;
        this.preventFallDamage = preventFallDamage;
        this.revertIfOnGround = revertIfOnGround;
        this.enableFly = enableFly;
        if (duration > 0) {
            this.setRevertTime(System.currentTimeMillis() + duration);
        }
        if (livingEntity.hasMetadata("tablecloth:flight")) {
            List.copyOf(this.livingEntity.getMetadata("tablecloth:flight")).forEach(metadataValue -> {
                if (metadataValue.value() instanceof Temporary temporary) {
                    temporaryService.revert(temporary);
                }
            });
        }
        this.register();
    }

    @Override
    public void init() {
        if (livingEntity instanceof Player player) {
            this.livingEntity.setMetadata("tablecloth:flight", new FixedMetadataValue(this.plugin, this));
            this.originalAllowFlight = player.getAllowFlight();
            this.originalIsFlying = player.isFlying();
            if (this.enableFly) {
                player.setAllowFlight(true);
                player.setFlying(true);
            }
        }
    }

    @Override
    public TemporaryUpdateState update() {
        if (this.revertIfOnGround && notOnGround) {
            if (WorldUtils.isOnGround(this.livingEntity)) {
                return TemporaryUpdateState.REVERT;
            } else if (!notOnGround) notOnGround = true;
        }
        if (this.livingEntity instanceof OfflinePlayer player) {
            if (!player.isOnline()) return TemporaryUpdateState.REVERT;
        }
        return TemporaryUpdateState.CONTINUE;
    }

    @Override
    public void revert() {
        this.livingEntity.removeMetadata("tablecloth:flight", this.plugin);
        if (this.livingEntity instanceof Player player) {
            player.setAllowFlight(originalAllowFlight);
            player.setFlying(originalIsFlying);
        }
    }
}
