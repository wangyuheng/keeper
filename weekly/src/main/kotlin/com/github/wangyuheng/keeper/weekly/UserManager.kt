package com.github.wangyuheng.keeper.weekly

import com.alibaba.fastjson.JSON
import com.github.wangyuheng.keeper.core.client.GitlabApiClient
import org.springframework.beans.factory.annotation.Autowired

interface UserManager {

    fun findAll(): List<User>

}

data class User(
        var name: String,
        var username: String,
        var email: String
)

class GitlabUserManager : UserManager {

    @Autowired
    private lateinit var gitlabApiClient: GitlabApiClient

    override fun findAll(): List<User> {
        return gitlabApiClient.listActiveUsers().map { JSON.parseObject(it, User::class.java) }
    }

}