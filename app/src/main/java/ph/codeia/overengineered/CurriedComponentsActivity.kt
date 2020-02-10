package ph.codeia.overengineered

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.Composable
import androidx.compose.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.ui.core.Modifier
import androidx.ui.core.Text
import androidx.ui.core.setContent
import androidx.ui.layout.*
import androidx.ui.material.Button
import androidx.ui.material.MaterialTheme
import androidx.ui.unit.dp
import ph.codeia.overengineered.controls.InContext
import ph.codeia.overengineered.controls.Procedure
import ph.codeia.overengineered.controls.Sink

class CurriedComponentsActivity : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val viewModel: TheViewModel by viewModels()
		setContent {
			MaterialTheme {
				Column(
					arrangement = Arrangement.Center,
					children = Body(
						text = viewModel.text,
						controls = Input(
							count = viewModel.timesClicked,
							doIt = viewModel::doSomething
						)
					)
				)
			}
		}
	}
}

fun Body(
	text: String,
	controls: @Composable Sink<Modifier>
): @Composable InContext<ColumnScope> = {
	controls(LayoutGravity.Center)
	if (text.isNotEmpty()) {
		Spacer(LayoutHeight(12.dp))
		Text(text = text, modifier = LayoutGravity.Center)
	}
}

fun Input(count: Int, doIt: Procedure): @Composable Sink<Modifier> = {
	val details = if (count == 0) "" else " ($count)"
	Button(
		text = "do it.$details",
		modifier = it,
		onClick = doIt
	)
}

class TheViewModel : ViewModel() {
	var text: String by mutableStateOf("")
		private set

	var timesClicked: Int by mutableStateOf(0)
		private set

	fun doSomething() {
		timesClicked += 1
		text = when {
			timesClicked == 1 -> "i did it!"
			timesClicked in 2..10 -> "i did it again!"
			timesClicked in 11..20 -> "i have nothing better to do!"
			else -> "you can stop now."
		}
	}
}