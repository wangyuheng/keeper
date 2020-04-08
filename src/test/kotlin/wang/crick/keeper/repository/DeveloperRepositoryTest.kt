package wang.crick.keeper.repository

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.assertTrue

@SpringBootTest
@RunWith(SpringRunner::class)
class DeveloperRepositoryTest {

    @Autowired
    private lateinit var developerRepository: DeveloperRepository

    @Test
    fun should_return_list_when_context_run() {
        developerRepository.findAll().forEach { println(it) }
        assertTrue(developerRepository.findAll().size > 0)
    }

}