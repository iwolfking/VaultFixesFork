package net.cdnbcn.vaultfixes.config

import net.minecraftforge.common.ForgeConfigSpec

class Config(builder: ForgeConfigSpec.Builder) {
    private constructor() : this(ForgeConfigSpec.Builder())
    companion object {
        val DEFAULT = Config()
    }

    val modularRouters : ModularRoutersConfig
    init {
        modularRouters = ModularRoutersConfig(builder)
    }

    internal fun initCache() {
        modularRouters.initCache()
    }
    internal fun onUpdate(){
        modularRouters.onUpdate()
    }
}