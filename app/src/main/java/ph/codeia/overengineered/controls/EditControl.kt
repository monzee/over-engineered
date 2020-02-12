package ph.codeia.overengineered.controls

import androidx.compose.Composable
import androidx.compose.ambient
import androidx.compose.remember
import androidx.ui.core.*
import androidx.ui.foundation.shape.corner.RoundedCornerShape
import androidx.ui.graphics.Color
import androidx.ui.graphics.SolidColor
import androidx.ui.input.ImeAction
import androidx.ui.input.KeyboardType
import androidx.ui.layout.*
import androidx.ui.material.MaterialTheme
import androidx.ui.material.surface.Surface
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.dp
import androidx.ui.unit.withDensity
import ph.codeia.overengineered.StateMachine

/*
 * This file is a part of the Over Engineered project.
 */


sealed class FieldType {
	class Password(val mask: Char, val isNumeric: Boolean = false) : FieldType()
	class Plain(val keyboard: KeyboardType) : FieldType()

	companion object {
		val Email = Plain(KeyboardType.Email)
		val Number = Plain(KeyboardType.Number)
		val NumberPassword = Password('\u2022', true)
		val Password = Password('\u2022', false)
		val Phone = Plain(KeyboardType.Phone)
		val Plain = Plain(KeyboardType.Text)
	}
}


object EditControl {
	private sealed class Activity(val state: State) {
		class Blurred(s: State) : Activity(s)
		class Disabled(s: State) : Activity(s)
		class Focused(s: State) : Activity(s)
		class HeldDown(s: State) : Activity(s)
		class Selecting(s: State) : Activity(s)
	}

	private sealed class Action {
		object Blur : Action()
		class Change(val newValue: String) : Action()
		object Conceal : Action()
		object Drag : Action()
		object Release : Action()
		object Reveal : Action()
		object Tap : Action()
	}

	private fun transition(
		current: Activity,
		event: Action
	): Activity? = when (event) {
		Action.Blur -> Activity.Blurred(current.state)
		is Action.Change -> {
			current.state.value = event.newValue
			current
		}
		Action.Conceal -> TODO()
		Action.Drag -> TODO()
		Action.Release -> TODO()
		Action.Reveal -> TODO()
		Action.Tap -> when (current) {
			is Activity.Blurred -> Activity.Focused(current.state)
			is Activity.Focused -> Activity.Selecting(current.state)
			is Activity.HeldDown -> Activity.Focused(current.state)
			else -> null
		}
	}

	private fun machine(state: State) = run {
		StateMachine(Activity.Blurred(state), ::transition)
	}

	private class State(
		var value: String = "",
		var error: String? = null,
		var isEnabled: Boolean = true,
		var isSecret: Boolean = false
	) {
		val isEmpty: Boolean get() = value.isEmpty()
		val isValid: Boolean get() = error.isNullOrEmpty()
	}

	@Composable
	operator fun invoke(
		fieldType: FieldType = FieldType.Plain,
		focusIdentifier: String? = null,
		hint: String? = null,
		imeAction: ImeAction = ImeAction.Unspecified,
		modifier: Modifier = Modifier.None,
		onBlur: Procedure = Pass,
		onFocus: Procedure = Pass,
		onImeAction: CallWith<ImeAction> = Ignore,
		onValueChange: CallWith<String> = Ignore,
		error: String? = null,
		isEnabled: Boolean = true,
		value: String = ""
	) {
		val absolute = ambient(Metrics.Handle)
		val density = ambientDensity()
		val textStyle = currentTextStyle()
		val (activity, on) = remember(value, error, isEnabled, fieldType) {
			machine(State(value, error, isEnabled, fieldType is FieldType.Password))
		}
		val state = activity.state
		val colors = MaterialTheme.colors()
		val textColor = textStyle.color ?: colors.onSurface
		val hintColor = textColor.copy(alpha = 0.5f)
		val smallFontSize = textStyle.fontSize * 2 / 3f
		val reservedVerticalSpace = remember(density, smallFontSize, absolute) {
			withDensity(density) {
				val height = smallFontSize.toDp() + absolute.half
				LayoutPadding(top = height, bottom = height)
			}
		}
		val borderColor = remember(activity, state.isValid, colors) {
			when (activity) {
				is Activity.Blurred -> when {
					!state.isValid -> colors.error.copy(alpha = 0.5f)
					else -> Color.LightGray
				}
				is Activity.Focused -> when {
					!state.isValid -> colors.error
					else -> Color.Gray
				}
				else -> Color.Transparent
			}
		}
		val contentColor = remember(activity, textColor) {
			when (activity) {
				is Activity.Disabled -> textColor.copy(alpha = 0.25f)
				else -> textColor
			}
		}
		val surfaceColor = remember(activity, state.isValid, colors) {
			when (activity) {
				is Activity.Disabled -> Color.DarkGray.copy(alpha = 0.25f)
				else -> when {
					!state.isValid -> colors.error.copy(alpha = 0.25f)
					else -> colors.surface
				}
			}
		}

		Stack(modifier = modifier) {
			Surface(
				borderWidth = 1.dp,
				borderBrush = SolidColor(borderColor),
				color = surfaceColor,
				contentColor = contentColor,
				modifier = LayoutGravity.Center + reservedVerticalSpace,
				shape = RoundedCornerShape(absolute.tiny)
			) {
				Padding(absolute.half) {
					if (state.isSecret) PasswordTextField(
						// missing `keyboard` and `modifier` params!
						focusIdentifier = focusIdentifier,
						imeAction = imeAction,
//						mask = fieldType.mask,
						onBlur = {
							on(Action.Blur)
							onBlur()
						},
						onFocus = {
							on(Action.Tap)
							onFocus()
						},
						onImeActionPerformed = onImeAction,
						onValueChange = {
							on(Action.Change(it))
							onValueChange(it)
						},
						value = state.value
					)
					else TextField(
						focusIdentifier = focusIdentifier,
						imeAction = imeAction,
//						keyboardType = fieldType.keyboard,
						onBlur = {
							on(Action.Blur)
							onBlur()
						},
						onFocus = {
							on(Action.Tap)
							onFocus()
						},
						onImeActionPerformed = onImeAction,
						onValueChange = {
							on(Action.Change(it))
							onValueChange(it)
						},
						value = state.value
					)
				}
			}
			hint?.let {
				val hintStyle = textStyle.copy(color = hintColor)
				if (activity !is Activity.Focused && state.isEmpty) Text(
					style = hintStyle,
					text = it,
					modifier = LayoutGravity.CenterLeft + LayoutPadding(left = absolute.half)
				)
				else Text(
					maxLines = 1,
					modifier = LayoutGravity.TopLeft,
					style = hintStyle.copy(fontSize = smallFontSize),
					text = it
				)
			}
			state.error.takeUnless { it.isNullOrEmpty() }?.let {
				Text(
					maxLines = 1,
					modifier = LayoutGravity.BottomLeft,
					style = textStyle.copy(color = Color.Red, fontSize = smallFontSize),
					text = it
				)
			}
		}
	}
}

@[Composable Preview]
fun preview() {
	MaterialTheme {
		CurrentTextStyleProvider(MaterialTheme.typography().h5) {
			Column(
				arrangement = Arrangement.Center,
				modifier = LayoutPadding(12.dp)
			) {
				EditControl(
					hint = "username gqpj",
					error = "bad email format",
					value = "lorem ipsum dolor sit amet"
				)
			}
		}
	}
}