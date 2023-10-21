package net.cdnbcn.vaultfixes.mixin.gear;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import iskallia.vault.core.net.BitBuffer;
import iskallia.vault.gear.VaultGearRarity;
import iskallia.vault.gear.VaultGearState;
import iskallia.vault.gear.attribute.VaultGearAttributeRegistry;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.data.AttributeGearData;
import iskallia.vault.gear.data.GearDataVersion;
import iskallia.vault.gear.data.VaultGearData;
import net.cdnbcn.vaultfixes.data.ArrayListDeque;
import net.cdnbcn.vaultfixes.mixin_interfaces.VaultGearDataMixinInterface;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.*;
import java.util.function.BiConsumer;

@Mixin(VaultGearData.class)
public abstract class VaultGearDataMixin extends AttributeGearData implements VaultGearDataMixinInterface {

    @Shadow(remap = false)
    private int itemLevel;
    @Shadow(remap = false)
    private VaultGearRarity rarity;
    @Shadow(remap = false)
    private VaultGearState state;
    @Shadow(remap = false)
    private int repairSlots;
    @Shadow(remap = false)
    private int usedRepairSlots;
    @Unique
    private ArrayListDeque<VaultGearModifier<?>> baseModifiers_al = new ArrayListDeque<>();
    @Unique
    private ArrayListDeque<VaultGearModifier<?>> prefixes_al = new ArrayListDeque<>();
    @Unique
    private ArrayListDeque<VaultGearModifier<?>> suffixes_al = new ArrayListDeque<>();

    public ArrayListDeque<VaultGearModifier<?>> getBaseModifiers_al() { return baseModifiers_al; }
    public ArrayListDeque<VaultGearModifier<?>> getPrefixes_al() { return prefixes_al; }
    public ArrayListDeque<VaultGearModifier<?>> getSuffixes_al() { return  suffixes_al; }


    /**
     * @author Koromaru Koruko
     * @reason moved LinkedList to ArrayListDeque (locality optimization)
     */
    @Overwrite(remap = false)
    protected boolean addModifier(VaultGearModifier.AffixType type, VaultGearModifier<?> modifier, BiConsumer<Deque<VaultGearModifier<?>>, VaultGearModifier<?>> addFn) {
        if (!this.isModifiable()) {
            return false;
        } else if (!modifier.isValid()) {
            return false;
        } else {
            switch (type) {
                case IMPLICIT -> addFn.accept(this.baseModifiers_al, modifier);
                case PREFIX -> addFn.accept(this.prefixes_al, modifier);
                case SUFFIX -> addFn.accept(this.suffixes_al, modifier);
            }

            return true;
        }
    }

    /**
     * @author Koromaru Koruko
     * @reason moved LinkedList to ArrayListDeque (locality optimization)
     */
    @Overwrite(remap = false)
    public List<VaultGearModifier<?>> getModifiers(VaultGearModifier.AffixType type) {
        return switch (type) {
            case IMPLICIT -> Collections.unmodifiableList(this.baseModifiers_al);
            case PREFIX -> Collections.unmodifiableList(this.prefixes_al);
            case SUFFIX -> Collections.unmodifiableList(this.suffixes_al);
        };
    }

    /**
     * @author Koromaru Koruko
     * @reason moved LinkedList to ArrayListDeque (locality optimization)
     */
    @Overwrite(remap = false)
    public void clear() {
        super.clear();
        this.itemLevel = 0;
        this.rarity = VaultGearRarity.SCRAPPY;
        this.state = VaultGearState.UNIDENTIFIED;
        this.repairSlots = 0;
        this.usedRepairSlots = 0;
        this.baseModifiers_al.clear();
        this.prefixes_al.clear();
        this.suffixes_al.clear();
    }

    /**
     * @author Koromaru Koruko
     * @reason moved LinkedList to ArrayListDeque (locality optimization)
     */
    @Overwrite(remap = false)
    protected void write(BitBuffer buf) {
        super.write(buf);
        buf.writeInt(this.itemLevel);
        buf.writeEnum(this.state);
        buf.writeEnum(this.rarity);
        buf.writeInt(this.repairSlots);
        buf.writeInt(this.usedRepairSlots);
        buf.writeCollection(this.baseModifiers_al, VaultGearAttributeRegistry::writeAttributeInstance);
        buf.writeCollection(this.prefixes_al, VaultGearAttributeRegistry::writeAttributeInstance);
        buf.writeCollection(this.suffixes_al, VaultGearAttributeRegistry::writeAttributeInstance);
    }

    /**
     * @author Koromaru Koruko
     * @reason moved LinkedList to ArrayListDeque (locality optimization)
     */
    @Overwrite(remap = false)
    protected void read(BitBuffer buf) {
        super.read(buf);

        this.itemLevel = buf.readInt();
        this.state = buf.readEnum(VaultGearState.class);
        this.rarity = buf.readEnum(VaultGearRarity.class);
        this.repairSlots = buf.readInt();
        this.usedRepairSlots = buf.readInt();

        this.baseModifiers_al = buf.readCollection(ArrayListDeque::new, this.versioned(VaultGearAttributeRegistry::readModifier));
        this.baseModifiers_al.removeIf(Objects::isNull);

        this.prefixes_al = buf.readCollection(ArrayListDeque::new, this.versioned(VaultGearAttributeRegistry::readModifier));
        this.prefixes_al.removeIf(Objects::isNull);

        this.suffixes_al = buf.readCollection(ArrayListDeque::new, this.versioned(VaultGearAttributeRegistry::readModifier));
        this.suffixes_al.removeIf(Objects::isNull);
    }

    /**
     * @author Koromaru Koruko
     * @reason moved LinkedList to ArrayListDeque (locality optimization)
     */
    @Overwrite(remap = false)
    public CompoundTag toNbt() {
        CompoundTag tag = super.toNbt();
        tag.putInt("itemLevel", this.itemLevel);
        tag.putInt("state", this.state.ordinal());
        tag.putInt("rarity", this.rarity.ordinal());
        tag.putInt("repairSlots", this.repairSlots);
        tag.putInt("usedRepairSlots", this.usedRepairSlots);
        ListTag baseModifiers = new ListTag();
        this.baseModifiers_al.stream().map(VaultGearAttributeRegistry::serializeAttributeInstance).forEach(baseModifiers::add);
        tag.put("baseModifiers", baseModifiers);
        ListTag prefixes = new ListTag();
        this.prefixes_al.stream().map(VaultGearAttributeRegistry::serializeAttributeInstance).forEach(prefixes::add);
        tag.put("prefixes", prefixes);
        ListTag suffixes = new ListTag();
        this.suffixes_al.stream().map(VaultGearAttributeRegistry::serializeAttributeInstance).forEach(suffixes::add);
        tag.put("suffixes", suffixes);
        return tag;
    }

    /**
     * @author Koromaru Koruko
     * @reason moved LinkedList to ArrayListDeque (locality optimization)
     */
    @Overwrite(remap = false)
    protected void fromNbt(CompoundTag tag, GearDataVersion version) {
        this.clear();
        super.fromNbt(tag, version);
        this.itemLevel = tag.getInt("itemLevel");
        this.state = VaultGearState.values()[tag.getInt("state")];
        this.rarity = VaultGearRarity.values()[tag.getInt("rarity")];
        this.repairSlots = tag.getInt("repairSlots");
        this.usedRepairSlots = tag.getInt("usedRepairSlots");
        ListTag baseModifiers = tag.getList("baseModifiers", 10);
        baseModifiers.stream()
                .map(nbt -> (CompoundTag)nbt)
                .map(nbt -> VaultGearAttributeRegistry.deserializeModifier(nbt, version))
                .filter(Objects::nonNull)
                .forEach(this.baseModifiers_al::add);
        ListTag prefixes = tag.getList("prefixes", 10);
        prefixes.stream()
                .map(nbt -> (CompoundTag)nbt)
                .map(nbt -> VaultGearAttributeRegistry.deserializeModifier(nbt, version))
                .filter(Objects::nonNull)
                .forEach(this.prefixes_al::add);
        ListTag suffixes = tag.getList("suffixes", 10);
        suffixes.stream()
                .map(nbt -> (CompoundTag)nbt)
                .map(nbt -> VaultGearAttributeRegistry.deserializeModifier(nbt, version))
                .filter(Objects::nonNull)
                .forEach(this.suffixes_al::add);
    }

    /**
     * @author Koromaru Koruko
     * @reason moved LinkedList to ArrayListDeque (locality optimization)
     */
    @Overwrite(remap = false)
    public JsonObject serialize() {
        JsonObject obj = super.serialize();
        obj.addProperty("level", this.itemLevel);
        obj.addProperty("rarity", this.rarity.name());
        obj.addProperty("state", this.state.name());
        obj.addProperty("maxRepairs", this.repairSlots);
        obj.addProperty("usedRepairs", this.usedRepairSlots);
        JsonArray implicits = new JsonArray();
        this.baseModifiers_al.forEach(attr -> implicits.add(attr.serialize(VaultGearModifier.AffixType.IMPLICIT)));
        obj.add("implicits", implicits);
        JsonArray prefixes = new JsonArray();
        this.prefixes_al.forEach(attr -> prefixes.add(attr.serialize(VaultGearModifier.AffixType.PREFIX)));
        obj.add("prefixes", prefixes);
        JsonArray suffixes = new JsonArray();
        this.suffixes_al.forEach(attr -> suffixes.add(attr.serialize(VaultGearModifier.AffixType.SUFFIX)));
        obj.add("suffixes", suffixes);
        return obj;
    }
}
