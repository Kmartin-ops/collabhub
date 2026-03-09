package com.collabhub.notification;

import com.collabhub.domain.Task;
import com.collabhub.domain.User;

// Sealed — only the listed classes can extend this
public abstract sealed class StatusChangeHandler permits TaskCreatedHandler, TaskAssignedHandler, TaskCompletedHandler {

    protected final Notifiable notifier;

    public StatusChangeHandler(Notifiable notifier) {
        this.notifier = notifier;
    }

    // Every handler must define how it handles its event
    public abstract void handle(Task task, User actor);
}
