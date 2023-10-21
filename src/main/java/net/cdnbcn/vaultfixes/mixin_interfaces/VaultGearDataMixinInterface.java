package net.cdnbcn.vaultfixes.mixin_interfaces;

import iskallia.vault.gear.attribute.VaultGearModifier;
import net.cdnbcn.vaultfixes.data.ArrayListDeque;

public interface VaultGearDataMixinInterface {
    @SuppressWarnings("UNUSED")
    ArrayListDeque<VaultGearModifier<?>> getBaseModifiers_al();
    @SuppressWarnings("UNUSED")
    ArrayListDeque<VaultGearModifier<?>> getPrefixes_al();
    @SuppressWarnings("UNUSED")
    ArrayListDeque<VaultGearModifier<?>> getSuffixes_al();
}
