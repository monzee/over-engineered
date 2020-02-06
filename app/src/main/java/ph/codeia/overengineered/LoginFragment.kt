@file:Suppress("PropertyName", "LocalVariableName")

package ph.codeia.overengineered

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.*
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
	val state = LoginViewModel.State()
	val Screen = LoginScreen(
		LoginControl(state),
		FizzBuzzControl(state)
	)
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


class LoginScreen @Inject constructor(
	private val Login: LoginControl,
	private val FizzBuzz: FizzBuzzControl
) : Control<Action> {
	@Composable
	override fun invoke(): LiveData<Action> = liveComposable {
		Column(modifier = LayoutPadding(12.dp)) {
			Spacer(LayoutFlexible(1f))
			+Login()
			Spacer(LayoutHeight(24.dp))
			Row {
				+FizzBuzz().map { positive ->
					if (positive) Action.Plus
					else Action.Minus
				}
				Spacer(LayoutFlexible(1f))
				AnotherCounter()
			}
			Spacer(LayoutFlexible(1f))
		}
	}
}


class LoginControl @Inject constructor(
	private val model: Model
) : Control<Action> {
	interface Model {
		val username: String
		val password: String
	}

	@Composable
	override fun invoke(): LiveData<Action> = liveComposable {
		val lens = ambient(FocusManagerAmbient)
		CurrentTextStyleProvider(value = MaterialTheme.typography().h6) {
			Column {
				Surface(
					borderWidth = 1.dp,
					borderBrush = SolidColor(Color.Gray),
					shape = RoundedCornerShape(3.dp)
				) {
					TextField(
						value = model.username,
						onValueChange = { +Action.SetUsername(it) },
						modifier = LayoutPadding(6.dp),
						imeAction = ImeAction.Next,
						onImeActionPerformed = { lens.requestFocusById("password") }
					)
				}
				Spacer(LayoutHeight(12.dp))
				Surface(
					borderWidth = 1.dp,
					borderBrush = SolidColor(Color.Gray),
					shape = RoundedCornerShape(3.dp)
				) {
					PasswordTextField(
						value = model.password,
						onValueChange = { +Action.SetPassword(it) },
						imeAction = ImeAction.Done,
						onImeActionPerformed = { +Action.Submit },
						focusIdentifier = "password"
					)
				}
				Spacer(LayoutHeight(12.dp))
				Button(text = "LOGIN", onClick = { +Action.Submit })
			}
		}
	}
}


class FizzBuzzControl @Inject constructor(
	private val model: Model
) : Control<Boolean> {
	interface Model {
		val text: String
	}

	@Composable
	override fun invoke(): LiveData<Boolean> = liveComposable {
		Column {
			Text(
				text = model.text,
				style = MaterialTheme.typography().h4,
				modifier = LayoutGravity.Center
			)
			Spacer(LayoutHeight(12.dp))
			Row {
				Button(text = "-", style = OutlinedButtonStyle(), onClick = { +false })
				Spacer(LayoutWidth(12.dp))
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
		Spacer(LayoutHeight(12.dp))
		Row {
			Button(text = "-", onClick = { counter -= 1 })
			Spacer(LayoutWidth(12.dp))
			Button(text = "+", onClick = { counter += 1})
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
class LoginViewModel(
	private val savedState: SavedStateHandle
) : ViewModel() {
	@Model
	class State(
		internal var count: Int = 1,
		username: String = "",
		password: String = ""
	) : LoginControl.Model, FizzBuzzControl.Model {
		override val text: String
			get() = when {
				count % 15 == 0 -> "FizzBuzz"
				count % 3 == 0 -> "Fizz"
				count % 5 == 0 -> "Buzz"
				else -> count.toString()
			}

		override var username: String = username
			internal set

		override var password: String = password
			internal set
	}

	private val message = MutableLiveData<SingleUse<String>>()
	private val state = State()

	val toasts: LiveData<SingleUse<String>> = message

	@get:Provides
	val loginModel: LoginControl.Model = state

	@get:Provides
	val fizzBuzzModel: FizzBuzzControl.Model = state

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
