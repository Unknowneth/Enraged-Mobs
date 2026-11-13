package com.notunanancyowen.enraged.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.notunanancyowen.enraged.Enrageable;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.DisplayRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Display;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DisplayRenderer.BlockDisplayRenderer.class)
public class BlockDisplayRendererMixin {
    @Shadow @Final private BlockRenderDispatcher blockRenderer;
    @Inject(method = "renderInner(Lnet/minecraft/world/entity/Display$BlockDisplay;Lnet/minecraft/world/entity/Display$BlockDisplay$BlockRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IF)V", at = @At("HEAD"), cancellable = true)
    private void forceFullbright(Display.BlockDisplay blockDisplay, Display.BlockDisplay.BlockRenderState blockRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, float f, CallbackInfo ci) {
        if(blockDisplay.getVehicle() instanceof Enrageable && blockDisplay.getVehicle().isOnFire() && blockDisplay.blockRenderState() != null) {
            BlockState blockState = blockDisplay.blockRenderState().blockState();
            if(blockState.is(BlockTags.FIRE) && blockState.getRenderShape() == RenderShape.MODEL) {
                Level level = blockDisplay.level();
                if(blockState != level.getBlockState(blockDisplay.blockPosition()) && blockState.getRenderShape() != RenderShape.INVISIBLE) {
                    poseStack.pushPose();
                    BlockPos blockPos = BlockPos.containing(blockDisplay.getX(), blockDisplay.getBoundingBox().maxY, blockDisplay.getZ());
                    blockRenderer.getModelRenderer().tesselateBlock(level, blockRenderer.getBlockModel(blockState), blockState, blockPos, poseStack, multiBufferSource.getBuffer(ItemBlockRenderTypes.getMovingBlockRenderType(blockState)), false, RandomSource.create(), blockState.getSeed(blockDisplay.getOnPos()), OverlayTexture.NO_OVERLAY);
                    poseStack.popPose();
                    ci.cancel();
                }
            }
        }
    }
}
