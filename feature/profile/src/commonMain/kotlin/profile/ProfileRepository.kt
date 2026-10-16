package profile

import auth.SessionStore

class ProfileRepository {
    fun isLoggedIn(): Boolean = SessionStore.token != null

    fun profileTitle(): String =
        if (isLoggedIn()) "Profile (session ${SessionStore.token})" else "Guest"
}
