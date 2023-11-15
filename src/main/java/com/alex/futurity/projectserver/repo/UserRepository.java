package com.alex.futurity.projectserver.repo;

import com.alex.futurity.projectserver.entity.Task;
import jakarta.annotation.Nonnull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<Task, Long> {
    @Query("SELECT column.project.userId FROM Task WHERE id = :taskId")
    Optional<Long> findUserIdByTask(@Nonnull @Param("taskId") Long taskId);
}
