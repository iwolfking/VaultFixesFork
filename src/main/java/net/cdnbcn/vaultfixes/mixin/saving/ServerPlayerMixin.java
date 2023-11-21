package net.cdnbcn.vaultfixes.mixin.saving;

import net.cdnbcn.vaultfixes.mixin_interfaces.saving.IVaultPlayerDataRW;
import net.cdnbcn.vaultfixes.mixin_interfaces.saving.ServerPlayerMixinInterface;
import net.cdnbcn.vaultfixes.saving.PlayerSaveManger;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Stream;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin implements ServerPlayerMixinInterface, IVaultPlayerDataRW {

    @Override
    public Boolean vaultFixes$isOnline() { return true; }
    @Override
    public UUID vaultFixes$getPlayerUUID() { return ((ServerPlayer)(Object)this).getUUID(); }

    @Inject(method="addAdditionalSaveData", at = @At("HEAD"))
    private void addAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
        PlayerSaveManger.notifyPlayerSaving$vaultfixes((ServerPlayer)(Object)this);
    }

    //region Snapshots
    @Unique
    ArrayList<UUID> vaultFixes$snapshots;
    @Override
    public void vaultFixes$setSnapshots(@NotNull ArrayList<UUID> snapshots) { this.vaultFixes$snapshots = snapshots; }
    @Override
    public @NotNull ArrayList<UUID> vaultFixes$getSnapshots() { return vaultFixes$snapshots; }
    @Override
    public Stream<UUID> vaultFixes$getAllSnapshots() { return vaultFixes$snapshots.parallelStream(); }
    @Override
    public void vaultFixes$addSnapshot(UUID snapshotId) {
        vaultFixes$snapshots.add(snapshotId);
        vaultFixes$isDirty = true;
    }
    //endregion Snapshots


    //region IsDirty
    @Unique
    private boolean vaultFixes$isDirty = false;
    @Override
    public boolean vaultFixes$isDirty() {
        return vaultFixes$isDirty;
    }
    @Override
    public void vaultFixes$markClean() {
        vaultFixes$isDirty = false;
    }
    @Override
    public void vaultFixes$markDirty() {
        vaultFixes$isDirty = true;
    }
    //endregion IsDirty
}
