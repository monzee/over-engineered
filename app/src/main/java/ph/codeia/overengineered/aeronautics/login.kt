package ph.codeia.overengineered.aeronautics

import ph.codeia.shiv.LateBound
import javax.inject.Inject

/*
 * This file is a part of the Over Engineered project.
 */


sealed class Action {
	class SetUsername(val value: String) : Action()
	class SetPassword(val value: String) : Action()
	object Submit : Action()
}

class Credentials(val username: String, val password: String)

class Validation(val usernameError: String?, val passwordError: String?) {
	val isValid = usernameError == null && passwordError == null
}

interface AuthService {
	fun validate(credentials: Credentials): Validation
	suspend fun login(credentials: Credentials): String
}

class Validate @Inject constructor(
	private val auth: AuthService,
	private val credentials: Get<Credentials>,
	private val output: Put<Progress<Validation, Nothing>>
) {
	operator fun invoke() {
		val result = auth.validate(credentials())
		output(Progress.Active(result))
	}
}

class SetFieldValue(
	private val validate: Validate,
	private val tag: Get<Progress<*, *>>,
	@LateBound private val setValue: Put<String>
) {
	operator fun invoke(value: String) {
		setValue(value.trim())
		if (tag() is Progress.Active) {
			validate()
		}
	}
}

class Submit @Inject constructor(
	private val auth: AuthService,
	private val credentials: Get<Credentials>,
	private val next: Put<Progress<Nothing, String>>
) {
	suspend operator fun invoke() {
		next(Progress.Busy)
		try {
			val token = auth.login(credentials())
			next(Progress.Done(token))
		}
		catch (error: Exception) {
			next(Progress.Failed(error))
		}
	}
}
