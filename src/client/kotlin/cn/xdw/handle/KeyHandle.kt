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
                                val regex = Regex("^\\*|\\*\$")
                                val affix = cursor.second.affixes(0).replace(regex,"")
                                when {
                                    cursor.second.offset(0).first == 0->{
                                        HudData.tagItem().values.flatten().toSortedSet()
                                            .filter { it.contains(affix) }
                                    }
                                    cursor.second.offset(0).first != 0->{
                                        HudData.tagItem().filter { it.key.contains(affix) }
                                            .values.flatten().toSortedSet()
                                            .also {
                                                it.addAll(HudData.tagItem().values.flatten().toSortedSet().filter { it.contains(affix) } )
                                            }
                                    }
                                    else->null
                                }
                                    ?.map { HudData.Item(it) }
                                    ?.takeIf { it.isNotEmpty() }
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
                            }).apply {
                                switchDisplay(true)
                                val relIndex = (0..8).fold(-1){acc, i ->
                                    when {
                                        i>inventory.selectedSlot->acc
                                        !inventory.getStack(i).isEmpty->acc+1
                                        else->acc
                                    }
                                }
                                offset(relIndex-offset(0).first)
                            }
                        }
                }
                onShortClick = {
                    run sidebarToCurrentGroup@{
                        if(!currentItemGroup.switchDisplay(false)) sidebarToCurrentGroup()
                    }
                    run modeSwitch@{
                        if (currentItemGroup.switchDisplay(false)) {
                            currentItemGroup.modeSwitch(1)
                            currentItemGroup.recomputeOrderNext(true)
                        }
                    }
                }
                onLongPressOne = {
                    run sidebarToCurrentGroup@{
                        if(currentItemGroup.switchDisplay(false)) sidebarToCurrentGroup()
                    }
                }
            }
            // TODO: 长按Alt添加主手物品或所看方块到Hud
            // TODO: 改V短按为长按触发 添加快捷栏物品到Hud
            // TODO: 切换物品组，保留,物品,Tag，词缀 (暂无实现）
            // TODO: 拆分物品搜索，组搜索，添加组词缀搜索支持
        }
    }
}