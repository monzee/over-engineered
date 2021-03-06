package ph.codeia.overengineered

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.Composable
import androidx.compose.mutableStateOf
import androidx.compose.remember
import androidx.lifecycle.ViewModel
import androidx.ui.core.Modifier
import androidx.ui.core.Text
import androidx.ui.core.setContent
import androidx.ui.layout.*
import androidx.ui.material.Button
import androidx.ui.material.MaterialTheme
import androidx.ui.unit.dp
import ph.codeia.overengineered.controls.CallWith
import ph.codeia.overengineered.controls.EditControl
import ph.codeia.overengineered.controls.Procedure
import ph.codeia.overengineered.controls.RunIn

class CurriedComponentsActivity : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val viewModel: TheViewModel by viewModels()
		setContent {
			MaterialTheme {
				Column(
					arrangement = Arrangement.Center,
					modifier = LayoutPadding(12.dp),
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
	controls: @Composable CallWith<Modifier>
): @Composable RunIn<ColumnScope> = {
	controls(LayoutGravity.Center)
	if (text.isNotEmpty()) {
		Text(
			text = text,
			modifier = LayoutGravity.Center + LayoutPadding(top = 12.dp)
		)
	}
}

fun Input(count: Int, doIt: Procedure): @Composable CallWith<Modifier> = { modifier ->
	val edit = remember { EditControl() }
	edit.error = count.takeIf { it > 0 }?.let {
		"clicked $it time${if (it == 1) "" else "s"}"
	}
	edit.render(
		hint = "Something goes here.",
		modifier = modifier
	)
	Button(modifier = modifier, onClick = doIt) {
		Text(text = "do it.")
	}
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