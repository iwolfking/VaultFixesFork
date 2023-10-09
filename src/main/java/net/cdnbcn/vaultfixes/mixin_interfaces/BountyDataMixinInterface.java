package net.cdnbcn.vaultfixes.mixin_interfaces;

import iskallia.vault.bounty.task.Task;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.stream.Stream;

public interface BountyDataMixinInterface {
    <T extends Task<?>> Stream<T> vaultFixes$getAllActiveByIdAsStream(ServerPlayer player, ResourceLocation taskId);
    <T extends Task<?>> Stream<T> vaultFixes$getAllLegendaryByIdAsStream(ServerPlayer player, ResourceLocation taskId);
}
