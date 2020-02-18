package ph.codeia.overengineered.mvi.login

import dagger.Subcomponent

@Subcomponent(modules = [LoginViewModel::class])
interface LoginView {
	val form: LoginForm

	@Subcomponent.Factory
	interface Binder {
		fun bind(model: LoginViewModel): LoginView
	}
}
