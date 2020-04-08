package wang.crick.keeper.repository

import org.springframework.data.jpa.repository.JpaRepository
import wang.crick.keeper.model.Issue

interface IssueRepository : JpaRepository<Issue, String>