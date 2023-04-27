package cn.xdw

import cn.xdw.data.HudData
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.ItemStack
import kotlin.math.absoluteValue


class Hud:HudRenderCallback {
    override fun onHudRender(matrixStack: MatrixStack?, tickDelta: Float) {
        if(!HudData.currentItemGroup.switchDisplay(false)) return
        val client = MinecraftClient.getInstance()
        val x = client.window.scaledWidth
        val y = client.window.scaledHeight

        HudData.currentItemGroup.takeIf { it.items.isNotEmpty() }?.let { it ->
            matrixStack?.push()
            val cursor = it.offset(0)
            it.items
                .map { ItemStack(it.item,it.count) }
                .forEachIndexed each@{ index, item ->
                    val rel = index - cursor.first
                    if(rel.absoluteValue > it.displayWidth) return@each
                    val i = 16 * rel + x / 2 - 8
                    val j = 16*2+y/2-8
                    val itemRenderer = client.itemRenderer
                    itemRenderer.renderInGuiWithOverrides(matrixStack,item, i, j)
                    itemRenderer.renderGuiItemOverlay(matrixStack,client.textRenderer,item, i, j)
            }
            val drawIterator = { initPos:Int->
                var index = initPos
                { text:String,color:Int->
                    val textRenderer = client.textRenderer
                    val u = (x - textRenderer.getWidth(text)) / 2 + 1
                    val v = 16*index+y/2-7
                    textRenderer.draw(matrixStack, text, u.toFloat(), v.toFloat(), color)
                    index+=1
                }
            }
            drawIterator(3).let {
                it("^",0xFFFF00)
                it(cursor.second.affixOffset(0).second,0xFFFF00)
            }
            matrixStack?.pop()
        }
    }
}