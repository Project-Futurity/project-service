package com.alex.futurity.projectserver.entity;

import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    @NotBlank(message = "Wrong name. Name must not be empty")
    private String name;

    @Column(nullable = false)
    @NotBlank(message = "Wrong description. Description must not be empty")
    private String description;

    @NotNull(message = "Project preview must not be null")
    @Lob
    @Basic(fetch = FetchType.LAZY, optional = false)
    private byte[] preview;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "project")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<ProjectColumn> columns;

    public Project(Long userId, String name, String description, byte[] preview) {
        this.userId = userId;
        this.name = name;
        this.description = description;
        this.preview = preview;
    }

    public void addColumn(ProjectColumn projectColumn) {
        projectColumn.setIndex(columns.size());
        columns.add(projectColumn);
    }
}
