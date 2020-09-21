package com.github.wangyuheng.keeper.core.biz

import com.github.wangyuheng.keeper.core.model.User
import com.github.wangyuheng.keeper.core.repo.UserRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class UserService {
    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    @Autowired
    private lateinit var userRepository: UserRepository

    fun findByUsername(username: String): User? {
        return userRepository.findByUsername(username);
    }

    fun findAll(): List<User> {
        return userRepository.findAll()
    }

}