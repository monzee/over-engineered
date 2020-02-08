package ph.codeia.overengineered.login

import androidx.compose.Composable
import androidx.lifecycle.LiveData
import androidx.ui.material.MaterialTheme
import androidx.ui.tooling.preview.Preview
import ph.codeia.overengineered.liveComposable
import javax.inject.Inject

class LoginControl @Inject constructor(private val state: State) {
	constructor() : this(Sample)

	interface State {
		val username: String
		val password: String
		val errors: ValidationResult
	}

	@Composable
	operator fun invoke(): LiveData<LoginAction> = liveComposable {

	}

	private object Sample : State {
		override val username = "foo@example.com"
		override val password = "hunter2"
		override val errors = ValidationResult.Valid

		@[Composable Preview]
		fun preview() {
			MaterialTheme {
				LoginControl()()
			}
		}
	}
}
