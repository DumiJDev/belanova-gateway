package io.github.dumijdev.belanova.gateway.common.model;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class RequestLogEntry {
    private Instant timestamp;
    private String method;
    private String path;
    private String queryString;
    private String clientIp;
    private String userAgent;
    private Integer statusCode;
    private Long responseSize;
    private Long processingTimeMs;
    private String backend;
    private String service;
    private String userId;
}