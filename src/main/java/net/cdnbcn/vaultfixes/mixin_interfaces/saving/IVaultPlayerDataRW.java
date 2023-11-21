package net.cdnbcn.vaultfixes.mixin_interfaces.saving;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.UUID;

@SuppressWarnings("unused")
public interface IVaultPlayerDataRW extends IVaultPlayerData {
    void vaultFixes$markClean();
    void vaultFixes$markDirty();

    void vaultFixes$setSnapshots(@NotNull ArrayList<UUID> snapshots);
    @NotNull ArrayList<UUID> vaultFixes$getSnapshots();

}
