package cn.xdw.mixin.client;

import cn.xdw.handle.InteractionHandle;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public class InteractionMixin {
    @Inject(at = @At(value = "RETURN", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;sendSequencedPacket(Lnet/minecraft/client/world/ClientWorld;Lnet/minecraft/client/network/SequencedPacketCreator;)V"), method = "interactBlock")
    public void interactBlock(ClientPlayerEntity player, Hand hand, BlockHitResult blockHitResult, CallbackInfoReturnable<ActionResult> info) {
        InteractionHandle.Companion.interactBlock(player,hand,blockHitResult,info);
    }
}
