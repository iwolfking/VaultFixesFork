package net.cdnbcn.vaultfixes.mixin.bounty;

import iskallia.vault.VaultMod;
import iskallia.vault.bounty.TaskRegistry;
import iskallia.vault.bounty.task.ItemDiscoveryTask;
import iskallia.vault.bounty.task.Task;
import iskallia.vault.bounty.task.properties.ItemDiscoveryProperties;
import iskallia.vault.core.event.common.ChestGenerationEvent;
import iskallia.vault.core.event.common.CoinStacksGenerationEvent;
import iskallia.vault.core.event.common.LootableBlockGenerationEvent;
import iskallia.vault.world.data.BountyData;
import net.cdnbcn.vaultfixes.mixin_interfaces.TaskMixinInterface;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mixin(value = ItemDiscoveryTask.class, priority = 1)
public abstract class MixinItemDiscoveryTask extends MixinTask {


    /**
     * @author Koromaru Koruko
     * @reason Substitute unused event for ItemCache, remove need for hash lookups
     */
    @Overwrite(remap = false)
    protected <E> boolean doValidate(ServerPlayer player, E event) {
            @SuppressWarnings("unchecked")
            List<ItemStack> stacks = (List<ItemStack>)event;
            if(stacks.isEmpty())
                return false;

            Iterator<ItemStack> var4 = stacks.iterator();

            ResourceLocation itemId;
            ResourceLocation requiredItem;
            do {
                if (!var4.hasNext()) {
                    return false;
                }

                ItemStack stack = var4.next();
                Item item = stack.getItem();
                itemId = ForgeRegistries.ITEMS.getKey(item);
                //noinspection unchecked
                requiredItem = (((Task<ItemDiscoveryProperties>)(Object)this).getProperties()).getItemId();
            } while(itemId == null || !itemId.equals(requiredItem));

            return true;
    }


    /**
     * @author Koromaru Koruko
     * @reason optimization, remove needless allocations
     */
    @Overwrite(remap = false)
    public static <T> void onLootGeneration(T event) {
        ServerPlayer player;
        Stream<ItemStack> items;
        switch (event) {
            case ChestGenerationEvent.Data e -> {
                player = e.getPlayer();
                items = e.getLoot().stream();
            }
            case LivingDropsEvent e -> {
                if (!(e.getSource().getEntity() instanceof ServerPlayer tempPlayer)) {
                    return;
                }
                player = tempPlayer;
                items = e.getDrops().stream().map(ItemEntity::getItem);
            }
            case CoinStacksGenerationEvent.Data e -> {
                player = e.getPlayer();
                items = e.getLoot().stream();
            }
            case null, default -> {
                if (!(event instanceof LootableBlockGenerationEvent.Data e)) {
                    VaultMod.LOGGER.warn("Attempted to validate an unregistered event.");
                    return;
                }
                player = e.getPlayer();
                items = e.getLoot().stream();
            }
        }

        List<ItemStack> cached = items.collect(Collectors.toCollection(ArrayList::new));
        BountyData data = BountyData.get();
        List<ItemDiscoveryTask> legendary = data.getAllLegendaryById(player, TaskRegistry.ITEM_DISCOVERY);
        List<ItemDiscoveryTask> activeTasks = data.getAllActiveById(player, TaskRegistry.ITEM_DISCOVERY);
        Iterator<ItemDiscoveryTask> var7 = legendary.iterator();

        while (true) {
            ItemDiscoveryTask task;
            // do legendary bounties, then normal once empty
            do {
                // do normal bounties
                if (!var7.hasNext()) {
                    Iterator<ItemDiscoveryTask> var21 = activeTasks.iterator();

                    while (true) {
                        // skip completed and invalid tasks
                        do {
                            if (!var21.hasNext()) return;
                            task = var21.next();
                        } while (task.isComplete() || !task.validate(player, cached));

                        // increment valid and non complete tasks
                        Iterator<ItemStack> var25 = cached.iterator();
                        while (var25.hasNext()) {
                            ItemStack stack = var25.next();
                            Item item = stack.getItem();
                            ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(item);
                            ResourceLocation requiredItem = task.getProperties().getItemId();
                            if (itemId != null && itemId.equals(requiredItem)) {
                                task.increment(stack.getCount());
                                var25.remove();
                                if (task.isComplete()) {
                                    ((TaskMixinInterface) task).vaultFixes$callComplete(player);
                                    break;
                                }
                            }
                        }
                    }
                }

                task = var7.next();
            } while (task.isComplete() || !task.validate(player, cached));

            // increment legendary bounty
            ResourceLocation requiredItem = task.getProperties().getItemId();
            for (ItemStack stack : cached) {
                Item item = stack.getItem();
                ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(item);
                if (itemId != null && itemId.equals(requiredItem)) {
                    task.increment(stack.getCount());
                    cached.remove(stack);
                    if (task.isComplete()) {
                        ((TaskMixinInterface) task).vaultFixes$callComplete(player);
                        break;
                    }
                }
            }
        }
    }
}
