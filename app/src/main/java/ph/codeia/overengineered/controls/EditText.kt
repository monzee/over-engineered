package ph.codeia.overengineered.controls

import androidx.compose.*
import androidx.ui.core.*
import androidx.ui.foundation.Clickable
import androidx.ui.foundation.shape.corner.RoundedCornerShape
import androidx.ui.graphics.Color
import androidx.ui.graphics.SolidColor
import androidx.ui.graphics.vector.DrawVector
import androidx.ui.input.ImeAction
import androidx.ui.input.KeyboardType
import androidx.ui.layout.*
import androidx.ui.material.MaterialTheme
import androidx.ui.material.ripple.Ripple
import androidx.ui.material.surface.Surface
import androidx.ui.res.LoadedResource
import androidx.ui.res.loadVectorResource
import androidx.ui.unit.dp
import androidx.ui.unit.withDensity
import ph.codeia.overengineered.R
import ph.codeia.overengineered.StateMachine

/*
 * This file is a part of the Over Engineered project.
 */


object EditText {
	sealed class Type {
		class Password(val mask: Char, val isNumeric: Boolean = false) : Type()
		class Plain(val keyboard: KeyboardType) : Type()
	}

	const val DEFAULT_MASK = '\u2022'
	val Email = Type.Plain(KeyboardType.Email)
	val Number = Type.Plain(KeyboardType.Number)
	val NumberPassword = Type.Password(DEFAULT_MASK, true)
	val Password = Type.Password(DEFAULT_MASK, false)
	val Phone = Type.Plain(KeyboardType.Phone)
	val Plain = Type.Plain(KeyboardType.Text)

	private sealed class Activity(val state: State) {
		class Blurred(s: State) : Activity(s)
		class Disabled(s: State) : Activity(s)
		class Focused(s: State) : Activity(s)
		class HeldDown(s: State) : Activity(s)
		class Selecting(s: State) : Activity(s)
	}

	private enum class Tag {
		Blurred, Disabled, Focused, HeldDown, Selecting
	}

	private enum class Action {
		Blur, Conceal, Reveal, Tap
	}

	private fun transition(
		current: Activity,
		event: Action
	): Activity? = when (event) {
		Action.Blur -> Activity.Blurred(current.state)
		Action.Conceal -> {
			current.state.isSecret = true
			current
		}
		Action.Reveal -> {
			current.state.isSecret = false
			current
		}
		Action.Tap ->
			if (current is Activity.Blurred) Activity.Focused(current.state)
			else null
	}

	private fun machine(state: State) = run {
		StateMachine(
			if (state.isEnabled) Activity.Blurred(state)
			else Activity.Disabled(state),
			::transition
		)
	}

	private class State(
		var isEmpty: Boolean,
		var isEnabled: Boolean,
		var isSecret: Boolean,
		var isValid: Boolean,
		tag: Tag = Tag.Blurred
	) {
		var tag by mutableStateOf(tag)
	}

	@Composable
	operator fun invoke(
		error: String? = null,
		fieldType: Type = Plain,
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
	) {
		val (activity, on) = remember {
			machine(State(
				value.isEmpty(),
				isEnabled,
				fieldType is Type.Password,
				error.isNullOrEmpty()
			))
		}
		onPreCommit(value, isEnabled, error) {
			activity.state.isEmpty = value.isEmpty()
			activity.state.isEnabled = isEnabled
			activity.state.isValid = error.isNullOrEmpty()
		}
		val absolute = ambient(Metrics.Handle)
		val density = ambientDensity()
		val textStyle = currentTextStyle()
		val state = activity.state
		val colors = MaterialTheme.colors()
		val textColor = textStyle.color ?: colors.onSurface
		val hintColor = textColor.copy(alpha = 0.5f)
		val smallFontSize = textStyle.fontSize * 3 / 4f
		val reveal = loadVectorResource(R.drawable.reveal)
		val conceal = loadVectorResource(R.drawable.conceal)
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
				Padding(
					bottom = absolute.half,
					left = absolute.half,
					right = absolute.half +
						if (state.isSecret) 24.dp
						else 0.dp,
					top = absolute.half
				) {
					if (state.isSecret) PasswordTextField(
						// missing `keyboard` and `modifier` params!
						focusIdentifier = focusIdentifier,
						imeAction = imeAction,
						mask = when (fieldType) {
							is Type.Password -> fieldType.mask
							else -> DEFAULT_MASK
						},
						onBlur = {
							on(Action.Blur)
							onBlur()
						},
						onFocus = {
							on(Action.Tap)
							onFocus()
						},
						onImeActionPerformed = onImeAction,
						onValueChange = onValueChange,
						value = value
					)
					else TextField(
						focusIdentifier = focusIdentifier,
						imeAction = imeAction,
						keyboardType = when (fieldType) {
							is Type.Plain -> fieldType.keyboard
							else -> KeyboardType.Password
						},
						onBlur = {
							on(Action.Blur)
							onBlur()
						},
						onFocus = {
							on(Action.Tap)
							onFocus()
						},
						onImeActionPerformed = onImeAction,
						onValueChange = onValueChange,
						value = value
					)
				}
			}
			hint.takeUnless { it.isNullOrEmpty() }?.let {
				val hintStyle = textStyle.copy(color = hintColor)
				if (activity !is Activity.Focused && state.isEmpty) Text(
					modifier = LayoutGravity.CenterLeft + LayoutPadding(left = absolute.half),
					style = hintStyle,
					text = it
				)
				else Text(
					maxLines = 1,
					modifier = LayoutGravity.TopLeft,
					style = hintStyle.copy(fontSize = smallFontSize),
					text = it
				)
			}
			if (fieldType is Type.Password) {
				val toggle = if (state.isSecret) reveal else conceal
				(toggle.resource as? LoadedResource)?.let {
					it.resource?.let { res ->
						Ripple(bounded = false) {
							Clickable(
								onClick = {
									val action =
										if (state.isSecret) Action.Reveal
										else Action.Conceal
									on(action)
								},
								consumeDownOnStart = false
							) {
								Container(
									height = absolute.double,
									modifier = LayoutGravity.CenterRight +
										LayoutPadding(right = absolute.half),
									width = absolute.double
								) {
									DrawVector(
										tintColor = contentColor,
										vectorImage = res
									)
								}
							}
						}
					}
				}
			}
			error.takeUnless { state.isValid }?.let {
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
