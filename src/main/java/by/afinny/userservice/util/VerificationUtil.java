package by.afinny.userservice.util;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public final class VerificationUtil {

    private final static int SMS_BLOCK_EXPIRATION_IN_SECONDS = 30;

    private VerificationUtil() {
    }

    public static boolean isExpire(LocalDateTime codeExpirationTime) {
        return codeExpirationTime.isBefore(LocalDateTime.now());
    }

    public static boolean isCodeInvalid(String receivedCode, String sentCode) {
        return !receivedCode.equals(sentCode);
    }

    public static boolean isRemain(long leftSeconds) {
        return leftSeconds > 0;
    }

    public static long calculateRemainingSeconds(LocalDateTime blockExpiration) {
        return blockExpiration == null ? 0 : LocalDateTime.now().until(blockExpiration, ChronoUnit.SECONDS) + 1;
    }

    public static String createVerificationMessage(String verificationCode) {
        return "Ваш код подтверждения: " + verificationCode;
    }

    public static LocalDateTime generateUserBlockExpiration() {
        return LocalDateTime.now().plusMinutes(10);
    }

    public static LocalDateTime generateSmsBlockExpiration(Integer sendingCount) {
        int smsBlockExpirationInSeconds = (int) (Math.pow(2, sendingCount) * SMS_BLOCK_EXPIRATION_IN_SECONDS);
        return LocalDateTime.now().plusSeconds(smsBlockExpirationInSeconds);
    }

    public static String generateVerificationCode() {
        return new SecureRandom()
                .ints(0, 10)
                .limit(6)
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append).toString();
    }
}
