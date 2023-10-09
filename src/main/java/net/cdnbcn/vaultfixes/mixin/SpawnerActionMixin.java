package net.cdnbcn.vaultfixes.mixin;

import iskallia.ispawner.world.spawner.SpawnerAction;
import iskallia.ispawner.world.spawner.SpawnerContext;
import iskallia.vault.core.world.storage.VirtualWorld;
import net.cdnbcn.vaultfixes.MobMixinInterface;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(SpawnerAction.class)
public class SpawnerActionMixin {
    @Inject(method = "applyEggOverride", locals = LocalCapture.CAPTURE_FAILSOFT, remap = false, at = @At(value="INVOKE_ASSIGN",
            target = "Liskallia/ispawner/world/spawner/SpawnerAction;create(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/network/chat/Component;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/MobSpawnType;ZZ)Lnet/minecraft/world/entity/Entity;"))
    void applyEggOverride(Level world, ItemStack stack, SpawnerContext context, CallbackInfoReturnable<Boolean> cir, BlockState state, EntityType type, BlockPos pos, Entity entity) {
        if(!(world instanceof VirtualWorld)) {
            ((MobMixinInterface)entity).vaultFixes$setAware(false);
        }
    }
}
