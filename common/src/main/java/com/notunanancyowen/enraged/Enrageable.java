package com.notunanancyowen.enraged;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Function;

public interface Enrageable extends NeutralMob {
    @NotNull default ParticleOptions enrageParticleTrigger() {
        return ParticleTypes.ANGRY_VILLAGER;
    }
    @NotNull default ParticleOptions enrageParticlePassive() {
        return ParticleTypes.SMOKE;
    }
    @NotNull default ParticleOptions enrageParticleEnd() {
        return ParticleTypes.HAPPY_VILLAGER;
    }
    @Nullable default Function<DamageSource, Boolean> enrageOnHurtCondition() {
        return null;
    }
    default void enrageDeathEffect(boolean despawned, boolean initialDeath) {
    }
    default void updatePersistentAnger(ServerLevel serverLevel, boolean bl) {
        LivingEntity livingEntity = getTarget();
        UUID uUID = getPersistentAngerTarget();
        if((livingEntity == null || livingEntity.isDeadOrDying()) && uUID != null && serverLevel.getEntity(uUID) instanceof Mob) stopBeingAngry();
        else if(getRemainingPersistentAngerTime() > 0) {
            setRemainingPersistentAngerTime(this.getRemainingPersistentAngerTime() - 1);
            if(getRemainingPersistentAngerTime() == 0) stopBeingAngry();
        }
    }
}
