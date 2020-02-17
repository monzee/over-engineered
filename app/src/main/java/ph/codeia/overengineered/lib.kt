package ph.codeia.overengineered

import androidx.compose.Composable
import androidx.compose.Model
import androidx.compose.remember
import androidx.lifecycle.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/*
 * This file is a part of the Over Engineered project.
 */


interface LiveComposableScope<T> {
	val latestValue: T?
	operator fun T.unaryPlus()
	operator fun LiveData<T>.unaryPlus(): () -> Unit
}


@Composable
inline fun <T> liveComposable(
	block: @Composable LiveComposableScope<T>.() -> Unit
): LiveData<T> = MediatorLiveData<T>().also { dest ->
	block(object : LiveComposableScope<T> {
		override val latestValue: T? = dest.value

		override fun T.unaryPlus() = let {
			dest.value = it
		}

		override fun LiveData<T>.unaryPlus(): () -> Unit = let { src ->
			dest.addSource(src) { +it }
			({ dest.removeSource(src) })
		}
	})
}


class SingleUse<out T: Any>(val value: T)

typealias Consumable<T> = LiveData<SingleUse<T>>

inline fun <T: Any> Consumable<T>.consume(
	owner: LifecycleOwner?,
	crossinline consumer: (T) -> Unit
) : Observer<SingleUse<T>> = let { live ->
	val asMutable = live as MutableLiveData<SingleUse<T>>
	val observer = Observer<SingleUse<T>> { singleUse ->
		singleUse?.let {
			consumer(it.value)
			asMutable.value = null
		}
	}
	owner?.let { observe(it, observer) }
		?: run { observeForever(observer) }
	observer
}


var <T: Any> MutableLiveData<SingleUse<T>>.unwrapped: T?
	get() = value?.value
	set(value) {
		setValue(value?.let(::SingleUse))
	}


operator fun <T: Any> MutableLiveData<SingleUse<T>>.plusAssign(value: T) {
	unwrapped = value
}


fun <T: Any> MutableLiveData<SingleUse<T>>.setValue(value: T?) {
	setValue(value?.let(::SingleUse))
}


fun <T: Any> MutableLiveData<SingleUse<T>>.postValue(value: T?) {
	postValue(value?.let(::SingleUse))
}


@Model
class StateMachine<S: Any, E>(
	initial: S,
	private val transition: (S, E) -> S?
) {
	var value: S = initial
		private set

	fun dispatch(event: E) {
		transition(value, event)?.let {
			value = it
		}
	}

	operator fun component1(): S = value
	operator fun component2(): (E) -> Unit = ::dispatch
}


@Composable
fun <S: Any, E> scan(
	initial: S,
	accumulate: (S, E) -> S?
): StateMachine<S, E> = remember {
	StateMachine(initial, accumulate)
}


@Composable
@JvmName("flipScan")
inline fun <S: Any, E> scan(
	initial: S,
	crossinline accumulate: (E, S) -> S?
): StateMachine<S, E> = scan(initial) { state, event: E ->
	accumulate(event, state)
}


/**
 * LiveData with completion semantics.
 *
 * Makeshift Flow<T> because collecting a flow would cause a compile
 * error at the moment.
 */
typealias Many<T> = CoroutineScope.() -> Collectible<T>
typealias Collectible<T> = MutableLiveData<Finite<T>>

sealed class Finite<out T> {
	class Next<T>(val value: T) : Finite<T>()
	object Done : Finite<Nothing>()
}

fun <T> Collectible<T>.emit(value: T) {
	setValue(Finite.Next(value))
}

fun <T> CoroutineScope.collect(produce: Many<T>, block: (T) -> Unit) {
	val data = produce()
	data.observeForever(object : Observer<Finite<T>> {
		override fun onChanged(next: Finite<T>?) {
			when (next) {
				is Finite.Next -> block(next.value)
				Finite.Done -> data.removeObserver(this)
			}
		}
	})
}

fun <T> many(
	block: suspend Collectible<T>.() -> Unit
): Many<T> = {
	object : Collectible<T>() {
		override fun onActive() {
			launch {
				block()
				setValue(Finite.Done)
			}
		}
	}
}