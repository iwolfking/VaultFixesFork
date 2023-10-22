package net.cdnbcn.vaultfixes.config

import net.cdnbcn.vaultfixes.data.SimpleEventHandler
import net.minecraftforge.common.ForgeConfigSpec
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.fml.config.ModConfig
import net.minecraftforge.fml.event.config.ModConfigEvent
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext

object ConfigManager {
    val config: Config
    val onConfigReload : SimpleEventHandler = SimpleEventHandler()


    private val configSpec: ForgeConfigSpec
    init {
        val (conf, spec) = ForgeConfigSpec.Builder()
            .configure { builder: ForgeConfigSpec.Builder? -> Config(builder!!)}
        conf.initCache()
        config = conf
        configSpec = spec

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, spec)
        FMLJavaModLoadingContext.get().modEventBus.addListener(::onConfigChanged)

    }

    private fun onConfigChanged(event : ModConfigEvent){
        if(event.config.getSpec() == configSpec) {
            onConfigReload.fire()
            config.onUpdate()
        }
    }
}