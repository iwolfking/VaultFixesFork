package net.cdnbcn.vaultfixes.mixin;

import com.mojang.serialization.Codec;
import iskallia.vault.world.gen.decorator.OverworldOreFeature;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.OreFeature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Random;

@Mixin(OverworldOreFeature.class)
public abstract class ChromaticIronMixin extends OreFeature {
    public ChromaticIronMixin(Codec<OreConfiguration> p_66531_) {
        super(p_66531_);
    }

    /**
     * @author CanadianBacon
     * @reason Rewrite to support OVERWORLD type dimensions, such as resource worlds, etc.
     */
    @Overwrite
    @SuppressWarnings({"OptionalGetWithoutIsPresent", "DataFlowIssue"})
    public boolean place(FeaturePlaceContext<OreConfiguration> context) {
        WorldGenLevel world = context.level();
        if (world.dimensionType() != context.level().getServer().registryAccess().registry(Registry.DIMENSION_TYPE_REGISTRY).get().get(DimensionType.OVERWORLD_LOCATION)) {
            return false;
        }
        OreConfiguration config = context.config();
        if (config.size == 1) {
            BlockPos pos = context.origin();
            Random random = context.random();
            for(OreConfiguration.TargetBlockState targetState : config.targetStates) {
                if (targetState.target.test(world.getBlockState(pos), random)) {
                    world.setBlock(pos, targetState.state, 2);
                    return true;
                }
            }
            return false;
        } else {
            return super.place(context);
        }
    }

}
