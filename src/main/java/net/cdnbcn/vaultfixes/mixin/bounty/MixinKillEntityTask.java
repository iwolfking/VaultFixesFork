package net.cdnbcn.vaultfixes.mixin.bounty;


import iskallia.vault.bounty.TaskRegistry;
import iskallia.vault.bounty.task.KillEntityTask;
import net.cdnbcn.vaultfixes.bounty.TaskHelpers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;


@Mixin(value = KillEntityTask.class, priority = 1)
public abstract class MixinKillEntityTask extends MixinTask {


    /**
     * @author Koromaru Koruko
     * @reason optimization, remove needless allocations
     */
    @Overwrite(remap = false)
    @SubscribeEvent
    public static void onKillEntity(LivingDeathEvent event) {
        Entity var2 = event.getSource().getEntity();
        if (var2 instanceof ServerPlayer player) {
            TaskHelpers.INSTANCE.processEvent(player, TaskRegistry.KILL_ENTITY, event);
        }
    }
}