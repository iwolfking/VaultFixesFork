package net.cdnbcn.vaultfixes.config

import net.cdnbcn.vaultfixes.data.SimpleEventHandler
import net.minecraftforge.common.ForgeConfigSpec
import net.minecraftforge.common.ForgeConfigSpec.IntValue

class ModularRoutersConfig(builder: ForgeConfigSpec.Builder){
    private var cacheActivatorModuleLimiter: Int = 0
    val activatorModuleLimiter: IntValue
    val onLimiterUpdate = SimpleEventHandler()
    init
    {
        builder.push("modular_routers")
        try {
            activatorModuleLimiter = builder.comment("limit min tick rate for any router using an activator module (-1 = disabled)")
                .defineInRange("activator_module_limit", -1, -1, 20*60)
        } finally {
            builder.pop()
        }
    }
    internal fun initCache(){
        cacheActivatorModuleLimiter = activatorModuleLimiter.get()
    }
    internal fun onUpdate() {
        if (
            cacheActivatorModuleLimiter != activatorModuleLimiter.get()
        ) {
            cacheActivatorModuleLimiter = activatorModuleLimiter.get()
            onLimiterUpdate.fire()
        }
    }
}
