package cn.xdw

import cn.xdw.data.KeyPressState
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback

object ExampleModClient : ClientModInitializer {
	override fun onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		HudRenderCallback.EVENT.register(ColorWheelHud())
		KeyPressState.register()
	}
}