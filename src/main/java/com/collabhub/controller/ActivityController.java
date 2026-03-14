package com.collabhub.controller;

import com.collabhub.dto.ActivityResponse;
import com.collabhub.service.ActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/projects/{projectId}/activity")
@Tag(name = "Activity", description = "Project activity feed")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @GetMapping
    @Operation(summary = "Get paginated activity feed for a project")
    public Page<ActivityResponse> getActivity(
            @PathVariable UUID projectId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return activityService.getProjectActivity(projectId, pageable);
    }
}
