package cn.xdw

import cn.xdw.data.ItemGroupList
import cn.xdw.data.KeyPressState
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.InputUtil
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import org.lwjgl.glfw.GLFW


class ColorWheelHud:HudRenderCallback {
    @Suppress("NestedLambdaShadowedImplicitParameter")
    override fun onHudRender(matrixStack: MatrixStack?, tickDelta: Float) {
        val client = MinecraftClient.getInstance()
        if(KeyPressState.keyState[GLFW.GLFW_KEY_LEFT_ALT]?.isPress() != true) return
        val x = client.window.scaledWidth
        val y = client.window.scaledHeight

        ItemGroupList.currentItemGroup.let { it ->
            matrixStack?.push()
            it.items
                .map { ItemStack(Registry.ITEM.getOrEmpty(Identifier(it.id)).orElse(null),it.count) }
                .forEachIndexed { index, item ->
                    val i = 16 * (index - it.cursor) + x / 2 - 8
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
                it(KeyPressState.keyState[GLFW.GLFW_KEY_LEFT_ALT]?.tickNum.toString(),0xFFFF00)
                val draw = it
                client.player?.let { (it.mainHandStack.item as? BlockItem)
                    ?.let { it.block.defaultState.registryEntry.streamTags()
                        .map { it.id.toString() }
                        .reduce{acc,key->"$acc,$key"}
                    }?.let { draw(it.get(),0xFFFF00) }
                }
            }
            matrixStack?.pop()
        }
    }
}