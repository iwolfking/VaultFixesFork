package net.cdnbcn.vaultfixes

import com.mojang.logging.LogUtils
import iskallia.vault.world.data.VaultSnapshots
import net.cdnbcn.vaultfixes.config.ConfigManager.config
import net.cdnbcn.vaultfixes.data.TemporalMapCache.Companion.doCleanUpAll
import net.cdnbcn.vaultfixes.mixin_interfaces.saving.VaultSnapshotsMixinInterface
import net.cdnbcn.vaultfixes.saving.PlayerSaveManger.initialize
import net.minecraft.server.MinecraftServer
import net.minecraft.server.dedicated.DedicatedServerProperties
import net.minecraftforge.event.server.ServerStoppingEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.loading.FMLEnvironment
import net.minecraftforge.server.ServerLifecycleHooks
import org.slf4j.Logger
import thedarkcolour.kotlinforforge.forge.FORGE_BUS
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

@Mod("vaultfixes")
object VaultFixes {
    /**
     * a publicly accessible method to get the logger (for use within a mixin).
     *
     * @return VaultFixes Logger
     */
    val logger: Logger = LogUtils.getLogger()
    val dataDir: Path

    init {
        if (!FMLEnvironment.dist.isDedicatedServer) {
            logger.error("VaultFixes is only for DedicatedServers, REFUSING TO LOAD")
            throw RuntimeException("VaultFixes is only for DedicatedServers, REFUSING TO LOAD")
        }

        try {
            dataDir = Files.createDirectories(
                Path.of(
                    DedicatedServerProperties.fromFile(Path.of("server.properties")).levelName,
                    "data",
                    "vaultfixes"
                )
            ).toAbsolutePath()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

        config // invoke initializer to preload config
        FORGE_BUS.addListener(this::onServerStopping)
        initialize()
    }

    private fun onServerStopping(@Suppress("UNUSED_PARAMETER") event: ServerStoppingEvent) {
        doCleanUpAll() // eject all potentially unsaved data
    }

    val server: MinecraftServer
        /**
         * gets the current MinecraftServer Instance
         *
         * @return MinecraftServer Instance
         */
        get() = ServerLifecycleHooks.getCurrentServer()
    val vaultSnapshots: VaultSnapshotsMixinInterface
        /** gets the VaultSnapshots Manager as our own Interface Impl
         *
         * @return (VaultSnapshotsMixinInterface)VaultSnapshots.get(getServer())
         */
        get() = VaultSnapshots.get(server) as VaultSnapshotsMixinInterface
}
