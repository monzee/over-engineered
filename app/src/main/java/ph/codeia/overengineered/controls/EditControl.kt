package ph.codeia.overengineered.controls

import androidx.compose.Composable
import androidx.compose.Model
import androidx.lifecycle.LiveData
import androidx.ui.core.Modifier
import androidx.ui.input.ImeAction
import ph.codeia.overengineered.liveComposable

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

	@Composable
	fun render(
		fieldType: EditText.Type = EditText.Plain,
		focusIdentifier: String? = null,
		hint: String? = null,
		imeAction: ImeAction = ImeAction.Unspecified,
		modifier: Modifier = Modifier.None
	): LiveData<Event> = liveComposable {
		EditText(
			error = error,
			fieldType = fieldType,
			focusIdentifier = focusIdentifier,
			hint = hint,
			imeAction = imeAction,
			isEnabled = isEnabled,
			modifier = modifier,
			onBlur = { +Event.Blur },
			onFocus = { +Event.Focus },
			onImeAction = { +Event.InputMethod(it) },
			onValueChange = {
				value = it
				+Event.Change(it)
			},
			value = value
		)
	}
}