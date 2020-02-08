package ph.codeia.overengineered.login

import androidx.lifecycle.Observer
import dagger.Subcomponent

@Subcomponent(modules = [LoginViewModel::class])
interface LoginView {
	val input: Observer<LoginAction>
	val output: LoginControl

	@Subcomponent.Factory
	interface Binder {
		fun bind(model: LoginViewModel): LoginView
	}
}
