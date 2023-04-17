package cn.xdw.data

import net.minecraft.client.MinecraftClient
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.item.Item as MItem
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.pow
import kotlin.random.Random


class HudData {
    data class Item(
        val id:String = "minecraft:air",
        val item: MItem = Registry.ITEM.getOrEmpty(Identifier.tryParse(id)).get(),
        var count: Int = 1,
        var tag: (Int) -> String = run {
            val tags = (item as BlockItem).let { it ->
                it.block.defaultState.registryEntry.streamTags().map { it.id.toString() }.toList()
            }
            var tagIndex = 0
            ({ offset: Int ->
                tagIndex = (tagIndex+offset).coerceIn(0, tags.size-1)
                tags[tagIndex]
            })
        },
    )
    data class ItemGroup(
        var items: MutableList<Item> = mutableListOf(),
        var switchDisplay: (Boolean) -> Boolean = run {
            var display = false
            {
                display = when{it->!display else->display}
                display
            }
        },
        val switchItem: (MItem)->Unit = {
            val client = MinecraftClient.getInstance()
            val player = client.player
            val inventory = player?.inventory
            val inter = client.interactionManager
            when{
                client!=null && player!=null && inventory!=null && inter !=null ->{
                    inter.clickSlot(player.currentScreenHandler.syncId, inventory.selectedSlot + 36, 0, SlotActionType.QUICK_MOVE, client.player)
                    player.dropSelectedItem(true)
                    val slot: Int = inventory.getSlotWithStack(ItemStack(it))
                    if (slot>=0 && slot!= inventory.selectedSlot) {
                        inter.pickFromInventory(slot)
                    }
                }
            }
        },
        var offset: (Int) -> Pair<Int, Item> = run {
            var cursor = (items.size+1)/2
            {
                cursor = (cursor+it).coerceIn(0, items.size-1)
                val rt = cursor to items[cursor]
                if(it!=0) switchItem(rt.second.item)
                rt
            }
        },
        val perlinNextItem:()->Unit = run {
            val perlin1d: (Int)->(Double)->Double = { seed->run {
                val ease = { y0: Double, y1: Double, t: Double ->
                    val t3 = t.pow(3)
                    val t4 = t3 * t
                    val t5 = t4 * t
                    ((6 * t5 - 15 * t4 + 10 * t3) * y1 + (-6 * t5 + 15 * t4 - 10 * t3 + 1) * y0)
                }
                { x: Double ->
                    val x0 = floor(x).toInt()
                    val x1 = x0 + 1
                    val f0 = Random(seed+x0).nextDouble()
                    val f1 = Random(seed+x1).nextDouble()
                    val t = x - x0
                    ease(f0, f1, t)
                }
            } }
            var x = .0
            {
                val curIndex = offset(0).first
                when{
                    items.size > 0 ->{
                        val weightSum = items.fold(0){acc,next->acc+next.count }
                        val hz = (3+2*ln(weightSum.toDouble().coerceAtLeast(1.0)) )
                        x += 1/hz
                        val targetIndex = items
                            .mapIndexed{index,t-> index to (t.count * perlin1d(weightSum + index)(x)) }
                            .sortedByDescending { it.second }[0].first
                        offset(targetIndex-curIndex)
                    }
                }
            }
        },
        val randomNextItem:()->Item = {when{items.size>0->items[Random.nextInt(items.size-1)] else->Item()}},
    )
    companion object{
        var currentItemGroup: ItemGroup = ItemGroup(mutableListOf(
            Item("minecraft:oak_log"),
            Item("minecraft:spruce_log"),
            Item("minecraft:birch_log"),
        ))
        var itemGroupList = mutableListOf(
            ItemGroup(mutableListOf(
                    Item("minecraft:oak_log"),
                    Item("minecraft:spruce_log"),
                    Item("minecraft:birch_log"),
                )),
            ItemGroup(mutableListOf(
                    Item("minecraft:oak_planks"),
                    Item("minecraft:spruce_planks"),
                    Item("minecraft:birch_planks"),
                )),
            ItemGroup(mutableListOf(
                    Item("minecraft:oak_stairs"),
                    Item("minecraft:spruce_stairs"),
                    Item("minecraft:birch_stairs"),
                )),
        )
        fun save(){

        }
        fun load(){

        }
    }
}