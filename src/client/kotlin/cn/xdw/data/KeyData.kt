package cn.xdw.data

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.START_CLIENT_TICK
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.InputUtil
import org.lwjgl.glfw.GLFW.*

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
        val keyState = sortedMapOf(
            GLFW_KEY_LEFT_ALT to KeyPress(GLFW_KEY_LEFT_ALT),
            GLFW_KEY_LEFT_SHIFT to KeyPress(GLFW_KEY_LEFT_SHIFT),
            GLFW_KEY_LEFT_CONTROL to KeyPress(GLFW_KEY_LEFT_CONTROL),
            GLFW_KEY_V to KeyPress(GLFW_KEY_V),
        )
        fun register(){
            val isPress = run {
                val client = MinecraftClient.getInstance();
                { code: Int -> InputUtil.isKeyPressed(client.window.handle, code) }
            };
            { keyState.forEach { (_,key)->key.pressHandle(isPress(key.code)) } }.let {
                START_CLIENT_TICK.register { it() }
                END_CLIENT_TICK.register { it() }
            }
        }
    }
}
