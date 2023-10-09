package net.cdnbcn.vaultfixes.mixin.bounty;

import iskallia.vault.bounty.TaskRegistry;
import iskallia.vault.bounty.task.CompletionTask;
import iskallia.vault.event.event.VaultLeaveEvent;
import net.cdnbcn.vaultfixes.bounty.TaskHelpers;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = CompletionTask.class, priority = 1)
public abstract class MixinCompletionTask extends MixinTask {
    /**
     * @author Koromaru Koruko
     * @reason optimization, remove needless allocations
     */
    @Overwrite(remap = false)
    @SubscribeEvent
    public static void onVaultLeave(VaultLeaveEvent event) {
        TaskHelpers.INSTANCE.processEvent(event.getPlayer(), TaskRegistry.COMPLETION, event);
    }
}
