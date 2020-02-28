package ph.codeia.overengineered.aeronautics

import androidx.compose.Composable
import androidx.compose.ambient
import androidx.ui.core.CurrentTextStyleProvider
import androidx.ui.core.FocusManagerAmbient
import androidx.ui.input.ImeAction
import androidx.ui.layout.Column
import androidx.ui.layout.LayoutHeight
import androidx.ui.layout.Spacer
import androidx.ui.material.MaterialTheme
import androidx.ui.tooling.preview.Preview
import ph.codeia.overengineered.controls.EditText
import ph.codeia.overengineered.controls.Metrics
import ph.codeia.overengineered.liveComposable
import javax.inject.Inject

/*
 * This file is a part of the Over Engineered project.
 */


@[Composable Preview]
private fun loginForm() {
	val model = LoginModel()
	model.setUsername("foo@example.com")
	model.setPassword("hunter2")
	LoginSegment(model)()
}


class LoginSegment @Inject constructor(private val model: LoginModel) {
	@Composable
	operator fun invoke() = run {
		invoke(model)
	}

	@Composable
	operator fun invoke(model: LoginModel) = liveComposable<Action> {
		val credentials = model.getCredentials()
		var usernameError: String? = null
		var passwordError: String? = null
		(model.getTag() as? Progress.Active)?.work?.let {
			usernameError = it.usernameError
			passwordError = it.passwordError
		}

		val typography = MaterialTheme.typography()
		val size = Metrics.Handle.current
		val focus = FocusManagerAmbient.current
		CurrentTextStyleProvider(typography.h6) {
			Column {
				EditText(
					error = usernameError,
					imeAction = ImeAction.Next,
					onImeAction = { focus.requestFocusById("password") },
					onValueChange = { +Action.SetUsername(it) },
					value = credentials.username
				)
				Spacer(LayoutHeight(size.unit))
				EditText(
					error = passwordError,
					fieldType = EditText.Password,
					focusIdentifier = "password",
					imeAction = ImeAction.Done,
					onImeAction = { +Action.Submit },
					onValueChange = { +Action.SetPassword(it) },
					value = credentials.password
				)
			}
		}
	}
}