package com.codewithpot.store.common.repository.shoply;

import com.codewithpot.store.common.entity.shoply.UserEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity,UUID> {
    boolean existsByUserName(String userName);
    boolean existsByEmail(String email);
    UserEntity findByUserName(String userName);
    Optional<UserEntity> findByUserId(UUID userId);


    @Query(value = """
            SELECT
                u
            FROM
                UserEntity u
            WHERE
                u.id = :id
            """)
    UserEntity findNameById(UUID id);

    @Modifying
    @Transactional
    @Query(value = """
            DELETE FROM
                users u
            WHERE
                u.user_id = :id
            """, nativeQuery = true)
    void deleteUserById(@Param("id") UUID id);

    @Query("""
            SELECT
                COUNT(u) > 0
            FROM
                UserEntity u
            WHERE
                u.userName = :userName AND u.userId <> :id
           """)
    boolean existsByUserNameAndUserIdNot(@Param("userName") String userName, @Param("id") UUID id);

    @Query("""
            SELECT
                COUNT(u) > 0
            FROM
                UserEntity u
            WHERE
                u.email = :email AND u.userId <> :id
           """)
    boolean existsByEmailAndUserIdNot(@Param("email") String email, @Param("id") UUID id);
}
