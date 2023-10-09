package net.cdnbcn.vaultfixes.mixin.bounty;

import iskallia.vault.bounty.TaskRegistry;
import iskallia.vault.bounty.task.MiningTask;
import net.cdnbcn.vaultfixes.bounty.TaskHelpers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;


@Mixin(value = MiningTask.class, priority = 1)
public abstract class MixinMiningTask extends MixinTask {

    /**
     * @author Koromaru Koruko
     * @reason optimization, remove needless allocations
     */
    @Overwrite(remap = false)
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onOreBroken(BlockEvent.BreakEvent event) {
        Player var2 = event.getPlayer();
        if (var2 instanceof ServerPlayer player) {
            TaskHelpers.INSTANCE.processEvent(player, TaskRegistry.MINING, event);
        }
    }
}