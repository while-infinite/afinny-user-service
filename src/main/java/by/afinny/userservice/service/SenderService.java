package by.afinny.userservice.service;

import org.springframework.scheduling.annotation.Async;

public interface SenderService {

    @Async
    void sendMessageToMobilePhone(String mobilePhone, String message);
}
