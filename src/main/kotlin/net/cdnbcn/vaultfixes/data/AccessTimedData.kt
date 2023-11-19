package net.cdnbcn.vaultfixes.data
import java.time.Instant

data class AccessTimedData<T>(private val snapshot: T) {
    private var _lastAccess: Instant = Instant.now()

    val lastAccess: Instant
        get() {return _lastAccess }

    fun get(): T{
        _lastAccess = Instant.now()
        return snapshot
    }
    fun getNoAccess() : T {
        return snapshot
    }
}
