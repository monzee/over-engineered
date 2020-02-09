package ph.codeia.overengineered

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.Ambient
import androidx.compose.Composable
import androidx.compose.Model
import androidx.lifecycle.ViewModel
import androidx.ui.core.Text
import androidx.ui.core.setContent
import androidx.ui.layout.*
import androidx.ui.material.Button
import androidx.ui.material.MaterialTheme
import androidx.ui.unit.dp
import ph.codeia.overengineered.controls.InContext

class CurriedComponentsActivity : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val viewModel: TheViewModel by viewModels()
		setContent {
			val Body = view(viewModel.text, viewModel.controller)
			MaterialTheme {
				Column(arrangement = Arrangement.Center) {
					Body()
				}
			}
		}
	}
}

fun view(
	text: String,
	actions: Controller
): @Composable InContext<ColumnScope> = {
	Button(
		text = "do it.",
		modifier = LayoutGravity.Center,
		onClick = { actions.doSomething() }
	)
	Spacer(LayoutHeight(12.dp))
	Text(
		text = text,
		modifier = LayoutGravity.Center
	)
}

interface Controller {
	fun doSomething()
}

class TheViewModel : ViewModel() {
	@Model
	private class State {
		var text = ""
	}

	private val state = State()
	private var timesClicked = 0

	val text: String
		get() = state.text

	val controller = object : Controller {
		override fun doSomething() {
			timesClicked += 1
			state.text = when {
				timesClicked == 1 -> "i did it!"
				timesClicked in 2..9 -> "i did it again!"
				timesClicked in 10..20 -> "i have nothing better to do!"
				else -> "you can stop now."
			}
		}
	}
}