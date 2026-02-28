package com.collabhub.seed;

import com.collabhub.domain.*;
import com.collabhub.service.ProjectService;
import com.collabhub.service.TaskService;

import java.time.LocalDate;
import java.util.List;

public class DataSeeder {

    private final ProjectService projectService;
    private final TaskService taskService;

    public DataSeeder(ProjectService projectService, TaskService taskService) {
        this.projectService = projectService;
        this.taskService = taskService;
    }

    public void seed() {
        System.out.println("\n[Seeder] Seeding CollabHub with realistic data...\n");

        // ── 10 Users ──────────────────────────────────────
        User alice   = new User("Alice",   "alice@collabhub.com",   "MANAGER");
        User bob     = new User("Bob",     "bob@collabhub.com",     "DEVELOPER");
        User carol   = new User("Carol",   "carol@collabhub.com",   "DEVELOPER");
        User dave    = new User("Dave",    "dave@collabhub.com",     "DEVELOPER");
        User eve     = new User("Eve",     "eve@collabhub.com",      "DEVELOPER");
        User frank   = new User("Frank",   "frank@collabhub.com",    "MANAGER");
        User grace   = new User("Grace",   "grace@collabhub.com",    "DEVELOPER");
        User henry   = new User("Henry",   "henry@collabhub.com",    "DEVELOPER");
        User iris    = new User("Iris",    "iris@collabhub.com",     "DEVELOPER");
        User james   = new User("James",   "james@collabhub.com",    "ADMIN");

        // ── Project 1 — CollabHub MVP ─────────────────────
        Project mvp = projectService.createProject(
                "CollabHub MVP", "Core platform features", alice);
        projectService.addMember(mvp, bob);
        projectService.addMember(mvp, carol);
        projectService.addMember(mvp, dave);
        projectService.addMember(mvp, james);

        // ── Project 2 — Mobile App ────────────────────────
        Project mobile = projectService.createProject(
                "CollabHub Mobile", "iOS & Android app", frank);
        projectService.addMember(mobile, eve);
        projectService.addMember(mobile, grace);
        projectService.addMember(mobile, henry);

        // ── Project 3 — DevOps & Infrastructure ───────────
        Project infra = projectService.createProject(
                "CollabHub Infrastructure", "CI/CD and cloud setup", james);
        projectService.addMember(infra, iris);
        projectService.addMember(infra, bob);
        projectService.addMember(infra, dave);

        // ── 50 Tasks spread across projects ───────────────
        seedMvpTasks(mvp, alice, bob, carol, dave);
        seedMobileTasks(mobile, frank, eve, grace, henry);
        seedInfraTasks(infra, james, iris, bob, dave);

        System.out.println("\n[Seeder] Done — 3 projects, 10 users, 50 tasks seeded.\n");
    }

    private void seedMvpTasks(Project p, User manager,
                              User dev1, User dev2, User dev3) {
        List<Object[]> tasks = List.of(
                new Object[]{"Design domain model",        "HIGH",   -3, dev1, "DONE"},
                new Object[]{"Set up Spring Boot",         "HIGH",   -2, dev1, "DONE"},
                new Object[]{"Build user registration",    "HIGH",   -1, dev2, "DONE"},
                new Object[]{"Implement JWT auth",         "HIGH",    1, dev2, "IN_PROGRESS"},
                new Object[]{"Create project endpoints",   "HIGH",    2, dev1, "IN_PROGRESS"},
                new Object[]{"Create task endpoints",      "HIGH",    3, dev3, "IN_PROGRESS"},
                new Object[]{"Add input validation",       "MEDIUM",  4, dev2, "BACKLOG"},
                new Object[]{"Write integration tests",    "MEDIUM",  5, dev3, "BACKLOG"},
                new Object[]{"Set up Swagger docs",        "MEDIUM",  6, dev1, "BACKLOG"},
                new Object[]{"Configure PostgreSQL",       "HIGH",    7, dev3, "BACKLOG"},
                new Object[]{"Add Flyway migrations",      "MEDIUM",  8, dev2, "BACKLOG"},
                new Object[]{"Implement role-based auth",  "HIGH",    9, dev1, "BACKLOG"},
                new Object[]{"Add pagination support",     "MEDIUM", 10, dev3, "BACKLOG"},
                new Object[]{"Performance testing",        "LOW",    14, manager,"BACKLOG"},
                new Object[]{"Security audit",             "HIGH",   -1, manager,"BACKLOG"},
                new Object[]{"Fix login timeout bug",      "HIGH",   -2, dev2, "IN_PROGRESS"},
                new Object[]{"Update API documentation",   "LOW",    12, dev1, "BACKLOG"}
        );
        createTaskBatch(p, manager, tasks);
    }

    private void seedMobileTasks(Project p, User manager,
                                 User dev1, User dev2, User dev3) {
        List<Object[]> tasks = List.of(
                new Object[]{"Set up React Native",        "HIGH",   -1, dev1, "DONE"},
                new Object[]{"Build login screen",         "HIGH",    1, dev1, "IN_PROGRESS"},
                new Object[]{"Build project list screen",  "HIGH",    3, dev2, "BACKLOG"},
                new Object[]{"Build task board screen",    "HIGH",    5, dev3, "BACKLOG"},
                new Object[]{"Integrate push notifications","MEDIUM", 7, dev1, "BACKLOG"},
                new Object[]{"Offline mode support",       "MEDIUM", 10, dev2, "BACKLOG"},
                new Object[]{"App store submission",       "LOW",    20, manager,"BACKLOG"},
                new Object[]{"Design system setup",        "MEDIUM",  2, dev3, "IN_PROGRESS"},
                new Object[]{"Dark mode support",          "LOW",    15, dev1, "BACKLOG"},
                new Object[]{"Performance optimisation",   "MEDIUM", 12, dev2, "BACKLOG"},
                new Object[]{"Accessibility audit",        "MEDIUM", 18, dev3, "BACKLOG"},
                new Object[]{"Beta testing",               "HIGH",   -1, manager,"BACKLOG"},
                new Object[]{"Crash reporting setup",      "MEDIUM",  6, dev1, "BACKLOG"},
                new Object[]{"Deep linking support",       "LOW",    14, dev2, "BACKLOG"},
                new Object[]{"Fix Android back button",    "HIGH",   -2, dev3, "IN_PROGRESS"},
                new Object[]{"Tablet layout support",      "LOW",    25, dev1, "BACKLOG"},
                new Object[]{"API error handling",         "HIGH",    4, dev2, "BACKLOG"}
        );
        createTaskBatch(p, manager, tasks);
    }

    private void seedInfraTasks(Project p, User manager,
                                User dev1, User dev2, User dev3) {
        List<Object[]> tasks = List.of(
                new Object[]{"Set up GitHub Actions CI",   "HIGH",   -3, dev1, "DONE"},
                new Object[]{"Dockerise all services",     "HIGH",   -1, dev2, "IN_PROGRESS"},
                new Object[]{"Set up staging environment", "HIGH",    2, dev1, "BACKLOG"},
                new Object[]{"Configure AWS ECS",          "HIGH",    5, dev3, "BACKLOG"},
                new Object[]{"Set up RDS PostgreSQL",      "HIGH",    4, dev2, "BACKLOG"},
                new Object[]{"Configure auto-scaling",     "MEDIUM",  8, dev1, "BACKLOG"},
                new Object[]{"Set up Prometheus monitoring","MEDIUM", 10, dev3, "BACKLOG"},
                new Object[]{"Configure Grafana dashboards","MEDIUM", 12, dev2, "BACKLOG"},
                new Object[]{"Set up log aggregation",     "MEDIUM",  9, dev1, "BACKLOG"},
                new Object[]{"SSL certificate setup",      "HIGH",    3, dev3, "BACKLOG"},
                new Object[]{"Secrets Manager integration","HIGH",    6, dev2, "BACKLOG"},
                new Object[]{"Disaster recovery plan",     "MEDIUM", 15, manager,"BACKLOG"},
                new Object[]{"Cost optimisation review",   "LOW",    20, manager,"BACKLOG"},
                new Object[]{"Load testing infrastructure","MEDIUM",  7, dev1, "BACKLOG"},
                new Object[]{"Fix deployment pipeline",    "HIGH",   -1, dev3, "IN_PROGRESS"},
                new Object[]{"Update runbook docs",        "LOW",    14, dev2, "BACKLOG"}
        );
        createTaskBatch(p, manager, tasks);
    }

    private void createTaskBatch(Project project, User creator, List<Object[]> taskData) {
        for (Object[] data : taskData) {
            String title    = (String) data[0];
            String priority = (String) data[1];
            int    daysOffset = (int) data[2];
            User   assignee = (User) data[3];
            String status   = (String) data[4];

            Task task = taskService.createTask(
                    title, priority,
                    LocalDate.now().plusDays(daysOffset),
                    project, creator
            );
            taskService.assignTask(task, assignee, creator);

            if (!"BACKLOG".equals(status)) {
                taskService.changeStatus(task, status, assignee);
            }
        }
    }
}