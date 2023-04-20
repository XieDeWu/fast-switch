package cn.xdw.data

import net.minecraft.client.MinecraftClient
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.item.Item as MItem
import net.minecraft.util.Identifier
import net.minecraft.util.math.noise.OctavePerlinNoiseSampler
import net.minecraft.util.math.random.LocalRandom
import net.minecraft.util.registry.Registry
import kotlin.math.E
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
            val tags = item.registryEntry.streamTags().map { it.id.toString() }.toList()
                ?: (item as? BlockItem)?.block?.defaultState?.registryEntry?.streamTags()?.map { it.id.toString() }?.toList()
                ?: listOf("Null Tags")
            var tagIndex = 0
            {when{
                tags.isNotEmpty() ->{
                    tagIndex = (tagIndex + it).coerceIn(tags.indices)
                    tags[tagIndex]
                }
                else->"Null Tag"
            } }
        },
    )
    data class ItemGroup(
        var items: MutableList<Item>,
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
                cursor = (cursor+it).coerceIn(items.indices)
                val rt = cursor to items[cursor]
                if(it!=0) switchItem(rt.second.item)
                rt
            }
        },
        val perlinNextItem:()->Unit = run {
            val noiseSampler by lazy{ { seed:Long,hz:Int->
                val noiseSampler = OctavePerlinNoiseSampler.create(LocalRandom(seed), 6, 32.0, 26.0, 20.0, 14.0)
                (1 .. hz).map { noiseSampler.sample(it.toDouble() * ln(E + hz) / 100, .0, .0) }
            } }
            val noiseToWeight by lazy{ { noiseList: List<Double>, weights: List<Int> ->
                val noiseVK = noiseList.mapIndexed { index, d -> d to index }.sortedBy { it.first }
                fun spl(noiseVK: List<Pair<Double, Int>>, weights: List<Int>, index: Int): List<Pair<Int, Int>> {
                    if (weights.isEmpty()) return listOf()
                    val totalWeight = weights.sum()
                    val headWeight = weights.first()
                    val tailWeights = weights.drop(1)
                    val headNumSize = (1.0 * headWeight / totalWeight * noiseVK.size).toInt()
                    val noiseToWeightByIndex = noiseVK.slice(0 until headNumSize).map { it.second to index }
                    val tailNoiseVK = noiseVK.drop(headNumSize)
                    return noiseToWeightByIndex.plus(spl(tailNoiseVK, tailWeights, index + 1))
                }
                spl(noiseVK,weights,0).sortedBy { it.first }
            } }
            var callNum = 0
            {
                ++callNum
                if(items.size > 0){
                    val seed = items.fold(233){old,new->old xor new.count}
                    val sum = items.sumOf { it.count }
                    val noises = noiseSampler(seed.toLong(),sum)
                    val weights = items.map { it.count }
                    val placeList = noiseToWeight(noises, weights).map { it.second }
                    val currentCursor = offset(0).first
                    val targetCursor = placeList[callNum % placeList.size]
                    offset(targetCursor-currentCursor)
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