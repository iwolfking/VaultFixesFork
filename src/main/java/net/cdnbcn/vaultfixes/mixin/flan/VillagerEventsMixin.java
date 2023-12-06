package net.cdnbcn.vaultfixes.mixin.flan;

import de.maxhenkel.easyvillagers.events.VillagerEvents;
import io.github.flemmli97.flan.api.data.IPermissionContainer;
import io.github.flemmli97.flan.api.permission.PermissionRegistry;
import io.github.flemmli97.flan.claim.ClaimStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VillagerEvents.class)
public class VillagerEventsMixin {
    /**
     * @author CanadianBacon
     * @reason Disable Easy Villager's villager pickup inside others Flan claims
     */
    @Inject(method = "pickUp", at = @At("HEAD"), cancellable = true, remap = false)
    private static void arePickupConditionsMet(Villager villager, Player player, CallbackInfo ci) {
        if(villager.level instanceof ServerLevel level && player instanceof ServerPlayer serverPlayer) {
            ClaimStorage storage = ClaimStorage.get(level);
            BlockPos pos = villager.blockPosition();
            IPermissionContainer claim = storage.getForPermissionCheck(pos);
            if(claim != null) {
                if(!claim.canInteract(serverPlayer, PermissionRegistry.BREAK, pos, true)) {
                    ci.cancel();
                }
            }
        }
    }
}
