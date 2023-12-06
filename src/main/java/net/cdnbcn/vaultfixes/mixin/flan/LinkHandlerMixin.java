package net.cdnbcn.vaultfixes.mixin.flan;

import com.simibubi.create.content.redstone.link.LinkHandler;
import io.github.flemmli97.flan.api.data.IPermissionContainer;
import io.github.flemmli97.flan.api.permission.PermissionRegistry;
import io.github.flemmli97.flan.claim.ClaimStorage;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LinkHandler.class)
public class LinkHandlerMixin {
    @Inject(method = "onBlockActivated", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onBlockActivated(PlayerInteractEvent.RightClickBlock event, CallbackInfo ci) {
        if(event.getWorld() instanceof ServerLevel level && event.getPlayer() instanceof ServerPlayer serverPlayer) {
            ClaimStorage storage = ClaimStorage.get(level);
            IPermissionContainer claim = storage.getForPermissionCheck(event.getPos());
            if(claim != null) {
                if(!claim.canInteract(serverPlayer, PermissionRegistry.REDSTONE, event.getPos(), true)) {
                    ci.cancel();
                }
            }
        }
    }
}
