package org.lovetropics.peekaboo.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.lovetropics.peekaboo.diguise.Disguise;
import org.lovetropics.peekaboo.diguise.EntityDisguiseHolder;
import org.lovetropics.peekaboo.diguise.TypedEntityData;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.Nullable;
import java.util.Optional;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity {
    private PlayerMixin(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public boolean canBeCollidedWith(@Nullable Entity entity) {
        Disguise disguise = EntityDisguiseHolder.getDisguise((Player) (Object) this);
        Optional<TypedEntityData> disguiseEntity = disguise.entity();
        if (disguiseEntity.isPresent() && disguiseEntity.get().type() == EntityType.FALLING_BLOCK) {
            return true;
        }
        return super.canBeCollidedWith(entity);
    }
}
