package cn.xdw.handle

import cn.xdw.data.HudData
import cn.xdw.data.KeyData
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.InputUtil
import net.minecraft.item.ItemStack
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.util.Hand
import org.lwjgl.glfw.GLFW
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo


class MouseHandle {
    companion object{
        @Suppress("UNUSED_PARAMETER")
        fun mouseHandle(window: Long, horizontal: Double, vertical: Double, info: CallbackInfo) {
            val client = MinecraftClient.getInstance()
            if (client.currentScreen != null) return;
            val alt = HudData.currentItemGroup.switchDisplay(false)
            val ctrl = KeyData.keyState[GLFW.GLFW_KEY_LEFT_CONTROL]?.isPress()?:false
            when{
                alt && ctrl ->HudData.currentItemGroup.offset(0).second.tag(vertical.toInt())
                alt && !ctrl -> {
                    val itemPair = HudData.currentItemGroup.offset(vertical.toInt())
                    val player = client.player!!
                    val inventory = player.inventory!!
                    client.interactionManager?.clickSlot(client.player!!.currentScreenHandler.syncId, inventory.selectedSlot + 36, 0, SlotActionType.QUICK_MOVE, client.player)
                    player.dropSelectedItem(true)
                    val slot: Int = inventory.getSlotWithStack(ItemStack(itemPair.second.item))
                    if (slot>=0 && slot!= inventory.selectedSlot) {
                        client.interactionManager?.pickFromInventory(slot)
                    }
                }
            }
            when{alt->info.cancel()}
        }
    }
}