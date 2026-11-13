package com.notunanancyowen.enraged.fabric;

import com.notunanancyowen.enraged.EnragedMobs;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.level.GameRules;

public final class EnragedMobsFabric implements ModInitializer {
    @Override public void onInitialize() {
        EnragedMobs.init();
        EnragedMobs.MOBS_ENRAGE_ON_LOW_HEALTH = GameRuleRegistry.register("mobEnrageOnHealthPercent", GameRules.Category.MOBS, GameRuleFactory.createIntRule(0, 0, 100));
        EnragedMobs.PERMANENT_ENRAGE = GameRuleRegistry.register("permanentEnrage", GameRules.Category.MOBS, GameRuleFactory.createBooleanRule(false));
    }
}
