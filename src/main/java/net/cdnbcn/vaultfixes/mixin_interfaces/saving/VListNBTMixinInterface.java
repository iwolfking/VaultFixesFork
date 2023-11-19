package net.cdnbcn.vaultfixes.mixin_interfaces.saving;

import net.minecraft.nbt.Tag;

import java.util.function.Function;

public interface VListNBTMixinInterface<T, N extends Tag> {

    Function<T, N> vaultFixes$getWrite();
    Function<N, T> vaultFixes$getRead();
}
