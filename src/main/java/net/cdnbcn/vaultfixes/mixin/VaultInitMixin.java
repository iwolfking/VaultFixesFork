package net.cdnbcn.vaultfixes.mixin;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.player.ClassicListenersLogic;
import iskallia.vault.core.world.storage.VirtualWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClassicListenersLogic.class)
public class VaultInitMixin {
    //Disable auto save for vault worlds
    @Inject(method = "initServer", at = @At("HEAD"), remap = false)
    public void initServer(VirtualWorld world, Vault vault, CallbackInfo ci) {
        world.noSave = true;
    }
}
