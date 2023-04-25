package cn.xdw.handle

import cn.xdw.data.HudData
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.text.Text
import java.util.*


class CommandHandle {
    companion object{
        fun registry() {
            ClientCommandRegistrationCallback.EVENT.register { _, _ ->
                getActiveDispatcher()?.register(run {
                    val add = "add"
                    val del = "del"
                    val handle: (String) -> (CommandContext<FabricClientCommandSource>) -> Int = { opt ->
                        code@{ pack ->
                            val name = pack.nodes.last().range.get(pack.input)
                                .takeIf { "^[a-z0-9_]*$".toRegex().matches(it) } ?: run {
                                pack.source.sendFeedback(Text.literal("自定义标签组名应仅包含数字,小写字母,下划线!"))
                                return@code -1
                            }
                            val groupName = "custom:${name}"
                            when(opt) {
                                add-> {
                                    HudData.customGroup = HudData.customGroup.plus(groupName to HudData.currentItemGroup.items.map { it.id to it.count }).toSortedMap()
                                }
                                del->{
                                    HudData.customGroup = HudData.customGroup.filter { it.key != groupName }.toSortedMap()
                                }
                            }
                            pack.source.sendFeedback(Text.literal("已${when (opt) {add -> "增加";del -> "删除";else -> "" }}自定义标签组: ${groupName}"))
                            0
                        }
                    }
                    listOf(add,del).fold(literal("fast-switch")){acc, s ->
                        acc.then(literal(s).then(argument("customGroupName", StringArgumentType.greedyString()).executes(handle(s))))
                    }
                })
            }
        }
    }
}