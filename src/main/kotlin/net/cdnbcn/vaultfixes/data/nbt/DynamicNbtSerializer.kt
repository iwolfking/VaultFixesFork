package net.cdnbcn.vaultfixes.data.nbt

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import org.apache.logging.log4j.core.tools.picocli.CommandLine.TypeConversionException
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

class DynamicNbtSerializer<T : Any> private constructor(type: KClass<T>) {
    internal companion object {
        private val serializerMap: ConcurrentMap<KClass<*>, DynamicNbtSerializer<*>> = ConcurrentHashMap()

        public inline fun <reified T : Any> get() : DynamicNbtSerializer<T> {
            return get(T::class)
        }
        public inline fun <reified T : Any> get(type: KClass<T>) : DynamicNbtSerializer<T> {
            ensureValid(type)

            @Suppress("UNCHECKED_CAST")
            return getRaw(type) as DynamicNbtSerializer<T>
        }

        private fun getRaw(type: KClass<*>) : DynamicNbtSerializer<*>  {
            return serializerMap.getOrPut(type) {
                DynamicNbtSerializer::class.primaryConstructor!!.call(type)
            }
        }

        public inline fun <reified T: Any> isValid() : Boolean {return isValid(T::class) }
        fun isValid(type: KClass<*>) : Boolean {
            return type.annotations.any{ it is DynNbt }
        }
        fun isValid(field: Field) : Boolean {
            return !field.annotations.any { annotation -> annotation is DynNbtIgnore }
        }

        private fun ensureValid(type: KClass<*>) {
            if (!isValid(type))
                throw TypeConversionException("Unable to build Generic ${type.qualifiedName}")
        }
    }

    private data class SLActions<T: Any>(inline val save: (data: T, tag: CompoundTag) -> Unit, inline val load: (data: T, tag: CompoundTag) -> Unit)

    public val save: (data: T, tag: CompoundTag) -> Unit
    public val load: (data: T, tag: CompoundTag) -> Unit

    init {
        var last= SLActions<T>({_,_->},{_,_->})

        for(field in type.java.declaredFields) {
            if(field.isEnumConstant
                .or(field.isSynthetic)
                .or(field.modifiers.and(Modifier.STATIC) != 0)
                || field.annotations.any { it is DynNbtIgnore }
                )
                continue

            val name = run {
                val x = field.annotations.firstOrNull { (it is DynNbtName) }
                if(x == null)
                    field.name
                else
                    (x as DynNbtName).name
            }

            last =
            when (field.type)
            {
                Long::class.java -> longActions(field, name, last)
                LongArray::class.java -> longArrayActions(field, name, last)
                Int::class.java -> intActions(field, name, last)
                IntArray::class.java -> intArrayActions(field, name, last)
                Short::class.java -> shortActions(field, name, last)
                String::class.java -> stringActions(field, name, last)
                UUID::class.java -> uuidActions(field, name, last)
                Boolean::class.java -> booleanActions(field, name, last)
                Byte::class.java -> byteActions(field, name, last)
                ByteArray::class.java -> byteArrayActions(field, name, last)
                Float::class.java -> floatActions(field, name, last)
                Double::class.java -> doubleActions(field, name, last)
                ListTag::class.java -> listActions(field, name, last)
                CompoundTag::class.java -> compoundActions(field, name, last)



                else -> {
                    ensureValid(field.type.kotlin)

                    remoteActions(field, name, last, getRaw(field.type.kotlin))
                }
            }
        }

        save = last.save
        load = last.load
    }
    private fun saveRaw(data: Any, tag: CompoundTag) {
        @Suppress("UNCHECKED_CAST")
        save(data as T, tag)
    }
    private fun loadRaw(data: Any, tag: CompoundTag) {
        @Suppress("UNCHECKED_CAST")
        load(data as T, tag)
    }

    //region Action Creators
    private fun remoteActions(field: Field, name: String, last: SLActions<T>, remoteSerializer: DynamicNbtSerializer<*>) : SLActions<T> {
        return SLActions(
            { data, tag ->
                val stag = CompoundTag()
                remoteSerializer.saveRaw(field.get(data), stag)
                tag.put(name, stag)
                last.save(data,tag)
            },
            { data, tag ->
                if(tag.contains(name))
                    remoteSerializer.loadRaw(field.get(data), tag.getCompound(name))
                last.load(data,tag)
            }
        )
    }
    private fun longActions(field: Field, name: String, last: SLActions<T>) : SLActions<T> {
        return SLActions(
            {data, tag -> tag.putLong(name, field.get(data) as Long); last.save(data,tag) },
            {data, tag -> if (tag.contains(name)) field.set(data, tag.getLong(name)); last.load(data,tag) }
        )
    }
    private fun longArrayActions(field: Field, name: String, last: SLActions<T>) : SLActions<T> {
        return SLActions(
            {data, tag -> tag.putLongArray(name, field.get(data) as LongArray); last.save(data,tag) },
            {data, tag -> if (tag.contains(name)) field.set(data, tag.getLongArray(name)); last.load(data,tag) }
        )
    }
    private fun intActions(field: Field, name: String, last: SLActions<T>) : SLActions<T> {
        return SLActions(
            { data, tag -> tag.putInt(name, field.get(data) as Int); last.save(data,tag) },
            { data, tag -> if (tag.contains(name)) field.set(data, tag.getInt(name)); last.load(data,tag) }
        )
    }
    private fun intArrayActions(field: Field, name: String, last: SLActions<T>) : SLActions<T> {
        return SLActions(
            { data, tag -> tag.putIntArray(name, field.get(data) as IntArray); last.save(data,tag) },
            { data, tag -> if (tag.contains(name)) field.set(data, tag.getIntArray(name)); last.load(data,tag) }
        )
    }
    private fun stringActions(field: Field, name: String, last: SLActions<T>) : SLActions<T> {
        return SLActions(
            {data, tag -> tag.putString(name, field.get(data) as String); last.save(data,tag) },
            {data, tag -> if (tag.contains(name)) field.set(data, tag.getString(name)); last.load(data,tag) }
        )
    }
    private fun uuidActions(field: Field, name: String, last: SLActions<T>) : SLActions<T> {
        return SLActions(
            {data, tag -> tag.putUUID(name, field.get(data) as UUID); last.save(data,tag) },
            {data, tag -> if (tag.contains(name)) field.set(data, tag.getUUID(name)); last.load(data,tag) }
        )
    }
    private fun shortActions(field: Field, name: String, last: SLActions<T>) : SLActions<T> {
        return SLActions(
            {data, tag -> tag.putShort(name, field.get(data) as Short); last.save(data,tag) },
            {data, tag -> if (tag.contains(name)) field.set(data, tag.getShort(name)); last.load(data,tag) }
        )
    }
    private fun booleanActions(field: Field, name: String, last: SLActions<T>) : SLActions<T> {
        return SLActions(
            {data, tag -> tag.putBoolean(name, field.get(data) as Boolean); last.save(data,tag) },
            {data, tag -> if (tag.contains(name)) field.set(data, tag.getBoolean(name)); last.load(data,tag) }
        )
    }
    private fun byteActions(field: Field, name: String, last: SLActions<T>) : SLActions<T> {
        return SLActions(
            {data, tag -> tag.putByte(name, field.get(data) as Byte); last.save(data,tag) },
            {data, tag -> if (tag.contains(name)) field.set(data, tag.getByte(name)); last.load(data,tag) }
        )
    }
    private fun byteArrayActions(field: Field, name: String, last: SLActions<T>) : SLActions<T> {
        return SLActions(
            {data, tag -> tag.putByteArray(name, field.get(data) as ByteArray); last.save(data,tag) },
            {data, tag -> if (tag.contains(name)) field.set(data, tag.getByteArray(name)); last.load(data,tag) }
        )
    }
    private fun floatActions(field: Field, name: String, last: SLActions<T>) : SLActions<T> {
        return SLActions(
            {data, tag -> tag.putFloat(name, field.get(data) as Float); last.save(data,tag) },
            {data, tag -> if (tag.contains(name)) field.set(data, tag.getFloat(name)); last.load(data,tag) }
        )
    }
    private fun doubleActions(field: Field, name: String, last: SLActions<T>) : SLActions<T> {
        return SLActions(
            {data, tag -> tag.putDouble(name, field.get(data) as Double); last.save(data,tag) },
            {data, tag -> if (tag.contains(name)) field.set(data, tag.getDouble(name)); last.load(data,tag) }
        )
    }
    private fun compoundActions(field: Field, name: String, last: SLActions<T>) : SLActions<T> {
        return SLActions(
            {data, tag -> tag.put(name, field.get(data) as CompoundTag) },
            {data, tag -> if (tag.contains(name)) field.set(data, tag.getCompound(name)) }
        )
    }
    private fun listActions(field: Field, name: String, last: SLActions<T>) : SLActions<T> {
        return SLActions(
            {data, tag -> tag.put(name, field.get(data) as ListTag) },
            {data, tag -> if (tag.contains(name)) field.set(data, tag.get(name) as ListTag)  }
        )
    }
    //endregion Action Creators
}
