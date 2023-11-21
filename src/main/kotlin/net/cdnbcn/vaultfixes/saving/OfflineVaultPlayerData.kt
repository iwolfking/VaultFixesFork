package net.cdnbcn.vaultfixes.saving

import net.cdnbcn.vaultfixes.mixin_interfaces.saving.IVaultPlayerDataRW
import java.util.*
import java.util.stream.Stream

internal class OfflineVaultPlayerData(val playerId: UUID) : IVaultPlayerDataRW {

    private var isDirty = false
    override fun `vaultFixes$isDirty`(): Boolean { return isDirty }
    override fun `vaultFixes$markClean`() { isDirty = false }
    override fun `vaultFixes$markDirty`() { isDirty = true }



    override fun `vaultFixes$isOnline`(): Boolean { return false }
    override fun `vaultFixes$getPlayerUUID`(): UUID { return playerId }



    private lateinit var snapshots: ArrayList<UUID>
    override fun `vaultFixes$getAllSnapshots`(): Stream<UUID> { return snapshots.parallelStream() }
    override fun `vaultFixes$addSnapshot`(snapshotId: UUID) { this.snapshots.add(snapshotId); isDirty = true }
    override fun `vaultFixes$setSnapshots`(snapshots: ArrayList<UUID>) { this.snapshots = snapshots }
    override fun `vaultFixes$getSnapshots`(): ArrayList<UUID> { return this.snapshots }
}