package com.notunanancyowen.enraged.mixin;

import com.notunanancyowen.enraged.Enrageable;
import com.notunanancyowen.enraged.EnragedMobs;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;
import java.util.function.Function;

@Mixin(Guardian.class)
public abstract class GuardianMixin extends Monster implements Enrageable {
    @Unique private int remainingPersistentAngerTime;
    @Unique @Nullable private UUID persistentAngerTarget;
    @Shadow @Nullable public abstract LivingEntity getActiveAttackTarget();
    GuardianMixin(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }
    @Override @Nullable public Function<DamageSource, Boolean> enrageOnHurtCondition() {
        return damageSource -> !damageSource.isCreativePlayer() && getTarget() != null && damageSource.is(DamageTypeTags.IS_PROJECTILE) && damageSource.getEntity() instanceof LivingEntity l && l.distanceToSqr(position()) > 36.0;
    }
    @Inject(method = "getAttackDuration", at = @At("TAIL"), cancellable = true)
    private void chargeFasterWhenEnraged(CallbackInfoReturnable<Integer> cir) {
        if(getActiveAttackTarget() != null && isAngryAt(getActiveAttackTarget())) cir.setReturnValue(cir.getReturnValue() / 2);
    }
    @Override public void startPersistentAngerTimer() {
        if(getRemainingPersistentAngerTime() == -1) return; //vanilla treats -1 as permanent anger, this is solely done for compatibility purposes
        setRemainingPersistentAngerTime(level().getGameRules().getBoolean(EnragedMobs.PERMANENT_ENRAGE) ? Integer.MAX_VALUE : 900);
        if(level() instanceof ServerLevel s) {
            if(s.getEntity(getPersistentAngerTarget()) instanceof LivingEntity l) setTarget(l);
            s.sendParticles(enrageParticleTrigger(), getX(), getEyeY(), getZ(), 5, 0.25, 0.25, 0.25, 0.15);
        }
    }
    @Override public void setRemainingPersistentAngerTime(int i) {
        remainingPersistentAngerTime = i;
    }
    @Override public int getRemainingPersistentAngerTime() {
        return remainingPersistentAngerTime;
    }
    @Override public void setPersistentAngerTarget(@Nullable UUID uUID) {
        persistentAngerTarget = uUID;
    }
    @Override @Nullable public UUID getPersistentAngerTarget() {
        return persistentAngerTarget;
    }
    @Override public void stopBeingAngry() {
        Enrageable.super.stopBeingAngry();
        if(level() instanceof ServerLevel s) s.sendParticles(enrageParticleEnd(), getX(), getEyeY(), getZ(), 10, 0.2, 0.2, 0.2, 0.15);
    }
    @Override @NotNull public ParticleOptions enrageParticlePassive() {
        return ParticleTypes.BUBBLE;
    }
}
