package ph.codeia.overengineered.login

data class ValidationResult(val username: String?, val password: String?) {
	val isValid = username == null && password == null

	companion object {
		val Valid = ValidationResult(null, null)
	}
}