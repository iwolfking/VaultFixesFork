package net.cdnbcn.vaultfixes.mixin.saving;

import iskallia.vault.block.VaultCrateBlock;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.stat.StatCollector;
import iskallia.vault.init.ModBlocks;
import iskallia.vault.init.ModItems;
import iskallia.vault.util.calc.PlayerStatisticsCollector;
import iskallia.vault.world.data.VaultSnapshots;
import net.cdnbcn.vaultfixes.VaultFixes;
import net.cdnbcn.vaultfixes.mixin_interfaces.saving.VaultSnapshotsMixinInterface;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.CapabilityItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(PlayerStatisticsCollector.VaultRunsSnapshot.class)
public class PlayerStatisticsCollector$VaultRunsSnapshotMixin {
    /**
     * @author KoromaruKoruko
     * @reason Modify to use new snapshot storage
     */
    @Overwrite
    public static PlayerStatisticsCollector.VaultRunsSnapshot ofPlayer(ServerPlayer sPlayer) {
        PlayerStatisticsCollector.VaultRunsSnapshot data = new PlayerStatisticsCollector.VaultRunsSnapshot();
        final var vaultSnapshots = (VaultSnapshotsMixinInterface)VaultSnapshots.get(VaultFixes.getServer());

        vaultSnapshots.vaultFixes$getAllForPlayer(sPlayer.getUUID()).map(vaultSnapshots::vaultFixes$getSnapshot).forEach(snapshot ->{
            Vault vault = snapshot.getEnd();
            if (vault != null) {
                vault.ifPresent(Vault.STATS, collector -> {
                    StatCollector stats = collector.get(sPlayer.getUUID());
                    if (stats != null) {
                        switch (stats.get(StatCollector.COMPLETION)) {
                            case COMPLETED -> ++data.completed;
                            case BAILED -> ++data.survived;
                            case FAILED -> ++data.failed;
                        }

                        for(ItemStack reward : stats.get(StatCollector.REWARD)) {
                            if (reward.getItem() instanceof BlockItem blockItem
                                && blockItem.getBlock() instanceof VaultCrateBlock block && reward.getTag() != null
                            ) {
                                CompoundTag tag = reward.getOrCreateTag().getCompound("BlockEntityTag").copy();
                                //noinspection DataFlowIssue
                                tag.putString("id", ModBlocks.VAULT_CRATE_TILE_ENTITY.getRegistryName().toString());
                                BlockEntity te = BlockEntity.loadStatic(BlockPos.ZERO, block.defaultBlockState(), tag);
                                if (te != null) {
                                    te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(handler -> {
                                        for(int i = 0; i < handler.getSlots(); ++i) {
                                            ItemStack stack = handler.getStackInSlot(i);
                                            if (stack.getItem() == ModItems.UNIDENTIFIED_ARTIFACT) {
                                                ++data.artifacts;
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    }
                });
            }
        });

        return data;
    }
}
