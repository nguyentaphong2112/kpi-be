package vn.kpi.configs.handler;

import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import vn.kpi.configs.AppConfigResource;
import vn.kpi.constants.BaseConstants;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.exceptions.ErrorImportException;
import vn.kpi.models.BaseResponse;
import vn.kpi.models.ImportResultDTO;
import vn.kpi.models.beans.ServiceHeaderBean;
import vn.kpi.utils.I18n;
import vn.kpi.utils.ResponseUtils;
import vn.kpi.utils.Utils;

import java.util.Arrays;

@RestControllerAdvice
@Slf4j
public class BaseExceptionHandler extends ResponseEntityExceptionHandler {
    @Autowired
    AppConfigResource appConfigResource;


    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity handleExceptions(ExpiredJwtException exception, WebRequest webRequest) {
        return ResponseUtils.error(HttpStatus.UNAUTHORIZED, "token is expired");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity handleExceptions(AccessDeniedException exception, WebRequest webRequest) {
        return ResponseUtils.error(HttpStatus.FORBIDDEN, exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity handleExceptions(Exception exception, WebRequest webRequest) {
        log.error("handleExceptions", exception);
        return ResponseUtils.error(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
    }

    @ExceptionHandler(HttpClientErrorException.Unauthorized.class)
    public ResponseEntity handleExceptions(HttpClientErrorException.Unauthorized exception, WebRequest webRequest) {
        return ResponseUtils.error(HttpStatus.UNAUTHORIZED, exception.getMessage());
    }


    @ExceptionHandler(BaseAppException.class)
    public ResponseEntity handleExceptions(BaseAppException exception, WebRequest webRequest) {
        log.error("handleExceptions", exception);
        return ResponseUtils.error(exception.getStatus(), exception.getErrorCode(), exception.getMessage());
    }

    @ExceptionHandler(ErrorImportException.class)
    public ResponseEntity handleExceptions(ErrorImportException exception, WebRequest webRequest) {
        log.error("handleException : ErrorImportException");
        ImportResultDTO importResultBean = new ImportResultDTO();
        try {
            importResultBean.setErrorFile(exception.getImportExcel().getFileErrorDescription(exception.getFileImport(), Utils.getExportFolder()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        importResultBean.setErrorList(exception.getImportExcel().getErrorList());
        BaseResponse baseResponse = new BaseResponse<ImportResultDTO>(extractServiceHeader().getClientMessageId())
                .success(importResultBean).status(BaseConstants.RESPONSE_STATUS.IMPORT_ERROR);
        baseResponse.setMessage(I18n.getMessage(exception.getImportExcel().getImportResult().getMessageKey()));
        ResponseEntity responseEntity = new ResponseEntity<>(baseResponse,HttpStatus.BAD_REQUEST);
        return responseEntity;
    }

    public ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        log.error("handleHttpMessageNotReadable", ex);
        return ResponseUtils.badRequest("handleHttpMessageNotReadable", ex.getMessage());
    }

    public ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        log.error("handleMethodArgumentNotValid", ex);
        StringBuilder errorMessage = new StringBuilder("Validation error: ");
        ex.getBindingResult().getAllErrors().forEach(error -> {
            Arrays.stream(error.getArguments()).forEach(arg ->{
                DefaultMessageSourceResolvable temp = (DefaultMessageSourceResolvable) arg;
                errorMessage.append(Utils.join("," ,temp.getCodes()));
            } );
            errorMessage.append(" ").append(error.getDefaultMessage()).append(", ");
        });
        return ResponseUtils.badRequest("handleMethodArgumentNotValid", errorMessage.toString());
    }
    public ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        log.error("handleMissingServletRequestParameter", ex);
        return ResponseUtils.badRequest("handleMissingServletRequestParameter", ex.getMessage());
    }
    public ResponseEntity<Object> handleAsyncRequestTimeoutException(AsyncRequestTimeoutException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        log.error("handleAsyncRequestTimeoutException", ex);
        return ResponseUtils.error(HttpStatus.SERVICE_UNAVAILABLE, "ASYNC_REQUEST_TIMEOUT", ex.getMessage());
    }

    public ResponseEntity<Object> handleBindException(BindException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        log.error("handleBindException", ex);
        return ResponseUtils.error(HttpStatus.BAD_REQUEST, "BIND_EXCEPTION", ex.getMessage());
    }
    public ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        log.error("handleBindException", ex);
        return ResponseUtils.error(HttpStatus.BAD_REQUEST, "TYPE_MISMATCH", ex.getMessage());
    }

    private static ServiceHeaderBean extractServiceHeader() {
        try {
            String str = ThreadContext.get("serviceHeader");
            if (str == null) {
                return new ServiceHeaderBean();
            }
            return Utils.fromJson(str, ServiceHeaderBean.class);
        } catch (Exception e) {
            return new ServiceHeaderBean();
        }
    }
}
