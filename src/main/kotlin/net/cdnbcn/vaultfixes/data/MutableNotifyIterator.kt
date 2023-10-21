package net.cdnbcn.vaultfixes.data

class MutableNotifyIterator<T>(private val iter: MutableIterator<T>, private val onModified: Runnable) : MutableIterator<T> {
    override fun hasNext(): Boolean { return iter.hasNext() }

    override fun next(): T { return iter.next() }

    override fun remove() {
        iter.remove()
        onModified.run()
    }
}