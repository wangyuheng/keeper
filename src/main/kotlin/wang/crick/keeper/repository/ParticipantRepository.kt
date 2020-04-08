package wang.crick.keeper.repository

import org.springframework.data.jpa.repository.JpaRepository
import wang.crick.keeper.model.Participant

interface ParticipantRepository : JpaRepository<Participant, String>