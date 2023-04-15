package cn.xdw.data

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.StartTick
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.InputUtil
import org.lwjgl.glfw.GLFW

class KeyPressState{
    @Suppress("unused")
    data class KeyPress(
        val code: Int,
        val name: String,
        var tickNum: Int = 0
    ){
        fun isPress():Boolean {
            return tickNum > 0
        }
        fun isShortPress():Boolean {
            return tickNum in 1..19
        }
        fun isLongPress():Boolean {
            return tickNum >= 20
        }
    }
    @Suppress("MemberVisibilityCanBePrivate", "unused")
    companion object{
        var keyState = mutableMapOf(
            GLFW.GLFW_KEY_LEFT_ALT.let { it to KeyPress(it,"key.example.alt") },
            GLFW.GLFW_KEY_LEFT_CONTROL.let { it to KeyPress(it,"key.example.ctrl") },
        )
        fun register(){
            val isPress = {code:Int->InputUtil.isKeyPressed(MinecraftClient.getInstance().window.handle,code)}
            ClientTickEvents.START_CLIENT_TICK.register{ StartTick {
                keyState.forEach { (_, state) ->
                    state.tickNum += if(isPress(state.code)) 1 else 0
                }
            } }
            ClientTickEvents.END_CLIENT_TICK.register{ EndTick {
                keyState.forEach { (_, state) ->
                    state.tickNum = if(!isPress(state.code)) 0 else state.tickNum
                }
            } }
        }
    }
}
