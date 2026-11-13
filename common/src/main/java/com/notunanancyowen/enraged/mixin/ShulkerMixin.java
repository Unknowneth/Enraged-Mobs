package com.notunanancyowen.enraged.mixin;

import com.notunanancyowen.enraged.Enrageable;
import com.notunanancyowen.enraged.EnragedMobs;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(Shulker.class)
public abstract class ShulkerMixin extends AbstractGolem implements Enrageable {
    @Unique private int teleportCounter;
    @Unique private int remainingPersistentAngerTime;
    @Unique @Nullable private UUID persistentAngerTarget;
    ShulkerMixin(EntityType<? extends AbstractGolem> entityType, Level level) {
        super(entityType, level);
    }
    @Inject(method = "tick", at = @At("TAIL"))
    private void shootFasterWhenEnraged(CallbackInfo ci) {
        if(isAngry()) goalSelector.getAvailableGoals().stream().filter(goal -> goal.getPriority() == 4).forEach(WrappedGoal::tick);
    }
    @Inject(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Shulker;teleportSomewhere()Z"))
    private void enrageOnEnoughTeleports(DamageSource damageSource, float f, CallbackInfoReturnable<Boolean> cir) {
        if(!damageSource.isCreativePlayer() && damageSource.getEntity() instanceof LivingEntity l && !isAngryAt(l) && ++teleportCounter > 2) {
            setTarget(l);
            setPersistentAngerTarget(l.getUUID());
            startPersistentAngerTimer();
            teleportCounter = 0;
        }
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
        teleportCounter = 0;
        if(level() instanceof ServerLevel s) s.sendParticles(enrageParticleEnd(), getX(), getEyeY(), getZ(), 10, 0.2, 0.2, 0.2, 0.15);
    }
    @Override @NotNull public ParticleOptions enrageParticlePassive() {
        return ParticleTypes.END_ROD;
    }
}
