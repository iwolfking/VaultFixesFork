package net.cdnbcn.vaultfixes.data

class ListDescendingMutableIterator<T>(private val list: MutableList<T>) : MutableIterator<T> {
    private var index: Int = list.size-1
    override fun hasNext(): Boolean { return index >= 0 }
    override fun next(): T { index-=1; return list[index] }
    override fun remove() { list.removeAt(index) }

}