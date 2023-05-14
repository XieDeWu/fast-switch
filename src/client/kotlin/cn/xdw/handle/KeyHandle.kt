package cn.xdw.handle

import cn.xdw.data.HudData
import cn.xdw.data.HudData.Companion.currentItemGroup
import cn.xdw.data.KeyData
import net.minecraft.client.MinecraftClient
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.util.hit.BlockHitResult
import org.lwjgl.glfw.GLFW

@Suppress("NestedLambdaShadowedImplicitParameter")
class KeyHandle {
    companion object{
        val getItemStack:(Int)->ItemStack = run@{ slot->
            val inventory = MinecraftClient.getInstance().player?.inventory ?: return@run ItemStack.EMPTY
            inventory.getStack(slot).takeIf {
                !(inventory.selectedSlot == slot && inventory.mainHandStack.isEmpty)
            } ?: run {
                val client = MinecraftClient.getInstance()
                val world = client.world
                val player = client.player
                if (client == null || world == null || player == null) return@run ItemStack.EMPTY
                player.raycast(20.0, 0f, false)
                    .let { it as? BlockHitResult }
                    ?.let { world.getBlockState(it.blockPos).block.asItem() }
                    ?.let { ItemStack(it) }
                    ?: ItemStack.EMPTY
            }
        }
        val addCurrentToList:(Int)->Unit = addToLast@{ offset->
            val player = MinecraftClient.getInstance().player?.inventory ?: return@addToLast
            getItemStack(((player.selectedSlot+offset)%9+9)%9)
                .also { if (it.isEmpty) return@addToLast }
                .also {
                    val curItems = currentItemGroup.let { when(it.switchDisplay(false)){ true->it.items false-> listOf() } }
                    currentItemGroup = HudData.ItemGroup(
                        items = curItems + HudData.Item(id = it.registryEntry.key.get().value.toString(),count = it.count)
                    ).apply {
                        switchDisplay(true)
                        offset(9999)
                    }
                }
        }
        @Suppress("RedundantUnitExpression")
        fun registry(){
            KeyData.keyState[GLFW.GLFW_KEY_LEFT_ALT]?.apply {
                onShortClick = {
                    currentItemGroup.switchDisplay(true)
                    Unit
                }
                onLongPressOne = {
                    val oldItem = currentItemGroup.offset(0).second
                    val oldID = oldItem.id
                    val oldTag = oldItem.tagOffset(0).second
                    val oldAffix = oldItem.affixOffset(0).second
                    val newGroupBuild = { it:List<HudData.Item>->
                        HudData.ItemGroup(it).apply {
                            switchDisplay(true)
                            offsetByName(oldID)
                            val item = offset(0).second
                            item.tagByName(oldTag)
                            item.affixByName(oldAffix)
                        }
                    }
                    val sidebarToGroup: ()->HudData.ItemGroup? = sidebarToCurrentGroup@{
                        val inventory = MinecraftClient.getInstance().player?.inventory ?: return@sidebarToCurrentGroup currentItemGroup
                        (0..8)
                            .map { getItemStack(it) }
                            .filter { !it.isEmpty }
                            .takeIf { it.isNotEmpty() }
                            ?.let {
                                HudData.ItemGroup(it.map {
                                    HudData.Item(
                                        id = it.registryEntry.key.get().value.toString(),
                                        count = it.count
                                    )
                                }).apply {
                                    switchDisplay(true)
                                    val relIndex = (0..8).fold(-1) { acc, i ->
                                        when {
                                            i > inventory.selectedSlot -> acc
                                            !getItemStack(i).isEmpty -> acc + 1
                                            else -> acc
                                        }
                                    }
                                    offset(relIndex - offset(0).first)
                                }
                            }
                    }
                    currentItemGroup = currentItemGroup.let {
                        when {
                            !it.switchDisplay(false)-> sidebarToGroup()?.also { newGroup->
                                MinecraftClient.getInstance().player?.inventory?.mainHandStack
                                    ?.takeIf { it.isEmpty }
                                    ?.let { newGroup.offset(0).second.let { newGroup.switchItem(it.item,it.count) } }
                            }
                            it.switchDisplay(false) -> {
                                val regex = Regex("^\\*|\\*\$")
                                val originAffix = oldAffix.replace(regex,"")
                                sortedSetOf<String>()
                                    .apply groupItems@{
                                        if (oldItem.affixOffset(0).first != 0) return@groupItems
                                        addAll(HudData.tagItem().filter { it.key.contains(originAffix) }.values.flatten())
                                    }
                                    .apply {
                                        addAll(HudData.tagItem().values.flatten().toSortedSet().filter { it.contains(originAffix) } )
                                    }.apply {
                                        addAll(HudData.customGroup.filter { it.key.contains(originAffix) }.values.flatMap { it.map { it.id } })
                                    }.toSortedSet()
                                    .takeIf { it.isNotEmpty() }
                                    ?.map { HudData.Item(it) }
                                    ?.filter { it.item != Items.AIR }
                                    ?.takeIf { it.isNotEmpty() }
                                    ?.let { newGroupBuild(it) }
                            }
                            else->null
                        } ?: currentItemGroup
                    }
                    Unit
                }
            }
            KeyData.keyState[GLFW.GLFW_KEY_V]?.apply {
                onShortClick = {
                    run modeSwitch@{ if (currentItemGroup.switchDisplay(false)) {
                        currentItemGroup.modeSwitch(1)
                        currentItemGroup.recomputeOrderNext(true)
                    } }
                }
                onLongPressOne = { addCurrentToList(0) }
            }
        }
    }
}