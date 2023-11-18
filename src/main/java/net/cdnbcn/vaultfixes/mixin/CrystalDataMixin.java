package net.cdnbcn.vaultfixes.mixin;

import iskallia.vault.item.crystal.CrystalData;
import iskallia.vault.item.crystal.modifiers.CrystalModifiers;
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
        //note: it is possible to disable cata-frag on Vautls with wooden modifier (even after x number of wooden modifier)
        // see <iskallia.vault.item.crystal.modifiers.CrystalModifiers.getList>
        // see <iskallia.vault.core.vault.modifier.modifier.VaultLootableWeightModifier>
        // see <iskallia.vault.block.PlaceholderBlock$Type>

        return true;
    }
}
