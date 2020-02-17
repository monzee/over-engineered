package ph.codeia.overengineered.login.mvi

import androidx.core.util.PatternsCompat
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import ph.codeia.overengineered.controls.Value
import ph.codeia.overengineered.emit
import ph.codeia.overengineered.many
import kotlin.random.Random

object FakeLoginService : LoginAdapter {
	private const val Required = "required"
	private val EmailRegex = PatternsCompat.EMAIL_ADDRESS.toRegex()

	override fun LoginModel.submit(sync: Value<LoginModel>) = many<LoginModel> {
		val model = this@submit
		val tag = when (model.tag) {
			Busy -> return@many
			is Validated -> model.tag
			else -> validate(model.username, model.password)
		}
		if (!tag.isValid) emit(model.copy(tag = tag))
		else {
			emit(model.copy(tag = Busy))
			delay(2000)
			val promise = CompletableDeferred<LoginModel>()
			try {
				when (Random.nextInt(4)) {
					0 -> throw LoginError("wrong username or password")
					1 -> throw LoginError("service unavailable")
					2 -> if (Random.nextInt(25) == 0) error("unlucky")
				}
				emit(sync().finish(promise, "congrats!"))
			}
			catch (ex: Throwable) {
				emit(sync().finish(promise, ex))
			}
			finally {
				emit(promise.await())
			}
		}
	}

	override fun LoginModel.validate() = run {
		copy(tag = validate(username, password))
	}

	private fun validate(username: String, password: String) = run {
		Validated(
			when {
				username.isEmpty() -> Required
				!EmailRegex.matches(username) -> "bad email address format"
				else -> null
			},
			when {
				password.isEmpty() -> Required
				password.length < 5 -> "too simple"
				else -> null
			}
		)
	}

	// cannot be inlined because of a runtime error about a missing
	// class called "$$$$$NON_LOCAL_RETURN$$$$$"
	private fun LoginModel.finish(
		promise: CompletableDeferred<LoginModel>,
		token: String
	) = copy(tag = Done {
		promise.complete(validate())
		token
	})

	// likewise
	private fun LoginModel.finish(
		promise: CompletableDeferred<LoginModel>,
		cause: Throwable
	) = copy(tag = Failed {
		promise.complete(validate())
		cause
	})
}