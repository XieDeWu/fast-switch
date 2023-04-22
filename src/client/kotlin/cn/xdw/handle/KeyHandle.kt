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
                                val cursor = it.offset(0)
                                val oldID = cursor.second.id
                                val oldTag = cursor.second.offset(0).second
                                HudData.tagItem()[it.offset(0).second.offset(0).second]
                                    ?.map { HudData.Item(it) }
                                    ?.let {
                                        HudData.ItemGroup(it).apply {
                                            switchDisplay(true)
                                            items.indexOfFirst { it.id == oldID }.takeIf { it in items.indices }?.let {
                                                val newCursor = offset(it - offset(0).first)
                                                val curTagIndex = newCursor.second.offset(0).first
                                                val tags = newCursor.second.tags
                                                tags.indexOfFirst { it == oldTag }.takeIf { it in tags.indices }?.let {
                                                    newCursor.second.offset(it - curTagIndex)
                                                }
                                            }
                                            offset(0).second.offset(0)
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
                        !currentItemGroup.switchDisplay(false) && inventory!=null->{
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
                    if(currentItemGroup.switchDisplay(false))
                        currentItemGroup.modeSwitch(1)
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