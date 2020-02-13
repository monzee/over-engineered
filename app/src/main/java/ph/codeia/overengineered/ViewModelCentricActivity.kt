@file:Suppress("MemberVisibilityCanBePrivate")

package ph.codeia.overengineered

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.*
import androidx.core.util.PatternsCompat
import androidx.lifecycle.*
import androidx.ui.core.CurrentTextStyleProvider
import androidx.ui.core.FocusManagerAmbient
import androidx.ui.core.setContent
import androidx.ui.input.ImeAction
import androidx.ui.layout.Arrangement
import androidx.ui.layout.Column
import androidx.ui.layout.LayoutGravity
import androidx.ui.layout.LayoutPadding
import androidx.ui.material.MaterialTheme
import androidx.ui.tooling.preview.Preview
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ph.codeia.overengineered.controls.ButtonControl
import ph.codeia.overengineered.controls.EditControl
import ph.codeia.overengineered.controls.EditText
import ph.codeia.overengineered.controls.Metrics
import kotlin.random.Random

/*
 * This file is a part of the Over Engineered project.
 */


class ViewModelCentricActivity : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val screen: LoginScreen by viewModels()
		screen.messages.consume(this) {
			Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
		}
		setContent {
			screen.render(this)
		}
	}
}

@[Composable Preview]
fun loginScreenPreview() {
	val owner = object : LifecycleOwner {
		override fun getLifecycle(): Lifecycle = run {
			LifecycleRegistry(this)
		}
	}
	val screen = LoginScreen()
	screen.setUsername("lorem ipsum dolor sit amet")
	screen.submit()
	screen.render(owner)
}


class LoginScreen : ViewModel() {
	val username = EditControl()
	val password = EditControl()
	val submit = ButtonControl()
	private val _messages = MutableLiveData<SingleUse<String>>()
	val messages: Consumable<String> = _messages

	private sealed class Tag {
		object Idle : Tag()
		object Busy : Tag()
		class Active(
			val usernameError: String?,
			val passwordError: String?
		) : Tag() {
			val isValid = usernameError == null && passwordError == null
		}
		class Failed(val cause: Throwable) : Tag()
		class LoggedIn(val token: String) : Tag()
	}

	private class State(
		var username: String,
		var password: String,
		tag: Tag
	) {
		var tag: Tag by mutableStateOf(tag)
	}

	private val emailPattern = PatternsCompat.EMAIL_ADDRESS.toRegex()
	private val state = State("", "", Tag.Idle)

	fun setUsername(value: String) {
		state.username = value
		validateIfActive()
	}

	fun setPassword(value: String) {
		state.password = value
		validateIfActive()
	}

	private fun validateIfActive() {
		if (state.tag is Tag.Active) {
			validate()
		}
	}

	fun validate() {
		val username = state.username
		val password = state.password
		val usernameError = when {
			username.isBlank() -> "required"
			!emailPattern.matches(username) -> "bad email format"
			else -> null
		}
		val passwordError = when {
			password.isBlank() -> "required"
			password.length < 5 -> "too simple"
			else -> null
		}
		state.tag = Tag.Active(usernameError, passwordError)
	}

	fun submit() {
		when (val tag = state.tag) {
			Tag.Idle -> {
				validate()
				submit()
			}
			is Tag.Active -> {
				if (tag.isValid) {
					state.tag = Tag.Busy
					viewModelScope.launch {
						delay(2000)
						state.tag = try {
							when (Random.nextInt(4)) {
								0 -> error("bad username or password")
								1 -> error("unavailable")
								2 -> when (Random.nextInt(25)) {
									0 -> error("unlucky")
									else -> Tag.LoggedIn("congrats")
								}
								else -> Tag.LoggedIn("congrats")
							}
						}
						catch (ex: Throwable) {
							Tag.Failed(ex)
						}
					}
				}
			}
		}
	}

	@Composable
	fun render(lifetime: LifecycleOwner) {
		val absolute = ambient(Metrics.Handle)
		val focus = ambient(FocusManagerAmbient)

		onActive {
			username.events.consume(lifetime) {
				when (it) {
					is EditControl.Event.Change -> setUsername(it.value)
					is EditControl.Event.InputMethod ->
						focus.requestFocusById("password")
				}
			}
			password.events.consume(lifetime) {
				when (it) {
					is EditControl.Event.Change -> setPassword(it.value)
					is EditControl.Event.InputMethod -> submit()
				}
			}
			submit.events.consume(lifetime) {
				submit()
			}
		}

		remember(state.tag) {
			username.value = state.username
			password.value = state.password
			when (val tag = state.tag) {
				Tag.Idle -> {
					_messages += "hello!"
				}
				Tag.Busy -> submit.isEnabled = false
				is Tag.Active -> {
					username.error = tag.usernameError
					password.error = tag.passwordError
					submit.isEnabled = tag.isValid
				}
				is Tag.Failed -> {
					_messages += "FAIL! ${tag.cause.message}"
					validate()
				}
				is Tag.LoggedIn -> {
					_messages += tag.token
					validate()
				}
			}
		}

		MaterialTheme {
			Column(
				arrangement = Arrangement.Center,
				modifier = LayoutPadding(absolute.unit)
			) {
				CurrentTextStyleProvider(MaterialTheme.typography().body1) {
					username.render(
						fieldType = EditText.Email,
						hint = "Username",
						imeAction = ImeAction.Next
					)

					password.render(
						fieldType = EditText.Password,
						focusIdentifier = "password",
						hint = "Password",
						imeAction = ImeAction.Done
					)

					submit.render(
						modifier = LayoutGravity.Center,
						text = "LOGIN"
					)
				}
			}
		}
	}
}