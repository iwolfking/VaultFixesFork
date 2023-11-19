package net.cdnbcn.vaultfixes.mixin_interfaces.saving;

import iskallia.vault.core.vault.Vault;

import java.util.UUID;
import java.util.stream.Stream;

public interface StatTotalsMixinInterface {

    void vaultFixes$CalculateFor(UUID playerUUID, Stream<Vault> vaults);
}
