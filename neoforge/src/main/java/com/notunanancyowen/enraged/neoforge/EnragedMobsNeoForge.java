package com.notunanancyowen.enraged.neoforge;

import com.notunanancyowen.enraged.EnragedMobs;
import net.minecraft.world.level.GameRules;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(EnragedMobs.MOD_ID)
public final class EnragedMobsNeoForge {
    public EnragedMobsNeoForge(IEventBus modBus) {
        EnragedMobs.init();
        modBus.addListener(this::registerGamerules);
    }
    private void registerGamerules(final FMLCommonSetupEvent event) {
        EnragedMobs.MOBS_ENRAGE_ON_LOW_HEALTH = GameRules.register("mobEnrageOnHealthPercent", GameRules.Category.MOBS, GameRules.IntegerValue.create(0));
        EnragedMobs.PERMANENT_ENRAGE = GameRules.register("permanentEnrage", GameRules.Category.MOBS, GameRules.BooleanValue.create(false));
    }
}
