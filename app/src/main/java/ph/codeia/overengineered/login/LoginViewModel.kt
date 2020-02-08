package ph.codeia.overengineered.login

import androidx.compose.Model
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.Module
import dagger.Provides

@Module
class LoginViewModel(private val savedState: SavedStateHandle) : ViewModel() {
	@Model
	private class State : LoginControl.State {
		override var username: String = ""
		override var password: String = ""
		override var errors: ValidationResult = ValidationResult.Valid
	}

	private val state = State()

	@get:Provides
	val loginState: LoginControl.State = state

	@Provides
	fun controller(): Observer<LoginAction> = Observer {
		when (it) {
			is SetUsername -> {
				state.username = it.value
			}
			is SetPassword -> {
				state.password = it.value
			}
			Submit -> TODO()
		}
	}
}