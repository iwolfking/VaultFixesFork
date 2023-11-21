package net.cdnbcn.vaultfixes.mixin_interfaces.saving;

import net.cdnbcn.vaultfixes.data.ISavableData;

import java.util.UUID;
import java.util.stream.Stream;

public interface IVaultPlayerData extends ISavableData {
    @SuppressWarnings("unused")
    Boolean vaultFixes$isOnline();
    @SuppressWarnings("unused")
    UUID vaultFixes$getPlayerUUID();

    Stream<UUID> vaultFixes$getAllSnapshots();
    void vaultFixes$addSnapshot(UUID snapshotId);

}
