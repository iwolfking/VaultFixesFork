package net.cdnbcn.vaultfixes.data

import java.util.*
import java.util.function.Supplier
import kotlin.collections.ArrayList

class ArrayListDeque<T>(private val inner: ArrayList<T>) : List<T>, Deque<T> {

    override var size: Int = inner.size

    constructor(size: Int) : this(ArrayList(size)) {}
    constructor() : this(ArrayList()) {}

    private fun <T> modifyAction(action: Supplier<T>) : T
    {
        val ret: T = action.get()
        onModified()
        return ret
    }
    private fun modifyActionNoReturn(action: Runnable)
    {
        action.run()
        onModified()
    }

    private fun onModified() {
        size = inner.size
    }

    override fun iterator(): MutableIterator<T> { return MutableNotifyIterator(inner.iterator(), ::onModified) }
    override fun listIterator(): ListIterator<T> { return inner.listIterator() }
    override fun listIterator(index: Int): ListIterator<T> { return inner.listIterator(index) }
    override fun descendingIterator(): MutableIterator<T> { return MutableNotifyIterator(ListDescendingMutableIterator(inner), ::onModified) }
    override fun contains(element: T): Boolean { return inner.contains(element) }
    override fun containsAll(elements: Collection<T>): Boolean { return inner.containsAll(elements) }
    override fun get(index: Int): T  { return inner[index] }
    override fun add(element: T): Boolean { return modifyAction { inner.add(element) } }
    override fun addAll(elements: Collection<T>): Boolean { return modifyAction { inner.addAll(elements) } }
    override fun clear() { modifyActionNoReturn { inner.clear() } }
    override fun remove(): T { return modifyAction {
        val elem: T = inner[0]
        inner.removeAt(0)
        elem
    } }
    override fun retainAll(elements: Collection<T>): Boolean { return modifyAction { inner.retainAll(elements.toSet()) } }
    override fun removeAll(elements: Collection<T>): Boolean { return modifyAction { inner.removeAll(elements.toSet()) } }
    override fun remove(element: T): Boolean { return modifyAction { inner.remove(element) } }
    override fun subList(fromIndex: Int, toIndex: Int): List<T> { return inner.subList(fromIndex, toIndex) }
    override fun lastIndexOf(element: T): Int { return inner.lastIndexOf(element) }
    override fun isEmpty(): Boolean { return inner.isEmpty() }
    override fun poll(): T? { return if (this.isEmpty()) { null } else { remove() } }
    override fun element(): T { return inner[0] }
    override fun peek(): T? { return if (this.isEmpty()) { null } else { element() }}
    override fun removeFirst(): T { return remove() }
    override fun removeLast(): T { return modifyAction {
        val elem: T = inner[inner.size-1]
        inner.removeAt(inner.size-1)
        elem
    } }
    override fun pollFirst(): T? { return poll() }
    override fun pollLast(): T? { return if (this.isEmpty()) { null } else { removeLast() } }
    override fun getFirst(): T { return element() }
    override fun getLast(): T { return inner[inner.size-1] }
    override fun peekFirst(): T? { return peek() }
    override fun peekLast(): T? { return if (this.isEmpty()) { null } else { last } }
    override fun removeFirstOccurrence(o: Any?): Boolean { return remove(o) }
    override fun removeLastOccurrence(o: Any?): Boolean { return modifyAction {
        var x: Int = inner.size-1
        while(x > -1)
        {
            if(inner[x] == o)
            {
                inner.removeAt(x)
                return@modifyAction true
            }
            ++x
        }
        false
    } }
    override fun pop(): T { return removeFirst() }
    override fun push(e: T) { return modifyAction { inner.add(0, e) } }
    override fun offerLast(e: T): Boolean { return modifyAction { inner.add(e); true } }
    override fun offerFirst(e: T): Boolean { return modifyAction { inner.add(0, e); true } }
    override fun addLast(e: T) { offerLast(e) }
    override fun addFirst(e: T) { addFirst(e) }
    override fun offer(e: T): Boolean { offerLast(e); return true }
    override fun indexOf(element: T): Int { return inner.indexOf(element) }

}