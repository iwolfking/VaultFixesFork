package net.cdnbcn.vaultfixes.mixin_interfaces.saving;

import java.util.UUID;
import java.util.stream.Stream;

public interface ServerPlayerMixinInterface {

    Stream<UUID> vaultFixes$getAllSnapshots();
    void vaultFixes$setAllSnapshots(Stream<UUID> snapshots);
    void vaultFixes$addSnapshot(UUID snapshotId);
}
