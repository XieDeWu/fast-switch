package cn.xdw.data

import cn.xdw.data.HudData.RandomMode.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.text.Text
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.math.noise.OctavePerlinNoiseSampler
import net.minecraft.util.math.random.LocalRandom
import net.minecraft.util.registry.Registry
import java.io.FileReader
import java.io.FileWriter
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
        val tags: List<String> = (listOf(id)
                +HudData.customGroup.filter { it.value.any{ it.id == id } }.map { it.key }.sorted()
                +((item.registryEntry.streamTags().map { it.id.toString() }.toList()?: listOf())
                +((item as? BlockItem)?.block?.defaultState?.registryEntry?.streamTags()?.map { it.id.toString() }?.toList()?: listOf())
                ).sorted().distinct()).takeIf { it.isNotEmpty() }
            ?: listOf("Null Tags"),
        val tagOffset: (Int) -> Pair<Int,String> = run {
            var tagIndex = 0
            {
                tagIndex = (tagIndex + it).coerceIn(tags.indices)
                tagIndex to tags[tagIndex]
            }
        },
        val tagByName: (String)->Boolean = { name->
            tags.indexOfFirst { it == name }.takeIf { it in tags.indices }?.let {
                tagOffset(it - tagOffset(0).first)
                true
            } == true
        },
        val affixes:()->List<String> = run {
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
                val (namespace, name) = regex.matchEntire(it)?.destructured ?: return@splitID listOf(it)
                listOf(it) + splitAffix(name).map { "*${it}*" } + listOf("${namespace}*")
            } };
            {
                splitID(tags[tagOffset(0).first])
            }
        },
        val affixOffset: (Int)->Pair<Int,String> = run {
            val tagAffixIndex = List(tags.size){ 0 }.toMutableList();
            {
                val tagIndex = tagOffset(0).first
                val arr = affixes()
                tagAffixIndex[tagIndex] = (tagAffixIndex[tagIndex] + it).coerceIn(arr.indices)
                tagAffixIndex[tagIndex] to arr[tagAffixIndex[tagIndex]]
            }
        },
        val affixByName: (String)->Boolean = { name->
            val arr = affixes()
            arr.indexOfFirst { it == name }.takeIf { it in arr.indices }?.let {
                affixOffset(it - affixOffset(0).first)
                true
            } == true
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
        val workHand: (Hand?) -> Hand = run {
            var hand = Hand.MAIN_HAND
            {
                it?.also{ hand = it }
                hand
            }
        },
        val switchItem: (MItem,Int)->Unit = { item,count->
            val client = MinecraftClient.getInstance()
            val player = client.player
            val inventory = player?.inventory
            val inter = client.interactionManager
            when{
                client!=null && player!=null && inventory!=null && inter !=null ->{
                    val handSlot = when(workHand(null)){ Hand.MAIN_HAND->inventory.selectedSlot + 36; Hand.OFF_HAND->45 }
                    when{
                        player.isCreative->{
                            player.networkHandler.sendPacket(CreativeInventoryActionC2SPacket(handSlot, ItemStack(item,count)))
                        }
                        else->{
                            val click:(Int,Int,SlotActionType)->Unit = { slotId,button,mode->
                                inter.clickSlot(player.currentScreenHandler.syncId, slotId, button, mode, client.player)
                            }
                            inventory.main.indexOfFirst { it.isEmpty.not() && it.isOf(item) }
                                .takeIf { it >= 0 && it != player.inventory.selectedSlot }
                                ?.also{
                                    click(handSlot, 0, SlotActionType.QUICK_MOVE)
                                    click(it+when(it){ in 0..8 ->36 else->0 }, 0, SlotActionType.PICKUP)
                                    click(handSlot, 0, SlotActionType.PICKUP)
                                }
                        }
                    }
                }
            }
        },
        val offset: (Int) -> Pair<Int, Item> = run {
            var cursor = (items.size+1)/2
            val player = MinecraftClient.getInstance().player
            val isChanged: (String,Boolean) -> Boolean = run {
                var prev: Pair<String, Boolean>? = null
                { id, isEmpty ->
                    val curr = Pair(id, isEmpty)
                    if (prev == curr) {
                        false
                    } else {
                        prev = curr
                        true
                    }
                }
            };
            { offset->
                cursor = (cursor+offset).coerceIn(items.indices)
                (cursor to items[cursor])
                    .also { rt-> when(workHand(null)){
                            Hand.MAIN_HAND->player?.mainHandStack
                            Hand.OFF_HAND->player?.offHandStack
                        }
                        ?.takeIf{ switchDisplay(false) && isChanged(rt.second.id,it.isEmpty) || offset!=0 }
                        ?.let{ switchItem(rt.second.item,rt.second.count) }
                    }
            }
        },
        val offsetByName: (String) -> Boolean = { name->
            items.indexOfFirst { it.id == name }.takeIf { it in items.indices }?.let {
                offset(it - offset(0).first)
                true
            } == true
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
                var count = items.filterIndexed{ index, _ -> index in 0 until offset(0).first }.sumOf { it.count };
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
                var seed = Random.nextLong();
                {
                    val noises = noiseSampler(seed, items.sumOf { it.count })
                    val weights = items.map { it.count }
                    val placeList = noiseToWeight(noises, weights).map { it.second }
                    val currentCursor = offset(0).first
                    val targetCursor = placeList[callNum % placeList.size]
                    offset(targetCursor - currentCursor)
                    (++callNum).also { if(it % weights.sum() == 0) seed = Random.nextLong() }
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
    @Serializable
    data class JsonItem(
        val id: String = "minecraft:air",
        val count: Int = 1,
    )
    companion object{
        var customGroup: Map<String,List<JsonItem>> = sortedMapOf()
        val tagItem = run {
            var oldCustom = customGroup
            val getTags:(Int)->SortedMap<String, Set<String>> by lazy{{
                listOf(Registry.ITEM, Registry.BLOCK).fold(mapOf<String, List<String>>()) { old, new ->
                    old + new.streamTagsAndEntries().toList()
                        .map { it.first.id.toString() to it.second.map { it.key.get().value.toString() } }
                }.asSequence().groupBy({ it.key }, { it.value })
                    .mapValues { (_, values) -> values.flatten().toSet() }
                    .toSortedMap()
            }}
            var calcID = 0
            {
                calcID += when{
                    oldCustom !== customGroup ->1.also { oldCustom = customGroup }
                    calcID > 3 ->0
                    else->0
                }
                getTags(calcID)
            }
        }
        var currentItemGroup = ItemGroup(listOf(Item()))
        val syncConfig:(String)->Unit = { opt->
            FabricLoader.getInstance().configDir.resolve("fast-switch.json")
                .toFile()
                .takeIf { when(it.exists()){ true->true else->it.createNewFile()}}
                ?.also {
                    when(opt){
                        "save"->FileWriter(it).apply {
                            write(Json.encodeToString(customGroup))
                            flush()
                            close()
                        }
                        "load"->FileReader(it).apply {
                            customGroup = runCatching { Json.decodeFromString<Map<String,List<JsonItem>>>(readText()).toSortedMap() }.getOrDefault(customGroup)
                        }
                    }
                }
        }
        fun registry() {
            syncConfig("load")
        }
    }
}