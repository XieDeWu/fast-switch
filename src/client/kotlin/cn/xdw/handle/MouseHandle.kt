package cn.xdw.handle

import cn.xdw.data.HudData
import cn.xdw.data.KeyData
import net.minecraft.client.MinecraftClient
import org.lwjgl.glfw.GLFW
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo


class MouseHandle {
    companion object{
        @Suppress("UNUSED_PARAMETER")
        fun mouseHandle(window: Long, horizontal: Double, vertical: Double, info: CallbackInfo) {
            val group = HudData.currentItemGroup
            val client = MinecraftClient.getInstance()
            if (client.currentScreen != null) return;
            val hud = group.switchDisplay(false)
            val shift = KeyData.keyState[GLFW.GLFW_KEY_LEFT_SHIFT]?.isPress()?:false
            val ctrl = KeyData.keyState[GLFW.GLFW_KEY_LEFT_CONTROL]?.isPress()?:false
            val v = KeyData.keyState[GLFW.GLFW_KEY_V]?.isPress()?:false
            when{
                hud && !shift && ctrl && !v -> group.offset(0).second.tagOffset(vertical.toInt())
                hud && !shift && !ctrl && !v -> {
                    group.offset(vertical.toInt())
                    group.recomputeOrderNext(true)
                }
                hud && shift && !ctrl && !v -> group.offset(0).second.affixOffset(vertical.toInt())
            }
            when{
                hud && v -> { KeyHandle.addCurrentToList(-vertical.toInt()) }
                hud && !v->info.cancel()
            }
        }
    }
}