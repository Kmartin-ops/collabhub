package com.collabhub.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "collabhub")
public class CollabHubProperties {

    private String appDescription;
    private Notifications notifications = new Notifications();
    private Pagination pagination = new Pagination();
    private Security security = new Security();  // ← add

    public String getAppDescription() {
        return appDescription;
    }

    public void setAppDescription(String appDescription) {
        this.appDescription = appDescription;
    }

    public Notifications getNotifications() {
        return notifications;
    }

    public void setNotifications(Notifications notifications) {
        this.notifications = notifications;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }

    public Security getSecurity() { return security; }
    public void setSecurity(Security security) { this.security = security; }

    public static class Security {
        private String jwtSecret;
        private long jwtExpirationMs;

        public String getJwtSecret() { return jwtSecret; }
        public void setJwtSecret(String jwtSecret) { this.jwtSecret = jwtSecret; }
        public long getJwtExpirationMs() { return jwtExpirationMs; }
        public void setJwtExpirationMs(long jwtExpirationMs) {
            this.jwtExpirationMs = jwtExpirationMs;
        }
    }

    public static class Notifications {
        private int dispatcherThreads;
        private int queueCapacity;

        public int getDispatcherThreads() { return dispatcherThreads; }
        public void setDispatcherThreads(int t) { this.dispatcherThreads = t; }
        public int getQueueCapacity() { return queueCapacity; }
        public void setQueueCapacity(int q) { this.queueCapacity = q; }
    }

    public static class Pagination {
        private int defaultPageSize;
        private int maxPageSize;

        public int getDefaultPageSize() { return defaultPageSize; }
        public void setDefaultPageSize(int s) { this.defaultPageSize = s; }
        public int getMaxPageSize() { return maxPageSize; }
        public void setMaxPageSize(int s) { this.maxPageSize = s; }
    }
}