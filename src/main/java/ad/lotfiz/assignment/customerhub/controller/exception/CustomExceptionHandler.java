package ad.lotfiz.assignment.customerhub.controller.exception;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.customerhub.api.v1.model.DuplicateError;
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
        log.debug("Article service encounter an exception", ex);

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new DuplicateError().message(ex.getMessage()));
    }

}
