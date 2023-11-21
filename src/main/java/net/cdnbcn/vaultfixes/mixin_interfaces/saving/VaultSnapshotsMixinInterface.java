package net.cdnbcn.vaultfixes.mixin_interfaces.saving;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.stat.VaultSnapshot;

import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Stream;

public interface VaultSnapshotsMixinInterface {
    VaultSnapshot vaultFixes$getSnapshot(UUID id);

    /** Gets all Snapshots (SLOW DO NOT USE)
     *
     * @return all VaultSnapshot(s)
     */
    Stream<VaultSnapshot> vaultFixes$getAllSnapshots();

    Stream<VaultSnapshot> vaultFixes$readSnapshots(Stream<UUID> snapshots);

    Stream<UUID> vaultFixes$getAllForPlayer(UUID playerId);
    void vaultFixes$addForPlayer(UUID player, UUID snapshotId);


    @SuppressWarnings("unused")
    ArrayList<UUID> vaultFixes$compileAllForPlayer(UUID playerId);
    void vaultFixes$createSnapshot(Vault vault);
}
