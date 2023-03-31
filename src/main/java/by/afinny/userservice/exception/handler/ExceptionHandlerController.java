package by.afinny.userservice.exception.handler;

import by.afinny.userservice.exception.AccountExistException;
import by.afinny.userservice.exception.BadCredentialsException;
import by.afinny.userservice.exception.BlockedReceiverException;
import by.afinny.userservice.exception.DocumentsAlreadyExistException;
import by.afinny.userservice.exception.EntityNotFoundException;
import by.afinny.userservice.exception.IncorrectParameterException;
import by.afinny.userservice.exception.RegistrationException;
import by.afinny.userservice.exception.VerificationCodeException;
import by.afinny.userservice.exception.WrongCredentialsException;
import by.afinny.userservice.exception.dto.AccountExistErrorDto;
import by.afinny.userservice.exception.dto.BlockedReceiverErrorDto;
import by.afinny.userservice.exception.dto.ErrorDto;
import by.afinny.userservice.exception.dto.VerificationErrorDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.ConstraintViolationException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class ExceptionHandlerController extends ResponseEntityExceptionHandler {

    @ExceptionHandler(AccountExistException.class)
    public ResponseEntity<AccountExistErrorDto> accountExistExceptionHandler(AccountExistException e) {
        log.error("Account already exist. " + e.getErrorMessage());
        AccountExistErrorDto body = new AccountExistErrorDto(e.getStatus(), e.getErrorCode(), e.getErrorMessage());
        return createResponseEntity(getStatus(e.getErrorCode()), body);
    }

    @ExceptionHandler(RegistrationException.class)
    public ResponseEntity<ErrorDto> registrationExceptionHandler(RegistrationException e) {
        log.error("Registration failure. " + e.getErrorMessage());
        ErrorDto body = new ErrorDto(e.getErrorCode(), e.getErrorMessage());
        return createResponseEntity(getStatus(e.getErrorCode()), body);
    }

    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(VerificationCodeException.class)
    protected ResponseEntity<VerificationErrorDto> verificationCodeExceptionHandler(VerificationCodeException e) {
        log.error("Verification failure. " + e.getErrorMessage());
        VerificationErrorDto body = new VerificationErrorDto(e.getErrorMessage());
        return createResponseEntity(getStatus(e.getErrorCode()), body);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorDto> badCredentialsExceptionHandler(BadCredentialsException e) {
        log.error("Bad credentials. " + e.getErrorMessage());
        ErrorDto body = new ErrorDto(e.getErrorCode(), e.getErrorMessage());
        return createResponseEntity(getStatus(e.getErrorCode()), body);
    }

    @ResponseStatus(code = HttpStatus.NOT_ACCEPTABLE)
    @ExceptionHandler(BlockedReceiverException.class)
    public ResponseEntity<BlockedReceiverErrorDto> blockedReceiverExceptionHandler(BlockedReceiverException e) {
        log.error("Receiver is blocked. " + e.getMessage());
        BlockedReceiverErrorDto body = new BlockedReceiverErrorDto(e.getBlockSeconds());
        return createResponseEntity(HttpStatus.NOT_ACCEPTABLE, body);
    }

    @ResponseStatus(code = HttpStatus.CONFLICT)
    @ExceptionHandler(DocumentsAlreadyExistException.class)
    public ResponseEntity<ErrorDto> documentsAlreadyExistsExceptionHandler(DocumentsAlreadyExistException e) {
        log.error(String.format("Verification documents have already been uploaded by the client(clientId=%s)",
                        e.getClientId()));
        ErrorDto body = new ErrorDto(getErrorCode(HttpStatus.CONFLICT), e.getMessage());
        return createResponseEntity(HttpStatus.CONFLICT, body);
    }

    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IncorrectParameterException.class)
    public ResponseEntity<ErrorDto> incorrectParameterExceptionHandler(IncorrectParameterException e) {
        log.error("Verification documents are null or empty");
        ErrorDto body = new ErrorDto(getErrorCode(HttpStatus.BAD_REQUEST), e.getMessage());
        return createResponseEntity(HttpStatus.BAD_REQUEST, body);
    }

    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<String> maxUploadSizeExceededExceptionHandler(MaxUploadSizeExceededException e) {
        log.error("Max upload size exceeded. " + e.getMessage());
        return createResponseEntity(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<String> test(ConstraintViolationException e) {
        log.error("Invalid value. " + e.getMessage());
        return createResponseEntity(HttpStatus.BAD_REQUEST, "Invalid value");
    }

    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDto> illegalArgumentException(IllegalArgumentException e) {
        log.error("Wrong argument. " + e.getMessage());
        ErrorDto body = new ErrorDto("400", e.getMessage());
        return createResponseEntity(getStatus(body.getErrorCode()), body);
    }

    @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Void> exceptionHandler(Exception e) {
        log.error("Internal server error. " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(WrongCredentialsException.class)
    public ResponseEntity<Void> wrongCredentialsExceptionHandler(Exception e) {
        log.error("Wrong credentials. " + e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    private <T> ResponseEntity<T> createResponseEntity(HttpStatus status, T body) {
        return ResponseEntity.status(status)
                .header("Content-Type", "application/json")
                .body(body);
    }

    private HttpStatus getStatus(String errorCode) {
        return HttpStatus.valueOf(Integer.parseInt(errorCode));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> userProfileException(EntityNotFoundException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatus status,
                                                                  WebRequest request) {
        log.error("Invalid values" + ex);
        Map<String, Object> responseBody = new LinkedHashMap<>();
        List<String> listError = ex.getBindingResult().getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());
        responseBody.put("timestamp", LocalDateTime.now());
        responseBody.put("status", status.value());
        responseBody.put("errors", listError);
        return new ResponseEntity<>(responseBody, headers, status);
    }



    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorDto> handleDataIntegrityViolationException(SQLException e) {
        log.error("Repeating field. " + e.getMessage());
        String message = e.getMessage();
        Pattern pattern = Pattern.compile("\"(.*?)=(.*?)\"");
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            String fieldName = matcher.group(1);
            String fieldValue = matcher.group(2);
            ErrorDto errorDto = new ErrorDto("400", String.format("Duplicate value in a field: %s = %s", fieldName, fieldValue));
            return new ResponseEntity<>(errorDto, HttpStatus.BAD_REQUEST);
        }
        ErrorDto errorDto = new ErrorDto("400", e.getMessage());
        return new ResponseEntity<>(errorDto, HttpStatus.BAD_REQUEST);
    }

    private String getErrorCode(HttpStatus httpStatus) {
        return String.valueOf(httpStatus.value());
    }
}

