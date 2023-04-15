package cn.xdw

import cn.xdw.data.ItemGroupList
import cn.xdw.data.KeyPressState
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.InputUtil
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import org.lwjgl.glfw.GLFW


class ColorWheelHud:HudRenderCallback {
    override fun onHudRender(matrixStack: MatrixStack?, tickDelta: Float) {
        val client = MinecraftClient.getInstance()
        if(!InputUtil.isKeyPressed(client.window.handle, GLFW.GLFW_KEY_LEFT_ALT)) return
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
            val flag = "^" to 0xFFFF00
            val textRenderer = client.textRenderer
            val u = (x - textRenderer.getWidth(flag.first)) / 2 + 1
            val v = 16*3+y/2-7
            textRenderer.draw(matrixStack, flag.first, u.toFloat(), v.toFloat(), flag.second)
            textRenderer.draw(matrixStack, KeyPressState.keyState[GLFW.GLFW_KEY_LEFT_ALT]?.tickNum.toString(), u.toFloat(), v.toFloat()+16, flag.second)
            matrixStack?.pop()
        }
    }
}