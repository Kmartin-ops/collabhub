package com.collabhub;

import com.collabhub.async.NotificationDispatcher;
import com.collabhub.notification.ConsoleNotification;
import com.collabhub.report.ReportEngine;
import com.collabhub.seed.DataSeeder;
import com.collabhub.service.ProjectService;
import com.collabhub.service.TaskService;
import com.collabhub.service.UserService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class StartupRunner implements ApplicationRunner {

    private final ProjectService projectService;
    private final TaskService    taskService;
    private final UserService    userService;
    private final ConsoleNotification notifier;
    private final ApplicationContext  context;

    public StartupRunner(ProjectService projectService,
                         TaskService taskService,
                         UserService userService,
                         ConsoleNotification notifier,
                         ApplicationContext context) {
        this.projectService = projectService;
        this.taskService    = taskService;
        this.userService    = userService;
        this.notifier       = notifier;
        this.context        = context;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        // Print beans
        System.out.println("\n[CollabHub] Beans registered by Spring:");
        Arrays.stream(context.getBeanDefinitionNames())
                .filter(name -> name.contains("Service")
                        || name.contains("Controller")
                        || name.contains("Runner"))
                .sorted()
                .forEach(name -> System.out.println("   ✓ " + name));

        // Seed users into UserService so API can look them up
        System.out.println("\n[CollabHub] Seeding users...");
        userService.createUser("Alice", "alice@collabhub.com", "MANAGER");
        userService.createUser("Bob",   "bob@collabhub.com",   "DEVELOPER");
        userService.createUser("Carol", "carol@collabhub.com", "DEVELOPER");
        userService.createUser("Dave",  "dave@collabhub.com",  "DEVELOPER");
        userService.createUser("Eve",   "eve@collabhub.com",   "DEVELOPER");
        userService.createUser("Frank", "frank@collabhub.com", "MANAGER");
        userService.createUser("Grace", "grace@collabhub.com", "DEVELOPER");
        userService.createUser("Henry", "henry@collabhub.com", "DEVELOPER");
        userService.createUser("Iris",  "iris@collabhub.com",  "DEVELOPER");
        userService.createUser("James", "james@collabhub.com", "ADMIN");

        // Seed projects and tasks
        System.out.println("\n[CollabHub] Seeding projects and tasks...");
        NotificationDispatcher dispatcher =
                new NotificationDispatcher(notifier, 3, 200);
        taskService.setDispatcher(dispatcher);

        DataSeeder seeder = new DataSeeder(projectService, taskService);
        seeder.seed();

        Thread.sleep(1000);

        ReportEngine report = new ReportEngine(taskService.getRegistry());
        report.printFullReport();

        dispatcher.shutdown();

        System.out.println("\n[CollabHub] Ready on http://localhost:8080");
    }
}