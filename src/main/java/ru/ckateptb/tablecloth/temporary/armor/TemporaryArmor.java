package ru.ckateptb.tablecloth.temporary.armor;

import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import ru.ckateptb.tablecloth.Tablecloth;
import ru.ckateptb.tablecloth.ioc.IoC;
import ru.ckateptb.tablecloth.temporary.AbstractTemporary;
import ru.ckateptb.tablecloth.temporary.Temporary;
import ru.ckateptb.tablecloth.temporary.TemporaryService;
import ru.ckateptb.tablecloth.temporary.TemporaryUpdateState;

import java.util.List;

public class TemporaryArmor extends AbstractTemporary {
    private final TemporaryService temporaryService;
    private final Tablecloth plugin;

    private final LivingEntity livingEntity;
    private final ItemStack helmet;
    private final ItemStack chestplate;
    private final ItemStack leggings;
    private final ItemStack boots;
    private ItemStack[] origin;

    public TemporaryArmor(LivingEntity livingEntity, ItemStack helmet, ItemStack chestplate, ItemStack leggings, ItemStack boots, long duration) {
        this.livingEntity = livingEntity;
        this.helmet = helmet;
        this.chestplate = chestplate;
        this.leggings = leggings;
        this.boots = boots;
        this.temporaryService = IoC.get(TemporaryService.class);
        this.plugin = Tablecloth.getInstance();
        if (this.livingEntity.hasMetadata("tablecloth:armor")) {
            List.copyOf(this.livingEntity.getMetadata("tablecloth:armor")).forEach(metadataValue -> {
                if (metadataValue.value() instanceof Temporary temporary) {
                    temporaryService.revert(temporary);
                }
            });
        }
        if (duration > 0) {
            this.setRevertTime(System.currentTimeMillis() + duration);
        }
        this.register();
    }


    @Override
    public void init() {
        this.livingEntity.setMetadata("tablecloth:armor", new FixedMetadataValue(this.plugin, this));
        EntityEquipment equipment = livingEntity.getEquipment();
        if (equipment == null) return;
        this.origin = equipment.getArmorContents();
        if (helmet != null) equipment.setHelmet(helmet);
        if (chestplate != null) equipment.setChestplate(chestplate);
        if (leggings != null) equipment.setLeggings(leggings);
        if (boots != null) equipment.setBoots(boots);
    }

    @Override
    public TemporaryUpdateState update() {
        return TemporaryUpdateState.CONTINUE;
    }

    @Override
    public void revert() {
        if (livingEntity.hasMetadata("tablecloth:armor")) livingEntity.removeMetadata("tablecloth:armor", plugin);
        EntityEquipment equipment = livingEntity.getEquipment();
        if (equipment == null || this.origin == null) return;
        equipment.setArmorContents(origin);
    }
}
