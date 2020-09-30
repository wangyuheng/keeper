package com.github.wangyuheng.keeper.workload

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(value = ["workload.enable"], havingValue = "true")
class WorkloadJob {

    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    @Value("\${workload.groups:}")
    private lateinit var workloadGroups: List<Int>
    @Autowired
    private lateinit var workloadService: WorkloadService

    @Scheduled(cron = "\${workload.cron}")
    fun workloadJob() {
        log.info("start workload job ! workloadGroups:{}", workloadGroups)
        workloadService.statistics(workloadGroups)
    }

}