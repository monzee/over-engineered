package ph.codeia.overengineered

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.Composable
import androidx.compose.Model
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
	zombies: SavedStateViewModelFactory
) : Fragment(R.layout.empty) {
	private val viewModel: LoginViewModel by viewModels { zombies }

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		val login = loginComponent.bind(viewModel)
		val lifetime = viewLifecycleOwner
		(view as ViewGroup).setContent {
			MaterialTheme {
				login.output.view().observe(lifetime, login.input)
			}
		}
		viewModel.toasts.consume(lifetime) {
			Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
		}
	}
}


@[Preview Composable]
fun DefaultPreview() {
	MaterialTheme {
		LoginScreen(
			LoginControl(LoginViewModel.Form()),
			FizzBuzzControl(LoginViewModel.Counter())
		).view()
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
	private val login: LoginControl,
	private val fizzBuzz: FizzBuzzControl
) : Control<Action> {
	@Composable
	override fun view() = liveComposable<Action> {
		Column(modifier = LayoutPadding(12.dp)) {
			Spacer(LayoutFlexible(1f))
			+login.view()
			Spacer(LayoutHeight(24.dp))
			+fizzBuzz.view().map { positive ->
				if (positive) Action.Plus
				else Action.Minus
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
	override fun view() = liveComposable<Action> {
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
						focusIdentifier = "username"
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
	override fun view() = liveComposable<Boolean> {
		Column {
			Text(text = model.text, style = MaterialTheme.typography().h4)
			Spacer(LayoutHeight(12.dp))
			Row {
				Button(text = "-", style = OutlinedButtonStyle(), onClick = { +false })
				Spacer(LayoutWidth(12.dp))
				Button(text = "+", style = OutlinedButtonStyle(), onClick = { +true })
			}
		}
	}
}


@Subcomponent(modules = [LoginViewModel::class])
interface LoginComponent {
	val output: LoginScreen
	val input: Observer<Action>

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
	class Form(
		override var username: String = "",
		override var password: String = ""
	) : LoginControl.Model

	@Model
	class Counter(var count: Int = 1) : FizzBuzzControl.Model {
		override val text: String
			get() = when {
				count % 15 == 0 -> "FizzBuzz"
				count % 3 == 0 -> "Fizz"
				count % 5 == 0 -> "Buzz"
				else -> count.toString()
			}
	}

	private val login = Form()
	private val counter = Counter()
	private val message = MutableLiveData<SingleUse<String>>()

	val toasts: LiveData<SingleUse<String>> = message

	@get:Provides
	val loginModel: LoginControl.Model = login

	@get:Provides
	val fizzBuzzModel: FizzBuzzControl.Model = counter

	@get:Provides
	val controller: Observer<Action> = Observer {
		when (it) {
			Action.Plus -> counter.count += 1
			Action.Minus -> counter.count -= 1
			is Action.SetUsername -> login.username = it.value
			is Action.SetPassword -> login.password = it.value
			Action.Submit -> message.unwrapped = "Received submission."
		}
	}
}
