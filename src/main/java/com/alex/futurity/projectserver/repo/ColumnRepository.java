package com.alex.futurity.projectserver.repo;

import com.alex.futurity.projectserver.entity.ProjectColumn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ColumnRepository extends JpaRepository<ProjectColumn, Long> {
    List<ProjectColumn> findAllByProjectIdAndProjectUserIdOrderByIndex(long projectId, long userId);
}
