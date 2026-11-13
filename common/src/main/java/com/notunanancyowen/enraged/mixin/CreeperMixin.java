package com.notunanancyowen.enraged.mixin;

import com.notunanancyowen.enraged.Enrageable;
import com.notunanancyowen.enraged.EnragedMobs;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(Creeper.class)
public abstract class CreeperMixin extends Monster implements Enrageable {
    @Shadow public abstract boolean isIgnited();
    @Shadow public abstract void ignite();
    @Unique private int edgedCounter;
    @Unique private int remainingPersistentAngerTime;
    @Unique @Nullable private UUID persistentAngerTarget;
    CreeperMixin(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }
    @Inject(method = "setSwellDir", at = @At("TAIL"))
    private void enrageIfEdgedTwice(int i, CallbackInfo ci) {
        if(!isIgnited() && getTarget() != null) {
            if((i > 0 && edgedCounter % 2 == 0) || (i < 0 && edgedCounter % 2 != 0)) edgedCounter++;
            if(!isAngry() && edgedCounter > 3) startPersistentAngerTimer();
            else if(isAngry() && edgedCounter > 4) ignite();
        }
    }
    @Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
    private void forceTargetToPreventAccidentalGriefing(@Nullable LivingEntity arg, CallbackInfo ci) {
        //forces the creeper to lock in to the target they are angry at to prevent accidental griefing in multiplayer
        if(isAngry() && level() instanceof ServerLevel s && s.getEntity(getPersistentAngerTarget()) instanceof LivingEntity l) {
            super.setTarget(l);
            ci.cancel();
        }
        if(arg == null) edgedCounter = 0;
    }
    @Override public void startPersistentAngerTimer() {
        if(getRemainingPersistentAngerTime() == -1) return; //vanilla treats -1 as permanent anger, this is solely done for compatibility purposes
        setRemainingPersistentAngerTime(level().getGameRules().getBoolean(EnragedMobs.PERMANENT_ENRAGE) ? Integer.MAX_VALUE : 100);
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
}
