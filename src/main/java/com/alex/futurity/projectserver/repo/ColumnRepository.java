package com.alex.futurity.projectserver.repo;

import com.alex.futurity.projectserver.entity.ProjectColumn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ColumnRepository extends JpaRepository<ProjectColumn, Long> {

    @Query("FROM ProjectColumn WHERE project.userId = :userId AND project.id = :projectId")
    List<ProjectColumn> findAllByProject(@Param("userId") Long userId, @Param("projectId") Long projectId);
}
