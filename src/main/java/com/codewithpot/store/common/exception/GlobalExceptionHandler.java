package com.codewithpot.store.common.exception;

import com.codewithpot.store.common.dto.ErrorResponse;
import com.codewithpot.store.common.exception.base.AllException;
import com.codewithpot.store.common.exception.base.InvalidInputException;
import com.codewithpot.store.common.utils.MessageUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.naming.AuthenticationException;
import java.util.List;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @Autowired
    private MessageSource messageSource;

    @ExceptionHandler(value = {InvalidInputException.class})
    public ResponseEntity<ErrorResponse> handleInvalidInputException(InvalidInputException except){
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(buildErrorResponse(
                        except.getErrorCode(),
                        except.getMessageProperties(),
                        except.getFieldNameList()
                    )
                );
    }
    @ExceptionHandler(AllException.class)
    public ResponseEntity<ErrorResponse> handleAllException(AllException exc){
        ErrorResponse err = new ErrorResponse();
        err.setMessage(exc.getMessage());
        return ResponseEntity
                .status(exc.getStatus())
                .body(err);
    }

    private ErrorResponse buildErrorResponse(String errorCode, String messageCode, List<String> params) {

        log.error("ErrorCode : {}", errorCode);
        ErrorResponse errorResponse = new ErrorResponse();

        String message = MessageUtils.getMessage(messageSource, messageCode);

        for (int i = 0; i < params.size(); i++) {
            message = message.replace("{" + i + "}", params.get(i));
        }

        errorResponse.setMessage(message);
        return errorResponse;
    }

}
