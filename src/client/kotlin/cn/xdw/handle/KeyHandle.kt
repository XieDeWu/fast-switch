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
        // TODO: 当Hud启用时，Alt(长按)+A 将快捷栏作为物品组添加到Hud队头
        // TODO: 当Hud启用时，Alt(长按)+D 将当前物品组从Hud中删除
        // TODO: 当Hud启用时，Ctrl(按压)+A 将主手物品添加到当前物品组队尾
        // TODO: 当Hud启用时，Ctrl(按压)+D 删除当前物品组的当前物品
        // TODO: 当Hud启用时，Ctrl(长按)+Alt(长按)+A 添加Tag搜索物品组(最大随机32个)到HUD
        // TODO: 当Hud启用时，Ctrl(长按)+Alt(长按)+D 清空HUD物品组
    }
}