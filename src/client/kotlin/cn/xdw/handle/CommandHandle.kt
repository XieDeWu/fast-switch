package cn.xdw.handle

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.client.network.ClientCommandSource
import net.minecraft.text.Text


class CommandHandle {
    companion object{
        fun registry() {
            ClientCommandRegistrationCallback.EVENT.register { _, _ ->
                getActiveDispatcher()?.register(run {
                    val add = "add"
                    val del = "del"
                    val handle: (String) -> (CommandContext<FabricClientCommandSource>) -> Int = { opt -> code@{ pack ->
                            val name = pack.nodes.last().range.get(pack.input)
                                .takeIf { "^[a-z_]*$".toRegex().matches(it) } ?: run {
                                    pack.source.sendFeedback(Text.literal("自定义标签组名应仅包含小写字母与下划线!"))
                                    return@code -1
                                }
                            pack.source.sendFeedback(Text.literal("已${when(opt){add->"增加";del->"删除";else->""}}自定义标签组:${name}"))
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