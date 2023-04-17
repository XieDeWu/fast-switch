package cn.xdw.handle

import cn.xdw.data.HudData
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand

class PlayerHandle {
    companion object{
        fun register(){
            UseBlockCallback.EVENT.register(UseBlockCallback { _, _, hand, _ ->
                if(HudData.currentItemGroup.switchDisplay(false) && hand == Hand.MAIN_HAND){
                    HudData.currentItemGroup.perlinNextItem()
                }
                ActionResult.PASS
            })
        }
    }
}