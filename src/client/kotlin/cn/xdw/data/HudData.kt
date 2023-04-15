package cn.xdw.data

import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry


class HudData {
    data class Item(
        val id:String = "",
        val item: net.minecraft.item.Item = Registry.ITEM.getOrEmpty(Identifier.tryParse(id)).get(),
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
        }
    )
    data class ItemGroup(
        var items: MutableList<Item> = mutableListOf(),
        var offset: (Int) -> Pair<Int, Item> = run {
            var cursor = (items.size+1)/2
            ({offset:Int->
                cursor = (cursor+offset).coerceIn(0, items.size-1)
                cursor to items[cursor]
            })
        },
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