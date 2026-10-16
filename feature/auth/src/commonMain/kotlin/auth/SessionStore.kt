package auth

object SessionStore {
    var token: String? = null
}

class AuthRepository {
    fun login(email: String): String {
        val token = "token-for-$email"
        SessionStore.token = token
        return token
    }

    fun logout() {
        SessionStore.token = null
    }
}
