package cn.xdw.data

class ItemGroupList {
    data class Item(
        var id:String = "",
        var count: Int = 1,
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