package ph.codeia.overengineered.login.mvi

import androidx.compose.State
import androidx.compose.mutableStateOf
import androidx.core.util.PatternsCompat
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ph.codeia.overengineered.controls.CallWith
import kotlin.random.Random

@Module
class LoginViewModel(private val savedState: SavedStateHandle) : ViewModel() {
	private val state = mutableStateOf(InitialState)

	@get:Provides
	val readOnlyState: State<LoginState> = state

	@Provides
	fun controller(): CallWith<@JvmSuppressWildcards LoginAction> = { action ->
		val (current, next) = state
		when (action) {
			is SetUsername -> next(current.copy(username = action.value).let {
				when (it.tag) {
					is Active -> it.validate()
					else -> it
				}
			})
			is SetPassword -> next(current.copy(password = action.value).let {
				when (it.tag) {
					is Active -> it.validate()
					else -> it
				}
			})
			Submit -> submit()
		}
	}

	private fun submit() {
		val (current, next) = state
		val tag = when (current.tag) {
			Busy -> return
			!is Active -> validate(current)
			else -> current.tag
		}
		if (!tag.errors.isValid) next(current.copy(tag = tag))
		else {
			next(current.copy(tag = Busy))
			viewModelScope.launch {
				delay(2000)
				try {
					when (Random.nextInt(4)) {
						0 -> error("wrong username or password")
						1 -> error("service unavailable")
						2 -> if (Random.nextInt(25) == 0) error("unlucky")
					}
					finish("congrats")
				} catch (ex: Exception) {
					finish(ex)
				}
			}
		}
	}

	private fun finish(token: String) {
		val (current, next) = state
		next(current.copy(tag = Done {
			next(current.validate())
			token
		}))
	}

	private fun finish(cause: Throwable) {
		val (current, next) = state
		next(current.copy(tag = Failed {
			next(current.validate())
			cause
		}))
	}

	private fun LoginState.validate() = let {
		it.copy(tag = validate(it))
	}

	private fun validate(state: LoginState) = Active(
		ValidationResult(
			when {
				state.username.isEmpty() -> Required
				!EmailRegex.matches(state.username) -> "bad email address format"
				else -> null
			},
			when {
				state.password.isEmpty() -> Required
				state.password.length < 5 -> "too simple"
				else -> null
			}
		)
	)

	private companion object {
		const val Required = "required"
		val EmailRegex = PatternsCompat.EMAIL_ADDRESS.toRegex()
		val InitialState = LoginState("", "", Idle)
	}
}