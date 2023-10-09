package net.cdnbcn.vaultfixes.mixin.bounty;

import iskallia.vault.bounty.TaskRegistry;
import iskallia.vault.bounty.task.ItemSubmissionTask;
import net.cdnbcn.vaultfixes.bounty.TaskHelpers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;


@Mixin(value = ItemSubmissionTask.class, priority = 1)
public abstract class MixinItemSubmissionTask extends MixinTask {

    /**
     * @author Koromaru Koruko
     * @reason optimization, remove needless allocations
     */
    @Overwrite(remap = false)
    @SubscribeEvent
    public static void onInteractWithBountyTable(PlayerInteractEvent.RightClickBlock event) {
        Entity var2 = event.getEntity();
        if (var2 instanceof ServerPlayer player) {
            TaskHelpers.INSTANCE.processEvent(player, TaskRegistry.ITEM_SUBMISSION, event, task -> {
                // doIncrement
                event.setCanceled(true);
                ItemStack stack = event.getItemStack();
                int amount = stack.getCount();
                double remainder = Math.max(task.getAmountObtained() + (double)amount - task.getProperties().getAmount(), 0.0);
                task.increment((double)amount - remainder);
                stack.setCount((int)remainder);
                event.getPlayer().getLevel().playSound(
                        null,
                        event.getPos(),
                        SoundEvents.ITEM_PICKUP,
                        SoundSource.PLAYERS,
                        0.7F,
                        ((event.getPlayer().getRandom().nextFloat() - event.getPlayer().getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F
                );
            });
        }
    }
}
