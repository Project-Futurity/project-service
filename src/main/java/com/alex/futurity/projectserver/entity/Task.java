package com.alex.futurity.projectserver.entity;

import com.alex.futurity.projectserver.utils.DateUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.experimental.Accessors;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;

@Entity
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Wrong name. Name must not be empty")
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @NotNull(message = "Wrong index number. Number must not be empty")
    @Min(value = 0, message = "Wrong index number. Index number must start from 0")
    private Integer index;

    private ZonedDateTime deadline;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "column_id", nullable = false)
    private ProjectColumn column;

    @Column(nullable = false)
    private boolean isCompleted = false;

    public Task(String name, ZonedDateTime deadline, ProjectColumn column) {
        this.name = name;
        this.deadline = deadline;
        this.column = column;
        this.isCompleted = column.isDoneColumn();
    }

    public boolean hasDeadline() {
        return Optional.ofNullable(deadline)
                .filter(DateUtils::isInFuture)
                .isPresent();
    }
}
