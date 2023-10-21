package net.cdnbcn.vaultfixes.mixin_interfaces;

import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.gear.data.VaultGearData;

import java.util.function.Function;

public interface VaultGearData_TypeMixinInterface {
    @SuppressWarnings("UNUSED")
    void vaultFixes$setAttributeSource(Function<VaultGearData, Iterable<? extends VaultGearAttributeInstance<?>>> newSource);
}
