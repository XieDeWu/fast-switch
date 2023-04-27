package cn.xdw.data

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import org.lwjgl.glfw.GLFW

class KeyData{
    data class KeyPressData(
        var prePress: Boolean = false,
        var click: Boolean = false,
        var press: Boolean = false,
        var short: Boolean = false,
        var long: Boolean = false,
        var shortOne: Boolean = false,
        var longOne: Boolean = false,
        var pressTick: Int = 0,
    )
    @Suppress("unused")
    data class KeyPress(
        val code: Int,
        val name: String,
        val category: String = "key.category.example",
        val keyBinding: KeyBinding? = KeyBindingHelper.registerKeyBinding(KeyBinding(name,InputUtil.Type.KEYSYM,code,category)),
        var onShortClick:()->Unit? = {},
        var onLongClick:()->Unit? = {},
        var onShortPressOne:()->Unit? = {},
        var onShortPress:()->Unit? = {},
        var onLongPressOne:()->Unit? = {},
        var onLongPress:()->Unit? = {},
    ){
        var pressHandle:(Boolean?)->KeyPressData= run {
            val data = KeyPressData();
            { b->
                b?.let {
                    data.apply {
                        pressTick += when {
                            b -> 1
                            else -> 0
                        }
                        click = prePress && !b
                        press = prePress && b
                        short = pressTick in 1..12
                        long = pressTick > 20
                        when {
                            press && short && !shortOne ->{
                                shortOne = true
                                onShortPressOne()
                            }
                            press && long && !longOne ->{
                                longOne = true
                                onLongPressOne()
                            }
                        }
                        when {
                            click -> when {
                                short -> onShortClick()
                                long -> onLongClick()
                            }
                            press -> when {
                                short -> onShortPress()
                                long -> onLongPress()
                            }
                        }
                        when {
                            click-> {
                                shortOne = false
                                longOne = false
                            }
                        }
                        prePress = b
                        pressTick = when {
                            b -> pressTick
                            else -> 0
                        }
                    }
                }
                data
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
