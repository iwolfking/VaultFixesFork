package net.cdnbcn.vaultfixes.mixin_interfaces.saving;

import net.cdnbcn.vaultfixes.data.ISavableData;

import java.util.Optional;
import java.util.UUID;

public interface VaultSnapshotMixinInterface extends ISavableData {
    void vaultFixes$MarkClean();
    void vaultFixes$MarkDirty();

    Optional<UUID> vaultFixes$getVaultID();
}
