package ph.codeia.overengineered.controls

import androidx.compose.Ambient
import androidx.compose.Composable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.ui.unit.Dp
import androidx.ui.unit.dp

/*
 * This file is a part of the Over Engineered project.
 */


typealias Block<T, R> = (T) -> R
typealias Children = @Composable Procedure
typealias Predicate<T> = (T) -> Boolean
typealias Procedure = () -> Unit
typealias Property<R> = () -> R
typealias Sink<T> = (T) -> Unit

val Always: Predicate<Any> = { true }
val BlackHole: Block<Any, Nothing> = { error("Unimplemented block") }
val NoOp: Procedure = {}
val Missing: Property<Nothing> = { error("Unimplemented property") }
val Pass: Sink<Any> = {}
val Sizes: Ambient<Metrics> = Ambient.of { Metrics(12) }

data class Metrics(
	val unit: Dp
) {
	constructor(unit: Int) : this(unit.dp)

	val half = unit / 2
	val tiny = unit / 4
	val double = unit * 2
	val large = unit * 4
	val huge = unit * 8
}

inline fun <T, R: Any> LiveData<T>.mapNotNull(
	crossinline transform: (T) -> R?
): LiveData<R> = let { source ->
	val dest = MediatorLiveData<R>()
	dest.addSource(source) { t ->
		transform(t)?.let {
			dest.value = it
		}
	}
	dest
}
