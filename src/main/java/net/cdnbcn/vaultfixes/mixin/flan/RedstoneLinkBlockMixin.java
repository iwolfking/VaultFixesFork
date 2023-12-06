package net.cdnbcn.vaultfixes.mixin.flan;

import com.simibubi.create.content.redstone.link.RedstoneLinkBlock;
import io.github.flemmli97.flan.api.data.IPermissionContainer;
import io.github.flemmli97.flan.api.permission.PermissionRegistry;
import io.github.flemmli97.flan.claim.ClaimStorage;
import iskallia.vault.research.StageManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RedstoneLinkBlock.class)
public class RedstoneLinkBlockMixin {
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    public void use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        if(worldIn instanceof ServerLevel level && player instanceof ServerPlayer serverPlayer) {
            ClaimStorage storage = ClaimStorage.get(level);
            IPermissionContainer claim = storage.getForPermissionCheck(pos);
            if(claim != null) {
                if(!claim.canInteract(serverPlayer, PermissionRegistry.REDSTONE, pos, true)) {
                    cir.setReturnValue(InteractionResult.FAIL);
                }
            }
        }
    }

    @Inject(method = "onWrenched", at = @At("HEAD"), cancellable = true, remap = false)
    public void use(BlockState state, UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if(context.getLevel() instanceof ServerLevel level && context.getPlayer() instanceof ServerPlayer serverPlayer) {
            BlockPos pos = context.getClickedPos();
            ClaimStorage storage = ClaimStorage.get(level);
            IPermissionContainer claim = storage.getForPermissionCheck(pos);
            if(claim != null) {
                if(!claim.canInteract(serverPlayer, PermissionRegistry.REDSTONE, pos, true)) {
                    cir.setReturnValue(InteractionResult.FAIL);
                }
            }
        }
    }
}
