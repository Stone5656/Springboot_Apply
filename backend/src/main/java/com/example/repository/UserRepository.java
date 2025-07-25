package com.example.repository;

import com.example.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmailIncludingDeleted(@Param("email")
    String email);

    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdIncludingDeleted(@Param("id")
    UUID id);

    @Modifying @Query("UPDATE User u SET u.deleted = false WHERE u.id = :id")
    void restoreById(@Param("id")
    UUID id);
}
