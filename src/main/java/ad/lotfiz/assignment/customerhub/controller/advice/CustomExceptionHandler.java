package ad.lotfiz.assignment.customerhub.controller.advice;


import ad.lotfiz.assignment.customerhub.exception.CustomerNotFoundException;
import ad.lotfiz.assignment.customerhub.exception.FieldNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.customerhub.api.v1.model.DuplicateError;
import nl.customerhub.api.v1.model.ErrorFieldIsInvalid;
import nl.customerhub.api.v1.model.ErrorFieldIsRequired;
import nl.customerhub.api.v1.model.NotFoundError;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {


    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<DuplicateError> handleException(DataIntegrityViolationException ex) {
        log.debug("customer service encounter an exception", ex);

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new DuplicateError().message(ex.getMessage()));
    }

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<NotFoundError> handleException(CustomerNotFoundException ex) {
        log.debug("customer service encounter an exception", ex);

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new NotFoundError().message(ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorFieldIsInvalid> handleException(IllegalArgumentException ex) {
        log.debug("customer service encounter an exception", ex);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorFieldIsInvalid().message(ex.getMessage()));
    }

    @ExceptionHandler(FieldNotFoundException.class)
    public ResponseEntity<ErrorFieldIsRequired> handleException(FieldNotFoundException ex) {
        log.debug("customer service encounter an exception", ex);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorFieldIsRequired().message(ex.getMessage()));
    }

}
