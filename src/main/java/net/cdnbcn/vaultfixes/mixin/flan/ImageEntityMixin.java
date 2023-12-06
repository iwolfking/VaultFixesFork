package net.cdnbcn.vaultfixes.mixin.flan;

import de.maxhenkel.camera.entities.ImageEntity;
import io.github.flemmli97.flan.api.data.IPermissionContainer;
import io.github.flemmli97.flan.api.permission.PermissionRegistry;
import io.github.flemmli97.flan.claim.ClaimStorage;
import net.cdnbcn.vaultfixes.VaultFixes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ImageEntity.class)
public abstract class ImageEntityMixin extends Entity {
    public ImageEntityMixin(EntityType<?> p_19870_, Level p_19871_) {
        super(p_19870_, p_19871_);
    }
    /**
     * @author CanadianBacon
     * @reason Disallow modifying image frames inside others Flan claims
     */
    @Inject(method = "interact", at=@At("HEAD"), cancellable = true)
    public void interact(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if(this.level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
            ClaimStorage storage = ClaimStorage.get(serverLevel);
            BlockPos pos = this.blockPosition();
            IPermissionContainer claim = storage.getForPermissionCheck(pos);
            if(claim != null) {
                if(!claim.canInteract(serverPlayer, PermissionRegistry.BREAK, pos, true)) {
                    cir.setReturnValue(InteractionResult.FAIL);
                }
            }
        }
    }
}
