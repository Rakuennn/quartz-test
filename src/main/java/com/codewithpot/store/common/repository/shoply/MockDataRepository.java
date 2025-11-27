package com.codewithpot.store.common.repository.shoply;

import com.codewithpot.store.common.entity.shoply.MockDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MockDataRepository extends JpaRepository<MockDataEntity, UUID> {
}
