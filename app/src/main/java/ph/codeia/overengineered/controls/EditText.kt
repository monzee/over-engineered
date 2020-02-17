package ph.codeia.overengineered.controls

import androidx.compose.Composable
import androidx.compose.ambient
import androidx.compose.remember
import androidx.ui.core.*
import androidx.ui.foundation.Clickable
import androidx.ui.foundation.shape.corner.RoundedCornerShape
import androidx.ui.graphics.Color
import androidx.ui.graphics.SolidColor
import androidx.ui.graphics.vector.DrawVector
import androidx.ui.graphics.vector.VectorAsset
import androidx.ui.input.ImeAction
import androidx.ui.input.KeyboardType
import androidx.ui.layout.*
import androidx.ui.material.MaterialTheme
import androidx.ui.material.ripple.Ripple
import androidx.ui.material.surface.Surface
import androidx.ui.res.DeferredResource
import androidx.ui.res.loadVectorResource
import androidx.ui.unit.dp
import androidx.ui.unit.withDensity
import ph.codeia.overengineered.R
import ph.codeia.overengineered.scan

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

	private val InitialState = State(
		activity = Activity.Blurred,
		isEmpty = true,
		isSecret = false,
		isValid = true
	)

	private data class State(
		val activity: Activity,
		val isEmpty: Boolean,
		val isSecret: Boolean,
		val isValid: Boolean
	) {
		fun transition(event: Action): State? = when (event) {
			Action.Blur ->
				if (activity == Activity.Focused) copy(activity = Activity.Blurred)
				else null
			Action.Conceal -> copy(isSecret = true)
			Action.Reveal -> copy(isSecret = false)
			is Action.Sync -> copy(
				activity = event.activity,
				isEmpty = event.isEmpty,
				isValid = event.isValid
			)
			Action.Tap ->
				if (activity == Activity.Blurred) copy(activity = Activity.Focused)
				else null
		}
	}

	private enum class Activity {
		Blurred, Disabled, Focused
	}

	private sealed class Action {
		object Blur : Action()
		object Conceal : Action()
		object Reveal : Action()
		class Sync(
			val activity: Activity,
			val isEmpty: Boolean,
			val isValid: Boolean
		) : Action()
		object Tap : Action()
	}

	@Composable
	private fun sync(
		activity: Activity,
		isEmpty: Boolean,
		isSecret: Boolean,
		isValid: Boolean
	) = run {
		val fsm = scan(InitialState.copy(isSecret = isSecret), State::transition)
		remember(activity, isEmpty, isValid) {
			fsm.dispatch(Action.Sync(activity, isEmpty, isValid))
		}
		fsm
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
		val (state, on) = sync(
			if (isEnabled) Activity.Blurred else Activity.Disabled,
			value.isEmpty(),
			fieldType is Type.Password,
			error.isNullOrEmpty()
		)
		val activity = state.activity
		val absolute = ambient(Metrics.Handle)
		val density = ambientDensity()
		val textStyle = currentTextStyle()
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
		val borderColor = remember(state, colors) {
			when (activity) {
				Activity.Blurred ->
					if (state.isValid) Color.LightGray
					else colors.error.copy(alpha = 0.5f)
				Activity.Focused ->
					if (state.isValid) Color.Gray
					else colors.error
				else -> Color.Transparent
			}
		}
		val contentColor = remember(activity, textColor) {
			if (activity != Activity.Disabled) textColor
			else textColor.copy(alpha = 0.25f)
		}
		val surfaceColor = remember(state, colors) {
			when {
				activity == Activity.Disabled -> Color.DarkGray.copy(alpha = 0.125f)
				state.isValid -> colors.surface
				else -> colors.error.copy(alpha = 0.125f)
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
				if (activity != Activity.Focused && state.isEmpty) Text(
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
				val action = if (state.isSecret) Action.Reveal else Action.Conceal
				val icon = if (state.isSecret) reveal else conceal
				VisibilitySwitch(
					absolute = absolute,
					action = { on(action) },
					icon = icon,
					modifier = LayoutGravity.CenterRight,
					tint = contentColor
				)
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

	@Composable
	private fun VisibilitySwitch(
		absolute: Metrics,
		action: Procedure,
		icon: DeferredResource<VectorAsset>,
		modifier: Modifier,
		tint: Color
	) {
		icon.resource.resource?.let { res ->
			Ripple(bounded = false) {
				Clickable(consumeDownOnStart = false, onClick = action) {
					Container(
						height = absolute.double,
						modifier = modifier + LayoutPadding(right = absolute.half),
						width = absolute.double
					) {
						DrawVector(tintColor = tint, vectorImage = res)
					}
				}
			}
		}
	}
}
