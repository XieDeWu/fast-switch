package cn.xdw

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
                ColorWheelHud.curIndex = (ColorWheelHud.curIndex+vertical.toInt()).coerceIn(0,16)
            }
            val a = 1
        }
    }
}