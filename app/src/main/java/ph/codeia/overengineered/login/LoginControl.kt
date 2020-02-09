package ph.codeia.overengineered.login

import androidx.compose.Composable
import androidx.compose.stateFor
import androidx.lifecycle.LiveData
import androidx.ui.core.PasswordTextField
import androidx.ui.core.TextField
import androidx.ui.foundation.shape.corner.RoundedCornerShape
import androidx.ui.graphics.Color
import androidx.ui.graphics.SolidColor
import androidx.ui.input.KeyboardType
import androidx.ui.layout.*
import androidx.ui.material.Button
import androidx.ui.material.MaterialTheme
import androidx.ui.material.surface.Surface
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.dp
import ph.codeia.overengineered.liveComposable
import javax.inject.Inject

class LoginControl @Inject constructor(private val state: State) {
	constructor() : this(Sample)

	interface State {
		val username: String
		val password: String
		val errors: ValidationResult
	}

	@Composable
	operator fun invoke(): LiveData<LoginAction> = liveComposable {
		val textAppearance = MaterialTheme.typography().h5
		Column(
			arrangement = Arrangement.Center,
			modifier = LayoutHeight.Fill + LayoutPadding(12.dp)
		) {
			FieldDecoration(state.errors.username) {
				TextField(
					onValueChange = { +SetUsername(it) },
					keyboardType = KeyboardType.Email,
					textStyle = textAppearance,
					value = state.username
				)
			}
			Spacer(LayoutHeight(12.dp))
			FieldDecoration(state.errors.password) {
				PasswordTextField(
					onValueChange = { +SetPassword(it) },
					textStyle = textAppearance,
					value = state.password
				)
			}
			Spacer(LayoutHeight(12.dp))
			Button(onClick = { +Submit }, text = "LOGIN")
		}
	}

	@Composable
	private fun FieldDecoration(
		error: String? = null,
		children: @Composable() () -> Unit
	) {
		val borderColor =
			if (error == null) Color.Gray
			else Color.Red
		Surface(
			borderBrush = SolidColor(borderColor),
			borderWidth = 1.dp,
			shape = RoundedCornerShape(3.dp)
		) {
			Container(modifier = LayoutPadding(6.dp)) {
				children()
			}
		}
	}

	private object Sample : State {
		override val username = "foo@example.com"
		override val password = "hunter2"
		override val errors = ValidationResult(null, "required")
	}
}

@[Composable Preview]
fun preview() {
	MaterialTheme {
		LoginControl()()
	}
}
