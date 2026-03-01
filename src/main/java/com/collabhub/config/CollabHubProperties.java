package com.collabhub.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "collabhub")
public class CollabHubProperties {

    private Notifications notifications = new Notifications();
    private Pagination pagination = new Pagination();
    private String appDescription = "Collaborative project management platform";

    // ── Getters & Setters ─────────────────────────────────
    public Notifications getNotifications() { return notifications; }
    public void setNotifications(Notifications notifications) {
        this.notifications = notifications;
    }

    public Pagination getPagination() { return pagination; }
    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }

    public String getAppDescription() { return appDescription; }
    public void setAppDescription(String appDescription) {
        this.appDescription = appDescription;
    }

    // ── Nested config classes ─────────────────────────────
    public static class Notifications {
        private int dispatcherThreads = 3;
        private int queueCapacity     = 200;

        public int getDispatcherThreads() { return dispatcherThreads; }
        public void setDispatcherThreads(int t) { this.dispatcherThreads = t; }

        public int getQueueCapacity() { return queueCapacity; }
        public void setQueueCapacity(int c) { this.queueCapacity = c; }
    }

    public static class Pagination {
        private int defaultPageSize = 20;
        private int maxPageSize     = 100;

        public int getDefaultPageSize() { return defaultPageSize; }
        public void setDefaultPageSize(int s) { this.defaultPageSize = s; }

        public int getMaxPageSize() { return maxPageSize; }
        public void setMaxPageSize(int s) { this.maxPageSize = s; }
    }

    @Override
    public String toString() {
        return "CollabHubProperties{" +
                "dispatcherThreads=" + notifications.dispatcherThreads +
                ", queueCapacity=" + notifications.queueCapacity +
                ", defaultPageSize=" + pagination.defaultPageSize +
                ", maxPageSize=" + pagination.maxPageSize + '}';
    }
}