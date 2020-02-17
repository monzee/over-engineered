package ph.codeia.overengineered.controls

import androidx.compose.Composable
import androidx.compose.Model
import androidx.lifecycle.MutableLiveData
import androidx.ui.core.Modifier
import androidx.ui.input.ImeAction
import ph.codeia.overengineered.Consumable
import ph.codeia.overengineered.SingleUse
import ph.codeia.overengineered.plusAssign

/*
 * This file is a part of the Over Engineered project.
 */


@Model
class EditControl(
	var error: String? = null,
	var isEnabled: Boolean = true,
	var value: String = ""
) {
	sealed class Event {
		object Blur : Event()
		object Focus : Event()
		class InputMethod(val action: ImeAction) : Event()
		class Change(val value: String) : Event()
	}

	private val _events = MutableLiveData<SingleUse<Event>>()
	val events: Consumable<Event> = _events

	@Composable
	fun render(
		modifier: Modifier = Modifier.None,
		fieldType: EditText.Type = EditText.Plain,
		focusIdentifier: String? = null,
		hint: String? = null,
		imeAction: ImeAction = ImeAction.Unspecified
	) {
		EditText(
			error = error,
			fieldType = fieldType,
			focusIdentifier = focusIdentifier,
			hint = hint,
			imeAction = imeAction,
			isEnabled = isEnabled,
			modifier = modifier,
			onBlur = { _events += Event.Blur },
			onFocus = { _events += Event.Focus },
			onImeAction = { _events += Event.InputMethod(it) },
			onValueChange = {
				value = it
				_events += Event.Change(it)
			},
			value = value
		)
	}
}