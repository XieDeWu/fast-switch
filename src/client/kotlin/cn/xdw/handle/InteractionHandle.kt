package cn.xdw.handle

import cn.xdw.data.HudData
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

class InteractionHandle {
    companion object{
        fun interactBlock(player: ClientPlayerEntity, hand: Hand, blockHitResult: BlockHitResult, info: CallbackInfoReturnable<ActionResult>) {
            val result = info.returnValue
            val world = MinecraftClient.getInstance().world?:return
            val group = HudData.currentItemGroup
            if(world.isClient
                && (hand == Hand.MAIN_HAND)
                && result.isAccepted
                && group.switchDisplay(false)
            ){
                group.nextItem()
            }
        }
    }
}