package cn.xdw.data

import net.minecraft.item.BlockItem
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry


class HudData {
    data class Item(
        var id:String = "",
        var count: Int = 1,
        var tag: (Int) -> String = run {
            val tags = (Registry.ITEM.get(Identifier.tryParse(id)) as BlockItem).let { it ->
                it.block.defaultState.registryEntry.streamTags().map { it.id.toString() }.toList()
            }
            var tagIndex = 0
            ({ offset: Int ->
                tagIndex = ((tagIndex+offset)%tags.size).coerceIn(0, tags.size)
                tags[tagIndex]
            })
        }
    )
    data class ItemGroup(
        var items: MutableList<Item> = mutableListOf(),
        var cursor: Int = (items.size + 1) / 2,
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