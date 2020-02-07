package ph.codeia.overengineered.aeronautics

import androidx.compose.Model
import androidx.lifecycle.Observer
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/*
 * This file is a part of the Over Engineered project.
 */


@[Model Module]
class LoginModel {
	private var username: String = ""
	private var password: String = ""
	private var tag: Progress<Validation, String> = Progress.Idle

	@get:Provides
	val getCredentials: Get<Credentials> = {
		Credentials(username, password)
	}

	@get:Provides
	val getTag: Get<Progress<Validation, *>> = {
		tag
	}

	@get:Provides
	val setValidationResult: Put<Progress<Validation, Nothing>> = {
		tag = it
	}

	@get:Provides
	val setSubmitResult: Put<Progress<Nothing, String>> = {
		tag = it
	}

	@Provides
	fun controller(
		scope: CoroutineScope,
		setValue: PartialSetFieldValue,
		submit: Submit
	): Observer<Action> = Observer { action ->
		when (action) {
			is Action.SetUsername -> {
				val setUsername = setValue.bind(action.value)
				setUsername { username = it }
			}
			is Action.SetPassword -> {
				val setPassword = setValue.bind(action.value)
				setPassword { password = it }
			}
			Action.Submit -> scope.launch {
				submit()
			}
		}
	}

	internal fun setUsername(value: String) {
		username = value
	}

	internal fun setPassword(value: String) {
		password = value
	}
}

