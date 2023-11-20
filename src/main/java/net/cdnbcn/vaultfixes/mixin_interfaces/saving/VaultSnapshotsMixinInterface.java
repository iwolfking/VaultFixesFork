package net.cdnbcn.vaultfixes.mixin_interfaces.saving;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.stat.VaultSnapshot;

import java.util.UUID;
import java.util.stream.Stream;

public interface VaultSnapshotsMixinInterface {
    Stream<VaultSnapshot> vaultFixes$getAllSnapshots();

    Stream<UUID> vaultFixes$getAllForPlayer(UUID playerId);
    Stream<UUID> vaultFixes$loadAllForPlayer(UUID playerId);
    void vaultFixes$setAllForPlayer(UUID playerId, Stream<UUID> snapshotIds);
    void vaultFixes$addForPlayer(UUID player, UUID snapshotId);


    VaultSnapshot vaultFixes$createSnapshot(Vault vault);
    VaultSnapshot vaultFixes$getSnapshot(UUID id);
}
