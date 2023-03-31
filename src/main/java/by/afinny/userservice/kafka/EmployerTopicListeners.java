package by.afinny.userservice.kafka;

import by.afinny.userservice.dto.kafka.EmployerEvent;
import by.afinny.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "kafka.enabled")
@Slf4j
public class EmployerTopicListeners {

    private final UserService userService;

    @KafkaListener(
        topics = "${kafka.topics.user-service-listener.path}",
        groupId = "user-service",
        containerFactory = "listenerFactory")
    public void onRequestUpdateEmployerIdEvent(Message<EmployerEvent> message) {
        EmployerEvent event = message.getPayload();
        log.info("Processing event: clientId = " + event.getClientId() + ", employerId = " + event.getEmployerIdentificationNumber());
        userService.modifyEmployerId(event.getClientId(), event.getEmployerIdentificationNumber());
    }
}
