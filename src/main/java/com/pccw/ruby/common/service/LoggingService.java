package com.pccw.ruby.common.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.stream.Stream;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
public class LoggingService {

    protected static final List<MediaType> VISIBLE_TYPES =
            Arrays.asList(
                    MediaType.valueOf("text/*"),
                    MediaType.APPLICATION_FORM_URLENCODED,
                    MediaType.APPLICATION_JSON,
                    MediaType.APPLICATION_XML,
                    MediaType.valueOf("application/*+json"),
                    MediaType.valueOf("application/*+xml"),
                    MediaType.MULTIPART_FORM_DATA);

    protected static final List<String> WHITELIST_HEADER = Arrays.asList("User-Agent");

    public void logRequest(ContentCachingRequestWrapper request) {
        Map<String, String> headers = buildHeadersMap(request);
        Map<String, String> parameters = buildParametersMap(request);
        log.info(
                "REQUEST {}, {}, {}, {}",
                kv("method", request.getMethod()),
                kv("path", request.getRequestURI()),
                kv("headers", headers),
                kv("parameters", parameters));

        if (log.isDebugEnabled()) {
            log.debug(
                    "REQUEST {}",
                    kv(
                            "body",
                            buildPayload(
                                    request.getContentAsByteArray(),
                                    request.getContentType(),
                                    request.getCharacterEncoding())));
        }
    }

    public void logResponse(
            ContentCachingRequestWrapper request, ContentCachingResponseWrapper response) {
        Map<String, String> headers = buildHeadersMap(response);

        log.info(
                "RESPONSE {}, {}, {}",
                kv("method", request.getMethod()),
                kv("path", request.getRequestURI()),
                kv("headers", headers));

        if (log.isDebugEnabled()) {
            log.debug(
                    "RESPONSE {}",
                    kv(
                            "body",
                            buildPayload(
                                    response.getContentAsByteArray(),
                                    response.getContentType(),
                                    response.getCharacterEncoding())));
        }
    }

    private String buildPayload(byte[] content, String contentType, String contentEncoding) {
        StringBuilder contentBuilder = new StringBuilder();
        if (content != null && content.length > 0) {
            MediaType mediaType = MediaType.valueOf(contentType);
            boolean visible =
                    VISIBLE_TYPES.stream().anyMatch(visibleType -> visibleType.includes(mediaType));
            if (visible) {
                try {
                    String contentString = new String(content, contentEncoding);
                    Stream.of(contentString.split("\r\n|\r|\n")).forEach(contentBuilder::append);
                } catch (UnsupportedEncodingException e) {
                    log.error("Unsupported encoding exception", e);
                }
            }
        }
        return contentBuilder.toString().replaceAll("\"", "");
    }

    private Map<String, String> buildHeadersMap(HttpServletRequest request) {
        Map<String, String> map = new HashMap<>();

        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = headerNames.nextElement();
            boolean isInWhitelist =
                    WHITELIST_HEADER.stream()
                            .anyMatch(headerName -> headerName.equalsIgnoreCase(key));
            if (isInWhitelist) {
                String value = request.getHeader(key);
                map.put(key, value);
            }
        }

        return map;
    }

    private Map<String, String> buildHeadersMap(HttpServletResponse response) {
        Map<String, String> map = new HashMap<>();

        Collection<String> headerNames = response.getHeaderNames();
        for (String header : headerNames) {
            boolean isInWhitelist =
                    WHITELIST_HEADER.stream()
                            .anyMatch(headerName -> headerName.equalsIgnoreCase(header));
            if (isInWhitelist) {
                map.put(header, response.getHeader(header));
            }
        }

        return map;
    }

    private Map<String, String> buildParametersMap(HttpServletRequest httpServletRequest) {
        Map<String, String> resultMap = new HashMap<>();
        Enumeration<String> parameterNames = httpServletRequest.getParameterNames();

        while (parameterNames.hasMoreElements()) {
            String key = parameterNames.nextElement();
            String value = httpServletRequest.getParameter(key);
            resultMap.put(key, value);
        }

        return resultMap;
    }
}
