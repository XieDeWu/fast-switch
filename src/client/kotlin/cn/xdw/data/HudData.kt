package cn.xdw.data

import cn.xdw.data.HudData.RandomMode.*
import net.minecraft.client.MinecraftClient
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.noise.OctavePerlinNoiseSampler
import net.minecraft.util.math.random.LocalRandom
import net.minecraft.util.registry.Registry
import java.util.*
import kotlin.math.E
import kotlin.math.ln
import kotlin.random.Random
import kotlin.random.nextInt
import net.minecraft.item.Item as MItem


@Suppress("NestedLambdaShadowedImplicitParameter", "MemberVisibilityCanBePrivate")
class HudData {
    data class Item(
        val id:String = "minecraft:air",
        val item: MItem = Registry.ITEM.getOrEmpty(Identifier.tryParse(id)).orElse(Items.AIR),
        val count: Int = 1,
        val tags: List<String> = (listOf(id)+(
                (item.registryEntry.streamTags().map { it.id.toString() }.toList()?: listOf())
                +((item as? BlockItem)?.block?.defaultState?.registryEntry?.streamTags()?.map { it.id.toString() }?.toList()?: listOf())
                ).sorted().distinct()).takeIf { it.isNotEmpty() }
            ?: listOf("Null Tags"),
        val offset: (Int) -> Pair<Int,String> = run {
            var tagIndex = 0
            {
                tagIndex = (tagIndex + it).coerceIn(tags.indices)
                tagIndex to tags[tagIndex]
            }
        },
        val affixes: (Int)->String = run {
            val splitAffix:(String)->List<String> = run {
                val affixRegex = "(_|/|\\p{Alpha}+)".toRegex();
                splitAffix@{
                    val atoms = affixRegex.findAll(it).map { it.value }.toList()
                    if(atoms.isEmpty()) return@splitAffix listOf(it)
                    val size = atoms.size
                    (size downTo 1).flatMap { w ->
                        atoms.indices.map{ o ->
                            val left = atoms.size-o-w
                            val right = atoms.size-o-1
                            val s = left..right
                            when (s.first) {
                                in atoms.indices ->s.fold("") { old, new -> old + atoms[new] }
                                else ->""
                            }
                        }.filter { !(it == "" || it == "_") }
                    }
                }
            }
            val splitID by lazy{ splitID@{ it: String ->
                val regex = """([^:]+:)(.+)""".toRegex()
                val (_, name) = regex.matchEntire(it)?.destructured ?: return@splitID listOf(it)
                listOf(it) + splitAffix(name).map { "*${it}*" }
            } }
            val tagAffixIndex = List(tags.size){ 0 }.toMutableList();
            {
                val tagIndex = offset(0).first
                val arr = splitID(tags[tagIndex])
                tagAffixIndex[tagIndex] = (tagAffixIndex[tagIndex] + it).coerceIn(arr.indices)
                arr[tagAffixIndex[tagIndex]]
            }
        },
    ){
        init {
            require(tags.isNotEmpty()){"tags is empty!"}
        }
    }
    enum class RandomMode{
        NULL_RANDOM,
        ORDER_CHOOSE,
        PERLIN_RANDOM,
        FULL_RANDOM,
    }
    data class ItemGroup(
        val items: List<Item>,
        val displayWidth:Int = 6+ln(items.size.toDouble()+1).toInt(),
        val switchDisplay: (Boolean) -> Boolean = run {
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
                    when{
                        player.isCreative->{
                            player.networkHandler.sendPacket(CreativeInventoryActionC2SPacket(inventory.selectedSlot + 36, ItemStack(it)))
                        }
                        else->{
                            inter.clickSlot(player.currentScreenHandler.syncId, inventory.selectedSlot + 36, 0, SlotActionType.QUICK_MOVE, client.player)
                            player.dropSelectedItem(true)
                            val slot: Int = inventory.getSlotWithStack(ItemStack(it))
                            if (slot>=0 && slot!= inventory.selectedSlot) {
                                inter.pickFromInventory(slot)
                            }
                        }
                    }
                }
            }
        },
        val offset: (Int) -> Pair<Int, Item> = run {
            var cursor = (items.size+1)/2
            {
                cursor = (cursor+it).coerceIn(items.indices)
                val rt = cursor to items[cursor]
                if(it!=0) switchItem(rt.second.item)
                rt
            }
        },
        val modeSwitch: (Int) -> RandomMode = run {
            var count = 0
            val map = listOf(
                NULL_RANDOM to "无随机",
                ORDER_CHOOSE to "顺序选取",
                PERLIN_RANDOM to "柏林随机",
                FULL_RANDOM to "完全随机",
            );
            {
                count += it
                val next = map[count % map.size]
                MinecraftClient.getInstance().player?.sendMessage(Text.literal(next.second),true)
                next.first
            }
        },
        val recomputeOrderNext:(Boolean?)->Boolean = run {
            var recompute = false
            {
                it?.let { recompute = it }
                recompute
            }
        },
        val nextItem: ()->Unit = run {
            val orderNext: ()->()->Unit by lazy{ { run {
                val placeList = items.foldIndexed(listOf<Int>()) { index, acc, i -> acc + List(i.count) { index } }
                var count = offset(0).first
                {
                    count += 1
                    offset(placeList[count % placeList.size] - offset(0).first)
                }
            } } }
            val perlinNext:()->Unit = run {
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
                    if(items.isNotEmpty()){
                        val seed = items.fold(233){old,new->old xor new.count}
                        val sum = items.sumOf { it.count }
                        val noises = noiseSampler(seed.toLong(),sum)
                        val weights = items.map { it.count }
                        val placeList = noiseToWeight(noises, weights).map { it.second }
                        val currentCursor = offset(0).first
                        val targetCursor = placeList[callNum % placeList.size]
                        offset(targetCursor-currentCursor)
                        ++callNum
                    }
                }
            }
            val fullNext:()->Unit = {
                offset(Random.nextInt(items.indices)-offset(0).first)
            }
            var curOrderNext = orderNext();
            {
                when(modeSwitch(0).also {
                    if(recomputeOrderNext(null)){
                        curOrderNext = orderNext()
                        recomputeOrderNext(false)
                    }
                }){
                    NULL_RANDOM -> Unit
                    ORDER_CHOOSE -> curOrderNext()
                    PERLIN_RANDOM ->perlinNext()
                    FULL_RANDOM ->fullNext()
                }
            }
        },
    ){
        init {
            require(items.isNotEmpty()){"Items is Empty!"}
        }
    }
    companion object{
        val tagItem = run {
            var tags: SortedMap<String, Set<String>> = sortedMapOf();
            { when {
                tags.isNotEmpty() ->tags
                else->{
                    tags = listOf(Registry.ITEM, Registry.BLOCK).fold(mapOf<String, List<String>>()) { old, new ->
                            old + new.streamTagsAndEntries().toList()
                                .map { it.first.id.toString() to it.second.map { it.key.get().value.toString() } }
                        }.asSequence().groupBy({ it.key }, { it.value })
                            .mapValues { (_, values) -> values.flatten().toSet() }
                            .toSortedMap()
                    tags
                }
            } }
        }
        var currentItemGroup = tagItem()["minecraft:logs"]?.let { ItemGroup(it.map { Item(it) }) }
            ?:ItemGroup(listOf(
                Item("minecraft:oak_log"),
                Item("minecraft:spruce_log"),
                Item("minecraft:birch_log"),
            ))
        var itemGroupList = listOf(
            ItemGroup(listOf(
                    Item("minecraft:oak_log"),
                    Item("minecraft:spruce_log"),
                    Item("minecraft:birch_log"),
                )),
            ItemGroup(listOf(
                    Item("minecraft:oak_planks"),
                    Item("minecraft:spruce_planks"),
                    Item("minecraft:birch_planks"),
                )),
            ItemGroup(listOf(
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