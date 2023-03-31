package by.afinny.userservice.service.impl;

import by.afinny.userservice.service.SenderService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class SenderServiceImpl implements SenderService {

    @Override
    public void sendMessageToMobilePhone(String mobilePhone, String message) {

    }
}

