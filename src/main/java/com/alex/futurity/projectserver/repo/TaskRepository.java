package com.alex.futurity.projectserver.repo;

import com.alex.futurity.projectserver.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    @Query(value = "select t.* from Task t " +
                    "join project_column pc on t.column_id = pc.id " +
                    "join project p on pc.project_id = p.id " +
                    "where p.user_id = ?1 and p.id = ?2 and pc.index = ?3 " +
                    "order by t.index", nativeQuery = true)
    List<Task> findAllTasks(long userId, long projectId, int columnIndex);

    @Modifying
    @Query(value = "delete from Task t where t.id = ?1", nativeQuery = true)
    void deleteTask(long id);

    @Modifying
    @Query(value = "update task set column_id = " +
            "(select pc.id from project_column pc " +
            "join project p on pc.project_id = p.id " +
            "where p.user_id = ?1 and p.id = ?2 and pc.index = ?3) " +
            "where id = ?4", nativeQuery = true)
    void changeColumnId(long userId, long projectId, int columnIndex, long taskId);
}
