package net.cdnbcn.vaultfixes.mixin.bounty;

import iskallia.vault.bounty.task.Task;
import net.cdnbcn.vaultfixes.mixin_interfaces.TaskMixinInterface;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = Task.class)
public abstract class MixinTask implements TaskMixinInterface {

    @Shadow(remap = false)
    protected abstract void complete(ServerPlayer player);

    @SuppressWarnings("unused")
    @Override
    public void vaultFixes$callComplete(ServerPlayer player){
        complete(player);
    }
}
