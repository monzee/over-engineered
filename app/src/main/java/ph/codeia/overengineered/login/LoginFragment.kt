package ph.codeia.overengineered.login

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.ui.core.setContent
import androidx.ui.material.MaterialTheme
import ph.codeia.overengineered.R

class LoginFragment(
	private val loginViewBinder: LoginView.Binder,
	vmFactory: SavedStateViewModelFactory
) : Fragment(R.layout.empty) {
	private val loginModel: LoginViewModel by viewModels { vmFactory }

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		val loginView = loginViewBinder.bind(loginModel)
		(view as ViewGroup).setContent {
			MaterialTheme {
				loginView.output().observe(viewLifecycleOwner, loginView.input)
			}
		}
	}
}
