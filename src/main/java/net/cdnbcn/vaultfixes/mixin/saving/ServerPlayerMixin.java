package net.cdnbcn.vaultfixes.mixin.saving;

import net.cdnbcn.vaultfixes.mixin_interfaces.saving.IVaultPlayerData;
import net.cdnbcn.vaultfixes.mixin_interfaces.saving.ServerPlayerMixinInterface;
import net.cdnbcn.vaultfixes.saving.PlayerSaveManger;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;
import java.util.stream.Stream;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin implements ServerPlayerMixinInterface {

    @Inject(method="addAdditionalSaveData", at = @At("HEAD"))
    private void addAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
        PlayerSaveManger.notifyPlayerSaving$vaultfixes((ServerPlayer)(Object)this);
    }

    @Unique
    @Nullable
    private IVaultPlayerData vaultFixes$vaultPlayerData = null;

    @Override
    public UUID vaultFixes$getPlayerUUID() { return vaultFixes$getVaultPlayerData().vaultFixes$getPlayerUUID(); }

    @Unique
    private IVaultPlayerData vaultFixes$getVaultPlayerData(){
        if(vaultFixes$vaultPlayerData == null){
            vaultFixes$vaultPlayerData = PlayerSaveManger.getPlayerData(((ServerPlayer)(Object)this).getUUID());
            if(!vaultFixes$vaultPlayerData.vaultFixes$getPlayerUUID().equals(((ServerPlayer)(Object)this).getUUID()))
                throw new RuntimeException("VaultPlayerData.playerUUID != Player.uuid ("+vaultFixes$vaultPlayerData.vaultFixes$getPlayerUUID()+"!="+((ServerPlayer)(Object)this).getUUID()+") ");
        }
        return vaultFixes$vaultPlayerData;
    }


    //region Snapshots
    @Override
    public Stream<UUID> vaultFixes$getAllSnapshots() { return vaultFixes$getVaultPlayerData().vaultFixes$getAllSnapshots(); }
    public Stream<UUID> vaultFixes$getLastSnapshots(int amount) {
        return vaultFixes$getVaultPlayerData().vaultFixes$getLastSnapshots(amount);
    }
    @Override
    public void vaultFixes$addSnapshot(UUID snapshotId) {
        vaultFixes$getVaultPlayerData().vaultFixes$addSnapshot(snapshotId);
    }
    //endregion Snapshots

    @Override
    public boolean vaultFixes$isDirty() {
        return vaultFixes$getVaultPlayerData().vaultFixes$isDirty();
    }
}
