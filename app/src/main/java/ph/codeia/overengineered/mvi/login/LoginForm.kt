package ph.codeia.overengineered.mvi.login

import androidx.compose.Composable
import androidx.compose.State
import androidx.compose.ambient
import androidx.lifecycle.MutableLiveData
import androidx.ui.core.FocusManagerAmbient
import androidx.ui.core.Modifier
import androidx.ui.core.Opacity
import androidx.ui.input.ImeAction
import androidx.ui.material.Button
import ph.codeia.overengineered.Consumable
import ph.codeia.overengineered.SingleUse
import ph.codeia.overengineered.controls.CallWith
import ph.codeia.overengineered.controls.EditText
import ph.codeia.overengineered.controls.Ignore
import ph.codeia.overengineered.unwrapped
import javax.inject.Inject

class LoginForm @Inject constructor(
	private val state: State<LoginModel>,
	private val exec: CallWith<@JvmSuppressWildcards LoginAction>
) {
	constructor() : this(Sample, Ignore)

	private val _result = MutableLiveData<SingleUse<LoginTag>>()
	val result: Consumable<LoginTag> = _result


	@Composable
	fun render(
		model: LoginModel = state.value,
		modifier: Modifier = Modifier.None
	) {
		render(model, modifier) {
			_result.unwrapped = it
		}
	}

	@Composable
	fun render(
		model: LoginModel,
		modifier: Modifier = Modifier.None,
		onFinish: CallWith<LoginTag>
	) {
		if (model.tag is Failed || model.tag is Done) {
			onFinish(model.tag)
		}

		val errors = model.tag as? Validated
		val focus = ambient(FocusManagerAmbient)
		val isEnabled = when (val tag = model.tag) {
			Busy -> false
			is Validated -> tag.isValid
			else -> true
		}
		EditText(
			error = errors?.username,
			fieldType = EditText.Email,
			hint = "Username",
			imeAction = ImeAction.Next,
			modifier = modifier,
			onImeAction = { focus.requestFocusById("password") },
			onValueChange = { exec(SetUsername(it)) },
			value = model.username
		)
		EditText(
			error = errors?.password,
			fieldType = EditText.Password,
			focusIdentifier = "password",
			hint = "Password",
			imeAction = ImeAction.Done,
			modifier = modifier,
			onImeAction = { exec(Submit) },
			onValueChange = { exec(SetPassword(it)) },
			value = model.password
		)
		Opacity(if (isEnabled) 1f else 0.4f) {
			Button(
				text = "LOGIN",
				modifier = modifier,
				onClick = { if (isEnabled) exec(Submit) }
			)
		}
	}

	private object Sample : State<LoginModel> {
		override val value = LoginModel(
			username = "foo@example.com",
			password = "abc",
			tag = Validated(null, "too simple")
		)
	}
}
