package cn.xdw.handle

import cn.xdw.data.HudData
import cn.xdw.data.HudData.Companion.currentItemGroup
import cn.xdw.data.KeyData
import net.minecraft.client.MinecraftClient
import org.lwjgl.glfw.GLFW

@Suppress("NestedLambdaShadowedImplicitParameter")
class KeyHandle {
    companion object{
        @Suppress("RedundantUnitExpression")
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
                val sidebarToCurrentGroup = sidebarToCurrentGroup@{
                    val inventory = MinecraftClient.getInstance().player?.inventory ?: return@sidebarToCurrentGroup
                    (0..8)
                        .map { inventory.getStack(it) }
                        .filter { !it.isEmpty }
                        .takeIf { it.isNotEmpty() }
                        ?.let {
                            currentItemGroup = HudData.ItemGroup(it.map {
                                HudData.Item(
                                    id = it.registryEntry.key.get().value.toString(),
                                    count = it.count
                                )
                            }).apply { switchDisplay(true) }
                        }
                }
                onShortClick = {
                    run sidebarToCurrentGroup@{
                        if(!currentItemGroup.switchDisplay(false)) sidebarToCurrentGroup()
                    }
                    run modeSwitch@{
                        if (currentItemGroup.switchDisplay(false)) currentItemGroup.modeSwitch(1)
                    }
                }
                onLongPressOne = {
                    run sidebarToCurrentGroup@{
                        if(currentItemGroup.switchDisplay(false)) sidebarToCurrentGroup()
                    }
                }
            }
        }
    }
}