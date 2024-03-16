package matt.reflect.j

import matt.lang.anno.Open
import matt.lang.function.Consume
import java.util.Spliterator
import kotlin.collections.Map.Entry
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1
import kotlin.reflect.cast
import kotlin.reflect.jvm.isAccessible

val KClass<*>.hasNoArgsConstructor
    get() = noArgConstructor != null

/*straight from createInstance()*/
val KClass<*>.noArgConstructor
    get() =
        constructors.singleOrNull {
            it.parameters.all(
                kotlin.reflect.KParameter::isOptional
            )
        }

@Target(AnnotationTarget.CLASS) annotation class ConstructedThroughReflection(val by: KClass<*>)
@Target(AnnotationTarget.CLASS) annotation class NoArgConstructor

fun <V: Any?, R: Any?> KFunction<V>.access(op: KFunction<V>.() -> R): R {
    val oldAccessible = isAccessible
    isAccessible = true
    val r = op(this)
    isAccessible = oldAccessible
    return r
}

fun <V: Any?, R: Any?> KProperty<V>.access(op: KProperty<V>.() -> R): R {
    val oldAccessible = isAccessible
    isAccessible = true
    val r = op(this)
    isAccessible = oldAccessible
    return r
}

fun KProperty0<*>.accessAndGetDelegate() =
    access {
        this@accessAndGetDelegate.getDelegate()
    }

fun <T> KProperty1<T, *>.accessAndGetDelegate(receiver: T) =
    access {
        this@accessAndGetDelegate.getDelegate(receiver)
    }

fun <T: Any> KClass<out T>.recurseSealedClasses(): Sequence<KClass<out T>> =
    sequence {
        yield(this@recurseSealedClasses)
        sealedSubclasses.forEach {
            yieldAll(it.recurseSealedClasses())
        }
    }

fun <T: Any> Sequence<KClass<out T>>.objectInstances() = mapNotNull { it.objectInstance }.toList()
fun <T : Any> KClass<T>.onEachSealedSubClassRecursive(op: Consume<KClass<out T>>): Unit =
    sealedSubclasses.forEach {
        if (it.isSealed) {
            it.onEachSealedSubClassRecursive(op)
        } else {
            op(it)
        }
    }


inline fun <reified R> List<*>.castedTo() = CastedList(this) { it as R }


abstract class CastedCollection<E>(private val innerCollection: Collection<*>, protected val cast: (Any?) -> E): Collection<E> {
    final override val size: Int
        get() = innerCollection.size
    final override fun isEmpty(): Boolean = innerCollection.isEmpty()
    @Open
    override fun iterator() =
        object: Iterator<E> {
            private val subItr = innerCollection.iterator()
            override fun hasNext(): Boolean = subItr.hasNext()

            override fun next(): E = cast(subItr.next())
        }
}

open class CastedList<E>(private val innerList: List<*>, cast: (Any?) -> E): CastedCollection<E>(innerList, cast), List<E> {

    final override fun get(index: Int): E = cast(innerList.get(index))



    @Open
    override fun listIterator(): ListIterator<E> {
        TODO()
    }

    @Open
    override fun listIterator(index: Int): ListIterator<E> {
        TODO()
    }

    @Open
    override fun subList(
        fromIndex: Int,
        toIndex: Int
    ): List<E> {
        TODO()
    }


    final override fun lastIndexOf(element: E): Int = innerList.lastIndexOf(element)

    final override fun indexOf(element: E): Int = innerList.indexOf(element)

    final override fun containsAll(elements: Collection<E>): Boolean = innerList.containsAll(elements)

    final override fun contains(element: E): Boolean = innerList.contains(element)
}

class CastedMutableList<E>(private val innerList: MutableList<*>, cast: (Any?) -> E): CastedList<E>(innerList, cast), MutableList<E> {
    override fun iterator(): MutableIterator<E> {
        TODO()
    }

    override fun add(element: E): Boolean {
        TODO()
    }

    override fun add(
        index: Int,
        element: E
    ) {
        TODO()
    }

    override fun addAll(
        index: Int,
        elements: Collection<E>
    ): Boolean {
        TODO()
    }

    override fun addAll(elements: Collection<E>): Boolean {
        TODO()
    }

    override fun clear() {
        TODO()
    }

    override fun listIterator(): MutableListIterator<E> {
        TODO()
    }

    override fun listIterator(index: Int): MutableListIterator<E> {
        TODO()
    }

    override fun remove(element: E): Boolean {
        TODO()
    }

    override fun removeAll(elements: Collection<E>): Boolean {
        TODO()
    }

    override fun removeAt(index: Int): E {
        TODO()
    }

    override fun retainAll(elements: Collection<E>): Boolean {
        TODO()
    }

    override fun set(
        index: Int,
        element: E
    ): E {
        TODO()
    }

    override fun subList(
        fromIndex: Int,
        toIndex: Int
    ): MutableList<E> {
        TODO()
    }

    /*STUPID KOTLIN BETA 2 BUG*/
    override fun spliterator(): Spliterator<E> = super<CastedList>.spliterator()
}

inline fun <reified E> Set<*>.castedTo() = CastedSet(this) { it as E }

open class CastedSet<E>(private val innerList: Set<*>, cast: (Any?) -> E): CastedCollection<E>(innerList, cast), Set<E> {




    final override fun containsAll(elements: Collection<E>): Boolean = innerList.containsAll(elements)

    final override fun contains(element: E): Boolean = innerList.contains(element)
}

class CastedMutableSet<T>(innerList: Set<*>, cast: (Any?) -> T): CastedSet<T>(innerList, cast), MutableSet<T> {
    override fun add(element: T): Boolean {
        TODO()
    }

    override fun addAll(elements: Collection<T>): Boolean {
        TODO()
    }

    override fun clear() {
        TODO()
    }

    override fun iterator(): MutableIterator<T> {
        TODO()
    }

    override fun remove(element: T): Boolean {
        TODO()
    }

    override fun removeAll(elements: Collection<T>): Boolean {
        TODO()
    }

    override fun retainAll(elements: Collection<T>): Boolean {
        TODO()
    }

    /*STUPID KOTLIN BETA 2 BUG*/
    override fun spliterator(): Spliterator<T> = super<CastedSet>.spliterator()
}
inline fun <reified K, reified V: Any> Map<*, *>.castedTo() = CastedMap(this, castKey = { it as K }, castValue = { it as V })

class CastedMap<K, V: Any>(private val innerList: Map<*, *>, private val castKey: (Any?) -> K, private val castValue: (Any?) -> V): Map<K, V> {
    override val entries: Set<Entry<K, V>> by lazy {
        object: Set<Entry<K, V>> {
            override val size: Int
                get() = TODO("Not yet implemented")

            override fun isEmpty(): Boolean {
                TODO("Not yet implemented")
            }

            override fun iterator(): Iterator<Entry<K, V>> {
                return object: Iterator<Entry<K, V>> {
                    private val subItr = innerList.entries.iterator()
                    override fun hasNext(): Boolean = subItr.hasNext()

                    override fun next(): Entry<K, V> {
                        val uncastedEntry = subItr.next()
                        return object: Entry<K, V> {
                            override val key: K = castKey(uncastedEntry.key)
                            override val value: V = castValue(uncastedEntry.value)
                        }
                    }
                }
            }

            override fun containsAll(elements: Collection<Entry<K, V>>): Boolean {
                TODO("Not yet implemented")
            }

            override fun contains(element: Entry<K, V>): Boolean {
                TODO("Not yet implemented")
            }
        }
    }
    override val keys: Set<K> =
        object: Set<K> {
            override val size get() = this@CastedMap.size

            override fun isEmpty(): Boolean {
                TODO("Not yet implemented")
            }

            override fun iterator(): Iterator<K> = entries.iterator().asSequence().map { it.key }.iterator()

            override fun containsAll(elements: Collection<K>): Boolean {
                TODO("Not yet implemented")
            }

            override fun contains(element: K): Boolean {
                TODO("Not yet implemented")
            }
        }
    override val size: Int
        get() = innerList.size
    override val values: Collection<V>
        get() = TODO()

    override fun isEmpty(): Boolean {
        TODO()
    }

    override fun get(key: K): V? {
        TODO()
    }

    override fun containsValue(value: V): Boolean {
        TODO()
    }

    override fun containsKey(key: K): Boolean {
        TODO()
    }
}

