package ph.codeia.overengineered.login.mvi

import androidx.compose.Composable
import androidx.compose.State
import androidx.compose.ambient
import androidx.lifecycle.MutableLiveData
import androidx.ui.core.FocusManagerAmbient
import androidx.ui.core.Opacity
import androidx.ui.input.ImeAction
import androidx.ui.layout.Arrangement
import androidx.ui.layout.Column
import androidx.ui.layout.LayoutGravity
import androidx.ui.layout.LayoutPadding
import androidx.ui.material.Button
import androidx.ui.unit.dp
import ph.codeia.overengineered.Consumable
import ph.codeia.overengineered.SingleUse
import ph.codeia.overengineered.controls.CallWith
import ph.codeia.overengineered.controls.EditText
import ph.codeia.overengineered.controls.Ignore
import ph.codeia.overengineered.unwrapped
import javax.inject.Inject

class LoginForm @Inject constructor(
	private val state: State<LoginState>,
	private val exec: CallWith<@JvmSuppressWildcards LoginAction>
) {
	constructor() : this(Sample, Ignore)

	private val _result = MutableLiveData<SingleUse<LoginTag>>()
	val result: Consumable<LoginTag> = _result


	@Composable
	fun render() {
		render(state.value) {
			_result.unwrapped = it
		}
	}

	@Composable
	fun render(state: LoginState, onFinish: CallWith<LoginTag>) {
		if (state.tag is Failed || state.tag is Done) {
			onFinish(state.tag)
		}

		Column(
			arrangement = Arrangement.Center,
			modifier = LayoutPadding(12.dp)
		) {
			val errors = (state.tag as? Active)?.errors
			val focus = ambient(FocusManagerAmbient)
			val isEnabled = when (val tag = state.tag) {
				Busy -> false
				is Active -> tag.errors.isValid
				else -> true
			}
			EditText(
				error = errors?.username,
				fieldType = EditText.Email,
				hint = "Username",
				imeAction = ImeAction.Next,
				onImeAction = { focus.requestFocusById("password") },
				onValueChange = { exec(SetUsername(it)) },
				value = state.username
			)
			EditText(
				error = errors?.password,
				fieldType = EditText.Password,
				focusIdentifier = "password",
				hint = "Password",
				imeAction = ImeAction.Done,
				onImeAction = { exec(Submit) },
				onValueChange = { exec(SetPassword(it)) },
				value = state.password
			)
			Opacity(if (isEnabled) 1f else 0.4f) {
				Button(
					text = "LOGIN",
					modifier = LayoutGravity.Center,
					onClick = { if (isEnabled) exec(Submit) }
				)
			}
		}
	}

	private object Sample : State<LoginState> {
		override val value = LoginState(
			username = "foo@example.com",
			password = "abc",
			tag = Active(ValidationResult(null, "too simple"))
		)
	}
}
