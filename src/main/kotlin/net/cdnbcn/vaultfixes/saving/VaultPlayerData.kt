package net.cdnbcn.vaultfixes.saving

import net.cdnbcn.vaultfixes.mixin_interfaces.saving.IVaultPlayerDataRW
import java.util.*
import java.util.stream.Stream

internal class VaultPlayerData(private val playerId: UUID) : IVaultPlayerDataRW {

    private var isDirty = false
    override fun `vaultFixes$isDirty`(): Boolean { return isDirty }
    override fun `vaultFixes$markClean`() { isDirty = false }
    override fun `vaultFixes$markDirty`() { isDirty = true }



    override fun `vaultFixes$getPlayerUUID`(): UUID { return playerId }



    private lateinit var snapshots: ArrayList<UUID>
    override fun `vaultFixes$getAllSnapshots`(): Stream<UUID> { return snapshots.parallelStream() }
    override fun `vaultFixes$getLastSnapshots`(amount: Int): Stream<UUID> {
        val to: Int = snapshots.size
        val from = if (to > amount) to - amount else 0

        return if (to == from) Stream.empty() else snapshots.subList(from, to).stream()
    }

    override fun `vaultFixes$addSnapshot`(snapshotId: UUID) { this.snapshots.add(snapshotId); isDirty = true }
    override fun `vaultFixes$setSnapshots`(snapshots: ArrayList<UUID>) { this.snapshots = Objects.requireNonNull(snapshots) }
    override fun `vaultFixes$getSnapshots`(): ArrayList<UUID> { return Objects.requireNonNull(this.snapshots) }
}