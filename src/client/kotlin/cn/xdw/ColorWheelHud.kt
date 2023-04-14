package cn.xdw

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.ItemStack
import net.minecraft.item.Items


class ColorWheelHud:HudRenderCallback {
    override fun onHudRender(matrixStack: MatrixStack?, tickDelta: Float) {
        val client = MinecraftClient.getInstance()
        val x = client.window.scaledWidth
        val y = client.window.scaledHeight
        matrixStack?.push()
        val itemRenderer = MinecraftClient.getInstance().itemRenderer
        val items = List(16){ItemStack(Items.APPLE,64)}
        val curIndex = 8
        items.forEachIndexed { index, item ->
            val i = 16 * (index - curIndex) + x / 2 - 8
            val j = 16*2+y/2-8
            itemRenderer.renderInGuiWithOverrides(item, i, j)
            itemRenderer.renderGuiItemOverlay(client.textRenderer,item, i, j)
        }
    }
}