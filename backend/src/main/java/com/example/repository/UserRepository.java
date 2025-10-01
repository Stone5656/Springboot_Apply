package com.example.repository;

import com.example.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, UUID> {

    // 1) 派生クエリ（入れ子を辿る）：primaryEmail.email
    @EntityGraph(attributePaths = "primaryEmail")   // ← これを追加
    Optional<User> findByPrimaryEmailEmailIgnoreCase(String email);

    // 2) JPQL で join（こちらもフィルタ有効時は削除済みが除外されます）
    @Query("""
      SELECT u
      FROM User u
      JOIN u.primaryEmail pe
      WHERE LOWER(pe.email) = LOWER(:email)
    """)
    Optional<User> findByEmailIncludingDeleted(@Param("email") String email);

    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdIncludingDeleted(@Param("id") UUID id);

    // 3) 論理復元：deletedAt 方式なら NULL に戻す
    @Modifying
    @Query("UPDATE User u SET u.deletedAt = NULL WHERE u.id = :id")
    void restoreById(@Param("id") UUID id);
}
