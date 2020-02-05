package ph.codeia.overengineered

import androidx.compose.Composable
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer


/*
 * This file is a part of the Over Engineered project.
 */


interface LiveComposableScope<T> {
	val latestValue: T?
	operator fun T.unaryPlus()
	operator fun LiveData<out T>.unaryPlus(): Runnable
}


inline fun <T> liveComposable(
	block: @Composable LiveComposableScope<T>.() -> Unit
): LiveData<out T> = MutableLiveData<T>().also { live ->
	block(object : LiveComposableScope<T> {
		override val latestValue: T? = live.value

		override fun T.unaryPlus() = let {
			live.value = it
		}

		override fun LiveData<out T>.unaryPlus(): Runnable = let { source ->
			val observer = Observer<T> { +it }
			source.observeForever(observer)
			Runnable {
				source.removeObserver(observer)
			}
		}
	})
}


interface Control<out Event> {
	@Composable
	fun view(): LiveData<out Event>

	companion object {
		operator fun <Event> invoke(
			block: @Composable LiveComposableScope<Event>.() -> Unit
		) = object : Control<Event> {
			override fun view(): LiveData<out Event> = liveComposable(block)
		}
	}
}


class SingleUse<out T: Any>(val value: T)


inline fun <T: Any> LiveData<SingleUse<T>>.consume(
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


fun <T: Any> MutableLiveData<SingleUse<T>>.setValue(value: T?) {
	setValue(value?.let(::SingleUse))
}


fun <T: Any> MutableLiveData<SingleUse<T>>.postValue(value: T?) {
	postValue(value?.let(::SingleUse))
}
