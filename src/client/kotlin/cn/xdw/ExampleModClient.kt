package cn.xdw

import cn.xdw.data.KeyData
import cn.xdw.handle.KeyHandle
import cn.xdw.handle.PlayerHandle
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback

object ExampleModClient : ClientModInitializer {
	override fun onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		HudRenderCallback.EVENT.register(Hud())
		KeyData.register()
		KeyHandle.registry()
		PlayerHandle.register()
	}
}