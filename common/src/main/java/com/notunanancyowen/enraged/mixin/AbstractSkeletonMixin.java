package com.notunanancyowen.enraged.mixin;

import com.notunanancyowen.enraged.Enrageable;
import com.notunanancyowen.enraged.EnragedMobs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;
import java.util.function.Function;

@Mixin(AbstractSkeleton.class)
public abstract class AbstractSkeletonMixin extends Monster implements Enrageable {
    @Unique private int remainingPersistentAngerTime;
    @Unique @Nullable private UUID persistentAngerTarget;
    @Unique private static final AttributeModifier FOLLOW_RANGE_MODIFIER_ENRAGED = new AttributeModifier(EnragedMobs.MOD_ID + ":enraged", 8.0, AttributeModifier.Operation.ADDITION);
    AbstractSkeletonMixin(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }
    @Shadow @Final private RangedBowAttackGoal<AbstractSkeleton> bowGoal;
    @Inject(method = "performRangedAttack", at = @At("TAIL"))
    private void handleEnragedBehavior(LivingEntity livingEntity, float f, CallbackInfo ci) {
        if(livingEntity != null && isAngry() && isAngryAt(livingEntity)) {
            boolean handsRaised = isAggressive();
            bowGoal.stop();
            if(handsRaised) setAggressive(getTarget() != null);
            bowGoal.setMinAttackInterval(0);
            getMoveControl().strafe(1F, 0F);
        }
    }
    @Override @Nullable public Function<DamageSource, Boolean> enrageOnHurtCondition() {
        return damageSource -> !damageSource.isCreativePlayer() && damageSource.is(DamageTypeTags.IS_PROJECTILE) && damageSource.getEntity() instanceof LivingEntity l && l.distanceToSqr(position()) > 225.0;
    }
    @Override public void startPersistentAngerTimer() {
        AttributeInstance attributeInstance = getAttribute(Attributes.FOLLOW_RANGE);
        if(attributeInstance != null && !attributeInstance.hasModifier(FOLLOW_RANGE_MODIFIER_ENRAGED)) attributeInstance.addTransientModifier(FOLLOW_RANGE_MODIFIER_ENRAGED);
        bowGoal.setMinAttackInterval(0);
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
        try {
            Enrageable.super.stopBeingAngry();
        }
        catch (Throwable ignore) {
            setPersistentAngerTarget(null);
            setRemainingPersistentAngerTime(0);
        }
        if(level() instanceof ServerLevel s) s.sendParticles(enrageParticleEnd(), getX(), getEyeY(), getZ(), 10, 0.2, 0.2, 0.2, 0.15);
        AttributeInstance attributeInstance = getAttribute(Attributes.FOLLOW_RANGE);
        if(attributeInstance != null) attributeInstance.removeModifier(FOLLOW_RANGE_MODIFIER_ENRAGED);
        bowGoal.setMinAttackInterval(level().getDifficulty() == Difficulty.HARD ? 20 : 40);
    }
}
