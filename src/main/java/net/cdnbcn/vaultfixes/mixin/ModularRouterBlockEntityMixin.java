package net.cdnbcn.vaultfixes.mixin;


import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.item.module.ActivatorModule;
import net.cdnbcn.vaultfixes.config.ConfigManager;
import net.minecraftforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ModularRouterBlockEntity.class)
public abstract class ModularRouterBlockEntityMixin {

    @Shadow(remap = false) private int tickRate;
    @Shadow(remap = false) @Final private ItemStackHandler modulesHandler;

    @Shadow(remap = false) public abstract void recompileNeeded(int what);

    /**
     * @author Koromaru Koruko
     * @reason Inject additional logic module limiters
     */
    @Inject(method = "compileUpgrades", locals = LocalCapture.NO_CAPTURE, remap = false, at = @At(value="INVOKE_ASSIGN",
            target = "Ljava/lang/Math;max(II)I"))
    public void compileUpgrades(CallbackInfo ci) {
        int activatorLimit = ConfigManager.INSTANCE.getConfig().getModularRouters().getActivatorModuleLimiter().get();
        if (activatorLimit > -1)
        {
            boolean hasActivatorModule = false;
            for (int x = 0; x < 9; x++) {
                if (modulesHandler.getStackInSlot(x).getItem() instanceof ActivatorModule) {
                    hasActivatorModule = true;
                    break; // remove when multiple limiters added
                }
            }

            if (/*activatorLimit > -1 & */hasActivatorModule)
                this.tickRate = Math.max(activatorLimit, this.tickRate);


        }
    }

    @Unique
    private void vaultFixes$onLimiterUpdate()
    {
        // recompile upgrades, to reapply the limiters
        this.recompileNeeded(ModularRouterBlockEntity.COMPILE_UPGRADES);
    }

    @Inject(method = "<init>", remap = false, at = @At("TAIL"))
    public void init(CallbackInfo ci) {
        // on construct
        ConfigManager.INSTANCE.getConfig().getModularRouters().getOnLimiterUpdate().register(this::vaultFixes$onLimiterUpdate);
    }
    @Inject(method = "setRemoved", at = @At("TAIL"))
    public void setRemoved(CallbackInfo ci) {
        // "on deconstruct"
        ConfigManager.INSTANCE.getConfig().getModularRouters().getOnLimiterUpdate().unregister(this::vaultFixes$onLimiterUpdate);
    }
}
