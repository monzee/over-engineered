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


typealias Callback<T> = (T) -> Unit
typealias Children = @Composable Procedure
typealias Predicate<T> = (T) -> Boolean
typealias Procedure = () -> Unit

val Always: Predicate<Any> = { true }
val NoOp: Procedure = {}
val Sink: Callback<Any> = {}
val Spacing = Ambient.of { Metrics(12) }

data class Metrics(
	val base: Dp
) {
	constructor(base: Int) : this(base.dp)

	val half = base / 2
	val double = base * 2
	val tiny = base / 4
	val large = base * 4
	val huge = base * 8
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
