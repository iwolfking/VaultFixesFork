package net.cdnbcn.vaultfixes.mixin_interfaces;

import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.gear.data.VaultGearData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.Function;

public interface VaultGearData_TypeMixinInterface {
    @Final
    public static VaultGearData.Type PREFIXES = null;
    @Final
    public static VaultGearData.Type SUFFIXES = null;
    @Final
    public static VaultGearData.Type ALL_MODIFIERS = null;
    @Final
    public static VaultGearData.Type IMPLICIT_MODIFIERS = null;
    @Final
    public static VaultGearData.Type EXPLICIT_MODIFIERS = null;
    @Final
    public static VaultGearData.Type ALL = null;

    @SuppressWarnings("UNUSED")
    void vaultFixes$setAttributeSource(Function<VaultGearData, Iterable<? extends VaultGearAttributeInstance<?>>> newSource);
}
