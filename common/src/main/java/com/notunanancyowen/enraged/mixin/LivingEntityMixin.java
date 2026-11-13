package com.notunanancyowen.enraged.mixin;

import com.notunanancyowen.enraged.Enrageable;
import com.notunanancyowen.enraged.EnragedMobs;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(method = "hurt", at = @At("TAIL"))
    private void enrageOnHurt(DamageSource damageSource, float amount, CallbackInfoReturnable<Boolean> cir) {
        if(cir.getReturnValue() && this instanceof Enrageable e) if(!damageSource.isCreativePlayer() && damageSource.getEntity() instanceof LivingEntity l) if(e.isAngryAt(l)) e.startPersistentAngerTimer();
        else if(e.enrageOnHurtCondition() != null && e.enrageOnHurtCondition().apply(damageSource)) {
            e.startPersistentAngerTimer();
            e.setPersistentAngerTarget(l.getUUID());
        }
        else if(EnragedMobs.canGoBerserk((LivingEntity)(Object)this)) {
            e.startPersistentAngerTimer();
            e.setPersistentAngerTarget(l.getUUID());
        }
    }
    @Inject(method = "die", at = @At("HEAD"))
    private void handleInitialDeathForEnrageMechanics(DamageSource damageSource, CallbackInfo ci) {
        if(this instanceof Enrageable e && e.isAngry()) e.enrageDeathEffect(false, true);
    }
    @Inject(method = "remove", at = @At("HEAD"))
    private void calmDownOnDeath(Entity.RemovalReason removalReason, CallbackInfo ci) {
        if(this instanceof Enrageable e && e.isAngry()) e.enrageDeathEffect(removalReason != Entity.RemovalReason.KILLED, false);
    }
}
