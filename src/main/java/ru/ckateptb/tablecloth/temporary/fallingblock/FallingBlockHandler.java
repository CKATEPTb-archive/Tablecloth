package ru.ckateptb.tablecloth.temporary.fallingblock;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.springframework.stereotype.Component;
import ru.ckateptb.tablecloth.temporary.Temporary;
import ru.ckateptb.tablecloth.temporary.TemporaryService;

@Component
public class FallingBlockHandler implements Listener {
    private final TemporaryService temporaryService;

    public FallingBlockHandler(TemporaryService temporaryService) {
        this.temporaryService = temporaryService;
    }

    @EventHandler
    public void on(EntityChangeBlockEvent event) {
        if (event.getEntityType() == EntityType.FALLING_BLOCK) {
            FallingBlock fallingBlock = (FallingBlock) event.getEntity();
            if (fallingBlock.hasMetadata("tablecloth:fallingblock")) {
                fallingBlock.getMetadata("tablecloth:fallingblock").forEach(metadataValue -> {
                    if (metadataValue.value() instanceof Temporary temporary) {
                        event.setCancelled(true);
                        temporaryService.revert(temporary);
                    }
                });
            }
        }
    }

}
