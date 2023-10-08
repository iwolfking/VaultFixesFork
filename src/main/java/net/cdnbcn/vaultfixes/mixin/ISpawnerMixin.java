package net.cdnbcn.vaultfixes.mixin;

import iskallia.ispawner.item.GenericSpawnEggItem;
import iskallia.vault.core.world.storage.VirtualWorld;
import net.cdnbcn.vaultfixes.IMobMixin;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(GenericSpawnEggItem.class)
public class ISpawnerMixin {
    @Inject(method="m_7203_", cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT, remap = false, at = @At(value="INVOKE_ASSIGN",
            target="Liskallia/ispawner/item/GenericSpawnEggItem;getType(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/entity/EntityType;"))
    void use(Level world, Player user, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir,
             ItemStack stack, BlockHitResult hitResult, BlockPos pos, EntityType<?> type) {
        if (type == null)
            cir.setReturnValue(InteractionResultHolder.pass(stack));

        Mob spawned = (Mob) type.spawn((ServerLevel)world, stack, user, pos, MobSpawnType.SPAWN_EGG, false, false);
        if(spawned == null)
            cir.setReturnValue(InteractionResultHolder.pass(stack));

//        //If not in a vault, nerf AI of iSpawner mobs
        if(!(world instanceof VirtualWorld)) {
            ((IMobMixin)spawned).vaultFixes$setAware(false);
//            spawned.getBrain().setDefaultActivity(Activity.IDLE);
//            spawned.getBrain().removeAllBehaviors();
//            spawned.getBrain().addActivity(Activity.IDLE, 1, ImmutableList.of(new DoNothing(Integer.MAX_VALUE, Integer.MAX_VALUE)));
        }

        if (!user.getAbilities().instabuild)
            stack.shrink(1);

        user.awardStat(Stats.ITEM_USED.get((GenericSpawnEggItem)(Object)this));
        world.gameEvent(GameEvent.ENTITY_PLACE, user);
        cir.setReturnValue(InteractionResultHolder.consume(stack));
    }
}
