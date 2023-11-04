package com.alex.futurity.projectserver.repo;

import com.alex.futurity.projectserver.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    Optional<Project> findByNameAndUserId(String name, long id);
    List<Project> findAllByUserId(long id);
    Optional<Project> findByIdAndUserId(long projectId, long userId);

    @Modifying
    @Query("delete from Project where id = ?1 and userId = ?2")
    int deleteProjectByIdAndUserId(long projectId, long userId);
}
