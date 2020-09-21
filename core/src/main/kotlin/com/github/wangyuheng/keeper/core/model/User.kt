package com.github.wangyuheng.keeper.core.model

import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class User(
        @Id var name: String, var username: String, var email: String, var mobile: String, var receivers: String)
