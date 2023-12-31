package com.alex.futurity.projectserver.message;

import com.alex.futurity.projectserver.context.UserContext;
import com.alex.futurity.projectserver.entity.Task;
import com.alex.futurity.projectserver.message.model.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class TaskEventPublisher {
    private final StreamBridge streamBridge;
    private static final String BINDER_NAME = "taskPublisher";

    public void publishCreationEvent(Task task) {
        CreationTaskEvent event = CreationTaskEvent.builder()
                .id(task.getId())
                .deadline(task.getDeadline())
                .userId(UserContext.getUserId())
                .build();

        publish(event, TaskRoutingKey.CREATED);
    }

    public void publishUpdateEvent(Task task) {
        UpdateTaskEvent event = UpdateTaskEvent.builder()
                .id(task.getId())
                .deadline(task.getDeadline())
                .completed(task.isCompleted())
                .userId(UserContext.getUserId())
                .build();

        publish(event, TaskRoutingKey.UPDATED);
    }

    public void publishDeleteEvent(Task task) {
        DeleteTaskEvent event = DeleteTaskEvent.builder()
                .id(task.getId())
                .build();

        publish(event, TaskRoutingKey.DELETED);
    }

    private void publish(TaskEvent taskEvent, TaskRoutingKey routingKey) {
        if (!UserContext.hasTelegram()) {
            return;
        }

        Message<TaskEvent> message = MessageBuilder
                .withPayload(taskEvent)
                .setHeader(TaskRoutingKey.HEADER_NAME, routingKey.getRoutingKey())
                .build();

        streamBridge.send(BINDER_NAME, message);
        log.info("Task with id {} and key {} has been published", taskEvent.getId(), routingKey.getRoutingKey());
    }
}
