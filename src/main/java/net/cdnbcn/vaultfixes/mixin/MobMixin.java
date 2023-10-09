package net.cdnbcn.vaultfixes.mixin;

import net.cdnbcn.vaultfixes.mixin_interfaces.MobMixinInterface;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Mob.class, priority = 1)
public class MobMixin implements MobMixinInterface {
    @Unique
    public boolean vaultFixes$isAware = true;

    @Inject(method = "serverAiStep", at = @At(value = "INVOKE", target="Lnet/minecraft/util/profiling/ProfilerFiller;push(Ljava/lang/String;)V", ordinal = 0), cancellable = true)
    void serverAiStep(CallbackInfo ci) {
        if(!vaultFixes$isAware)
            ci.cancel();
    }

    @SuppressWarnings("unused")
    @Override
    public boolean vaultFixes$getAware() {
        return vaultFixes$isAware;
    }

    @SuppressWarnings("unused")
    @Override
    public void vaultFixes$setAware(boolean value) {
        vaultFixes$isAware = value;
    }
}
