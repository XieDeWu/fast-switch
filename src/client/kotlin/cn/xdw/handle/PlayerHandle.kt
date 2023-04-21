package cn.xdw.handle

import cn.xdw.data.HudData
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemUsageContext
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand

class PlayerHandle {
    companion object{
        fun register(){
            UseBlockCallback.EVENT.register(UseBlockCallback { player, world, hand, hitResult ->
                if(world.isClient
                    && HudData.currentItemGroup.switchDisplay(false)
                    && hand == Hand.MAIN_HAND){
                    val itemStack = player.mainHandStack
                    (itemStack.item as? BlockItem)
                        ?.block
                        ?.getPlacementState(ItemPlacementContext(ItemUsageContext(world, player, hand, itemStack, hitResult)))
                        ?.canPlaceAt(world,hitResult.blockPos.offset(hitResult.side))
                        ?.let {
                            HudData.currentItemGroup.perlinNextItem()
                            return@UseBlockCallback ActionResult.SUCCESS
                        }
                }
                ActionResult.PASS
            })
        }
    }
}