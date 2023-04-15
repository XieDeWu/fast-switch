package cn.xdw

import cn.xdw.data.HudData
import cn.xdw.data.KeyData
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.ItemStack
import org.lwjgl.glfw.GLFW


class ColorWheelHud:HudRenderCallback {
    override fun onHudRender(matrixStack: MatrixStack?, tickDelta: Float) {
        val client = MinecraftClient.getInstance()
        if(KeyData.keyState[GLFW.GLFW_KEY_LEFT_ALT]?.isPress() != true) return
        val x = client.window.scaledWidth
        val y = client.window.scaledHeight

        HudData.currentItemGroup.let { it ->
            matrixStack?.push()
            val cursor = it.offset(0)
            it.items
                .map { ItemStack(it.item,it.count) }
                .forEachIndexed { index, item ->
                    val i = 16 * (index - cursor.first) + x / 2 - 8
                    val j = 16*2+y/2-8
                    val itemRenderer = client.itemRenderer
                    itemRenderer.renderInGuiWithOverrides(item, i, j)
                    itemRenderer.renderGuiItemOverlay(client.textRenderer,item, i, j)
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
                it(cursor.second.tag(0),0xFFFF00)
                it(KeyData.keyState[GLFW.GLFW_KEY_LEFT_ALT]?.let { it.pressHandle(null) }.toString(),0xFFFF00)
            }
            matrixStack?.pop()
        }
    }
}