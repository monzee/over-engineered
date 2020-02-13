package ph.codeia.overengineered.controls

import androidx.compose.Composable
import androidx.compose.Model
import androidx.lifecycle.MutableLiveData
import androidx.ui.core.Modifier
import androidx.ui.core.Opacity
import androidx.ui.layout.Stack
import androidx.ui.material.Button
import androidx.ui.material.ButtonStyle
import androidx.ui.material.ContainedButtonStyle
import ph.codeia.overengineered.Consumable
import ph.codeia.overengineered.SingleUse
import ph.codeia.overengineered.plusAssign


/*
 * This file is a part of the Over Engineered project.
 */

@Model
class ButtonControl(var isEnabled: Boolean = true) {
	object Event

	private val _events = MutableLiveData<SingleUse<Event>>()
	val events: Consumable<Event> = _events

	@Composable
	fun render(
		text: String,
		modifier: Modifier = Modifier.None,
		style: ButtonStyle = ContainedButtonStyle()
	) {
		Opacity(if (isEnabled) 1f else 0.5f) {
			Button(
				modifier = modifier,
				onClick = { if (isEnabled) _events += Event },
				style = style,
				text = text
			)
		}
	}

	@Composable
	fun render(
		modifier: Modifier = Modifier.None,
		style: ButtonStyle = ContainedButtonStyle(),
		children: Children
	) {
		Opacity(if (isEnabled) 1f else 0.5f) {
			Button(
				children = children,
				modifier = modifier,
				onClick = { if (isEnabled) _events += Event },
				style = style
			)
		}
	}
}