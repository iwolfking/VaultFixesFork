package net.cdnbcn.vaultfixes.saving

import iskallia.vault.world.data.VaultSnapshots
import net.cdnbcn.vaultfixes.VaultFixes
import net.cdnbcn.vaultfixes.data.TemporalMapCache
import net.cdnbcn.vaultfixes.mixin_interfaces.saving.IVaultPlayerData
import net.cdnbcn.vaultfixes.mixin_interfaces.saving.IVaultPlayerDataRW
import net.cdnbcn.vaultfixes.mixin_interfaces.saving.VaultSnapshotsMixinInterface
import net.minecraft.nbt.*
import net.minecraft.server.level.ServerPlayer
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent
import java.io.File
import java.nio.file.Path
import java.util.*
import kotlin.collections.ArrayList
import kotlin.io.path.createDirectories


object PlayerSaveManger {

    private lateinit var PlayerDataFolder: Path

    @JvmStatic
    internal fun initialize() {
        MinecraftForge.EVENT_BUS.addListener(this::onPlayerJoin)
        MinecraftForge.EVENT_BUS.addListener(this::onPlayerLeave)

        PlayerDataFolder = VaultFixes.getDataDir().resolve("playerdata").createDirectories()
    }

    private val offlinePlayerMap = TemporalMapCache<UUID, IVaultPlayerDataRW>(60, {}, ::saveOffline)

    private fun saveOffline(offlineData: IVaultPlayerDataRW){
        writeNbt(offlineData.`vaultFixes$getPlayerUUID`(), saveData(offlineData))
    }
    @JvmStatic
    private fun onPlayerJoin(event: PlayerLoggedInEvent) {
        val serverPlayer = event.player as ServerPlayer
        val player = serverPlayer as IVaultPlayerDataRW

        // load player data, from offlineCache else from disc
        synchronized(offlinePlayerMap) {
            val offlineData = offlinePlayerMap.remove(serverPlayer.uuid)
            if (offlineData != null)
                copyData(offlineData, player)
            else
                loadData(readNbt(serverPlayer.uuid), player)
        }
    }
    @JvmStatic
    private fun onPlayerLeave(event: PlayerLoggedOutEvent) {
        val serverPlayer = event.player as ServerPlayer
        val player = serverPlayer as IVaultPlayerDataRW

        synchronized(offlinePlayerMap) {
            val offlineData = OfflineVaultPlayerData(serverPlayer.uuid)
            copyData(player, offlineData)
            writeNbt(serverPlayer.uuid, saveData(player))
            offlinePlayerMap[serverPlayer.uuid] = offlineData
        }
    }

    @JvmStatic
    fun getPlayerData(player: ServerPlayer): IVaultPlayerData {
        return player as IVaultPlayerData
    }
    @JvmStatic
    fun getPlayerData(playerId: UUID): IVaultPlayerData {
        val playerList = VaultFixes.getServer().playerList
        synchronized(offlinePlayerMap) {
            val onlinePlayer = playerList.getPlayer(playerId)
            if(onlinePlayer != null)
                return getPlayerData(onlinePlayer)

            val offlineData = offlinePlayerMap.getOrDefault(playerId, null)
            if(offlineData != null)
                return offlineData

            val offlinePlayerData = OfflineVaultPlayerData(playerId)
            loadData(readNbt(playerId), offlinePlayerData)
            offlinePlayerMap[playerId] = offlinePlayerData
            return offlinePlayerData
        }
    }

    private fun getFileLoc(playerId: UUID) : File {
        return PlayerDataFolder.resolve("$playerId.nbt.dat").toFile()
    }
    private fun readNbt(playerId: UUID) : CompoundTag {
        val file = getFileLoc(playerId)
        return if(file.exists())
            NbtIo.readCompressed(file)
        else
            CompoundTag()
    }
    private fun writeNbt(playerId: UUID, tag: CompoundTag) {
        val file = getFileLoc(playerId)
        NbtIo.writeCompressed(tag, file)
    }


    private fun copyData(from: IVaultPlayerDataRW, into: IVaultPlayerDataRW) {
        if (from.`vaultFixes$isDirty`()) into.`vaultFixes$markDirty`() else into.`vaultFixes$markClean`()
        into.`vaultFixes$setSnapshots`(from.`vaultFixes$getSnapshots`())
    }
    private fun loadData(tag: CompoundTag, data: IVaultPlayerDataRW) {
        val uuid = data.`vaultFixes$getPlayerUUID`()
        val vaultSnapshots = VaultSnapshots.get(VaultFixes.getServer()) as VaultSnapshotsMixinInterface

        @Suppress("NAME_SHADOWING")
        val tag =
            if(!tag.contains("version") || tag.getInt("version") != 1)
                CompoundTag() // wipe save data if version mis-match
            else
                tag

        data.`vaultFixes$setSnapshots`(
            if(tag.contains("snapshots"))
                (tag["snapshots"] as ListTag).parallelStream()
                    .map(NbtUtils::loadUUID)
                    .collect(::ArrayList, ArrayList<UUID>::add, ArrayList<UUID>::addAll)
            else
                // TODO: don't query all vault snapshots if player is vault lvl0
                vaultSnapshots.`vaultFixes$compileAllForPlayer`(uuid)
        )
        data.`vaultFixes$markClean`()
    }
    private fun saveData(data: IVaultPlayerDataRW): CompoundTag {
        val tag = CompoundTag()

        tag.putInt("version", 1)
        tag.put("snapshots", data.`vaultFixes$getAllSnapshots`().map(NbtUtils::createUUID).collect(::ListTag, ListTag::add, ListTag::addAll))

        data.`vaultFixes$markClean`()
        return tag
    }

}