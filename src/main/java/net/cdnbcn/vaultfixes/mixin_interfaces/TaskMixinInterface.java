package net.cdnbcn.vaultfixes.mixin_interfaces;

import net.minecraft.server.level.ServerPlayer;

public interface TaskMixinInterface {
    @SuppressWarnings("unused")
    void vaultFixes$callComplete(ServerPlayer player);
}
