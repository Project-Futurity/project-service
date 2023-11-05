package com.alex.futurity.projectserver.repo;

import com.alex.futurity.projectserver.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findAllByColumnId(Long columnId);
}
