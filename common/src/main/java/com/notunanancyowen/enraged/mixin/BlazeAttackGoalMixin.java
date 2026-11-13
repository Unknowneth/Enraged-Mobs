package com.notunanancyowen.enraged.mixin;

import com.notunanancyowen.enraged.Enrageable;
import net.minecraft.world.entity.monster.Blaze;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.world.entity.monster.Blaze$BlazeAttackGoal")
public abstract class BlazeAttackGoalMixin {
    @Shadow @Final private Blaze blaze;
    @Shadow private int attackTime;
    @Shadow private int attackStep;
    @Inject(method = "tick", at = @At("HEAD"))
    private void attackFasterOnEnrage(CallbackInfo ci) {
        if(blaze instanceof Enrageable e && e.isAngry() && attackStep <= 1) attackTime--;
    }
}
