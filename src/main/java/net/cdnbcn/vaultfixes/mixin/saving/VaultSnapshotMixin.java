package net.cdnbcn.vaultfixes.mixin.saving;

import iskallia.vault.core.Version;
import iskallia.vault.core.net.BitBuffer;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.stat.VaultSnapshot;
import iskallia.vault.world.data.VaultSnapshots;
import net.cdnbcn.vaultfixes.VaultFixes;
import net.cdnbcn.vaultfixes.mixin_interfaces.saving.VaultSnapshotMixinInterface;
import net.cdnbcn.vaultfixes.mixin_interfaces.saving.VaultSnapshotsMixinInterface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.UUID;

@Mixin(VaultSnapshot.class)
public class VaultSnapshotMixin implements VaultSnapshotMixinInterface {
    @Shadow(remap = false) private Vault start;
    @Shadow(remap = false) private Vault end;
    @Unique
    Boolean vaultFixes$IsDirty = false;



    @Override
    public Boolean vaultFixes$NeedsSave() { return vaultFixes$IsDirty; }
    @Override
    public void vaultFixes$MarkSaved() { vaultFixes$IsDirty = false; }
    @Override
    public void vaultFixes$MarkNeedsSaving() { vaultFixes$IsDirty = true; }
    @Override
    public Optional<UUID> vaultFixes$getVaultID() {
        return start == null
                ? (end == null
                    ? Optional.empty()
                    : Optional.of(end.get(Vault.ID))
                ) : Optional.of(start.get(Vault.ID));
    }


    @Inject(method = "<init>(Liskallia/vault/core/Version;)V", at = @At("RETURN"), remap = false)
    private void ctorI(Version version, CallbackInfo ci){
        vaultFixes$IsDirty = true;
    }

    @Inject(method = "setStart", at = @At("RETURN"), remap = false)
    private void setStart(Vault start, CallbackInfoReturnable<VaultSnapshot> cir) {
        vaultFixes$IsDirty = true;
    }

    @Inject(method = "setEnd", at = @At("RETURN"), remap = false)
    private void setEnd(Vault end, CallbackInfoReturnable<VaultSnapshot> cir) {
        vaultFixes$IsDirty = true;
        @SuppressWarnings("OptionalGetWithoutIsPresent")
        final var snapshotId = vaultFixes$getVaultID().get();
        final var vaultSnapshots = (VaultSnapshotsMixinInterface)VaultSnapshots.get(VaultFixes.getServer());
        end.get(Vault.STATS).getMap().keySet().forEach(id -> vaultSnapshots.vaultFixes$addForPlayer(id, snapshotId));
    }

    @Inject(method = "readBits", at = @At("RETURN"), remap = false)
    private void readBits(BitBuffer buffer, CallbackInfo ci) {
        vaultFixes$IsDirty = false;
    }
}
