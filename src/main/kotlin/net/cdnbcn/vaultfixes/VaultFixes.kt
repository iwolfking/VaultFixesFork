package net.cdnbcn.vaultfixes

import com.electronwill.nightconfig.core.file.FileConfig
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.logging.LogUtils
import iskallia.vault.world.data.PlayerAbilitiesData
import iskallia.vault.world.data.PlayerTalentsData
import iskallia.vault.world.data.PlayerVaultStatsData
import iskallia.vault.world.data.QuestStatesData
import iskallia.vault.world.data.VaultSnapshots
import net.cdnbcn.vaultfixes.config.ConfigManager.config
import net.cdnbcn.vaultfixes.data.TemporalMapCache.Companion.doCleanUpAll
import net.cdnbcn.vaultfixes.mixin_interfaces.saving.VaultSnapshotsMixinInterface
import net.cdnbcn.vaultfixes.saving.PlayerSaveManger.initialize
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.TextComponent
import net.minecraft.server.MinecraftServer
import net.minecraft.server.dedicated.DedicatedServerProperties
import net.minecraft.server.level.ServerPlayer
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.event.server.ServerStoppingEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.loading.FMLEnvironment
import net.minecraftforge.fml.loading.FMLPaths
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
    val newDataStructureEnabled : Boolean


    init {
        if (!FMLEnvironment.dist.isDedicatedServer) {
            logger.error("VaultFixes is only for DedicatedServers, REFUSING TO LOAD")
            throw RuntimeException("VaultFixes is only for DedicatedServers, REFUSING TO LOAD")
        }

        try {
            val mixinConfig = FileConfig.of(FMLPaths.CONFIGDIR.get().resolve("vault-fixes-mixins.toml").toFile())
            mixinConfig.load()
            newDataStructureEnabled = mixinConfig.get("optimize_vault_data_storage")
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
        FORGE_BUS.addListener(this::onCommandRegistration)
        initialize()
    }

    private fun onServerStopping(@Suppress("UNUSED_PARAMETER") event: ServerStoppingEvent) {
        doCleanUpAll() // eject all potentially unsaved data
    }

    private fun onCommandRegistration(event: RegisterCommandsEvent) {
        event.dispatcher.register(
            literal<CommandSourceStack?>("correctmyskillpoints").executes {
                if(it.source.entity is ServerPlayer) {
                    val player = it.source.entity as ServerPlayer
                    val statsData: PlayerVaultStatsData = PlayerVaultStatsData.get(it.source.server)
                    val playerStats = statsData.getVaultStats(player)

                    var totalPoints = playerStats.vaultLevel
                    if(QuestStatesData.get().getState(player).completed.contains("learning_skills"))
                        totalPoints += 1

                    totalPoints -= (
                            PlayerAbilitiesData.get(it.source.server).getAbilities(player).spentLearnPoints +
                                    PlayerTalentsData.get(it.source.server).getTalents(player).spentLearnPoints +
                                    playerStats.unspentSkillPoints
                            )

                    statsData.addRegretPoints(player, -playerStats.unspentRegretPoints)
                    statsData.addSkillPoints(player, totalPoints)

                    if(totalPoints > 0) {
                        it.source.sendSuccess(TextComponent("$totalPoints has been returned to you."), false)
                    } else if (totalPoints < 0) {
                        it.source.sendSuccess(TextComponent("You have ${-totalPoints} extra points spent, these are being subtracted from your balance.\nYou can use regret orbs to return to zero skill points if in the negatives"), false)
                    }

                    return@executes 0
                } else {
                    it.source.sendFailure(TextComponent("This must be run by a player"))
                    return@executes 1
                }
            }
        )
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
