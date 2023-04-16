package cn.xdw.handle

import cn.xdw.data.HudData
import cn.xdw.data.KeyData
import net.minecraft.client.MinecraftClient
import net.minecraft.item.ItemStack
import net.minecraft.screen.slot.SlotActionType
import org.lwjgl.glfw.GLFW
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo


class MouseHandle {
    companion object{
        @Suppress("UNUSED_PARAMETER")
        fun mouseHandle(window: Long, horizontal: Double, vertical: Double, info: CallbackInfo) {
            val client = MinecraftClient.getInstance()
            if (client.currentScreen != null) return;
            val hud = HudData.currentItemGroup.switchDisplay(false)
            val alt = KeyData.keyState[GLFW.GLFW_KEY_LEFT_ALT]?.isPress()?:false
            val ctrl = KeyData.keyState[GLFW.GLFW_KEY_LEFT_CONTROL]?.isPress()?:false
            when{
                hud && ctrl ->HudData.currentItemGroup.offset(0).second.tag(vertical.toInt())
                hud && !ctrl -> {
                    val itemPair = HudData.currentItemGroup.offset(vertical.toInt())
                    val player = client.player!!
                    val inventory = player.inventory!!
                    client.interactionManager?.clickSlot(client.player!!.currentScreenHandler.syncId, inventory.selectedSlot + 36, 0, SlotActionType.QUICK_MOVE, client.player)
                    player.dropSelectedItem(true)
                    val slot: Int = inventory.getSlotWithStack(ItemStack(itemPair.second.item))
                    if (slot>=0 && slot!= inventory.selectedSlot) {
                        client.interactionManager?.pickFromInventory(slot)
                    }
                    // TODO: 柏林噪音

                }
            }
            when{hud->info.cancel()}

            // TODO: 当Hud启用时，Alt(长按)+上滚轮 上个物品组
            // TODO: 当Hud启用时，Alt(长按)+下滚轮 下个当前物品组
        }
    }
}