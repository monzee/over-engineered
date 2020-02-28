package ph.codeia.overengineered.controls

import androidx.compose.Composable
import androidx.compose.Model
import androidx.lifecycle.MutableLiveData
import androidx.ui.core.Modifier
import androidx.ui.core.Opacity
import androidx.ui.core.Text
import androidx.ui.material.*
import ph.codeia.overengineered.Consumable
import ph.codeia.overengineered.SingleUse
import ph.codeia.overengineered.plusAssign


/*
 * This file is a part of the Over Engineered project.
 */

@Model
class ButtonControl(var isEnabled: Boolean = true) {
	object Event

	enum class Style {
		Contained, Outlined, Text
	}

	private val _events = MutableLiveData<SingleUse<Event>>()
	val events: Consumable<Event> = _events

	private val onClick: Procedure = {
		_events += Event
	}

	@Composable
	fun render(
		text: String,
		modifier: Modifier = Modifier.None,
		style: Style = Style.Contained
	) {
		render(modifier = modifier, style = style) {
			Text(text = text)
		}
	}

	@Composable
	fun render(
		modifier: Modifier = Modifier.None,
		style: Style = Style.Contained,
		children: Children
	) {
		Opacity(if (isEnabled) 1f else 0.5f) {
			when (style) {
				Style.Contained -> Button(
					children = children,
					modifier = modifier,
					onClick = if (isEnabled) onClick else null
				)
				Style.Outlined -> OutlinedButton(
					children = children,
					modifier = modifier,
					onClick = if (isEnabled) onClick else null
				)
				Style.Text -> TextButton(
					children = children,
					modifier = modifier,
					onClick = if (isEnabled) onClick else null
				)
			}

		}
	}
}