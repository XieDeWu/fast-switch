package cn.xdw

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.InputUtil
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import org.lwjgl.glfw.GLFW


class ColorWheelHud:HudRenderCallback {
    companion object{
        var curIndex = 8
    }
    override fun onHudRender(matrixStack: MatrixStack?, tickDelta: Float) {
        val client = MinecraftClient.getInstance()
        if(!InputUtil.isKeyPressed(client.window.handle, GLFW.GLFW_KEY_LEFT_ALT)) return

        val x = client.window.scaledWidth
        val y = client.window.scaledHeight
        matrixStack?.push()
        val itemRenderer = MinecraftClient.getInstance().itemRenderer
        val items = List(16){ItemStack(Items.APPLE,64)}
        items.forEachIndexed { index, item ->
            val i = 16 * (index - curIndex) + x / 2 - 8
            val j = 16*2+y/2-8
            itemRenderer.renderInGuiWithOverrides(item, i, j)
            itemRenderer.renderGuiItemOverlay(client.textRenderer,item, i, j)
        }
        val textRenderer = MinecraftClient.getInstance().textRenderer
        val u = (x - textRenderer.getWidth("^")) / 2 + 1
        val v = 16*3+y/2-7
        textRenderer.draw(matrixStack, "^", u.toFloat(), v.toFloat(), 0xFFFF00)
        matrixStack?.pop()
    }
}