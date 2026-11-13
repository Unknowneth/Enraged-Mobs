package com.notunanancyowen.enraged.mixin;

import com.notunanancyowen.enraged.Enrageable;
import com.notunanancyowen.enraged.EnragedMobs;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.UUID;
import java.util.function.Function;

@Mixin(Blaze.class)
public abstract class BlazeMixin extends Monster implements Enrageable {
    @Unique private int remainingPersistentAngerTime;
    @Unique @Nullable private UUID persistentAngerTarget;
    BlazeMixin(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }
    @Override @Nullable public Function<DamageSource, Boolean> enrageOnHurtCondition() {
        return damageSource -> !damageSource.isCreativePlayer() && getTarget() != null && damageSource.is(DamageTypeTags.IS_PROJECTILE) && damageSource.getEntity() instanceof LivingEntity l && l.distanceToSqr(position()) >= 1024.0;
    }
    @Override public void startPersistentAngerTimer() {
        if(getRemainingPersistentAngerTime() == -1) return; //vanilla treats -1 as permanent anger, this is solely done for compatibility purposes
        setRemainingPersistentAngerTime(level().getGameRules().getBoolean(EnragedMobs.PERMANENT_ENRAGE) ? Integer.MAX_VALUE : 300);
        if(level() instanceof ServerLevel s) {
            if(s.getEntity(getPersistentAngerTarget()) instanceof LivingEntity l) setTarget(l);
            s.sendParticles(enrageParticleTrigger(), getX(), getEyeY(), getZ(), 5, 0.25, 0.25, 0.25, 0.15);
        }
        if(!getPassengers().isEmpty() && getPassengers().stream().anyMatch(e -> e.getTags().contains(EnragedMobs.MOD_ID + ":enraged_visual") && e instanceof Display.BlockDisplay)) return;
        CompoundTag tag;
        String d = "{block_state:{Name:\"minecraft:fire\",Properties:{age:\"15\",east:\"true\",north:\"true\",south:\"true\",up:\"false\",west:\"true\"}},transformation:[0.5f,0f,-0.0008726655f,-0.25f,0f,0.2f,0f,0.3f,0f,0f,0.5f,-0.25f,0f,0f,0f,1f],brightness:{sky:15,block:15}}";
        try {
            tag = TagParser.parseTag(d);
        }
        catch (Throwable ignore) {
            return;
        }
        tag.putString("id", "minecraft:block_display");
        var m = EntityType.loadEntityRecursive(tag, level(), e -> {
            e.moveTo(position().add(0d, 2d, 0d), 0F, 0F);
            e.addTag(EnragedMobs.MOD_ID + ":enraged_visual");
            e.setInvisible(true);
            return e;
        });
        if(m != null) {
            level().addFreshEntity(m);
            m.startRiding(this);
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
        enrageDeathEffect(false, false);
    }
    @Override public void enrageDeathEffect(boolean despawned, boolean initialDeath) {
        if(!getPassengers().isEmpty()) getPassengers().forEach(e -> {
            if(e.getTags().contains(EnragedMobs.MOD_ID + ":enraged_visual") && e instanceof Display.BlockDisplay) e.discard();
        });
        if(despawned && !initialDeath) Enrageable.super.stopBeingAngry(); //since these guys have a very far despawn distance, make sure to properly disable enrage when despawning
    }
    @Override @NotNull public ParticleOptions enrageParticleTrigger() {
        return ParticleTypes.SMALL_FLAME;
    }
    @Override @NotNull public ParticleOptions enrageParticlePassive() {
        return isOnFire() ? ParticleTypes.FLAME : Enrageable.super.enrageParticlePassive();
    }
    @Override @NotNull public ParticleOptions enrageParticleEnd() {
        return ParticleTypes.LARGE_SMOKE;
    }
}
