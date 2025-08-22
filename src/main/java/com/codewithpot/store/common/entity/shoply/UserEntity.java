package com.codewithpot.store.common.entity.shoply;

import com.codewithpot.store.common.Enum.Gender;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Accessors(chain = true)
@Entity
@Table(name = "users")
@Data
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id")
    private UUID userId;

    @Column(nullable = false, unique = true)
    private String userName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Min(12)
    @Max(120)
    @Column(name = "age")
    private int age;

    @Column(name = "is_active",nullable = false)
    private boolean active;

    @Column(name ="birth_date",nullable = false)
    private LocalDate birthDate;

    @Column(name = "Gender", nullable = false)
    private Gender gender;

    @Column(name = "account_balance", precision = 12, scale = 2)
    private BigDecimal accountBalance;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public boolean getActive(){
        return active;
    }
}