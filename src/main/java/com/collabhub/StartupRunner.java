package com.collabhub;

import com.collabhub.async.NotificationDispatcher;
import com.collabhub.config.CollabHubProperties;
import com.collabhub.domain.Project;
import com.collabhub.domain.Task;
import com.collabhub.domain.User;
import com.collabhub.notification.ConsoleNotification;
import com.collabhub.service.ProjectService;
import com.collabhub.service.TaskService;
import com.collabhub.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;

@Component
public class StartupRunner implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(StartupRunner.class);

    private final ProjectService projectService;
    private final TaskService taskService;
    private final UserService userService;
    private final ConsoleNotification notifier;
    private final ApplicationContext context;
    private final CollabHubProperties properties;
    private final Environment environment;

    public StartupRunner(ProjectService projectService, TaskService taskService, UserService userService,
            ConsoleNotification notifier, ApplicationContext context, CollabHubProperties properties, Environment environment) {
        this.projectService = projectService;
        this.taskService = taskService;
        this.userService = userService;
        this.notifier = notifier;
        this.context = context;
        this.properties = properties;
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String[] profiles = environment.getActiveProfiles();
        LOG.info("Active profiles: {}", profiles.length > 0 ? Arrays.toString(profiles) : "[default]");

        // Only seed if database is empty
        if (userService.findAll().isEmpty()) {
            LOG.info("Empty database detected — seeding...");
            seed();
        } else {
            LOG.info("Database already has data — skipping seed.");
        }

        LOG.info("CollabHub ready on port {}", environment.getProperty("server.port", "8080"));
    }

    private void seed() throws InterruptedException {
        NotificationDispatcher dispatcher = new NotificationDispatcher(notifier,
                properties.getNotifications().getDispatcherThreads(), properties.getNotifications().getQueueCapacity());
        taskService.setDispatcher(dispatcher);

        // Users
        User alice = userService.createUser("Alice Johnson", "alice@collabhub.com", "MANAGER", "Password123!");
        User bob   = userService.createUser("Bob",   "bob@collabhub.com",   "DEVELOPER", "Password123!");
        User carol = userService.createUser("Carol", "carol@collabhub.com", "DEVELOPER", "Password123!");
        User dave  = userService.createUser("Dave",  "dave@collabhub.com",  "DEVELOPER", "Password123!");
        User frank = userService.createUser("Frank", "frank@collabhub.com", "MANAGER",   "Password123!");
        userService.createUser("Eve",   "eve@collabhub.com",   "DEVELOPER", "Password123!");
        userService.createUser("Grace", "grace@collabhub.com", "DEVELOPER", "Password123!");
        userService.createUser("Henry", "henry@collabhub.com", "DEVELOPER", "Password123!");
        userService.createUser("Iris",  "iris@collabhub.com",  "DEVELOPER", "Password123!");
        userService.createUser("James", "james@collabhub.com", "ADMIN",     "Password123!");
        // Projects
        Project mvp = projectService.createProject("CollabHub MVP", "Core platform features", alice);
        projectService.addMember(mvp, bob);
        projectService.addMember(mvp, carol);

        Project mobile = projectService.createProject("CollabHub Mobile", "iOS & Android app", frank);
        projectService.addMember(mobile, dave);

        // Tasks
        Task t1 = taskService.createTask("Set up domain model", "HIGH", LocalDate.now().plusDays(2), mvp, alice);
        Task t2 = taskService.createTask("Build REST API", "HIGH", LocalDate.now().plusDays(5), mvp, alice);
        Task t3 = taskService.createTask("Write unit tests", "MEDIUM", LocalDate.now().plusDays(7), mvp, alice);
        Task t4 = taskService.createTask("Fix login bug", "HIGH", LocalDate.now().minusDays(1), mvp, alice);

        taskService.assignTask(t1, bob, alice);
        taskService.assignTask(t2, carol, alice);
        taskService.assignTask(t3, bob, alice);
        taskService.assignTask(t4, carol, alice);

        taskService.changeStatus(t1, "IN_PROGRESS", bob);
        taskService.changeStatus(t4, "IN_PROGRESS", carol);

        Thread.sleep(500);
        dispatcher.shutdown();

        LOG.info("Seed complete — users={}, projects={}, tasks={}", userService.findAll().size(),
                projectService.getAllProjects().size(), taskService.findAll().size());
    }
}
