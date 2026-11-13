package com.notunanancyowen.enraged.mixin;

import com.notunanancyowen.enraged.Enrageable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class MobMixin extends LivingEntity {
    MobMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }
    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void readEnrageFromNBT(CompoundTag compoundTag, CallbackInfo ci) {
        if(this instanceof Enrageable e) e.readPersistentAngerSaveData(level(), compoundTag);
    }
    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void addEnrageToNBT(CompoundTag compoundTag, CallbackInfo ci) {
        if(this instanceof Enrageable e) e.addPersistentAngerSaveData(compoundTag);
    }
    @Inject(method = "aiStep", at = @At("TAIL"))
    private void handleEnrageEffects(CallbackInfo ci) {
        if(this instanceof Enrageable e && level() instanceof ServerLevel s) {
            e.updatePersistentAnger(s, true);
            if(e.isAngry() && e.getRemainingPersistentAngerTime() % 5 == 0) s.sendParticles(e.enrageParticlePassive(), getX(), getEyeY(), getZ(), 1, 0.2, 0.2, 0.2, 0.1);
        }
    }
}
