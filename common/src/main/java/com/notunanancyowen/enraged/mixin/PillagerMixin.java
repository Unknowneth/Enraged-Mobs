package com.notunanancyowen.enraged.mixin;

import com.notunanancyowen.enraged.Enrageable;
import com.notunanancyowen.enraged.EnragedMobs;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(Pillager.class)
public abstract class PillagerMixin extends AbstractIllager implements Enrageable {
    @Shadow public abstract boolean isChargingCrossbow();
    @Unique private boolean swapToMelee;
    @Unique private int blockedArrowsCounter;
    @Unique private int remainingPersistentAngerTime;
    @Unique @Nullable private UUID persistentAngerTarget;
    @Unique private void reassessWeaponGoal() {
        swapToMelee = goalSelector.getAvailableGoals().stream().noneMatch(g -> g.getGoal() instanceof MeleeAttackGoal);
        if(!getUseItem().isEmpty() && getUseItem().getItem() instanceof CrossbowItem) {
            setItemInHand(getUsedItemHand(), Items.STONE_AXE.getDefaultInstance());
            stopUsingItem();
        }
        else if(getMainHandItem().getItem() instanceof CrossbowItem) setItemInHand(InteractionHand.MAIN_HAND, Items.STONE_AXE.getDefaultInstance());
        else if(getOffhandItem().getItem() instanceof CrossbowItem) setItemInHand(InteractionHand.OFF_HAND, Items.STONE_AXE.getDefaultInstance());
    }
    PillagerMixin(EntityType<? extends AbstractIllager> entityType, Level level) {
        super(entityType, level);
    }
    @Inject(method = "performRangedAttack", at = @At("TAIL"))
    private void countHowManyArrowsWereBlockedThenEnrage(LivingEntity livingEntity, float f, CallbackInfo ci) {
        if(livingEntity.isBlocking()) if(blockedArrowsCounter < 5) blockedArrowsCounter++;
        else startPersistentAngerTimer();
    }
    @Inject(method = "onCrossbowAttackPerformed", at = @At("TAIL"))
    private void enrageAfterSomeTime(CallbackInfo ci) {
        if(isAngry() && !isChargingCrossbow()) reassessWeaponGoal();
    }
    @Inject(method = "setChargingCrossbow", at = @At("HEAD"), cancellable = true)
    private void enrageAfterSomeTime(boolean charging, CallbackInfo ci) {
        if(isAngry() && charging) {
            reassessWeaponGoal();
            ci.cancel();
        }
    }
    @Override public void readPersistentAngerSaveData(Level level, CompoundTag compoundTag) {
        try {
            Enrageable.super.readPersistentAngerSaveData(level, compoundTag);
        }
        catch(Throwable ignore) {
            this.setRemainingPersistentAngerTime(compoundTag.getInt("AngerTime"));
            if(level instanceof ServerLevel) {
                if(!compoundTag.hasUUID("AngryAt")) setPersistentAngerTarget(null);
                else {
                    UUID uUID = compoundTag.getUUID("AngryAt");
                    this.setPersistentAngerTarget(uUID);
                    Entity entity = ((ServerLevel)level).getEntity(uUID);
                    if(entity instanceof Mob mob) setLastHurtByMob(mob);
                    if(entity instanceof Player player) setLastHurtByPlayer(player);
                }
            }
        }
        if(!getMainHandItem().isEmpty() && !(getMainHandItem().getItem() instanceof CrossbowItem) && !getOffhandItem().isEmpty() && !(getOffhandItem().getItem() instanceof CrossbowItem)) reassessWeaponGoal();
    }
    @Override public void updatePersistentAnger(ServerLevel serverLevel, boolean bl) {
        try {
            Enrageable.super.updatePersistentAnger(serverLevel, bl);
        }
        catch(Throwable ignore) {
            LivingEntity livingEntity = getTarget();
            UUID uUID = getPersistentAngerTarget();
            if((livingEntity == null || livingEntity.isDeadOrDying()) && uUID != null && serverLevel.getEntity(uUID) instanceof Mob) stopBeingAngry();
            else if(getRemainingPersistentAngerTime() > 0) {
                setRemainingPersistentAngerTime(this.getRemainingPersistentAngerTime() - 1);
                if(getRemainingPersistentAngerTime() == 0) stopBeingAngry();
            }
        }
        if(swapToMelee) {
            goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, true));
            swapToMelee = false;
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
        try {
            Enrageable.super.stopBeingAngry();
        }
        catch (Throwable ignore) {
            setPersistentAngerTarget(null);
            setRemainingPersistentAngerTime(0);
        }
        if(level() instanceof ServerLevel s) s.sendParticles(enrageParticleEnd(), getX(), getEyeY(), getZ(), 10, 0.2, 0.2, 0.2, 0.15);
    }
}
