package com.notunanancyowen.enraged.forge;

import net.minecraft.world.level.GameRules;
import net.minecraftforge.fml.common.Mod;

import com.notunanancyowen.enraged.EnragedMobs;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(EnragedMobs.MOD_ID)
public final class EnragedMobsForge {
    public EnragedMobsForge(FMLJavaModLoadingContext context) {
        EnragedMobs.init();
        context.getModEventBus().addListener(this::registerGamerules);
    }
    private void registerGamerules(final FMLCommonSetupEvent event) {
        EnragedMobs.MOBS_ENRAGE_ON_LOW_HEALTH = GameRules.register("mobEnrageOnHealthPercent", GameRules.Category.MOBS, GameRules.IntegerValue.create(0));
        EnragedMobs.PERMANENT_ENRAGE = GameRules.register("permanentEnrage", GameRules.Category.MOBS, GameRules.BooleanValue.create(false));
    }
}
