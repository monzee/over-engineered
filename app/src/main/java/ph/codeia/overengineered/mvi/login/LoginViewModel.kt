package ph.codeia.overengineered.mvi.login

import androidx.compose.State
import androidx.compose.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.Binds
import dagger.Module
import dagger.Provides
import ph.codeia.overengineered.collect
import ph.codeia.overengineered.controls.CallWith

@Module(includes = [LoginViewModel.Bindings::class])
class LoginViewModel(private val savedState: SavedStateHandle) : ViewModel() {

	@Module
	interface Bindings {
		@Binds
		fun loginAdapter(adapter: ValidateOnFieldChange): LoginAdapter
	}

	private val state = mutableStateOf(from(savedState))

	@get:Provides
	val readOnlyState: State<LoginModel> = state

	@get:Provides
	val service: LoginService = FakeLoginService

	@Provides
	fun controller(adapter: LoginAdapter): CallWith<LoginAction> = { action ->
		val (current, next) = state
		with(adapter) {
			when (action) {
				is SetUsername -> next(current.setUsername(action.value))
				is SetPassword -> next(current.setPassword(action.value))
				Submit -> viewModelScope.collect(current.submit(state::value), next)
			}
		}
	}

	override fun onCleared() {
		super.onCleared()
		val model = state.value
		val errors = model.tag as? Validated
		savedState[IsIdle] = model.tag == Idle
		savedState[Username] = model.username
		savedState[Password] = model.password
		savedState[UsernameError] = errors?.username
		savedState[PasswordError] = errors?.password
	}

	private companion object {
		const val IsIdle = "isIdle"
		const val Username = "username"
		const val Password = "password"
		const val UsernameError = "usernameError"
		const val PasswordError = "passwordError"

		// why am i forced to specify the type params here? shouldn't they be
		// inferrable from the use site?
		fun from(savedState: SavedStateHandle) = run {
			val isIdle = savedState.get<Boolean>(IsIdle) ?: true
			LoginModel(
				savedState.get<String>(Username) ?: "",
				savedState.get<String>(Password) ?: "",
				if (isIdle) Idle else Validated(
					savedState.get<String>(UsernameError),
					savedState.get<String>(PasswordError)
				)
			)
		}
	}
}