package net.cdnbcn.vaultfixes.mixin.bounty;


import iskallia.vault.bounty.Bounty;
import iskallia.vault.bounty.BountyList;
import iskallia.vault.bounty.task.Task;
import iskallia.vault.world.data.BountyData;
import net.cdnbcn.vaultfixes.mixin_interfaces.BountyDataMixinInterface;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.*;

import java.util.*;
import java.util.stream.Stream;

@Mixin(value = BountyData.class, priority = 1)
public class MixinBountyData implements BountyDataMixinInterface {

    //region Optimize getAll*ById

    @Shadow(remap = false)
    @Final
    private HashMap<UUID, BountyList> active;
    @Shadow(remap = false)
    @Final
    private HashMap<UUID, BountyList> legendary;


    /**
     * @author KoromaruKoruko
     * @reason Optimize search
     *
     * @implNote usage justification found in getAllActiveFor(UUID)   the key is a player uuid
     */
    @Overwrite(remap = false)
    @SuppressWarnings("unchecked")
    public <T extends Task<?>> List<T> getAllActiveById(ServerPlayer player, ResourceLocation taskId) {
        return (List<T>) vaultFixes$getAllActiveByIdAsStream(player, taskId).toList();
    }

    /**
     * @author KoromaruKoruko
     * @reason Optimize search
     *
     * @implNote usage justification found in getAllLegendaryFor(UUID)   the key is a player uuid
     */
    @Overwrite(remap = false)
    @SuppressWarnings("unchecked")
    public <T extends Task<?>> List<T> getAllLegendaryById(ServerPlayer player, ResourceLocation taskId) {
        return (List<T>) vaultFixes$getAllLegendaryByIdAsStream(player, taskId).toList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Task<?>> Stream<T> vaultFixes$getAllActiveByIdAsStream(ServerPlayer player, ResourceLocation taskId) {
        var x = active.getOrDefault(player.getUUID(), null);
        if(x == null)
            return Stream.empty();

        return (Stream<T>) x.stream().map(Bounty::getTask).filter((t) -> t.getTaskType().equals(taskId));
    }
    @Override
    @SuppressWarnings("unchecked")
    public <T extends Task<?>> Stream<T> vaultFixes$getAllLegendaryByIdAsStream(ServerPlayer player, ResourceLocation taskId) {
        var x = legendary.getOrDefault(player.getUUID(), null);
        if(x == null)
            return Stream.empty();

        return (Stream<T>) x.stream().map(Bounty::getTask).filter((t) -> t.getTaskType().equals(taskId));
    }

    //endregion


}
