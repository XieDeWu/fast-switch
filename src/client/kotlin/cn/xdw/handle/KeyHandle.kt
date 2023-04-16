package cn.xdw.handle

import cn.xdw.data.HudData.Companion.currentItemGroup
import cn.xdw.data.KeyData
import org.lwjgl.glfw.GLFW

class KeyHandle {
    companion object{
        fun registry(){
            KeyData.keyState[GLFW.GLFW_KEY_LEFT_ALT]?.apply {
                onShortClick = {
                    currentItemGroup.switchDisplay(true)
                    Unit
                }
            }
        }
    }
}