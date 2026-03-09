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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StartupRunner")
class StartupRunnerTest {

    @Mock
    private ProjectService projectService;
    @Mock
    private TaskService taskService;
    @Mock
    private UserService userService;
    @Mock
    private ConsoleNotification notifier;
    @Mock
    private ApplicationContext context;
    @Mock
    private Environment environment;

    private StartupRunner startupRunner;

    @BeforeEach
    void setUp() {
        CollabHubProperties properties = new CollabHubProperties();
        CollabHubProperties.Notifications notifications = new CollabHubProperties.Notifications();
        notifications.setDispatcherThreads(1);
        notifications.setQueueCapacity(10);
        properties.setNotifications(notifications);

        startupRunner = new StartupRunner(projectService, taskService, userService, notifier, context, properties,
                environment);
    }

    @Test
    @DisplayName("should seed initial data when database is empty")
    void shouldSeedWhenDatabaseEmpty() throws Exception {
        // Users
        User alice = user("Alice Johnson", "alice@collabhub.com", "MANAGER", "Password123!");
        User bob = user("Bob", "bob@collabhub.com", "DEVELOPER", "Password123!");
        User carol = user("Carol", "carol@collabhub.com", "DEVELOPER", "Password123!");
        User dave = user("Dave", "dave@collabhub.com", "DEVELOPER", "Password123!");
        User frank = user("Frank", "frank@collabhub.com", "MANAGER", "Password123!");
        User eve = user("Eve", "eve@collabhub.com", "DEVELOPER", "Password123!");
        User grace = user("Grace", "grace@collabhub.com", "DEVELOPER", "Password123!");
        User henry = user("Henry", "henry@collabhub.com", "DEVELOPER", "Password123!");
        User iris = user("Iris", "iris@collabhub.com", "DEVELOPER", "Password123!");
        User james = user("James", "james@collabhub.com", "ADMIN", "Password123!");

        // Projects
        Project mvp = new Project("CollabHub MVP", "Core platform features");
        mvp.setId(UUID.randomUUID());
        Project mobile = new Project("CollabHub Mobile", "iOS & Android app");
        mobile.setId(UUID.randomUUID());

        // Environment stubs
        when(environment.getActiveProfiles()).thenReturn(new String[]{"dev"});
        when(environment.getProperty("server.port", "8080")).thenReturn("9090");

        // Database empty first, then filled
        when(userService.findAll()).thenReturn(List.of())
                .thenReturn(List.of(alice, bob, carol, dave, frank, eve, grace, henry, iris, james));

        // Stub user creation
        when(userService.createUser(eq("Alice Johnson"), eq("alice@collabhub.com"), eq("MANAGER"), anyString())).thenReturn(alice);
        when(userService.createUser(eq("Bob"), eq("bob@collabhub.com"), eq("DEVELOPER"), anyString())).thenReturn(bob);
        when(userService.createUser(eq("Carol"), eq("carol@collabhub.com"), eq("DEVELOPER"), anyString())).thenReturn(carol);
        when(userService.createUser(eq("Dave"), eq("dave@collabhub.com"), eq("DEVELOPER"), anyString())).thenReturn(dave);
        when(userService.createUser(eq("Frank"), eq("frank@collabhub.com"), eq("MANAGER"), anyString())).thenReturn(frank);
        when(userService.createUser(eq("Eve"), eq("eve@collabhub.com"), eq("DEVELOPER"), anyString())).thenReturn(eve);
        when(userService.createUser(eq("Grace"), eq("grace@collabhub.com"), eq("DEVELOPER"), anyString())).thenReturn(grace);
        when(userService.createUser(eq("Henry"), eq("henry@collabhub.com"), eq("DEVELOPER"), anyString())).thenReturn(henry);
        when(userService.createUser(eq("Iris"), eq("iris@collabhub.com"), eq("DEVELOPER"), anyString())).thenReturn(iris);
        when(userService.createUser(eq("James"), eq("james@collabhub.com"), eq("ADMIN"), anyString())).thenReturn(james);

        // Project creation stubs
        when(projectService.createProject(eq("CollabHub MVP"), eq("Core platform features"), any(User.class))).thenReturn(mvp);
        when(projectService.createProject(eq("CollabHub Mobile"), eq("iOS & Android app"), any(User.class))).thenReturn(mobile);

        // Task creation stub
        when(taskService.createTask(anyString(), anyString(), any(LocalDate.class), any(Project.class), any(User.class)))
                .thenAnswer(invocation -> {
                    String title = invocation.getArgument(0);
                    String priority = invocation.getArgument(1);
                    LocalDate dueDate = invocation.getArgument(2);
                    Project project = invocation.getArgument(3);

                    Task task = new Task(title, priority, dueDate, project);
                    task.setId(UUID.randomUUID());
                    return task;
                });

        when(projectService.getAllProjects()).thenReturn(List.of(mvp, mobile));
        when(taskService.findAll()).thenReturn(List.of(
                new Task("seed-1", "HIGH", LocalDate.now().plusDays(1), mvp),
                new Task("seed-2", "LOW", LocalDate.now().plusDays(2), mobile)
        ));

        // Run startup
        startupRunner.run(mock(ApplicationArguments.class));

        // Verifications
        verify(taskService).setDispatcher(any(NotificationDispatcher.class));
        verify(userService, times(10)).createUser(anyString(), anyString(), anyString(), anyString());
        verify(projectService, times(2)).createProject(anyString(), anyString(), any(User.class));
        verify(projectService, times(3)).addMember(any(Project.class), any(User.class));
        verify(taskService, times(4)).createTask(anyString(), anyString(), any(LocalDate.class), any(Project.class), any(User.class));
        verify(taskService, times(4)).assignTask(any(Task.class), any(User.class), any(User.class));
        verify(taskService, times(2)).changeStatus(any(Task.class), anyString(), any(User.class));
        verify(userService, times(2)).findAll();
        verify(projectService).getAllProjects();
        verify(taskService).findAll();
    }

    @Test
    @DisplayName("should skip seed when users already exist")
    void shouldSkipSeedWhenUsersExist() throws Exception {
        User existing = user("Existing", "existing@collabhub.com", "MANAGER", "Password123!");
        when(environment.getActiveProfiles()).thenReturn(new String[0]);
        when(environment.getProperty("server.port", "8080")).thenReturn("8080");
        when(userService.findAll()).thenReturn(List.of(existing));

        startupRunner.run(mock(ApplicationArguments.class));

        verify(userService, times(1)).findAll();
        verify(taskService, never()).setDispatcher(any(NotificationDispatcher.class));
        verify(userService, never()).createUser(anyString(), anyString(), anyString(), anyString());
        verify(projectService, never()).createProject(anyString(), anyString(), any(User.class));
        verify(taskService, never()).createTask(anyString(), anyString(), any(LocalDate.class), any(Project.class), any(User.class));
    }

    private User user(String name, String email, String role, String passwordHash) {
        User user = new User(name, email, role, passwordHash);
        user.setPasswordHash("hashed");
        return user;
    }
}