package com.collabhub.notification;

import com.collabhub.domain.Task;
import com.collabhub.domain.User;

public final class TaskAssignedHandler extends StatusChangeHandler {

    public TaskAssignedHandler(Notifiable notifier) {
        super(notifier);
    }

    @Override
    public void handle(Task task, User actor) {
        String assigneeName = task.getAssignee() != null ? task.getAssignee().getName() : "nobody";

        notifier.notify(actor.getName(), "Task '" + task.getTitle() + "' was assigned to: " + assigneeName);
    }
}
