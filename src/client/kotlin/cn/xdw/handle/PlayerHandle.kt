package cn.xdw.handle

import cn.xdw.data.HudData
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d

class PlayerHandle {
    companion object{
        @Suppress("NestedLambdaShadowedImplicitParameter")
        fun register(){
            UseBlockCallback.EVENT.register( run {
                var oldHitResult = BlockHitResult(Vec3d.ZERO,Direction.UP, BlockPos.ORIGIN,false)
                UseBlockCallback { _, world, hand, hitResult ->
                    if(world.isClient
                        && HudData.currentItemGroup.switchDisplay(false)
                        && (hand == Hand.MAIN_HAND)
                        && (hitResult != oldHitResult)
                    ){
                        oldHitResult = hitResult
                        val client = MinecraftClient.getInstance()
                        client.interactionManager?.let {
                            it.interactBlock(client.player, hand, oldHitResult).takeIf { it.isAccepted }?.let {
                                HudData.currentItemGroup.nextItem()
                                return@UseBlockCallback ActionResult.SUCCESS
                            }
                        }
                    }
                    ActionResult.PASS
                }
            })
        }
    }
}