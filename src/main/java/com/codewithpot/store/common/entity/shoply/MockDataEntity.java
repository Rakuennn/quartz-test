package com.codewithpot.store.common.entity.shoply;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "mock_data")
@Accessors(chain = true)
@Data
public class MockDataEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "mock_id")
    private UUID mockId;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "age")
    private Integer age;

    @Column(name = "average_score")
    private Double averageScore;

    @Column(name = "is_online")
    private Boolean isOnline;

    @Column(name = "email_verified")
    private Boolean emailVerified;
}
