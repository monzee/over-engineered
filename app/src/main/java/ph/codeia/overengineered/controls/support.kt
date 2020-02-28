package ph.codeia.overengineered.controls

import androidx.compose.*
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.ui.core.DrawModifier
import androidx.ui.graphics.Canvas
import androidx.ui.graphics.Color
import androidx.ui.graphics.Paint
import androidx.ui.graphics.PaintingStyle
import androidx.ui.unit.*

/*
 * This file is a part of the Over Engineered project.
 */


typealias Block<T, R> = (T) -> R
typealias CallWith<T> = Block<T, Unit>
typealias Children = @Composable Procedure
typealias Field<C, R> = C.() -> R
typealias PassInto<C, T> = Method<C, T, Unit>
typealias Method<C, T, R> = C.(T) -> R
typealias Predicate<T> = Block<T, Boolean>
typealias Procedure = Value<Unit>
typealias RunIn<C> = Field<C, Unit>
typealias Value<R> = () -> R

val Always: Predicate<Any> = { true }
val BlackHole: Block<Any, Nothing> = { error("Unimplemented block") }
val Missing: Value<Nothing> = { error("Unimplemented property") }
val Never: Predicate<Any> = { false }
val Pass: Procedure = {}
val Ignore: CallWith<Any> = {}

data class Metrics(
	val unit: Dp
) {
	constructor(unit: Int) : this(unit.dp)

	val half = unit / 2
	val tiny = unit / 4
	val double = unit * 2
	val large = unit * 4
	val huge = unit * 8

	companion object {
		val Handle = ambientOf { Metrics(12) }
	}
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

class DebugOutline(private val color: Color) : DrawModifier {
	override fun draw(
		density: Density,
		drawContent: () -> Unit,
		canvas: Canvas,
		size: PxSize
	) {
		paint.color = color
		canvas.drawRect(size.toRect(), paint)
		drawContent()
	}

	companion object : DrawModifier by DebugOutline(Color.Blue) {
		val Red by lazy { DebugOutline(Color.Red) }
		val Green by lazy { DebugOutline(Color.Green) }
		private val paint = Paint().apply {
			strokeWidth = 2f
			style = PaintingStyle.stroke
			color = Color.Blue
		}
	}
}

fun <T> MutableState<T?>.consume(): T? = value.also {
	value = null
}

