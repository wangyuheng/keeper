package com.github.wangyuheng.keeper.core.repo

import org.springframework.data.jpa.repository.JpaRepository
import com.github.wangyuheng.keeper.core.model.Issue

interface IssueRepository : JpaRepository<Issue, String>