package cn.xdw

import cn.xdw.data.ItemGroupList.Companion.currentItemGroup
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.InputUtil
import org.lwjgl.glfw.GLFW
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

class MouseHandle {
    companion object{
        @Suppress("UNUSED_PARAMETER")
        fun mouseHandle(window: Long, horizontal: Double, vertical: Double, info: CallbackInfo) {
            val client = MinecraftClient.getInstance()
            if (client.currentScreen != null) return
            InputUtil.isKeyPressed(client.window.handle, GLFW.GLFW_KEY_LEFT_ALT).takeIf { it }?.let {
                info.cancel()
                currentItemGroup.cursor = (currentItemGroup.cursor+vertical.toInt()).coerceIn(0,currentItemGroup.items.size-1)
            }
            val a = 1
        }
    }
}