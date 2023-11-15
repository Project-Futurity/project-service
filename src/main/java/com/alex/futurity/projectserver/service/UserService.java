package com.alex.futurity.projectserver.service;

import com.alex.futurity.projectserver.entity.Task;
import com.alex.futurity.projectserver.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    private static final String MESSAGE = "Not found user for task id = %s";

    public Long findUserByTask(Task task) {
        return userRepository.findUserIdByTask(task.getId())
                .orElseThrow(() -> new IllegalStateException(MESSAGE.formatted(task.getId())));
    }
}
