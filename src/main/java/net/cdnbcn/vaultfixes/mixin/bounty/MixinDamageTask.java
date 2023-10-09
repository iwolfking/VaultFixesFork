package net.cdnbcn.vaultfixes.mixin.bounty;

import iskallia.vault.bounty.TaskRegistry;
import iskallia.vault.bounty.task.DamageTask;
import net.cdnbcn.vaultfixes.bounty.TaskHelpers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;


@Mixin(value = DamageTask.class, priority = 1)
public abstract class MixinDamageTask extends MixinTask {

    /**
     * @author Koromaru Koruko
     * @reason optimization, remove needless allocations
     */
    @Overwrite(remap = false)
    @SubscribeEvent
    public static void onDamageEntity(LivingHurtEvent event) {
        Entity var2 = event.getSource().getEntity();
        if (var2 instanceof ServerPlayer player) {
            TaskHelpers.INSTANCE.processEvent(player, TaskRegistry.DAMAGE_ENTITY, event, (task) -> task.increment(event.getAmount()));
        }
    }
}
