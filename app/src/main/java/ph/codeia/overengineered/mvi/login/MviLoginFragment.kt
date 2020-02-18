package ph.codeia.overengineered.mvi.login

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.ui.core.setContent
import androidx.ui.layout.Arrangement
import androidx.ui.layout.Column
import androidx.ui.layout.LayoutGravity
import androidx.ui.layout.LayoutPadding
import androidx.ui.material.MaterialTheme
import androidx.ui.unit.dp
import ph.codeia.overengineered.R
import ph.codeia.overengineered.consume
import javax.inject.Inject

class MviLoginFragment @Inject constructor(
	private val loginViewBinder: LoginView.Binder,
	vmFactory: SavedStateViewModelFactory
) : Fragment(R.layout.empty) {
	private val loginModel: LoginViewModel by viewModels { vmFactory }

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		val loginView = loginViewBinder.bind(loginModel)
		val update = loginView.input
		val form = loginView.output
		// seems like it's unsafe to pass a lambda doing something context-related
		// inside a composition, so i'm using LiveData for fragment communication.
		form.result.consume(viewLifecycleOwner) {
			when (it) {
				is Done -> toast(it.token())
				is Failed -> it.cause().let { cause ->
					if (cause is LoginError) toast("FAIL! ${cause.message}")
					else throw cause
				}
			}
		}
		(view as ViewGroup).setContent {
			MaterialTheme {
				Column(
					arrangement = Arrangement.Center,
					modifier = LayoutPadding(12.dp)
				) {
					form.render(
						modifier = LayoutGravity.Center,
						onAction = update
					)
				}
			}
		}
	}

	private fun toast(message: String) {
		Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
	}
}
