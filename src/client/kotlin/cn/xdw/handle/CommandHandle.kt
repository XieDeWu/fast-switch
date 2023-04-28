package cn.xdw.handle

import cn.xdw.data.HudData
import cn.xdw.data.HudData.*
import cn.xdw.data.HudData.Companion.currentItemGroup
import cn.xdw.data.HudData.Companion.customGroup
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.text.Text


@Suppress("NestedLambdaShadowedImplicitParameter")
class CommandHandle {
    companion object{
        fun registry() {
            ClientCommandRegistrationCallback.EVENT.register { _, _ ->
                getActiveDispatcher()?.register(run {
                    val add = "add"
                    val del = "del"
                    val list = "list"
                    val getInput: (CommandContext<FabricClientCommandSource>)->String = { it.nodes.last().range.get(it.input) }
                    val updHandle: (String) -> (CommandContext<FabricClientCommandSource>) -> Int = { opt ->
                        code@{ pack ->
                            val name = getInput(pack)
                                .takeIf { "^[a-z0-9_]*$".toRegex().matches(it) } ?: run {
                                pack.source.sendFeedback(Text.literal("自定义标签组名应仅包含数字,小写字母,下划线!"))
                                return@code -1
                            }
                            val groupName = "custom:${name}"
                            when(opt) {
                                add-> {
                                    customGroup = customGroup.plus(groupName to currentItemGroup.items.map { JsonItem(it.id,it.count) }).toSortedMap()
                                }
                                del->{
                                    customGroup = customGroup.filter { it.key != groupName }.toSortedMap()
                                }
                            }
                            currentItemGroup = currentItemGroup.items
                                .map { Item(id = it.id,count = it.count) }
                                .let { ItemGroup(it) }
                                .apply {
                                    val oldCursorItem = currentItemGroup.offset(0).second
                                    offsetByName(oldCursorItem.id)
                                    offset(0).second.apply {
                                        when(opt){
                                            add->tagByName(groupName)
                                            del->tagByName(oldCursorItem.tagOffset(0).second)
                                        }
                                    }
                                    switchDisplay(true)
                                }
                            HudData.syncConfig("save")
                            pack.source.sendFeedback(Text.literal("已${when (opt) {add -> "增加";del -> "删除";else -> "" }}自定义标签组: ${groupName}"))
                            0
                        }
                    }
                    val selHandle: (CommandContext<FabricClientCommandSource>) -> Int = {
                        it.source.sendFeedback(Text.literal(customGroup
                            .map { it.key to it.value.size }
                            .sortedBy { it.first }
                            .fold(""){ acc, pair -> "${acc}${when(acc.isNotEmpty()){true->"\n" else->""}}${pair}"
                        }))
                        0
                    }
                    val resetGroup: (CommandContext<FabricClientCommandSource>) -> Int = { pack->
                        val input = getInput(pack)
                        customGroup.entries
                            .find { it.key == input }
                            ?.takeIf { it.value.isNotEmpty() }
                            ?.also { currentItemGroup = ItemGroup(it.value.map { Item(id = it.id,count = it.count) }).apply {
                                switchDisplay(true)
                            } }
                            .also {
                                pack.source.sendFeedback(Text.literal(when(it) {
                                    null -> "标签组未找到! $input"
                                    else -> "标签组已设置! $input"
                                }))
                            }
                        0
                    }
                    val commonRegistry = {
                        listOf(
                            add to updHandle,
                            del to updHandle,
                            list to selHandle,
                        ).fold(literal("fast-switch")) { acc, s ->
                            acc.then(
                                literal(s.first).then(argument("customGroupName", StringArgumentType.greedyString())
                                    .suggests { _, builder ->
                                        customGroup.keys.forEach { builder.suggest(it) }.let { builder.buildFuture() }
                                    }
                                    .executes(when(s.first){list->resetGroup else->updHandle(s.first)})
                                )
                            )
                        }
                    }
                    commonRegistry()
                })
            }
        }
    }
}