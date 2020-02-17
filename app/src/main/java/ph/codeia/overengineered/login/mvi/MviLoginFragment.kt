package ph.codeia.overengineered.login.mvi

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.ui.core.setContent
import androidx.ui.material.MaterialTheme
import ph.codeia.overengineered.R
import ph.codeia.overengineered.consume
import javax.inject.Inject

class MviLoginFragment @Inject constructor(
	private val loginViewBinder: LoginView.Binder,
	vmFactory: SavedStateViewModelFactory
) : Fragment(R.layout.empty) {
	private val loginModel: LoginViewModel by viewModels { vmFactory }

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		val loginForm = loginViewBinder.bind(loginModel).output
		(view as ViewGroup).setContent {
			MaterialTheme {
				loginForm.render()
			}
		}
		loginForm.result.consume(viewLifecycleOwner) {
			when (it) {
				is Done -> toast(it.token())
				is Failed -> toast("FAILED! ${it.cause().message}")
			}
		}
	}

	private fun toast(message: String) {
		Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
	}
}
