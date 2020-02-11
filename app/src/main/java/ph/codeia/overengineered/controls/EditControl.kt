package ph.codeia.overengineered.controls

import androidx.compose.Composable
import androidx.compose.Model
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


@Composable
fun EditControl(
	error: String? = null,
	fieldType: FieldType = FieldType.Plain,
	focusIdentifier: String? = null,
	hint: String? = null,
	imeAction: ImeAction = ImeAction.Unspecified,
	isEnabled: Boolean = true,
	modifier: Modifier = Modifier.None,
	onBlur: Procedure = Pass,
	onFocus: Procedure = Pass,
	onImeAction: CallWith<ImeAction> = Ignore,
	onValueChange: CallWith<String> = Ignore,
	value: String = ""
) = EditControl.State(
	value,
	error,
	isEnabled,
	fieldType is FieldType.Password
).also {
	EditControl(it)(
		fieldType, focusIdentifier, hint, imeAction, modifier,
		onBlur, onFocus, onImeAction, onValueChange
	)
}


class EditControl(state: State) {
	@Model
	class State(
		var value: String = "",
		var error: String? = null,
		var isEnabled: Boolean = true,
		var isPassword: Boolean = false
	)

	private val machine = StateMachine(
		Activity.Blurred(when {
			state.isPassword -> Content.Secret(state)
			else -> Content.Normal(state)
		}),
		::transition
	)

	private sealed class Activity {
		abstract val content: Content
		abstract fun change(c: Content): Activity

		data class Blurred(override val content: Content) : Activity() {
			override fun change(c: Content): Activity = copy(content = c)
		}
		data class Disabled(override val content: Content) : Activity() {
			override fun change(c: Content): Activity = copy(content = c)
		}
		data class Focused(override val content: Content) : Activity() {
			override fun change(c: Content): Activity = copy(content = c)
		}
		data class HeldDown(override val content: Content) : Activity() {
			override fun change(c: Content): Activity = copy(content = c)
		}
		data class Selecting(override val content: Content) : Activity() {
			override fun change(c: Content): Activity = copy(content = c)
		}
	}

	private sealed class Content(val state: State) {
		val isEmpty: Boolean
			get() = state.value.isEmpty()

		val isValid: Boolean
			get() = state.error.isNullOrEmpty()

		class Normal(s: State) : Content(s)
		class Secret(s: State) : Content(s)
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
		Action.Blur -> Activity.Blurred(current.content)
		is Action.Change -> {
			current.content.state.value = event.newValue
			current.change(current.content)
		}
		Action.Conceal -> TODO()
		Action.Drag -> TODO()
		Action.Release -> TODO()
		Action.Reveal -> TODO()
		Action.Tap -> when (current) {
			is Activity.Blurred -> Activity.Focused(current.content)
			is Activity.Focused -> Activity.Selecting(current.content)
			is Activity.HeldDown -> Activity.Focused(current.content)
			else -> null
		}
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
		onValueChange: CallWith<String> = Ignore
	) {
		val absolute = ambient(Metrics.Handle)
		val density = ambient(DensityAmbient)
		val (activity, on) = machine
		val state = activity.content.state
		val colors = MaterialTheme.colors()
		val textStyle = currentTextStyle()
		val textColor = textStyle.color ?: colors.onSurface
		val hintColor = textColor.copy(alpha = 0.6f)
		val smallFontSize = textStyle.fontSize * 2 / 3f

		val reservedVerticalSpace = remember(density, smallFontSize) {
			withDensity(density) {
				val height = smallFontSize.toDp() + 6.dp
				LayoutPadding(top = height, bottom = height)
			}
		}

		val borderColor = remember(state.value, state.error, state.isEnabled) {
			when (activity) {
				is Activity.Blurred -> when {
					!activity.content.isValid -> colors.error.copy(alpha = 0.5f)
					else -> Color.LightGray
				}
				is Activity.Focused -> when {
					!activity.content.isValid -> colors.error
					else -> Color.Gray
				}
				else -> Color.Transparent
			}
		}

		val contentColor = remember(state.isEnabled) {
			if (state.isEnabled) textColor
			else textColor.copy(alpha = 0.25f)
		}

		val surfaceColor = remember(state.error, state.isEnabled) {
			when (activity) {
				is Activity.Disabled -> Color.DarkGray.copy(alpha = 0.25f)
				else -> when {
					!activity.content.isValid -> colors.error.copy(alpha = 0.25f)
					else -> colors.surface
				}
			}
		}

		Surface(
			borderWidth = 1.dp,
			borderBrush = SolidColor(borderColor),
			color = surfaceColor,
			contentColor = contentColor,
			modifier = modifier + reservedVerticalSpace,
			shape = RoundedCornerShape(absolute.tiny)
		) {
			Stack(modifier = LayoutPadding(absolute.half)) {
				when (activity.content) {
					is Content.Secret -> PasswordTextField(
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
					is Content.Normal -> TextField(
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
				hint?.let {
					val hintStyle = textStyle.copy(color = hintColor)
					when (activity) {
						is Activity.Focused -> Text(
							maxLines = 1,
							modifier = LayoutGravity.TopLeft + TranslateY(-1f, -12f),
							style = hintStyle.copy(fontSize = smallFontSize),
							text = it
						)
						else -> when {
							activity.content.isEmpty -> Text(style = hintStyle, text = it)
							else -> Text(
								maxLines = 1,
								modifier = LayoutGravity.TopLeft + TranslateY(-1f, -12f),
								style = hintStyle.copy(fontSize = smallFontSize),
								text = it
							)
						}
					}
				}
				state.error.takeUnless { it.isNullOrEmpty() }?.let {
					Text(
						maxLines = 1,
						modifier = LayoutGravity.BottomLeft + TranslateY(1f, 12f),
						style = textStyle.copy(color = Color.Red, fontSize = smallFontSize),
						text = it
					)
				}
			}
		}
	}
}

@[Composable Preview]
fun preview() {
	val state = EditControl.State(
		value = "lorem ipsum dolor sit amet",
		error = "not an email address"
	)
	val Edit = EditControl(state)
	MaterialTheme {
		CurrentTextStyleProvider(MaterialTheme.typography().h4) {
			Column(
				arrangement = Arrangement.Center,
				modifier = LayoutPadding(12.dp)
			) {
				Edit(hint = "Username")
			}
		}
	}
}