package ph.codeia.overengineered.controls

import androidx.compose.*
import androidx.ui.core.Modifier
import androidx.ui.core.PasswordTextField
import androidx.ui.core.TextField
import androidx.ui.foundation.shape.corner.RoundedCornerShape
import androidx.ui.graphics.Color
import androidx.ui.graphics.SolidColor
import androidx.ui.input.ImeAction
import androidx.ui.layout.Container
import androidx.ui.layout.LayoutPadding
import androidx.ui.material.surface.Surface
import androidx.ui.unit.dp

/*
 * This file is a part of the Over Engineered project.
 */


sealed class FieldType {
	class Password(val mask: Char) : FieldType()
	object Plain : FieldType()

	companion object {
		val Password = Password('\u2022')
	}
}


@Composable
fun EditControl(
	error: String? = null,
	fieldType: FieldType = FieldType.Plain,
	focusIdentifier: String? = null,
	hint: String? = null,
	imeAction: ImeAction = ImeAction.Unspecified,
	modifier: Modifier = Modifier.None,
	onBlur: Procedure = NoOp,
	onFocus: Procedure = NoOp,
	onImeAction: Sink<ImeAction> = Pass,
	onValueChange: Sink<String> = Pass,
	value: String = ""
) = EditControl(EditControl.State(value, error, hint)).also {
	it.Place(
		fieldType, focusIdentifier, imeAction, modifier,
		onBlur, onFocus, onImeAction, onValueChange
	)
}

class EditControl(val state: State) {
	@Model
	class State(
		var value: String = "",
		var error: String? = null,
		var hint: String? = null
	)

	@Composable
	fun Place(
		fieldType: FieldType = FieldType.Plain,
		focusIdentifier: String? = null,
		imeAction: ImeAction = ImeAction.Unspecified,
		modifier: Modifier = Modifier.None,
		onBlur: Procedure = NoOp,
		onFocus: Procedure = NoOp,
		onImeAction: Sink<ImeAction> = Pass,
		onValueChange: Sink<String> = Pass
	) {
		val borderColor by stateFor(state.error) {
			state.error?.let { Color.Red } ?: Color.Gray
		}
		val size = ambient(Sizes)
		Surface(
			borderWidth = 1.dp,
			borderBrush = SolidColor(borderColor),
			shape = RoundedCornerShape(size.tiny)
		) {
			Container(modifier = LayoutPadding(size.half) + modifier) {
				when (fieldType) {
					is FieldType.Password -> PasswordTextField(
						focusIdentifier = focusIdentifier,
						imeAction = imeAction,
						mask = fieldType.mask,
						onBlur = onBlur,
						onFocus = onFocus,
						onImeActionPerformed = onImeAction,
						onValueChange = {
							state.value = it
							onValueChange(it)
						},
						value = state.value
					)
					FieldType.Plain -> TextField(
						focusIdentifier = focusIdentifier,
						imeAction = imeAction,
						onBlur = onBlur,
						onFocus = onFocus,
						onImeActionPerformed = onImeAction,
						onValueChange = {
							state.value = it
							onValueChange(it)
						},
						value = state.value
					)
				}
			}
		}
	}
}
