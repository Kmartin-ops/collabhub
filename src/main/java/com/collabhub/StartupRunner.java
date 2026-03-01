package com.collabhub;

import com.collabhub.async.NotificationDispatcher;
import com.collabhub.config.CollabHubProperties;
import com.collabhub.notification.ConsoleNotification;
import com.collabhub.report.ReportEngine;
import com.collabhub.seed.DataSeeder;
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

import java.util.Arrays;

@Component
public class StartupRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(StartupRunner.class);

    private final ProjectService       projectService;
    private final TaskService          taskService;
    private final UserService          userService;
    private final ConsoleNotification  notifier;
    private final ApplicationContext   context;
    private final CollabHubProperties  properties;
    private final Environment          environment;

    public StartupRunner(ProjectService projectService,
                         TaskService taskService,
                         UserService userService,
                         ConsoleNotification notifier,
                         ApplicationContext context,
                         CollabHubProperties properties,
                         Environment environment) {
        this.projectService = projectService;
        this.taskService    = taskService;
        this.userService    = userService;
        this.notifier       = notifier;
        this.context        = context;
        this.properties     = properties;
        this.environment    = environment;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        // Active profiles
        String[] profiles = environment.getActiveProfiles();
        log.info("Active profiles: {}",
                profiles.length > 0 ? Arrays.toString(profiles) : "[default]");

        /*// Print loaded properties
        log.info("Loaded config: {}", properties);

        // Beans
        log.debug("CollabHub beans:");
        Arrays.stream(context.getBeanDefinitionNames())
                .filter(name -> name.contains("Service")
                        || name.contains("Controller")
                        || name.contains("Runner"))
                .sorted()
                .forEach(name -> log.debug("  ✓ {}", name));

        // Seed users
        log.info("Seeding users...");
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

        // Seed projects — values from properties, not hardcoded
        log.info("Seeding projects and tasks...");
        NotificationDispatcher dispatcher = new NotificationDispatcher(
                notifier,
                properties.getNotifications().getDispatcherThreads(),
                properties.getNotifications().getQueueCapacity()
        );
        taskService.setDispatcher(dispatcher);

        DataSeeder seeder = new DataSeeder(projectService, taskService);
        seeder.seed();

        Thread.sleep(1000);

        ReportEngine report = new ReportEngine(taskService.getRegistry());
        report.printFullReport();

        dispatcher.shutdown();
*/
        log.info("CollabHub ready on port {}",
                environment.getProperty("server.port", "8080"));
    }
}
