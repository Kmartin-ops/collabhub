package com.collabhub.mapper;

import com.collabhub.domain.Task;
import com.collabhub.dto.TaskResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TaskMapper {

    @Mapping(target = "projectId",    source = "project.id")
    @Mapping(target = "projectName",  source = "project.name")
    @Mapping(target = "assigneeId",   source = "assignee.id")
    @Mapping(target = "assigneeName", source = "assignee.name")
    TaskResponse toResponse(Task task);
}