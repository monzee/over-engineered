package ph.codeia.overengineered

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.Ambient
import androidx.compose.Composable
import androidx.compose.ambient
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.ui.core.Text
import androidx.ui.core.setContent
import androidx.ui.graphics.Color
import androidx.ui.layout.*
import androidx.ui.material.Button
import androidx.ui.material.MaterialTheme
import androidx.ui.material.Scaffold
import androidx.ui.text.TextStyle
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ph.codeia.overengineered.controls.InContext
import kotlin.random.Random

class ReduxLikeActivity : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val model: LabViewModel by viewModels()
		setContent {
			LabViewModel.Store.Provider(model.store) {
				MaterialTheme {
					Chrome {
						Body()
					}
				}
			}
		}
	}
}

@[Composable Preview]
fun preview() {
	val store = LabViewModel().store
	store.dispatch(Load.Finish("lorem ipsum dolor sit amet"))
	LabViewModel.Store.Provider(store) {
		MaterialTheme {
			Chrome {
				Body()
			}
		}
	}
}

@Composable
inline fun Chrome(
	crossinline children: @Composable InContext<ColumnScope>
) {
	Scaffold {
		Column(
			arrangement = Arrangement.Center,
			modifier = LayoutHeight.Fill + LayoutAlign.Center
		) {
			children()
		}
	}
}

@Composable
fun ColumnScope.Body() {
	val (state, next) = ambient(LabViewModel.Store)
	Button(
		text = "Load data",
		modifier = LayoutGravity.Center,
		onClick = { next(Load.Start(next)) }
	)
	if (state !is Loadable.Idle) {
		Spacer(LayoutHeight(12.dp))
		when (state) {
			Loadable.Busy -> Text(
				modifier = LayoutGravity.Center,
				text = "please wait..."
			)
			is Loadable.Done -> Text(
				modifier = LayoutGravity.Center,
				text = state.data
			)
			is Loadable.Failed -> Text(
				modifier = LayoutGravity.Center,
				style = TextStyle(color = Color.Red),
				text = state.cause.message ?: "something went wrong"
			)
		}
	}
}

class LabViewModel : ViewModel() {
	val store = Store(Loadable.Idle, ::fold)

	private fun fold(state: Loadable<String>, event: Load<String>) = when (event) {
		is Load.Start -> {
			viewModelScope.launch {
				delay(2000)
				val roll = Random.nextInt(10)
				event.next(
					if (roll != 0) Load.Finish("thank you for waiting.")
					else Load.Fail(RuntimeException("a random error has occurred."))
				)
			}
			Loadable.Busy
		}
		is Load.Fail -> Loadable.Failed(event.cause)
		is Load.Finish -> Loadable.Done(event.payload)
	}

	companion object {
		val Store = Ambient.of<Store<String>>()
	}
}

typealias Store<T> = StateMachine<Loadable<T>, Load<T>>

sealed class Loadable<out T> {
	object Idle : Loadable<Nothing>()
	object Busy : Loadable<Nothing>()
	class Failed(val cause: Throwable) : Loadable<Nothing>()
	class Done<T>(val data: T) : Loadable<T>()
}

sealed class Load<out T> {
	class Start<T>(val next: (Load<T>) -> Unit) : Load<T>()
	class Fail(val cause: Throwable) : Load<Nothing>()
	class Finish<T>(val payload: T) : Load<T>()
}