package cn.xdw.data

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import org.lwjgl.glfw.GLFW

class KeyData{
    data class KeyPressData(
        val click:Boolean,
        val press:Boolean,
        val short:Boolean,
        val long:Boolean,
        val pressTick:Int,
    )
    @Suppress("unused")
    data class KeyPress(
        val code: Int,
        val name: String,
        val category: String = "key.category.example",
        val keyBinding: KeyBinding? = KeyBindingHelper.registerKeyBinding(KeyBinding(name,InputUtil.Type.KEYSYM,code,category)),
        var onShortClick:()->Unit? = {},
        var onLongClick:()->Unit? = {},
        var onShortPress:()->Unit? = {},
        var onLongPress:()->Unit? = {},
    ){
        var pressHandle:(Boolean?)->KeyPressData= run {
            var prePress = false
            var click = false
            var press = false
            var short = false
            var long = false
            var pressTick = 0
            {
                it?.let{
                    pressTick += when{it->1 else->0}
                    click = prePress && !it
                    press = prePress && it
                    short = pressTick in 1..8
                    long = pressTick > 20
                    when{
                        click->when{
                            short->onShortClick()
                            long->onLongClick()
                        }
                        press->when{
                            short->onShortPress()
                            long->onLongPress()
                        }
                    }
                    prePress = it
                    pressTick = when{it->pressTick else->0}
                }
                KeyPressData(click,press,short,long,pressTick)
            }
        }
        fun isPress() = (pressHandle(null).press)
    }
    @Suppress("MemberVisibilityCanBePrivate", "unused")
    companion object{
        var keyState = mutableMapOf(
            GLFW.GLFW_KEY_LEFT_ALT.let { it to KeyPress(it,"key.example.alt") },
            GLFW.GLFW_KEY_LEFT_SHIFT.let { it to KeyPress(it,"key.example.shift") },
            GLFW.GLFW_KEY_LEFT_CONTROL.let { it to KeyPress(it,"key.example.ctrl") },
            GLFW.GLFW_KEY_V.let { it to KeyPress(it,"key.example.v") },
        )
        fun register(){
            val isPress = { client:MinecraftClient, code:Int->InputUtil.isKeyPressed(client.window.handle,code)}
            ClientTickEvents.START_CLIENT_TICK.register{ keyState.forEach { (_, state) ->
                state.pressHandle(isPress(it,state.code))
            } }
            ClientTickEvents.END_CLIENT_TICK.register{ keyState.forEach { (_, state) ->
                state.pressHandle(isPress(it,state.code))
            } }
        }
    }
}
