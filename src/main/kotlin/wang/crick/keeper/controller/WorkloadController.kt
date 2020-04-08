package wang.crick.keeper.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import wang.crick.keeper.service.WorkloadService
import java.util.*

@RestController
@RequestMapping("workload")
class WorkloadController {

    @Autowired
    private lateinit var workloadService: WorkloadService

    @GetMapping
    fun initByGroup(@RequestParam("groupId") groupId: Int): String {
        workloadService.statistics(Collections.singletonList(groupId))
        return "ok"
    }

}