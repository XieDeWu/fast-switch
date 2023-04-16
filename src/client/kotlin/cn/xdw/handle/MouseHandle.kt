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
            val client = MinecraftClient.getInstance()
            if (client.currentScreen != null) return;
            val alt = HudData.currentItemGroup.switchDisplay(false)
            val ctrl = KeyData.keyState[GLFW.GLFW_KEY_LEFT_CONTROL]?.isPress()?:false
            when{
                alt && ctrl ->HudData.currentItemGroup.offset(0).second.tag(vertical.toInt())
                alt && !ctrl ->HudData.currentItemGroup.offset(vertical.toInt())
            }
            when{alt->info.cancel()}
        }
    }
}