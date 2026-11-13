package com.notunanancyowen.enraged.mixin;

import com.notunanancyowen.enraged.Enrageable;
import com.notunanancyowen.enraged.EnragedMobs;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraft.world.entity.projectile.windcharge.BreezeWindCharge;
import net.minecraft.world.entity.projectile.windcharge.WindCharge;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;
import java.util.function.Function;

@Mixin(Breeze.class)
public abstract class BreezeMixin extends Monster implements Enrageable {
    @Unique private int remainingPersistentAngerTime;
    @Unique @Nullable private UUID persistentAngerTarget;
    BreezeMixin(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }
    @Inject(method = "customServerAiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/Brain;tick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;)V"))
    private void attackTwiceAsFast(CallbackInfo ci) {
        if(isAngry() && getPose() == Pose.INHALING) ((Brain<Breeze>)getBrain()).tick((ServerLevel)level(), (Breeze)(Monster)this);
    }
    @Override @Nullable public Function<DamageSource, Boolean> enrageOnHurtCondition() {
        return damageSource -> !damageSource.isCreativePlayer() && damageSource.getEntity() instanceof LivingEntity l1 && getTarget() != null && damageSource.getDirectEntity() instanceof BreezeWindCharge b && b.getOwner() instanceof LivingEntity l2 && getTarget() instanceof LivingEntity l3 && l1.getId() == l2.getId() && l2.getId() == l3.getId() && l1.getId() == l3.getId() && l3.distanceToSqr(position()) > 36.0;
    }
    @Override public void enrageDeathEffect(boolean despawned, boolean initialDeath) {
        if(!despawned && initialDeath) level().explode(this, null, BreezeWindCharge.EXPLOSION_DAMAGE_CALCULATOR, getX(), getY(0.5), getZ(), 3.0F, false, Level.ExplosionInteraction.TRIGGER, ParticleTypes.GUST_EMITTER_SMALL, ParticleTypes.GUST_EMITTER_LARGE, SoundEvents.BREEZE_WIND_CHARGE_BURST);
    }
    @Override public void startPersistentAngerTimer() {
        if(getRemainingPersistentAngerTime() == -1) return; //vanilla treats -1 as permanent anger, this is solely done for compatibility purposes
        setRemainingPersistentAngerTime(level().getGameRules().getBoolean(EnragedMobs.PERMANENT_ENRAGE) ? Integer.MAX_VALUE : 300);
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
        return ParticleTypes.SMALL_GUST;
    }
}
