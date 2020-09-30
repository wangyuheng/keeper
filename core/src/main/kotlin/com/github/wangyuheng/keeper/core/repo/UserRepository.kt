package com.github.wangyuheng.keeper.core.repo

import com.github.wangyuheng.keeper.core.model.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, String> {
    fun findByUsername(username: String): User?
}