package wang.crick.keeper.repository

import org.springframework.data.jpa.repository.JpaRepository
import wang.crick.keeper.model.Developer

interface DeveloperRepository : JpaRepository<Developer, String> {
    fun findByUsername(username: String): Developer?
}