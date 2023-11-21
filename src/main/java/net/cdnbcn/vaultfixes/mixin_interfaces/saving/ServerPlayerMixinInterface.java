package net.cdnbcn.vaultfixes.mixin_interfaces.saving;


import java.util.UUID;
import java.util.stream.Stream;

public interface ServerPlayerMixinInterface extends IVaultPlayerData {

    Stream<UUID> vaultFixes$getLastSnapshots(int amount);
}
