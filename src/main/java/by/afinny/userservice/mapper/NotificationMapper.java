package by.afinny.userservice.mapper;

import by.afinny.userservice.dto.NotificationDto;
import by.afinny.userservice.entity.UserProfile;
import org.mapstruct.Mapper;

@Mapper
public interface NotificationMapper {

    NotificationDto userProfileToNotificationDto(UserProfile userProfile);
}

