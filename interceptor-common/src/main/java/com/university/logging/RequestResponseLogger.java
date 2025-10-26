package com.university.logging;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Component
public class RequestResponseLogger implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(RequestResponseLogger.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Wrap request to cache body
        if (!(request instanceof ContentCachingRequestWrapper)) {
            request = new ContentCachingRequestWrapper(request);
        }

        long startTime = System.currentTimeMillis();
        request.setAttribute("startTime", startTime);

        String traceId = MDC.get("traceId");
        String serviceName = getServiceName();

        logger.info("=== {} REQUEST ===", serviceName);
        logger.info("TraceID: {}", traceId);
        logger.info("URL: {} {}", request.getMethod(), request.getRequestURL());
        logger.info("Query: {}", request.getQueryString());
        logger.info("Headers: {}", getHeaders(request));
        logger.info("Request Body: {}", getRequestBody((ContentCachingRequestWrapper) request));
        logger.info("Start Time: {}", startTime);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        long startTime = (Long) request.getAttribute("startTime");
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        String traceId = MDC.get("traceId");
        String serviceName = getServiceName();

        logger.info("=== {} RESPONSE ===", serviceName);
        logger.info("TraceID: {}", traceId);
        logger.info("Status: {}", response.getStatus());
        logger.info(" Duration: {}ms", duration);
        logger.info("Response Body: {}", getResponseBody(response));

        if (ex != null) {
            logger.error("Exception: {}", ex.getMessage());
        }

        logger.info("=== END {} ===", serviceName);
    }

    private String getRequestBody(ContentCachingRequestWrapper request) {
        try {
            byte[] content = request.getContentAsByteArray();
            if (content.length > 0) {
                return new String(content, request.getCharacterEncoding());
            }
        } catch (Exception e) {
            logger.warn("Could not read request body: {}", e.getMessage());
        }
        return "[Empty Body]";
    }

    private String getResponseBody(HttpServletResponse response) {
        if (response instanceof ContentCachingResponseWrapper) {
            ContentCachingResponseWrapper wrapper = (ContentCachingResponseWrapper) response;
            try {
                byte[] content = wrapper.getContentAsByteArray();
                if (content.length > 0) {
                    // Try to parse as JSON for pretty printing
                    String body = new String(content, response.getCharacterEncoding());
                    try {
                        Object json = objectMapper.readValue(body, Object.class);
                        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
                    } catch (Exception e) {
                        return body; // Return as plain text if not JSON
                    }
                }
            } catch (Exception e) {
                logger.warn("Could not read response body: {}", e.getMessage());
            }
        }
        return "[Empty Body]";
    }

    private Map<String, String> getHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName, request.getHeader(headerName));
        }
        return headers;
    }

    private String getServiceName() {
        String appName = MDC.get("spring.application.name");
        if (appName != null) {
            return appName.toUpperCase();
        }
        return "UNKNOWN_SERVICE";
    }
}