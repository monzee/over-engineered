package ph.codeia.overengineered.mvi.login

import kotlinx.coroutines.CompletableDeferred
import ph.codeia.overengineered.controls.Value
import ph.codeia.overengineered.emit
import ph.codeia.overengineered.many
import javax.inject.Inject

class ValidateOnFieldChange @Inject constructor(
	private val service: LoginService
) : LoginAdapter {
	override fun LoginModel.setUsername(value: String): LoginModel = run {
		copy(username = value).let {
			if (it.tag is Validated) it.validate()
			else it
		}
	}

	override fun LoginModel.setPassword(value: String): LoginModel = run {
		copy(password = value).let {
			if (it.tag is Validated) it.validate()
			else it
		}
	}

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
			val promise = CompletableDeferred<LoginModel>()
			try {
				val token = service.login(model.username, model.password)
				emit(sync().finish(promise, token))
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
			service.validateUsername(username),
			service.validatePassword(password)
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