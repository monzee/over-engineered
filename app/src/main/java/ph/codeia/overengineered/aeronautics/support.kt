package ph.codeia.overengineered.aeronautics


/*
 * This file is a part of the Over Engineered project.
 */


typealias Get<T> = () -> T
typealias Put<T> = (T) -> Unit
typealias Use<I, O> = ((I) -> O) -> Unit

sealed class Progress<out W, out T> {
	object Idle : Progress<Nothing, Nothing>()
	object Busy : Progress<Nothing, Nothing>()
	class Failed(val cause: Throwable) : Progress<Nothing, Nothing>()
	class Active<W>(val work: W) : Progress<W, Nothing>()
	class Done<T>(val payload: T) : Progress<Nothing, T>()
}