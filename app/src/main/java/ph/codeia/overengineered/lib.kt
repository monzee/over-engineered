package ph.codeia.overengineered

import androidx.compose.Composable
import androidx.lifecycle.*

/*
 * This file is a part of the Over Engineered project.
 */


interface LiveComposableScope<T> {
	val latestValue: T?
	operator fun T.unaryPlus()
	operator fun LiveData<T>.unaryPlus(): () -> Unit
}


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
