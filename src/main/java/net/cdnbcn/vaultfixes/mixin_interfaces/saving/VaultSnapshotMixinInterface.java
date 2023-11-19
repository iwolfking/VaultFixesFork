package net.cdnbcn.vaultfixes.mixin_interfaces.saving;

import java.util.Optional;
import java.util.UUID;

public interface VaultSnapshotMixinInterface {
    Boolean vaultFixes$NeedsSave();
    void vaultFixes$MarkSaved();
    void vaultFixes$MarkNeedsSaving();

    Optional<UUID> vaultFixes$getVaultID();
}
