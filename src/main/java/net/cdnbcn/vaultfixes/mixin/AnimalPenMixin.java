package net.cdnbcn.vaultfixes.mixin;

import com.google.common.collect.Maps;
import iskallia.vault.block.entity.AnimalPenTileEntity;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.init.ModBlocks;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.init.ModItems;
import iskallia.vault.init.ModNetwork;
import iskallia.vault.item.AnimalJarItem;
import iskallia.vault.network.message.AnimalPenParticleMessage;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(AnimalPenTileEntity.class)
public abstract class AnimalPenMixin extends BlockEntity implements MenuProvider {
    @Unique
    private static final Map<DyeColor, ItemLike> WOOL_ITEM_BY_DYE = Util.make(Maps.newEnumMap(DyeColor.class), (p_29841_) -> {
        p_29841_.put(DyeColor.WHITE, Blocks.WHITE_WOOL);
        p_29841_.put(DyeColor.ORANGE, Blocks.ORANGE_WOOL);
        p_29841_.put(DyeColor.MAGENTA, Blocks.MAGENTA_WOOL);
        p_29841_.put(DyeColor.LIGHT_BLUE, Blocks.LIGHT_BLUE_WOOL);
        p_29841_.put(DyeColor.YELLOW, Blocks.YELLOW_WOOL);
        p_29841_.put(DyeColor.LIME, Blocks.LIME_WOOL);
        p_29841_.put(DyeColor.PINK, Blocks.PINK_WOOL);
        p_29841_.put(DyeColor.GRAY, Blocks.GRAY_WOOL);
        p_29841_.put(DyeColor.LIGHT_GRAY, Blocks.LIGHT_GRAY_WOOL);
        p_29841_.put(DyeColor.CYAN, Blocks.CYAN_WOOL);
        p_29841_.put(DyeColor.PURPLE, Blocks.PURPLE_WOOL);
        p_29841_.put(DyeColor.BLUE, Blocks.BLUE_WOOL);
        p_29841_.put(DyeColor.BROWN, Blocks.BROWN_WOOL);
        p_29841_.put(DyeColor.GREEN, Blocks.GREEN_WOOL);
        p_29841_.put(DyeColor.RED, Blocks.RED_WOOL);
        p_29841_.put(DyeColor.BLACK, Blocks.BLACK_WOOL);
    });

    @Unique
    private String vaultFixes$resourceTimerName = "";

    @Final
    @Shadow(remap = false)
    private AnimalPenTileEntity.AnimalPenInventory inventory;
    @Shadow(remap = false)
    private ItemStack prevInput;
    @Shadow(remap = false)
    private Animal animalToReference;
    @Shadow(remap = false)
    private Animal dyingAnimalToReference;
    @Shadow(remap = false)
    private List<Integer> deathTime;
    @Shadow(remap = false)
    private int tickCount;


    public AnimalPenMixin(BlockPos pWorldPosition, BlockState pBlockState) {
        super(ModBlocks.ANIMAL_PEN_ENTITY, pWorldPosition, pBlockState);
    }

    @Inject(method = "load", at = @At("TAIL"))
    private void load(CompoundTag tag, CallbackInfo ci) {
        if (this.vaultFixes$resourceTimerName.equals("") && AnimalJarItem.containsEntity(this.inventory.getItem(0))) {
            this.vaultFixes$resourceTimerName = switch (this.inventory.getItem(0).getTag().getString("entity")) {
                case "minecraft:sheep" -> "shearTimer";
                case "minecraft:chicken" -> "eggTimer";
                case "minecraft:turtle" -> "turtleEggTimer";
                case "minecraft:bee" -> "pollenTimer";
                default -> "resourceTimer";
            };
        }
    }

    /**
     * @author CanadianBacon
     * @reason Optimize this shit
     */
    @Overwrite(remap = false)
    public static void tick(Level world, BlockPos pos, BlockState state, AnimalPenTileEntity _tile) {
        AnimalPenMixin tile = (AnimalPenMixin) (Object) _tile;

        int timer;
        ItemStack invItem = tile.inventory.getItem(0);

        if (world.isClientSide()) {
            ++tile.tickCount;
            int iter = 0;

            for (Iterator<Integer> var5 = tile.deathTime.iterator(); var5.hasNext(); ++iter) {
                int time = var5.next();
                if (time > 0) {
                    tile.deathTime.set(iter, time + 1);
                    if (time > 25) {
                        tile.deathTime.set(iter, 0);

                        for (timer = 0; timer < 10; ++timer) {
                            Random random = world.getRandom();
                            Vec3 offset = new Vec3(random.nextDouble() / 3.0 * (double) (random.nextBoolean() ? 1 : -1), 0.0, random.nextDouble() / 3.0 * (double) (random.nextBoolean() ? 1 : -1));
                            world.addParticle(ParticleTypes.POOF, true, (double) tile.getBlockPos().getX() + 0.5 + offset.x, (double) tile.getBlockPos().getY() + random.nextDouble() * 0.3499999940395355 + 0.3499999940395355, (double) tile.getBlockPos().getZ() + 0.5 + offset.z, offset.x / 20.0, random.nextDouble() * 0.1, offset.z / 20.0);
                        }
                    }
                }
            }

            tile.deathTime.remove(0);
            ItemStack prevItem = tile.prevInput;
            if (!prevItem.sameItem(invItem)) {
                if (AnimalJarItem.containsEntity(invItem)) {
                    tile.animalToReference = AnimalJarItem.getAnimalFromItemStack(invItem, world);
                } else {
                    tile.animalToReference = null;
                }
            }
        } else {
            if (AnimalJarItem.containsEntity(invItem)) {
                if(tile.animalToReference == null){
                    tile.animalToReference = AnimalJarItem.getAnimalFromItemStack(invItem, world);
                }
            } else {
                tile.animalToReference = null;
            }


            if (invItem.hasTag()) {
                CompoundTag tag = invItem.getOrCreateTag();
                boolean changed = false;

                if (tile.animalToReference instanceof Bee) {
                    if (tag.contains(tile.vaultFixes$resourceTimerName)) {
                        timer = tag.getInt(tile.vaultFixes$resourceTimerName);
                        int count = tag.getInt("count");
                        boolean honeyReady = tag.contains("honeyReady") && tag.getBoolean("honeyReady");

                        if (timer > 0 && !honeyReady) {
                            tag.putInt(tile.vaultFixes$resourceTimerName, timer - 1);
                            if (timer - 1 == 0) {
                                tag.putInt(tile.vaultFixes$resourceTimerName, Mth.clamp((3600 - (count - 1) * 10 * 20) / 5, 40, 3600));
                                if (tag.contains("honeyLevel")) {
                                    tile.level.playSound(null, tile.getBlockPos(), SoundEvents.BEEHIVE_EXIT, SoundSource.BLOCKS, 0.75F, 1.0F);
                                    if (tag.getInt("honeyLevel") < 5) {
                                        tag.putInt("honeyLevel", tag.getInt("honeyLevel") + 1);
                                    }

                                    if (tag.getInt("honeyLevel") >= 5) {
                                        tag.putBoolean("honeyReady", true);
                                        tile.level.playSound(null, tile.getBlockPos(), SoundEvents.BEEHIVE_DRIP, SoundSource.BLOCKS, 0.75F, 1.0F);
                                        tile.level.playSound(null, tile.getBlockPos(), SoundEvents.BEEHIVE_WORK, SoundSource.BLOCKS, 0.75F, 1.0F);
                                    }
                                }
                            }

                            tile.setChanged();
                            if (world instanceof ServerLevel serverWorld) {
                                serverWorld.sendBlockUpdated(tile.getBlockPos(), state, state, 3);
                            }
                        }
                    } else {
                        timer = tag.getInt("count");
                        tag.putInt("honeyLevel", 0);
                        tag.putInt(tile.vaultFixes$resourceTimerName, Mth.clamp((3600 - (timer - 1) * 10 * 20) / 5, 40, 3600));
                        tile.setChanged();
                        if (world instanceof ServerLevel serverWorld) {
                            serverWorld.sendBlockUpdated(tile.getBlockPos(), state, state, 3);
                        }
                    }
                } else {
                    if (tag.contains(tile.vaultFixes$resourceTimerName)) {
                        timer = tag.getInt(tile.vaultFixes$resourceTimerName);
                        if (timer > 1) {
                            tag.putInt(tile.vaultFixes$resourceTimerName, timer - 1);
                        } else {
                            tag.remove(tile.vaultFixes$resourceTimerName);
                            tile.vaultFixes$playAmbient(tile.animalToReference instanceof Turtle ? SoundEvents.TURTLE_LAY_EGG : null);
                        }

                        changed = true;
                    }
                }

                if (tag.contains("breedTimer")) {
                    timer = tag.getInt("breedTimer");
                    if (timer > 1) {
                        tag.putInt("breedTimer", timer - 1);
                    } else {
                        tag.remove("breedTimer");
                        tile.vaultFixes$playAmbient(null);
                    }

                    changed = true;
                }

                if (changed) {
                    tile.setChanged();
                    if (world instanceof ServerLevel serverWorld) {
                        serverWorld.sendBlockUpdated(tile.getBlockPos(), state, state, 3);
                    }
                }
            }
        }

        tile.prevInput = invItem;
    }

    /**
     * @author CanadianBacon
     * @reason Optimize shit
     */
    @Overwrite(remap = false)
    public boolean interact(@NotNull BlockState state, @NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        ItemStack itemInHand = player.getItemInHand(hand).copy();
        ItemStack itemInPen = this.inventory.getItem(0).copy();
        ItemStack blockInPen = this.inventory.getItem(1).copy();
        ServerLevel serverLevel = (ServerLevel) level;

        if (this.animalToReference == null && AnimalJarItem.containsEntity(itemInPen)) {
            this.animalToReference = AnimalJarItem.getAnimalFromItemStack(itemInPen, serverLevel);
        }

        if (!player.isCrouching()) {
            if (!itemInPen.isEmpty()) {
                if (this.animalToReference == null) {
                    return false;
                }

                CompoundTag tag = this.inventory.getItem(0).getOrCreateTag();

                int usedCount;


                if (itemInHand.is(Items.BUCKET)) {
                    if (this.animalToReference instanceof Cow) {
                        serverLevel.playSound(null, this.getBlockPos(), SoundEvents.COW_MILK, SoundSource.BLOCKS, 1.0F, 1.0F);
                        player.setItemInHand(hand, ItemUtils.createFilledResult(player.getItemInHand(hand), player, Items.MILK_BUCKET.getDefaultInstance()));

                        return true;
                    } else if (this.animalToReference instanceof Chicken && !tag.contains(vaultFixes$resourceTimerName)) {
                        serverLevel.playSound(null, this.getBlockPos(), SoundEvents.CHICKEN_EGG, SoundSource.BLOCKS, 1.0F, 1.0F);
                        usedCount = Mth.clamp(tag.getInt("count"), 1, 64);

                        for (int i = 0; i < usedCount; ++i) {
                            Block.popResource(serverLevel, this.getBlockPos(), new ItemStack(Items.EGG));
                        }

                        tag.putInt(vaultFixes$resourceTimerName, 6000);
                        this.setChanged();
                        serverLevel.sendBlockUpdated(hit.getBlockPos(), state, state, 3);

                        return true;
                    } else if (this.animalToReference instanceof Turtle && !tag.contains(vaultFixes$resourceTimerName)) {
                        serverLevel.playSound(null, this.getBlockPos(), SoundEvents.CHICKEN_EGG, SoundSource.BLOCKS, 1.0F, 1.0F);
                        usedCount = Mth.clamp((tag.getInt("count") + 1) / 2, 1, 16);

                        for (int i = 0; i < usedCount; ++i) {
                            Block.popResource(serverLevel, this.getBlockPos(), new ItemStack(Items.TURTLE_EGG));
                        }

                        tag.putInt(vaultFixes$resourceTimerName, 6000);
                        this.setChanged();
                        serverLevel.sendBlockUpdated(hit.getBlockPos(), state, state, 3);

                        return true;
                    }
                } else if (itemInHand.is(Items.BOWL) && this.animalToReference instanceof MushroomCow) {
                    if (!player.isCreative()) {
                        player.getItemInHand(hand).shrink(1);
                    }

                    player.setItemInHand(hand, ItemUtils.createFilledResult(itemInHand, player, new ItemStack(Items.MUSHROOM_STEW), false));
                    serverLevel.playSound(null, this.worldPosition, SoundEvents.MOOSHROOM_MILK, SoundSource.PLAYERS, 1.0F, 1.0F);
                    return true;
                } else if (this.animalToReference instanceof Sheep sheep) {
                    if (itemInHand.getItem() instanceof DyeItem dyeItem) {
                        if (sheep.getColor() != dyeItem.getDyeColor()) {
                            sheep.setColor(dyeItem.getDyeColor());
                            sheep.save(tag);
                            if (!player.isCreative()) {
                                player.getItemInHand(hand).shrink(1);
                            }

                            serverLevel.playSound(null, this.worldPosition, SoundEvents.DYE_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
                            this.setChanged();
                            serverLevel.sendBlockUpdated(hit.getBlockPos(), state, state, 3);

                            return true;
                        }
                    } else if (
                            itemInHand.getItem() instanceof ShearsItem ||
                                    (itemInHand.is(ModItems.TOOL) &&
                                            VaultGearData.read(itemInHand).get(ModGearAttributes.REAPING, VaultGearAttributeTypeMerger.anyTrue())
                                    )
                    ) {
                        if (tag.contains(vaultFixes$resourceTimerName)) {
                            return false;
                        }

                        Vec3 temp = this.animalToReference.position();
                        this.animalToReference.setPos((float) this.getBlockPos().getX() + 0.5F, (float) this.getBlockPos().getY() - 0.1F, (float) this.getBlockPos().getZ() + 0.5F);
                        this.animalToReference.setOldPosAndRot();
                        int count = 1;
                        if (tag.contains("count")) {
                            count = tag.getInt("count");
                        }

                        int spawnCount;
                        if (count < 75) {
                            for (int i = 0; i < count; ++i) {
                                spawnCount = 1 + serverLevel.random.nextInt(3);

                                for (int j = 0; j < spawnCount; ++j) {
                                    ItemEntity itementity = this.animalToReference.spawnAtLocation(WOOL_ITEM_BY_DYE.get(sheep.getColor()), 1);
                                    if (itementity != null) {
                                        itementity.setDeltaMovement(itementity.getDeltaMovement().add((serverLevel.random.nextFloat() - serverLevel.random.nextFloat()) * 0.1F, serverLevel.random.nextFloat() * 0.05F, (serverLevel.random.nextFloat() - serverLevel.random.nextFloat()) * 0.1F));
                                    }
                                }
                            }
                        } else {
                            for (spawnCount = 0; spawnCount < count; ++spawnCount) {
                                spawnCount += 1 + serverLevel.random.nextInt(3);
                            }

                            if (spawnCount > 256) {
                                spawnCount = 256;
                            }

                            while (spawnCount > 0) {
                                ItemEntity itementity;
                                if (spawnCount >= 64) {
                                    spawnCount -= 64;
                                    itementity = this.animalToReference.spawnAtLocation(new ItemStack(WOOL_ITEM_BY_DYE.get(sheep.getColor()), 64), 1.0F);
                                    if (itementity != null) {
                                        itementity.setDeltaMovement(itementity.getDeltaMovement().add((serverLevel.random.nextFloat() - serverLevel.random.nextFloat()) * 0.1F, serverLevel.random.nextFloat() * 0.05F, (serverLevel.random.nextFloat() - serverLevel.random.nextFloat()) * 0.1F));
                                    }
                                } else {
                                    itementity = this.animalToReference.spawnAtLocation(new ItemStack(WOOL_ITEM_BY_DYE.get(sheep.getColor()), spawnCount), 1.0F);
                                    if (itementity != null) {
                                        itementity.setDeltaMovement(itementity.getDeltaMovement().add(((serverLevel.random.nextFloat() - serverLevel.random.nextFloat()) * 0.1F), serverLevel.random.nextFloat() * 0.05F, (serverLevel.random.nextFloat() - serverLevel.random.nextFloat()) * 0.1F));
                                    }

                                    spawnCount = 0;
                                }
                            }
                        }

                        sheep.shear(SoundSource.BLOCKS);
                        serverLevel.playSound(null, this.getBlockPos(), SoundEvents.SHEEP_SHEAR, SoundSource.BLOCKS, 1.0F, 1.0F);
                        this.animalToReference.setPos(temp);
                        this.animalToReference.setOldPosAndRot();
                        tag.putInt(vaultFixes$resourceTimerName, 6000);
                        this.setChanged();
                        serverLevel.sendBlockUpdated(hit.getBlockPos(), state, state, 3);

                        return true;
                    }
                } else if (this.animalToReference instanceof Bee && tag.contains("honeyReady") && tag.getBoolean("honeyReady")) {
                    if (itemInHand.getItem() instanceof ShearsItem || itemInHand.is(ModItems.TOOL) && VaultGearData.read(itemInHand).get(ModGearAttributes.REAPING, VaultGearAttributeTypeMerger.anyTrue())) {
                        resetHoney(tag);
                        serverLevel.playSound(null, this.getBlockPos(), SoundEvents.BEEHIVE_SHEAR, SoundSource.BLOCKS, 1.0F, 1.0F);
                        Block.popResource(serverLevel, this.getBlockPos(), new ItemStack(Items.HONEYCOMB, 3));
                        this.setChanged();
                        serverLevel.sendBlockUpdated(hit.getBlockPos(), state, state, 3);

                        return true;
                    } else if (itemInHand.is(Items.GLASS_BOTTLE)) {
                        resetHoney(tag);
                        serverLevel.playSound(null, this.getBlockPos(), SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                        player.getItemInHand(hand).shrink(1);
                        player.getInventory().placeItemBackInInventory(new ItemStack(Items.HONEY_BOTTLE, 1));
                        this.setChanged();
                        serverLevel.sendBlockUpdated(hit.getBlockPos(), state, state, 3);

                        return true;
                    }
                }

                CompoundTag handItemTag = itemInHand.getOrCreateTag();
                if (itemInHand.getItem() instanceof AnimalJarItem && handItemTag.contains("entity") && tag.contains("entity") && handItemTag.getString("entity").equals(tag.getString("entity"))) {
                    tag.putInt("count", tag.getInt("count") + itemInHand.getOrCreateTag().getInt("count"));
                    player.setItemInHand(hand, new ItemStack(ModItems.ANIMAL_JAR));
                    this.setChanged();
                    serverLevel.sendBlockUpdated(hit.getBlockPos(), state, state, 3);
                    serverLevel.playSound(null, this.getBlockPos(), SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);

                    return true;
                } else if (this.animalToReference.isFood(itemInHand) && tag.contains("count") && !tag.contains("breedTimer")) {
                    int count = tag.getInt("count");
                    int stackCount = itemInHand.getCount();
                    usedCount = Math.min(stackCount, count);

                    if (itemInHand.getCount() == 1) {
                        usedCount = 1;
                    }
                    if (count > 1) {
                        serverLevel.playSound(null, this.getBlockPos(), this.animalToReference.getEatingSound(itemInHand), SoundSource.BLOCKS, 0.75F, 1.0F);
                        Vec3 temp = this.animalToReference.position();
                        this.animalToReference.setPos(((float) this.getBlockPos().getX() + 0.5F), ((float) this.getBlockPos().getY() + 0.1F), ((float) this.getBlockPos().getZ() + 0.5F));
                        this.animalToReference.setOldPosAndRot();
                        this.animalToReference.playAmbientSound();
                        this.animalToReference.setPos(temp);
                        this.animalToReference.setOldPosAndRot();
                        player.getItemInHand(hand).shrink(usedCount);
                        tag.putInt("breedTimer", 1100 + 100 * (usedCount / 2));
                        tag.putInt("count", tag.getInt("count") + usedCount / 2);
                        ModNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), new AnimalPenParticleMessage(this.getBlockPos()));

                        return true;
                    } else {
                        player.displayClientMessage(new TextComponent("Needs at least two to breed.."), true);
                        return false;
                    }
                }
            } else if (itemInHand.getItem() instanceof AnimalJarItem && AnimalJarItem.containsEntity(itemInHand)) {
                this.inventory.setItem(0, itemInHand);
                player.setItemInHand(hand, itemInPen);
                if (AnimalJarItem.containsEntity(this.inventory.getItem(0))) {
                    this.animalToReference = AnimalJarItem.getAnimalFromItemStack(this.inventory.getItem(0), serverLevel);
                    this.vaultFixes$resourceTimerName = switch (this.animalToReference) {
                        case Sheep ignored -> "shearTimer";
                        case Chicken ignored -> "eggTimer";
                        case Turtle ignored -> "turtleEggTimer";
                        case Bee ignored -> "pollenTimer";
                        default -> "resourceTimer";
                    };
                } else {
                    this.animalToReference = null;
                }

                if (this.animalToReference != null) {
                    serverLevel.playSound(null, this.getBlockPos(), SoundEvents.ITEM_FRAME_PLACE, SoundSource.BLOCKS, 0.75F, 1.0F);
                    Vec3 temp = this.animalToReference.position();
                    this.animalToReference.setPos(((float) this.getBlockPos().getX() + 0.5F), ((float) this.getBlockPos().getY() + 0.1F), ((float) this.getBlockPos().getZ() + 0.5F));
                    this.animalToReference.setOldPosAndRot();
                    this.animalToReference.playAmbientSound();
                    this.animalToReference.setPos(temp);
                    this.animalToReference.setOldPosAndRot();
                } else {
                    serverLevel.playSound(null, this.getBlockPos(), SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 0.75F, 1.0F);
                }

                this.setChanged();
                serverLevel.sendBlockUpdated(hit.getBlockPos(), state, state, 3);

                return true;
            } else if (blockInPen.isEmpty() && itemInHand.getItem() instanceof BlockItem blockItem) {
                if (!(blockItem.getBlock() instanceof ShulkerBoxBlock)) {
                    VoxelShape shape = blockItem.getBlock().getBlockSupportShape(blockItem.getBlock().defaultBlockState(), serverLevel, this.getBlockPos());
                    if (shape == Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0)) {
                        ItemStack stack = itemInHand.copy();
                        stack.setCount(1);
                        this.inventory.setItem(1, stack);
                        player.getItemInHand(hand).shrink(1);
                        serverLevel.playSound(null, this.getBlockPos(), blockItem.getBlock().getSoundType(blockItem.getBlock().defaultBlockState()).getPlaceSound(), SoundSource.BLOCKS, 0.75F, 1.5F);
                        this.setChanged();
                        serverLevel.sendBlockUpdated(hit.getBlockPos(), state, state, 3);

                        return true;
                    }
                }
            }
        } else if (itemInHand.isEmpty() && itemInPen.isEmpty()) {
            player.setItemInHand(hand, blockInPen);
            this.inventory.getItem(1).shrink(1);
            serverLevel.playSound(null, this.getBlockPos(), SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
            this.setChanged();
            serverLevel.sendBlockUpdated(hit.getBlockPos(), state, state, 3);

            return true;
        }
        return false;
    }

    @Unique
    public void vaultFixes$playAmbient(SoundEvent soundEvent) {
        if (this.animalToReference != null) {
            Vec3 temp = this.animalToReference.position();
            this.animalToReference.setPos(((float) this.getBlockPos().getX() + 0.5F), (float) this.getBlockPos().getY() - 0.1F, (float) this.getBlockPos().getZ() + 0.5F);
            this.animalToReference.setOldPosAndRot();
            if (soundEvent != null) {
                this.animalToReference.playSound(soundEvent, 1.0f, this.animalToReference.getVoicePitch());
            } else {
                this.animalToReference.playAmbientSound();
            }
            this.animalToReference.setPos(temp);
            this.animalToReference.setOldPosAndRot();
        }
    }

    @Shadow(remap = false)
    public static void resetHoney(CompoundTag tag) {
    }
}
