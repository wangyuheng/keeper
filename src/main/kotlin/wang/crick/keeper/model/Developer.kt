package wang.crick.keeper.model

import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class Developer(
        @Id var name: String, var username: String, var email: String, var mobile: String, var receivers: String)
