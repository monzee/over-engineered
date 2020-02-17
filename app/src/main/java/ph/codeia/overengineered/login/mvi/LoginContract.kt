package ph.codeia.overengineered.login.mvi

import ph.codeia.overengineered.controls.Value

sealed class LoginAction
class SetUsername(val value: String) : LoginAction()
class SetPassword(val value: String) : LoginAction()
object Submit : LoginAction()


data class LoginState(
	val username: String,
	val password: String,
	val tag: LoginTag
)

sealed class LoginTag
object Idle : LoginTag()
object Busy : LoginTag()
data class Active(val errors: ValidationResult) : LoginTag()
data class Failed(val cause: Value<Throwable>) : LoginTag()
data class Done(val token: Value<String>) : LoginTag()


data class ValidationResult(val username: String?, val password: String?) {
	val isValid = username == null && password == null
}
