package ph.codeia.overengineered.mvi.login

import androidx.core.util.PatternsCompat
import kotlinx.coroutines.delay
import kotlin.random.Random

object FakeLoginService : LoginService {
	private const val Required = "required"
	private val EmailRegex = PatternsCompat.EMAIL_ADDRESS.toRegex()

	override fun validateUsername(value: String): String? = when {
		value.isEmpty() -> Required
		!EmailRegex.matches(value) -> "bad email address format"
		else -> null
	}

	override fun validatePassword(value: String): String? = when {
		value.isEmpty() -> Required
		value.length < 5 -> "too simple"
		else -> null
	}

	override suspend fun login(username: String, password: String): String = run {
		delay(2000)
		when (Random.nextInt(4)) {
			0 -> throw LoginError("wrong username or password")
			1 -> throw LoginError("service unavailable")
			2 -> if (Random.nextInt(25) == 0) error("unlucky")
		}
		"congrats"
	}
}