package net.cdnbcn.vaultfixes.mixin.saving;

import iskallia.vault.nbt.VListNBT;
import net.cdnbcn.vaultfixes.mixin_interfaces.saving.VListNBTMixinInterface;
import net.minecraft.nbt.Tag;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.Function;

@Mixin(VListNBT.class)
public class VListNBTMixin<T, N extends Tag> implements VListNBTMixinInterface<T, N> {
    @Shadow @Final private Function<T, N> write;
    @Shadow @Final private Function<N, T> read;


    @Override
    public Function<T, N> vaultFixes$getWrite() { return write; }
    @Override
    public Function<N, T> vaultFixes$getRead() { return read; }
}
