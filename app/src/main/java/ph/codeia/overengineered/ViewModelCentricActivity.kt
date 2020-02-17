package ph.codeia.overengineered

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.*
import androidx.core.util.PatternsCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.ui.core.CurrentTextStyleProvider
import androidx.ui.core.FocusManagerAmbient
import androidx.ui.core.setContent
import androidx.ui.input.ImeAction
import androidx.ui.layout.Arrangement
import androidx.ui.layout.Column
import androidx.ui.layout.LayoutGravity
import androidx.ui.layout.LayoutPadding
import androidx.ui.material.MaterialTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ph.codeia.overengineered.controls.*
import kotlin.random.Random

/*
 * This file is a part of the Over Engineered project.
 */


class ViewModelCentricActivity : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val screen: DirectControlStateAccess by viewModels()
		setContent {
			screen.render(this)
			screen.toasts?.let {
				Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
			}
		}
	}
}


class DirectControlStateAccess : ViewModel() {
	private val username = EditControl()
	private val password = EditControl()
	private val submit = ButtonControl()
	private val toast = mutableStateOf<String?>("hello!")
	private var isActive by mutableStateOf(false)
	private var isBusy by mutableStateOf(false)
	private var validationResult by mutableStateOf(Valid, StructurallyEqual)

	private companion object {
		val EmailPattern = PatternsCompat.EMAIL_ADDRESS.toRegex()
		val Valid = ValidationResult(null, null)
	}

	private data class ValidationResult(
		val username: String?,
		val password: String?
	) {
		val isValid = username == null && password == null
	}

	val toasts: String?
		get() = toast.consume()

	@Composable
	fun render(lifetime: LifecycleOwner) {
		val absolute = ambient(Metrics.Handle)
		val focus = ambient(FocusManagerAmbient)

		onActive {
			username.events.consume(lifetime) {
				when (it) {
					is EditControl.Event.Change -> if (isActive) validate()
					is EditControl.Event.InputMethod ->
						focus.requestFocusById("password")
				}
			}
			password.events.consume(lifetime) {
				when (it) {
					is EditControl.Event.Change -> if (isActive) validate()
					is EditControl.Event.InputMethod -> submit()
				}
			}
			submit.events.consume(lifetime) {
				submit()
			}
		}

		username.error = validationResult.username
		password.error = validationResult.password
		submit.isEnabled = !isBusy && validationResult.isValid

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

	private fun validate() {
		validationResult = ValidationResult(
			username = when {
				username.value.isEmpty() -> "required"
				!EmailPattern.matches(username.value) -> "bad email format"
				else -> null
			},
			password = when {
				password.value.isEmpty() -> "required"
				password.value.length < 5 -> "too simple"
				else -> null
			}
		)
		isActive = true
	}

	private fun submit() {
		if (!isActive) {
			validate()
		}
		if (validationResult.isValid) {
			isBusy = true
			viewModelScope.launch {
				delay(2000)
				toast.value = try {
					when (Random.nextInt(4)) {
						0 -> error("bad username or password")
						1 -> error("unavailable")
						2 -> if (Random.nextInt(25) == 0) error("unlucky")
					}
					"congrats!"
				}
				catch (ex: Exception) {
					"FAIL! ${ex.message}"
				}
				finally {
					isBusy = false
					validate()
				}
			}
		}
	}
}