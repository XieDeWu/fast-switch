package cn.xdw.handle

import com.mojang.brigadier.arguments.StringArgumentType
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.minecraft.text.Text


class CommandHandle {
    companion object{
        fun registry(){
            ClientCommandRegistrationCallback.EVENT.register{ _, _ ->
                getActiveDispatcher()?.register(
                    literal("fast-switch")
                        .then(literal("add")
                            .then(argument("customGroupName",StringArgumentType.greedyString()).executes {

                                it.source.sendFeedback(Text.literal("已添加自定义标签组:${it.input}"))
                                0
                            }))
                        .then(literal("del")
                            .then(argument("customGroupName",StringArgumentType.greedyString()).executes {
                                it.source.sendFeedback(Text.literal("已删除自定义标签组:${it.input}"))
                                0
                            }))
                )
            }
        }
    }
}