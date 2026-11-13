package com.notunanancyowen.enraged.mixin;

import com.notunanancyowen.enraged.Enrageable;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WitherSkeleton.class)
public abstract class WitherSkeletonMixin extends AbstractSkeleton {
    WitherSkeletonMixin(EntityType<? extends AbstractSkeleton> entityType, Level level) {
        super(entityType, level);
    }
    @Inject(method = "registerGoals", at = @At("HEAD"))
    private void enragedBehavior(CallbackInfo ci) {
        goalSelector.addGoal(3, new Goal() {
            private int attackTimer;
            private WitherSkull skull; //bounding box fix so the guy doesn't shoot itself while chasing
            @Override public boolean canUse() {
                if(skull != null) return true;
                if(WitherSkeletonMixin.this instanceof Enrageable e && e.isAngry() && getTarget() != null) return !isUsingItem() && goalSelector.getAvailableGoals().stream().anyMatch(g -> g.getGoal() instanceof MeleeAttackGoal);
                return false;
            }
            @Override public boolean requiresUpdateEveryTick() {
                return true;
            }
            @Override public void tick() {
                LivingEntity l = getTarget();
                if(l != null) {
                    if(!swinging && ++attackTimer > (level().getDifficulty() == Difficulty.HARD ? 20 : 40) * 2) {
                        attackTimer = 0;
                        var velocity = l.getEyePosition().subtract(getEyePosition()).normalize();
                        skull = new WitherSkull(level(), WitherSkeletonMixin.this, velocity.x(), velocity.y(), velocity.z());
                        skull.setPos(getEyePosition().subtract(0, 0.4, 0));
                        level().addFreshEntity(skull);
                        if(!isSilent()) level().playSound(WitherSkeletonMixin.this, blockPosition(), SoundEvents.WITHER_SHOOT, getSoundSource(), 1F, 1F);
                        swing(InteractionHand.OFF_HAND);
                        yBodyRot = getYRot();
                    }
                    if(getNavigation().isStuck()) getNavigation().moveTo(l, 1.0D);
                    setAggressive(true);
                    getLookControl().setLookAt(l, 30.0F, 30.0F);
                }
                else setAggressive(false);
                //skulls don't insta rotate to where they are shooting in 1.21.1, this causes the skeleton to hit itself at certain angles
                //Mojang spaghetti code checks where the skull is looking rather than where it is moving for hit prediction
                //Mob AI Tweaks fixes this by embedding this patch inside the WitherSkull code
                if(skull != null && skull.getBoundingBox().intersects(getBoundingBox())) {
                    ProjectileUtil.rotateTowardsMovement(skull, 1F);
                    if(isDeadOrDying()) skull.discard(); //remove if spawned as the guy is dying to prevent cheap hits
                }
                else skull = null;
            }
            @Override public void stop() {
                skull = null;
                setAggressive(getTarget() != null);
            }
        });
    }
}
