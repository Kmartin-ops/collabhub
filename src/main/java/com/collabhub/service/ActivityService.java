package com.collabhub.service;

import com.collabhub.domain.Activity;
import com.collabhub.domain.Project;
import com.collabhub.dto.ActivityResponse;
import com.collabhub.repository.ActivityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ActivityService {

    private final ActivityRepository activityRepository;

    public ActivityService(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    @Transactional
    public void log(String action, String actorName, String entityType,
                    UUID entityId, String entityName, String detail, Project project) {
        activityRepository.save(
            new Activity(action, actorName, entityType, entityId, entityName, detail, project)
        );
    }

    @Transactional(readOnly = true)
    public Page<ActivityResponse> getProjectActivity(UUID projectId, Pageable pageable) {
        return activityRepository
                .findByProjectIdOrderByCreatedAtDesc(projectId, pageable)
                .map(this::toResponse);
    }

    private ActivityResponse toResponse(Activity a) {
        return new ActivityResponse(
                a.getId(),
                a.getAction(),
                a.getActorName(),
                a.getEntityType(),
                a.getEntityId(),
                a.getEntityName(),
                a.getDetail(),
                a.getCreatedAt()
        );
    }
}
