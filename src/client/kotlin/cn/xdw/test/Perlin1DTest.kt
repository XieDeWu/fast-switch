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

@Suppress("NestedLambdaShadowedImplicitParameter", "UNCHECKED_CAST")
class Perlin1DTest{
    @Test
    fun perlin1DTest() {
        val placeList = intArrayOf(64, 8, 16).flatMapIndexed { index, count -> IntArray(count) { index }.toList() }.toIntArray()


        val noiseSampler = OctavePerlinNoiseSampler.create(LocalRandom(Random.nextLong()), 6, 32.0,26.0,20.0,14.0)
        val hz = 32
        val data = mapOf (
            "x" to (1 .. hz).map { it },
            "y" to (1 .. hz).map { noiseSampler.sample(it.toDouble() * ln(E + hz) / 100, .0, .0) }
        )

        val split = { values: List<Double>, ratios: List<Int> ->
            val yData = values.sorted()
            val dotNum = yData.size.toDouble()
            val ratiosSum = ratios.sum().toDouble()
            var rate = .0
            ratios.dropLast(1).map {
                (1.0 * it / ratiosSum * dotNum).let {
                    rate += it
                    listOf(rate.toInt(), rate.toInt() + 1).map { yData[it] }.average()
                }
            }
        }


        val splitPoints = split(data["y"] as List<Double>, listOf(1,1,1))

        var p = letsPlot(data)
        p += geomPoint(color="dark_green", alpha=.3){x="x"; y="y"}
        p + ggsize(1600, 400)
        splitPoints.map { p += geomHLine(data, yintercept = it, linetype = "dashed", size = 1.0, color = "blue") }
        p.show()
        Thread.sleep(99999)
    }
}