package cn.xdw.handle

import cn.xdw.data.HudData
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

class InteractionHandle {
    companion object{
        fun interactBlock(player: ClientPlayerEntity, hand: Hand, blockHitResult: BlockHitResult, info: CallbackInfoReturnable<ActionResult>) {
            val group = HudData.currentItemGroup.apply { workHand(hand) }
            if(player.clientWorld.isClient
                && info.returnValue.isAccepted
                && group.switchDisplay(false)
            ){ group.apply {
                nextItem()
                when(hand){ Hand.MAIN_HAND->player.mainHandStack.isEmpty; Hand.OFF_HAND->player.offHandStack.isEmpty }
                    .takeIf { it }?.let { switchItem(offset(0).second.item,offset(0).second.count) }
            } }
        }
    }
}