package com.collabhub.notification;

import com.collabhub.domain.Task;
import com.collabhub.domain.User;

public final class TaskCreatedHandler extends StatusChangeHandler {

    public TaskCreatedHandler(Notifiable notifier) {
        super(notifier);
    }

    @Override
    public void handle(Task task, User actor) {
        notifier.notify(
                actor.getName(),
                "Task '" + task.getTitle() + "' was created in project: "
                        + task.getProject().getName()
        );
    }
}