@file:Suppress("LocalVariableName")

package ph.codeia.overengineered

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.Composable
import androidx.compose.Model
import androidx.compose.ambient
import androidx.compose.stateFor
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


class LabFragment @Inject constructor(
	private val loginView: LoginView.Binder,
	andZombies: SavedStateViewModelFactory
) : Fragment(R.layout.empty) {
	private val viewModel: LoginViewModel by viewModels { andZombies }

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		val login = loginView.bind(viewModel)
		val lifetime = viewLifecycleOwner
		(view as ViewGroup).setContent {
			MaterialTheme {
				login.output().observe(lifetime, login.input)
			}
		}
		viewModel.toasts.consume(lifetime) {
			Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
		}
	}
}


@[Preview Composable]
fun DefaultPreview() {
	val Body = LoginControl(FormControl(), CounterControl())
	MaterialTheme {
		Body()
	}
}


sealed class Action {
	class SetUsername(val value: String) : Action()
	class SetPassword(val value: String) : Action()
	object Submit : Action()
	object Plus : Action()
	object Minus : Action()
}

private val base = 12.dp
private val half = base / 2
private val double = base * 2


class LoginControl @Inject constructor(
	private val Form: FormControl,
	private val Counter: CounterControl
) {
	@Composable
	operator fun invoke(): LiveData<Action> = liveComposable {
		Column(modifier = LayoutPadding(base)) {
			Spacer(LayoutFlexible(1f))
			+Form()
			Spacer(LayoutHeight(double))
			Row {
				+Counter().map { if (it) Action.Plus else Action.Minus }
				Spacer(LayoutFlexible(1f))
				AnotherCounter()
			}
			Spacer(LayoutFlexible(1f))
		}
	}
}


class FormControl @Inject constructor(private val model: Model) {
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
	operator fun invoke(): LiveData<Action> = liveComposable {
		val lens = ambient(FocusManagerAmbient)
		CurrentTextStyleProvider(MaterialTheme.typography().h6) {
			Column {
				FieldDecoration {
					TextField(
						imeAction = ImeAction.Next,
						onImeActionPerformed = { lens.requestFocusById("password") },
						onValueChange = { +Action.SetUsername(it) },
						value = model.username
					)
				}
				Spacer(LayoutHeight(base))
				FieldDecoration {
					PasswordTextField(
						focusIdentifier = "password",
						imeAction = ImeAction.Done,
						onImeActionPerformed = { +Action.Submit },
						onValueChange = { +Action.SetPassword(it) },
						value = model.password
					)
				}
				Spacer(LayoutHeight(base))
				Button(text = "LOGIN", onClick = { +Action.Submit })
			}
		}
	}

	@Composable
	internal inline fun FieldDecoration(
		error: String? = null,
		crossinline children: @Composable() () -> Unit
	) {
		val borderColor by stateFor(error) {
			if (error == null) Color.Gray
			else Color.Red
		}
		Surface(
			borderBrush = SolidColor(borderColor),
			borderWidth = 1.dp,
			shape = RoundedCornerShape(3.dp)
		) {
			Container(modifier = LayoutPadding(half)) {
				children()
			}
		}
	}
}


class CounterControl @Inject constructor(private val model: Model) {
	constructor() : this(Model)

	interface Model {
		val text: String

		companion object : Model {
			override val text: String = "Fizz"
		}
	}

	@Composable
	operator fun invoke(): LiveData<Boolean> = liveComposable {
		Column {
			Text(
				modifier = LayoutGravity.Center,
				style = MaterialTheme.typography().h4,
				text = model.text
			)
			Spacer(LayoutHeight(base))
			Row {
				val outlinedButton = OutlinedButtonStyle()
				Button(text = "-", style = outlinedButton, onClick = { +false })
				Spacer(LayoutWidth(base))
				Button(text = "+", style = outlinedButton, onClick = { +true })
			}
		}
	}
}

enum class Event {
	Inc, Dec;

	fun reduce(n: Int): Int? = when (this) {
		Inc -> n + 1
		Dec -> n - 1
	}
}

@Composable
fun AnotherCounter() {
	val (count, next) = scan(0, Event::reduce)
	Column {
		Text(
			modifier = LayoutGravity.Center,
			style = MaterialTheme.typography().h4,
			text = count.toString()
		)
		Spacer(LayoutHeight(base))
		Row {
			Button(onClick = { next(Event.Dec) }, text = "-")
			Spacer(LayoutWidth(base))
			Button(onClick = { next(Event.Inc) }, text = "+")
		}
	}
}


@Subcomponent(modules = [LoginViewModel::class])
interface LoginView {
	val input: Observer<Action>
	val output: LoginControl

	@Subcomponent.Factory
	interface Binder {
		fun bind(viewModel: LoginViewModel): LoginView
	}
}


@Module
class LoginViewModel(private val savedState: SavedStateHandle) : ViewModel() {
	@Model
	private class State(
		var count: Int = 1,
		override var username: String = "",
		override var password: String = ""
	) : FormControl.Model, CounterControl.Model {
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
	val formModel: FormControl.Model = state

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
