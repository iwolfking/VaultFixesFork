package net.cdnbcn.vaultfixes.data

import java.lang.ref.WeakReference
import java.time.Instant
import java.util.*
import java.util.function.Consumer
import kotlin.collections.ArrayList

interface ISavableData {
    fun `vaultFixes$isDirty`(): Boolean
}
internal interface ICleanUpAccessor {
    fun doCleanUp()
    fun doCleanUpAll()
}
class TemporalMapCache<K, V : ISavableData>(private val lifeTimeSeconds: Long, private val onDestroy: Consumer<V>, private val onSave: Consumer<V>)
    : MutableMap<K, V>, ICleanUpAccessor
{

    companion object {
        private val timer = Timer("TemporalMapCacheCleanupThread",true)
        init {
            timer.scheduleAtFixedRate(DelegatingTimerTask(::doCleanUp), 10*1000L, 10*1000L)
            Runtime.getRuntime().addShutdownHook(Thread(::doCleanUpAll, "TemporalShutdownHook"))
        }

        private val lock = Object()
        private val caches = mutableListOf<WeakReference<ICleanUpAccessor>>()

        private fun doCleanUp() {
            synchronized(lock) {
                val iter = caches.listIterator()

                while(iter.hasNext()) {
                    val ref = iter.next()
                    val cache = ref.get()
                    if (cache == null)
                        iter.remove()
                    else
                        cache.doCleanUp()
                }
            }
        }
        internal fun doCleanUpAll() {
            synchronized(lock) {
                val iter = caches.listIterator()

                while(iter.hasNext()) {
                    val ref = iter.next()
                    val cache = ref.get()
                    if (cache == null)
                        iter.remove()
                    else
                        cache.doCleanUpAll()
                }
            }
        }
        private fun addCache(cache: ICleanUpAccessor) {
            synchronized(lock) {
                caches.add(WeakReference(cache))
            }
        }
    }

    init {
        addCache(this)
    }

    private val map = mutableMapOf<K, AccessTimedData<V>>()

    override fun doCleanUp() {
        synchronized(map) {
            val now = Instant.now()

            map.entries.parallelStream()
                .collect(::ArrayList,
                    { arr, kv ->
                        val data = kv.value.getNoAccess()
                        if (data.`vaultFixes$isDirty`())
                            try { onSave.accept(data) } catch (ignored: Exception) {}

                        if (now.isAfter(kv.value.lastAccess.plusSeconds(lifeTimeSeconds)))
                            try { onDestroy.accept(data) } catch (ignored: Exception) {}
                            finally { arr.add(kv.key) }
                    },
                    ArrayList<K>::addAll)
                .forEach(map::remove)
        }
    }
    override fun doCleanUpAll() {
        synchronized(map) {
            map.values.parallelStream().forEach{
                val data = it.getNoAccess()
                if (data.`vaultFixes$isDirty`())
                    try { onSave.accept(data) } catch (ignored: Exception) {}

                try { onDestroy.accept(it.getNoAccess()) } catch(ignored: Exception) { }
            }
            map.clear()
        }
    }

    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() = throw Exception("Not Supported!")
    override val keys: MutableSet<K>
        get() = synchronized(map) { map.keys.toMutableSet() } // copy-protect
    override val size: Int
        get() = map.size
    override val values: MutableCollection<V>
        get() = synchronized(map) { map.values.map(AccessTimedData<V>::getNoAccess).toCollection(ArrayList(map.size)) } // copy-protect

    override fun clear() {
        synchronized(map) {
            map.clear()
        }
    }
    override fun isEmpty(): Boolean { return map.isEmpty() }
    override fun remove(key: K): V? {
        synchronized(map) {
            return map.remove(key)?.getNoAccess()
        }
    }
    override fun putAll(from: Map<out K, V>) {
        synchronized(map) {
            map.putAll(from.map { Pair(it.key, AccessTimedData(it.value)) })
        }
    }
    override fun put(key: K, value: V): V? {
        synchronized(map) {
            return map.put(key, AccessTimedData(value))?.getNoAccess()
        }
    }
    override fun get(key: K): V? {
        synchronized(map) {
            return map[key]?.get()
        }
    }
    override fun containsValue(value: V): Boolean {
        synchronized(map) {
            return map.values.any { it.getNoAccess() == value }
        }
    }
    override fun containsKey(key: K): Boolean {
        synchronized(map) {
            return map.containsKey(key)
        }
    }
}