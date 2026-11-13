package com.notunanancyowen.enraged.mixin;

import com.notunanancyowen.enraged.Enrageable;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ElderGuardian.class)
public abstract class ElderGuardianMixin extends Guardian {
    ElderGuardianMixin(EntityType<? extends Guardian> entityType, Level level) {
        super(entityType, level);
    }
    @Inject(method = "getAttackDuration", at = @At("TAIL"), cancellable = true)
    private void chargeFasterWhenEnraged(CallbackInfoReturnable<Integer> cir) {
        if(this instanceof Enrageable e && getActiveAttackTarget() != null && e.isAngryAt(getActiveAttackTarget())) cir.setReturnValue(cir.getReturnValue() / 2);
    }
}
