package ph.codeia.overengineered.login.mvi

import dagger.Subcomponent

@Subcomponent(modules = [LoginViewModel::class])
interface LoginView {
	val output: LoginForm

	@Subcomponent.Factory
	interface Binder {
		fun bind(model: LoginViewModel): LoginView
	}
}
