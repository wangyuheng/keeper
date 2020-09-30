package com.github.wangyuheng.keeper.oauth

import com.github.wangyuheng.keeper.oauth.client.OauthUser

interface SessionManager {

    fun put(key: String, user: OauthUser)
    fun get(key: String): OauthUser?
}

class LocalSessionManager : SessionManager {

    private val store: HashMap<String, OauthUser> = HashMap()

    override fun put(key: String, user: OauthUser) {
        store[key] = user
    }

    override fun get(key: String): OauthUser? {
        return store[key]
    }

}