package com.pccw.ruby.common.filter;

import com.pccw.ruby.common.service.LoggingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Slf4j
public class CommonLoggingFilter extends OncePerRequestFilter {

    protected List<String> urlWhitelist;

    protected LoggingService loggingService;

    public CommonLoggingFilter(List<String> urlWhitelist, LoggingService loggingService) {
        this.urlWhitelist = urlWhitelist;
        this.loggingService = loggingService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();

        if (isInWhitelist(path) || isAsyncDispatch(request)) {
            filterChain.doFilter(request, response);
        } else {
            doFilterWrapped(wrapRequest(request), wrapResponse(response), filterChain);
        }
    }

    protected boolean isInWhitelist(String path) {
        return urlWhitelist.contains(path);
    }

    protected void doFilterWrapped(
            ContentCachingRequestWrapper request,
            ContentCachingResponseWrapper response,
            FilterChain filterChain)
            throws ServletException, IOException {
        try {
            beforeRequest(request, response);
            filterChain.doFilter(request, response);
        } finally {
            afterRequest(request, response);
            response.copyBodyToResponse();
        }
    }

    protected void beforeRequest(
            ContentCachingRequestWrapper request, ContentCachingResponseWrapper response) {}

    protected void afterRequest(
            ContentCachingRequestWrapper request, ContentCachingResponseWrapper response) {
        if (log.isInfoEnabled()) {
            loggingService.logRequest(request);
            loggingService.logResponse(request, response);
        }
    }

    protected static ContentCachingRequestWrapper wrapRequest(HttpServletRequest request) {
        if (request instanceof ContentCachingRequestWrapper) {
            return (ContentCachingRequestWrapper) request;
        } else {
            return new ContentCachingRequestWrapper(request);
        }
    }

    protected static ContentCachingResponseWrapper wrapResponse(HttpServletResponse response) {
        if (response instanceof ContentCachingResponseWrapper) {
            return (ContentCachingResponseWrapper) response;
        } else {
            return new ContentCachingResponseWrapper(response);
        }
    }
}
