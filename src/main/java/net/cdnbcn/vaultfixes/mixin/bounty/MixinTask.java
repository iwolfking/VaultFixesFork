package net.cdnbcn.vaultfixes.mixin.bounty;

import iskallia.vault.bounty.task.Task;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = Task.class)
public abstract class MixinTask {

    @Shadow(remap = false)
    protected abstract void complete(ServerPlayer player);

    @SuppressWarnings("unused")
    @Unique
    public void vaultFixes$callComplete(ServerPlayer player){
        complete(player);
    }
}
