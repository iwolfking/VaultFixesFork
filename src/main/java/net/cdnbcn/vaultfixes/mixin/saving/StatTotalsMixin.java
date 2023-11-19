package net.cdnbcn.vaultfixes.mixin.saving;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.stat.*;
import iskallia.vault.world.data.PlayerStatsData;
import iskallia.vault.world.data.VaultSnapshots;
import it.unimi.dsi.fastutil.objects.*;
import net.cdnbcn.vaultfixes.VaultFixes;
import net.cdnbcn.vaultfixes.mixin_interfaces.saving.StatTotalsMixinInterface;
import net.cdnbcn.vaultfixes.mixin_interfaces.saving.VaultSnapshotsMixinInterface;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.*;

import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

@Mixin(StatTotals.class)
public class StatTotalsMixin implements StatTotalsMixinInterface {

    @Shadow private int bailed;
    @Shadow private int failed;
    @Shadow private int completed;
    @Shadow private int experience;
    @Shadow @Final private Object2FloatMap<ResourceLocation> damageReceived;
    @Shadow @Final private Object2FloatMap<ResourceLocation> damageDealt;
    @Shadow @Final private Object2IntMap<ResourceLocation> entitiesKilled;
    @Shadow private int treasureRoomsOpened;
    @Shadow @Final private Object2IntMap<ResourceLocation> minedBlocks;
    @Shadow @Final private Object2IntMap<VaultChestType> trappedChests;
    @Shadow @Final private Object2IntMap<StatTotals.ChestKey> lootedChests;
    @Shadow private int crystalsCrafted;



    /**
     * @author KoromaruKoruko
     * @reason Optimize Stat Calculator based on new VaultSnapshot Storage
     */
    @Overwrite(remap = false)
    public static StatTotals of(UUID playerUuid) {
        final var vaultSnapshots = VaultSnapshots.get(VaultFixes.getServer());
        Stream<Vault> vaults = ((VaultSnapshotsMixinInterface)vaultSnapshots).vaultFixes$getAllForPlayer(playerUuid)
                .map(((VaultSnapshotsMixinInterface) vaultSnapshots)::vaultFixes$getSnapshot)
                .map(VaultSnapshot::getEnd)
                .filter(Objects::nonNull);

        StatTotals statTotals = new StatTotals();
        ((StatTotalsMixinInterface)statTotals).vaultFixes$CalculateFor(playerUuid, vaults);
        return statTotals;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void vaultFixes$CalculateFor(UUID playerUuid, Stream<Vault> vaults) {
        vaults.forEach(vault -> {
            StatsCollector statsCollector = vault.get(Vault.STATS);
            if (statsCollector != null) {
                StatCollector collector = statsCollector.get(playerUuid);
                if (collector != null) {
                    switch (collector.getCompletion()) {
                        case BAILED -> ++bailed;
                        case FAILED -> ++failed;
                        case COMPLETED -> ++completed;
                    }

                    experience += collector.getExperience(vault);
                    @SuppressWarnings("rawtypes")
                    ObjectIterator chestStats = collector.getDamageReceived().object2FloatEntrySet().iterator();

                    while(chestStats.hasNext()) {
                        var entry = (Object2FloatMap.Entry<ResourceLocation>) chestStats.next();
                        damageReceived.computeFloat(entry.getKey(), (resourceLocation, value) -> (value == null ? 0.0F : value) + entry.getFloatValue());
                    }

                    chestStats = collector.getDamageDealt().object2FloatEntrySet().iterator();

                    while(chestStats.hasNext()) {
                        var entry = (Object2FloatMap.Entry<ResourceLocation>) chestStats.next();
                        damageDealt.computeFloat(entry.getKey(), (resourceLocation, value) -> (value == null ? 0.0F : value) + entry.getFloatValue());
                    }

                    chestStats = collector.getEntitiesKilled().object2IntEntrySet().iterator();

                    while(chestStats.hasNext()) {
                        var entry = (Object2IntMap.Entry<ResourceLocation>) chestStats.next();
                        entitiesKilled.computeInt(entry.getKey(), (resourceLocation, value) -> (value == null ? 0 : value) + entry.getIntValue());
                    }

                    treasureRoomsOpened += collector.getTreasureRoomsOpened();
                    chestStats = collector.getMinedBlocks().object2IntEntrySet().iterator();

                    while(chestStats.hasNext()) {
                        var entry = (Object2IntMap.Entry<ResourceLocation>) chestStats.next();
                        minedBlocks.computeInt(entry.getKey(), (resourceLocation, value) -> (value == null ? 0 : value) + entry.getIntValue());
                    }

                    for(ChestStat stat : collector.get(StatCollector.CHESTS))
                        if (stat.has(ChestStat.TRAPPED))
                            trappedChests.computeInt(stat.get(ChestStat.TYPE), (resourceLocation, value) -> (value == null ? 0 : value) + 1);
                         else
                            lootedChests.computeInt(
                                    new StatTotals.ChestKey(stat.get(ChestStat.TYPE), stat.get(ChestStat.RARITY)),
                                    (resourceLocation, value) -> (value == null ? 0 : value) + 1
                            );
                }
            }
        });

        PlayerStatsData.Stats playerStatsData = PlayerStatsData.get().get(playerUuid);
        crystalsCrafted = playerStatsData.getCrystals().size();
    }
}
