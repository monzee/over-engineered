package ph.codeia.overengineered.mvi.login

import dagger.Subcomponent
import ph.codeia.overengineered.controls.CallWith

@Subcomponent(modules = [LoginViewModel::class])
interface LoginView {
	val input: CallWith<LoginAction>
	val output: LoginForm

	@Subcomponent.Factory
	interface Binder {
		fun bind(model: LoginViewModel): LoginView
	}
}
