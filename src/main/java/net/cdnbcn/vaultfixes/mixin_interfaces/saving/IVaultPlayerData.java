package net.cdnbcn.vaultfixes.mixin_interfaces.saving;

import net.cdnbcn.vaultfixes.data.ISavableData;

import java.util.UUID;
import java.util.stream.Stream;

public interface IVaultPlayerData extends ISavableData {
    @SuppressWarnings("unused")
    UUID vaultFixes$getPlayerUUID();

    Stream<UUID> vaultFixes$getAllSnapshots();
    Stream<UUID> vaultFixes$getLastSnapshots(int amount);
    void vaultFixes$addSnapshot(UUID snapshotId);


}
