package com.collabhub;

import com.collabhub.async.NotificationDispatcher;
import com.collabhub.notification.ConsoleNotification;
import com.collabhub.report.ReportEngine;
import com.collabhub.seed.DataSeeder;
import com.collabhub.service.ProjectService;
import com.collabhub.service.TaskService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class StartupRunner implements ApplicationRunner {

    private final ProjectService projectService;
    private final TaskService taskService;
    private final ConsoleNotification notifier;
    private final ApplicationContext context;

    public StartupRunner(ProjectService projectService,
                         TaskService taskService,
                         ConsoleNotification notifier,
                         ApplicationContext context) {
        this.projectService = projectService;
        this.taskService    = taskService;
        this.notifier       = notifier;
        this.context        = context;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        // Print all CollabHub beans Spring created
        System.out.println("\n[CollabHub] Beans registered by Spring:");
        Arrays.stream(context.getBeanDefinitionNames())
                .filter(name -> name.startsWith("com.collabhub")
                        || name.contains("Service")
                        || name.contains("Controller")
                        || name.contains("Runner"))
                .sorted()
                .forEach(name -> System.out.println("   ✓ " + name));

        System.out.println("\n[CollabHub] Seeding data...");

        NotificationDispatcher dispatcher =
                new NotificationDispatcher(notifier, 3, 200);
        taskService.setDispatcher(dispatcher);

        DataSeeder seeder = new DataSeeder(projectService, taskService);
        seeder.seed();

        Thread.sleep(1000);

        ReportEngine report = new ReportEngine(taskService.getRegistry());
        report.printFullReport();

        dispatcher.shutdown();

        System.out.println("\n[CollabHub] Ready — listening on port 8080");
    }
}