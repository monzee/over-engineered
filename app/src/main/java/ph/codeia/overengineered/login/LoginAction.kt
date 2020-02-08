package ph.codeia.overengineered.login

sealed class LoginAction
class SetUsername(val value: String) : LoginAction()
class SetPassword(val value: String) : LoginAction()
object Submit : LoginAction()