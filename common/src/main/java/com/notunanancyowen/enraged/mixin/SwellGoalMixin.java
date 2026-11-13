package com.notunanancyowen.enraged.mixin;

import com.notunanancyowen.enraged.Enrageable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.SwellGoal;
import net.minecraft.world.entity.monster.Creeper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SwellGoal.class)
public abstract class SwellGoalMixin {
    @Shadow @Final private Creeper creeper;
    @Shadow private LivingEntity target;
    @Inject(method = "tick", at = @At("TAIL"))
    private void chaseTargetWhileDetonating(CallbackInfo ci) {
        if(target != null && creeper instanceof Enrageable e && e.isAngry() && creeper.getSwellDir() > 0) {
            creeper.getNavigation().moveTo(target, 0.9);
            creeper.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }
    }
}
