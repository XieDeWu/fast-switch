package cn.xdw.test

import net.minecraft.util.math.noise.OctavePerlinNoiseSampler
import net.minecraft.util.math.random.LocalRandom
import org.jetbrains.letsPlot.geom.geomHLine
import org.jetbrains.letsPlot.geom.geomPoint
import org.jetbrains.letsPlot.ggsize
import org.jetbrains.letsPlot.letsPlot
import org.junit.jupiter.api.Test
import kotlin.math.E
import kotlin.math.ln
import kotlin.random.Random

@Suppress("NestedLambdaShadowedImplicitParameter", "RedundantSemicolon")
class Test{
    @Suppress("NestedLambdaShadowedImplicitParameter")
    @Test
    fun perlin1DTest() {
        val noiseSampler = { hz:Int->
            val noiseSampler = OctavePerlinNoiseSampler.create(LocalRandom(Random.nextLong()), 6, 32.0, 26.0, 20.0, 14.0)
            (1 .. hz).map { noiseSampler.sample(it.toDouble() * ln(E + hz) / 100, .0, .0) }
        }
        val noiseToWeight = { noiseList: List<Double>, weights: List<Int> ->
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
        }
        val getLines = { noiseList:List<Double>,weightList:List<Int>->
            data class Gdata(
                val group:Int = 0,
                val min:Double = Double.MIN_VALUE,
                val max:Double = Double.MAX_VALUE,
            )
            var lines = listOf<Double>()
            noiseToWeight(noiseList, weightList)
                .groupBy{ it.second }
                .toSortedMap()
                .map {
                    val sorted = it.value.map { noiseList[it.first] }.sorted()
                    Gdata(it.key,sorted.first(),sorted.last())
                }
                .reduce{ old, new ->
                    if(old.group != new.group) lines = lines.plus(listOf(old.max,new.min).average())
                    new
                }
            lines
        }

        val noises = noiseSampler(1000)
        val weights = listOf(2,2,4,8,8,16)
        val lines = getLines(noises,weights)
        val groupBy = noiseToWeight(noises, weights)
            .groupBy { it.second }
            .toSortedMap()
            .mapValues { it.value.count() }

        val data = mapOf (
            "x" to (1 .. noises.size).map { it },
            "y" to noises
        )
        var p = letsPlot(data)
        p += geomPoint(color="dark_green", alpha=.3){x="x"; y="y"}
        p + ggsize(1600, 400)
        lines.map{ p += geomHLine(data, yintercept = it, linetype = "dashed", size = 1.0, color = "blue") }
        p.show()
        Thread.sleep(99999)
    }
    @Test
    fun affixParse(){
        val splitNameSpace = run {
            val splitAffix:(String)->List<String> = run {
                val affixRegex = "(_|\\p{Alpha}+)".toRegex();
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
            };
            splitNameSpace@{ it: String ->
                val regex = """([^:]+:)(.+)""".toRegex()
                val (namespace, name) = regex.matchEntire(it)?.destructured ?: return@splitNameSpace listOf(it)
                val affixes = listOf(it) + splitAffix(name).map { "${namespace}*${it}*" }
                affixes
            }
        }
        val affixes = splitNameSpace("minecraft:light_gray_candle")
        val b = 1
    }
}