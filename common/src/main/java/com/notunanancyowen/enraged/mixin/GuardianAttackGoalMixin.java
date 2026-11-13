package com.notunanancyowen.enraged.mixin;

import com.notunanancyowen.enraged.Enrageable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.world.entity.monster.Guardian$GuardianAttackGoal")
public abstract class GuardianAttackGoalMixin {
    @Shadow @Final private Guardian guardian;
    @Shadow @Final private boolean elder;
    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private void enragedExplosion(CallbackInfo ci) {
        if(guardian instanceof Enrageable e && guardian.getTarget() instanceof LivingEntity l && e.isAngryAt(l)) guardian.level().explode(guardian, l.getX(), l.getY(0.5), l.getZ(), elder ? 3F : 1F, Level.ExplosionInteraction.MOB);
    }
}
