package com.collabhub.mapper;

import com.collabhub.domain.Project;
import com.collabhub.domain.User;
import com.collabhub.dto.ProjectResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProjectMapper {

    @Mapping(target = "memberNames", source = "members", qualifiedByName = "membersToNames")
    @Mapping(target = "memberCount", source = "members", qualifiedByName = "membersToCount")
    ProjectResponse toResponse(Project project);

    // Named helper — converts Set<User> to List<String> of names
    @Named("membersToNames")
    default List<String> membersToNames(Set<User> members) {
        if (members == null) {
            return List.of();
        }
        return members.stream().map(User::getName).sorted().toList();
    }

    // Named helper — converts Set<User> to count
    @Named("membersToCount")
    default int membersToCount(Set<User> members) {
        return members == null ? 0 : members.size();
    }
}
