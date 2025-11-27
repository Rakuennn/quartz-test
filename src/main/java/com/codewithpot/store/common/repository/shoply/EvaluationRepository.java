package com.codewithpot.store.common.repository.shoply;

import com.codewithpot.store.common.entity.shoply.EvaluationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EvaluationRepository extends JpaRepository<EvaluationEntity, UUID> {

}
