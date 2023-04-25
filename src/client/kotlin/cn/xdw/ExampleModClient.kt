package cn.xdw

import cn.xdw.data.KeyData
import cn.xdw.handle.CommandHandle
import cn.xdw.handle.KeyHandle
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback

object ExampleModClient : ClientModInitializer {
	override fun onInitializeClient() {
		HudRenderCallback.EVENT.register(Hud())
		KeyData.register()
		KeyHandle.registry()
		CommandHandle.registry()
	}
}