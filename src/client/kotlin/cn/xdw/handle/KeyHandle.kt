package cn.xdw.handle

import cn.xdw.data.HudData
import cn.xdw.data.HudData.Companion.currentItemGroup
import cn.xdw.data.KeyData
import net.minecraft.client.MinecraftClient
import org.lwjgl.glfw.GLFW

@Suppress("NestedLambdaShadowedImplicitParameter")
class KeyHandle {
    companion object{
        fun registry(){
            KeyData.keyState[GLFW.GLFW_KEY_LEFT_ALT]?.apply {
                onShortClick = {
                    currentItemGroup.switchDisplay(true)
                    Unit
                }
                onLongPressOne = {
                    currentItemGroup = currentItemGroup.let {
                        when {
                            it.switchDisplay(false) -> {
                                HudData.tagItem()[it.offset(0).second.tag(0)]
                                    ?.map { HudData.Item(it) }
                                    ?.let {
                                        HudData.ItemGroup(it).apply {
                                            switchDisplay(true)
                                        }
                                    }
                            }
                            else->null
                        }?: currentItemGroup
                    }
                    Unit
                }
            }
            KeyData.keyState[GLFW.GLFW_KEY_V]?.apply {
                onShortClick = {
                    val inventory = MinecraftClient.getInstance().player?.inventory
                    when{
                        inventory!=null->{
                            (0..8)
                                .map { inventory.getStack(it) }
                                .filter { !it.isEmpty }
                                .takeIf { it.isNotEmpty() }
                                ?.let {
                                    currentItemGroup = HudData.ItemGroup(it.map { HudData.Item(id = it.registryEntry.key.get().value.toString(), count = it.count) })
                                        .apply { switchDisplay(true) }
                                }
                        }
                    }
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