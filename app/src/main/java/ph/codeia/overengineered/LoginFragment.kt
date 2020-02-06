@file:Suppress("PropertyName", "LocalVariableName")

package ph.codeia.overengineered

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.Composable
import androidx.compose.Model
import androidx.compose.ambient
import androidx.compose.state
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import androidx.ui.core.*
import androidx.ui.foundation.shape.corner.RoundedCornerShape
import androidx.ui.graphics.Color
import androidx.ui.graphics.SolidColor
import androidx.ui.input.ImeAction
import androidx.ui.layout.*
import androidx.ui.material.Button
import androidx.ui.material.MaterialTheme
import androidx.ui.material.OutlinedButtonStyle
import androidx.ui.material.surface.Surface
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.dp
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import javax.inject.Inject

/*
 * This file is a part of the Over Engineered project.
 */


class LoginFragment @Inject constructor(
	private val loginComponent: LoginComponent.Factory,
	andZombies: SavedStateViewModelFactory
) : Fragment(R.layout.empty) {
	private val viewModel: LoginViewModel by viewModels { andZombies }

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		val login = loginComponent.bind(viewModel)
		val lifetime = viewLifecycleOwner
		(view as ViewGroup).setContent {
			MaterialTheme {
				login.Output().observe(lifetime, login.input)
			}
		}
		viewModel.toasts.consume(lifetime) {
			Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
		}
	}
}


@[Preview Composable]
fun DefaultPreview() {
	val Screen = LoginScreen(LoginControl(), CounterControl())
	MaterialTheme {
		Screen()
	}
}


sealed class Action {
	class SetUsername(val value: String) : Action()
	class SetPassword(val value: String) : Action()
	object Submit : Action()
	object Plus : Action()
	object Minus : Action()
}

private val full = 12.dp
private val half = 6.dp
private val double = 24.dp


class LoginScreen @Inject constructor(
	private val Login: LoginControl,
	private val Counter: CounterControl
) : Control<Action> {
	@Composable
	override fun invoke(): LiveData<Action> = liveComposable {
		Column(modifier = LayoutPadding(full)) {
			Spacer(LayoutFlexible(1f))
			+Login()
			Spacer(LayoutHeight(double))
			Row {
				+Counter().map { positive ->
					if (positive) Action.Plus
					else Action.Minus
				}
				Spacer(LayoutFlexible(1f))
				+Counter().map { positive ->
					if (positive) Action.Plus
					else Action.Minus
				}
			}
			Spacer(LayoutFlexible(1f))
		}
	}
}


class LoginControl @Inject constructor(
	private val model: Model
) : Control<Action> {
	constructor() : this(Model)

	interface Model {
		val username: String
		val password: String

		companion object : Model {
			override val username: String = "foo@example.com"
			override val password: String = "hunter2"
		}
	}

	@Composable
	override fun invoke(): LiveData<Action> = liveComposable {
		val lens = ambient(FocusManagerAmbient)
		CurrentTextStyleProvider(value = MaterialTheme.typography().h6) {
			Column {
				BorderedSurface {
					TextField(
						value = model.username,
						onValueChange = { +Action.SetUsername(it) },
						modifier = LayoutPadding(half),
						imeAction = ImeAction.Next,
						onImeActionPerformed = { lens.requestFocusById("password") }
					)
				}
				Spacer(LayoutHeight(full))
				BorderedSurface {
					Surface(modifier = LayoutPadding(half)) {
						PasswordTextField(
							value = model.password,
							onValueChange = { +Action.SetPassword(it) },
							imeAction = ImeAction.Done,
							onImeActionPerformed = { +Action.Submit },
							focusIdentifier = "password"
						)
					}
				}
				Spacer(LayoutHeight(full))
				Button(text = "LOGIN", onClick = { +Action.Submit })
			}
		}
	}

	@Composable
	private inline fun BorderedSurface(
		crossinline children: @Composable() () -> Unit
	) {
		Surface(
			borderWidth = 1.dp,
			borderBrush = SolidColor(Color.Gray),
			shape = RoundedCornerShape(3.dp)
		) {
			children()
		}
	}
}


class CounterControl @Inject constructor(
	private val model: Model
) : Control<Boolean> {
	constructor() : this(Model)

	interface Model {
		val text: String

		companion object : Model {
			override val text: String = "Fizz"
		}
	}

	@Composable
	override fun invoke(): LiveData<Boolean> = liveComposable {
		Column {
			Text(
				text = model.text,
				style = MaterialTheme.typography().h4,
				modifier = LayoutGravity.Center
			)
			Spacer(LayoutHeight(full))
			Row {
				Button(text = "-", style = OutlinedButtonStyle(), onClick = { +false })
				Spacer(LayoutWidth(full))
				Button(text = "+", style = OutlinedButtonStyle(), onClick = { +true })
			}
		}
	}
}

@Composable
fun AnotherCounter() {
	var counter: Int by state { 1 }
	Column {
		Text(
			text = counter.toString(),
			style = MaterialTheme.typography().h4,
			modifier = LayoutGravity.Center
		)
		Spacer(LayoutHeight(full))
		Row {
			Button(text = "-", onClick = { counter -= 1 })
			Spacer(LayoutWidth(full))
			Button(text = "+", onClick = { counter += 1 })
		}
	}
}


@Subcomponent(modules = [LoginViewModel::class])
interface LoginComponent {
	val input: Observer<Action>
	val Output: LoginScreen

	@Subcomponent.Factory
	interface Factory {
		fun bind(viewModel: LoginViewModel): LoginComponent
	}
}


@Module
class LoginViewModel(private val savedState: SavedStateHandle) : ViewModel() {
	@Model
	private class State(
		var count: Int = 1,
		override var username: String = "",
		override var password: String = ""
	) : LoginControl.Model, CounterControl.Model {
		override val text: String
			get() = when {
				count % 15 == 0 -> "FizzBuzz"
				count % 3 == 0 -> "Fizz"
				count % 5 == 0 -> "Buzz"
				else -> count.toString()
			}
	}

	private val message = MutableLiveData<SingleUse<String>>()
	private val state = State()

	val toasts: LiveData<SingleUse<String>> = message

	@get:Provides
	val loginModel: LoginControl.Model = state

	@get:Provides
	val fizzBuzzModel: CounterControl.Model = state

	@get:Provides
	val controller: Observer<Action> = Observer {
		when (it) {
			Action.Plus -> state.count += 1
			Action.Minus -> state.count -= 1
			is Action.SetUsername -> state.username = it.value
			is Action.SetPassword -> state.password = it.value
			Action.Submit -> message.unwrapped = "Received submission."
		}
	}
}
