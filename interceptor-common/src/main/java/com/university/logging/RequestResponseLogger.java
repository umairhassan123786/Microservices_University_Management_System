package com.university.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Component
public class RequestResponseLogger implements HandlerInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(RequestResponseLogger.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${spring.application.name:UNKNOWN_SERVICE}")
    private String applicationName;

    private String serviceName;

    @PostConstruct
    public void init() {
        this.serviceName = applicationName.toUpperCase();
        MDC.put("serviceName", this.serviceName);
        MDC.put("applicationName", applicationName);
        System.out.println("MDC configured for service: " + applicationName);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Don't wrap here - let the filter handle wrapping
        long startTime = System.currentTimeMillis();
        request.setAttribute("startTime", startTime);

        String traceId = MDC.get("traceId");
        if (traceId == null) {
            traceId = generateTraceId();
            MDC.put("traceId", traceId);
        }

        // Log basic request info without casting
        logger.info("=== {} REQUEST ===", serviceName);
        logger.info("TraceID: {}", traceId);
        logger.info("URL: {} {}", request.getMethod(), getRequestURL(request));
        logger.info("Query: {}", request.getQueryString());
        logger.info("Headers: {}", getHeaders(request));
        logger.info("Content Type: {}", request.getContentType());
        logger.info("Start Time: {}", startTime);

        // Only log request body if it's a wrapped request
        if (request instanceof ContentCachingRequestWrapper) {
            logger.info("Request Body: {}", getRequestBody((ContentCachingRequestWrapper) request));
        } else {
            logger.info("Request Body: [Not Available - Request not wrapped]");
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        long startTime = (Long) request.getAttribute("startTime");
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        String traceId = MDC.get("traceId");
        if (traceId == null) {
            traceId = "MISSING_TRACE_ID";
        }

        logger.info("=== {} RESPONSE ===", serviceName);
        logger.info("TraceID: {}", traceId);
        logger.info("Status: {}", response.getStatus());
        logger.info("Duration: {}ms", duration);

        // FIX: Check if response is wrapped before casting
        if (response instanceof ContentCachingResponseWrapper) {
            logger.info("Response Body: {}", getResponseBody((ContentCachingResponseWrapper) response));
            try {
                ((ContentCachingResponseWrapper) response).copyBodyToResponse();
            } catch (Exception e) {
                logger.warn("Failed to copy response body: {}", e.getMessage());
            }
        } else {
            logger.info("Response Body: [Not Available - Response not wrapped]");
        }

        if (ex != null) {
            logger.error("Exception: {}", ex.getMessage(), ex);
        }

        logger.info("=== END {} ===", serviceName);
        MDC.clear();
    }

    private String getRequestBody(ContentCachingRequestWrapper request) {
        try {
            byte[] content = request.getContentAsByteArray();
            if (content.length > 0) {
                String body = new String(content, request.getCharacterEncoding());
                try {
                    Object json = objectMapper.readValue(body, Object.class);
                    return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
                } catch (Exception e) {
                    return body;
                }
            }
        } catch (Exception e) {
            logger.warn("Could not read request body: {}", e.getMessage());
        }
        return "[Empty Body]";
    }

    private String getResponseBody(ContentCachingResponseWrapper response) {
        try {
            byte[] content = response.getContentAsByteArray();
            if (content.length > 0) {
                String body = new String(content, response.getCharacterEncoding());
                try {
                    Object json = objectMapper.readValue(body, Object.class);
                    return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
                } catch (Exception e) {
                    return body;
                }
            }
        } catch (Exception e) {
            logger.warn("Could not read response body: {}", e.getMessage());
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

    private String getRequestURL(HttpServletRequest request) {
        StringBuffer url = request.getRequestURL();
        String query = request.getQueryString();
        if (query != null) {
            url.append('?').append(query);
        }
        return url.toString();
    }

    private String generateTraceId() {
        return Long.toHexString(System.currentTimeMillis()) + Long.toHexString(System.nanoTime());
    }
}