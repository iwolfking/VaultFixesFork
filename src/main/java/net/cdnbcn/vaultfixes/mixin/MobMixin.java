package net.cdnbcn.vaultfixes.mixin;

import net.cdnbcn.vaultfixes.IMobMixin;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Mob.class, priority = 1)
public class MobMixin implements IMobMixin {
    @Unique
    public boolean isAware = true;

    @Inject(method = "m_8107_", at = @At("HEAD"), cancellable = true, remap = false)
    void aiStep(CallbackInfo ci) {
        if(!isAware)
            ci.cancel();
    }

    @Override
    public boolean vaultFixes$getAware() {
        return isAware;
    }

    @Override
    public void vaultFixes$setAware(boolean value) {
        isAware = value;
    }
}
