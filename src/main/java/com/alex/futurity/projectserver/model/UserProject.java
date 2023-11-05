package com.alex.futurity.projectserver.model;


import lombok.NonNull;
import lombok.Value;

@Value(staticConstructor = "of")
public class UserProject {
    @NonNull Long userId;
    @NonNull Long projectId;
}
