package ph.codeia.overengineered.mvi.login

import ph.codeia.overengineered.Many
import ph.codeia.overengineered.controls.Value

sealed class LoginAction
class SetUsername(val value: String) : LoginAction()
class SetPassword(val value: String) : LoginAction()
object Submit : LoginAction()

data class LoginModel(
	val username: String,
	val password: String,
	val tag: LoginTag
)

sealed class LoginTag
object Idle : LoginTag()
object Busy : LoginTag()
data class Failed(val cause: Value<Throwable>) : LoginTag()
data class Done(val token: Value<String>) : LoginTag()
data class Validated(
	val username: String?,
	val password: String?
) : LoginTag() {
	val isValid = username == null && password == null
}

interface LoginAdapter {
	fun LoginModel.setUsername(value: String): LoginModel
	fun LoginModel.setPassword(value: String): LoginModel
	fun LoginModel.submit(sync: Value<LoginModel>): Many<LoginModel>
	fun LoginModel.validate(): LoginModel
}

interface LoginService {
	fun validateUsername(value: String): String?
	fun validatePassword(value: String): String?
	suspend fun login(username: String, password: String): String
}

class LoginError(override val message: String) : RuntimeException(message)