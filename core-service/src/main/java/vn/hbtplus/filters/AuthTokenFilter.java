package vn.hbtplus.filters;

import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerMapping;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.models.CustomAuthentication;
import vn.hbtplus.models.UserTokenDto;
import vn.hbtplus.models.beans.ServiceHeaderBean;
import vn.hbtplus.utils.JwtTokenUtils;
import vn.hbtplus.utils.Utils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
public class AuthTokenFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenUtils jwtTokenUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        //save request header to ThreadContext
        var serviceHeader = getServiceHeader(request);
        request.setAttribute(BaseConstants.COMMON.START_TIME, System.nanoTime());
        ThreadContext.put(BaseConstants.COMMON.SERVICE_HEADER, Utils.toJson(serviceHeader));
        ThreadContext.put(BaseConstants.COMMON.SERVICE_MESSAGE_ID, serviceHeader.getServiceMessageId());

        String token = getTokenString(request);
        if (token != null) {
            UserTokenDto userTokenDto = jwtTokenUtils.validateToken(token);
            if (userTokenDto == null) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                return;
            }
            Authentication auth = new CustomAuthentication(userTokenDto, null);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With, remember-me");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
        log.info("start api | " + request.getRequestURI());
        filterChain.doFilter(request, response);
        log.info("end api | " + request.getRequestURI());
    }

    private String getTokenString(HttpServletRequest request) {
        String token = request.getHeader("access-token");
        String bearerToken = token != null ? token : request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        } else {
            return null;
        }
    }


    private static ServiceHeaderBean getServiceHeader(HttpServletRequest httpRequest) {
        var servicePath =
                httpRequest.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        var xForwardedFor = httpRequest.getHeader("x-forwarded-for");
        String currentUser = null;
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) currentUser = String.valueOf(auth.getPrincipal());
        var clientMessageId = httpRequest.getHeader(BaseConstants.COMMON.CLIENT_MESSAGE_ID);
        clientMessageId = clientMessageId == null ? UUID.randomUUID().toString() : clientMessageId;

        return ServiceHeaderBean.builder()
                .messageTimeStamp(new Date())
                .servicePath(servicePath != null ? servicePath.toString() : null)
                .sourceAppIp(xForwardedFor == null ? httpRequest.getRemoteHost() : xForwardedFor)
                .destAppIp(httpRequest.getLocalAddr())
                .destAppPort(httpRequest.getLocalPort())
                .httpMethod(httpRequest.getMethod())
                .httpPath(getFullURL(httpRequest))
                .clientMessageId(clientMessageId)
                .authorization(httpRequest.getHeader(HttpHeaders.AUTHORIZATION))
                .authUser(currentUser)
                .build();
    }

    private static String getFullURL(HttpServletRequest request) {
        var requestURL = new StringBuilder(request.getRequestURL().toString());
        var queryString = request.getQueryString();
        return queryString == null
                ? requestURL.toString()
                : requestURL.append('?').append(queryString).toString();
    }
}

