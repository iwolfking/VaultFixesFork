package net.cdnbcn.vaultfixes.mixin.gear;

import com.google.common.collect.Iterables;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.gear.data.VaultGearData;
import net.cdnbcn.vaultfixes.mixin_interfaces.VaultGearDataMixinInterface;
import net.cdnbcn.vaultfixes.mixin_interfaces.VaultGearData_TypeMixinInterface;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static iskallia.vault.gear.data.VaultGearData.Type.ATTRIBUTES;

@Mixin(VaultGearData.Type.class)
public class VaultGearData_TypeMixin implements VaultGearData_TypeMixinInterface {


    @Shadow(remap = false)
    @Mutable
    @Final
    private Function<VaultGearData, Stream<? extends VaultGearAttributeInstance<?>>> attributeSource;

    @Override
    public void vaultFixes$setAttributeSource(Function<VaultGearData, Stream<? extends VaultGearAttributeInstance<?>>> newSource) {
        this.attributeSource = newSource;
    }

    @Inject(method = "<clinit>", at = @At("RETURN"), remap = false)
    private static void cctor(CallbackInfo ci) {
        // overwrite attributeSource to use the al attributes
        vaultFixes$DoOverwrite(PREFIXES, (data) -> ((VaultGearDataMixinInterface)data).getPrefixes_al().stream());
        vaultFixes$DoOverwrite(SUFFIXES, (data) -> ((VaultGearDataMixinInterface)data).getSuffixes_al().stream());
        vaultFixes$DoOverwrite(ALL_MODIFIERS, (data -> {
            final var vData = ((VaultGearDataMixinInterface)data);
            return StreamSupport.stream(Iterables.concat(vData.getBaseModifiers_al(), vData.getPrefixes_al(), vData.getSuffixes_al()).spliterator(), false);
        }));
        vaultFixes$DoOverwrite(IMPLICIT_MODIFIERS,(data -> StreamSupport.stream(((VaultGearDataMixinInterface)data).getBaseModifiers_al().stream().spliterator(), false)));
        vaultFixes$DoOverwrite(EXPLICIT_MODIFIERS,(data -> {
            final var vData = ((VaultGearDataMixinInterface)data);
            return StreamSupport.stream(Iterables.concat(vData.getPrefixes_al(), vData.getSuffixes_al()).spliterator(), false);
        }));
        vaultFixes$DoOverwrite(ALL,(data -> {
            final var vData = ((VaultGearDataMixinInterface)data);
            return StreamSupport.stream(Iterables.concat(data.getAttributes(), vData.getBaseModifiers_al(), vData.getPrefixes_al(), vData.getSuffixes_al()).spliterator(), false);
        }));
    }
    @Unique
    private static void vaultFixes$DoOverwrite(VaultGearData.Type type, Function<VaultGearData, Stream<? extends VaultGearAttributeInstance<?>>> newSource){
        ((VaultGearData_TypeMixinInterface)(Object)type).vaultFixes$setAttributeSource(newSource);

    }
}
