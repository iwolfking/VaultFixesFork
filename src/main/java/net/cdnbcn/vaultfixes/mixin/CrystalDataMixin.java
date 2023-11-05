package net.cdnbcn.vaultfixes.mixin;

import iskallia.vault.item.crystal.CrystalData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(CrystalData.class)
public class CrystalDataMixin {
    /**
     * @author CanadianBacon
     * @reason Let catalyst fragments generate in all vaults
     */
    @Overwrite(remap = false)
    public boolean canGenerateCatalystFragments() {
        return true;
    }
}
