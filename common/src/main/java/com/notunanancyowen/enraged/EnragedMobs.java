package com.notunanancyowen.enraged;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.GameRules;

public class EnragedMobs {
    public final static String MOD_ID = "enraged_mobs";
    public static GameRules.Key<GameRules.IntegerValue> MOBS_ENRAGE_ON_LOW_HEALTH;
    public static GameRules.Key<GameRules.BooleanValue> PERMANENT_ENRAGE;
    public static void init() {
    }
    public static boolean canGoBerserk(LivingEntity entity) {
        return entity instanceof Enrageable && !entity.isDeadOrDying() && entity.getHealth() / entity.getMaxHealth() * 100F < entity.level().getGameRules().getInt(MOBS_ENRAGE_ON_LOW_HEALTH);
    }
}
