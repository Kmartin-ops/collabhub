package com.collabhub.mapper;

import com.collabhub.domain.User;
import com.collabhub.dto.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
// componentModel = SPRING means MapStruct registers this as a Spring bean
// so it can be @Autowired / constructor-injected anywhere
public interface UserMapper {

    // Fields match by name — MapStruct maps automatically:
    // User.id → UserResponse.id
    // User.name → UserResponse.name
    // User.email → UserResponse.email
    // User.role → UserResponse.role
    // User.createdAt → UserResponse.createdAt
    UserResponse toResponse(User user);
}
