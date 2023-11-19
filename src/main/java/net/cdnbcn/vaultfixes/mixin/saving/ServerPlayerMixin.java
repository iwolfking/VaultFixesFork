package net.cdnbcn.vaultfixes.mixin.saving;

import net.cdnbcn.vaultfixes.mixin_interfaces.saving.ServerPlayerMixinInterface;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin implements ServerPlayerMixinInterface {

    @Unique
    List<UUID> vaultFixes$snapshots = new ArrayList<>();

    @Override
    public Stream<UUID> vaultFixes$getAllSnapshots() {
        return vaultFixes$snapshots.stream();
    }

    @Override
    public void vaultFixes$setAllSnapshots(Stream<UUID> snapshots) {
        vaultFixes$snapshots = snapshots.collect(Collectors.toList());
    }

    @Override
    public void vaultFixes$addSnapshot(UUID snapshotId) {
        vaultFixes$snapshots.add(snapshotId);
    }
}
