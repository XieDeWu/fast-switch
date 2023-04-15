package cn.xdw

import cn.xdw.data.HudData
import cn.xdw.data.KeyPressState
import net.minecraft.client.MinecraftClient
import org.lwjgl.glfw.GLFW
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

class MouseHandle {
    companion object{
        @Suppress("UNUSED_PARAMETER")
        fun mouseHandle(window: Long, horizontal: Double, vertical: Double, info: CallbackInfo) {
            val client = MinecraftClient.getInstance()
            if (client.currentScreen != null) return;

            val alt = KeyPressState.keyState[GLFW.GLFW_KEY_LEFT_ALT]?.isPress()?:false
            val shift = KeyPressState.keyState[GLFW.GLFW_KEY_LEFT_SHIFT]?.isPress()?:false
            val ctrl = KeyPressState.keyState[GLFW.GLFW_KEY_LEFT_CONTROL]?.isPress()?:false
            if(alt){
                info.cancel()
                if (ctrl){
                    HudData.currentItemGroup.offset(0).second.tag(vertical.toInt())
                }else{
                    HudData.currentItemGroup.offset(vertical.toInt())
                }
            }
        }
    }
}