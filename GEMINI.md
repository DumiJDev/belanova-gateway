# Belanova Gateway - Spring Cloud Gateway MVC Implementation

## Overview

Belanova Gateway is a high-performance, cloud-native API Gateway built on Spring Cloud Gateway MVC with an integrated Vaadin Flow admin interface. This implementation uses the new Spring Cloud Gateway MVC stack, which provides the powerful routing and filtering capabilities of Spring Cloud Gateway while running on the traditional servlet stack instead of the reactive stack.

## Architecture

### Core Components

```
┌─────────────────────────────────────────────────────────────┐
│                    Belanova Gateway System                   │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────────────┐    ┌─────────────────────────────┐  │
│  │   Gateway Module    │    │      Admin Module           │  │
│  │                     │    │                             │  │
│  │ • Spring Cloud GW   │    │ • Vaadin Flow UI            │  │
│  │   MVC               │    │ • Configuration Management  │  │
│  │ • Plugin System     │    │ • Plugin Management         │  │
│  │ • Dynamic Routing   │    │ • Health Monitoring         │  │
│  │ • Rate Limiting     │    │ • H2 Database (Primary)     │  │
│  │ • Authentication    │    │ • PWA Support               │  │
│  │ (CACHE ONLY)        │    │                             │  │
│  └─────────────────────┘    └─────────────────────────────┘  │
│           │                              │                   │
│           └──────────────┬───────────────┘                   │
│                          │                                   │
│  ┌─────────────────────────────────────────────────────────┐  │
│  │             Shared Infrastructure                       │  │
│  │                                                         │  │
│  │ • Distributed Cache (Apache Ignite/Ignite - Java-native)   │  │
│  │ • Service Discovery (Optional Eureka)                  │  │
│  │ • Observability (Micrometer/OpenTelemetry)             │  │
│  └─────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

### Multi-Layered Architecture

#### Gateway Module (Spring Cloud Gateway MVC)
- **Route Configuration**: Dynamic route definitions using RouterFunction
- **Filter Chain**: MVC-based filters for request/response processing
- **Handler Functions**: Business logic handlers for routing
- **Plugin System**: Custom filters and handlers for extensibility
- **Repository Layer**: Cache-only access for configuration

#### Admin Module
- **UI Layer**: Vaadin Flow components with modern, responsive design
- **Service Layer**: Configuration management and gateway communication
- **Data Layer**: Database operations with optimized queries

## Technology Stack

### Core Technologies
- **Java**: 24+
- **Spring Boot**: 3.5.5+ or 4.x
- **Spring Cloud Gateway MVC**: Latest version (servlet-based)
- **Vaadin Flow**: Latest LTS version
- **Maven**: Multi-module project structure

### Database Support (Admin Only)
- H2 (Primary - lightweight, embedded)
- PostgreSQL
- MySQL/MariaDB
- SQL Server

### Cache Providers (Distributed, High Performance, Java-based)
- Apache Ignite (Primary - distributed, Java-native)
- Apache Ignite (High-performance distributed cache)
- Caffeine (Local cache fallback)
- Redis (Last option - external dependency)

### HTTP Client
- **RestClient**: New Spring 6+ HTTP client
- **WebClient**: Reactive HTTP client for advanced scenarios
- **RestTemplate**: Traditional HTTP client with connection pooling

## Backend and Service Management

### Backend Registration

#### Backend Definition
```java
@Entity
@Table(name = "backends")
public class Backend {
    @Id
    private String id;
    
    @Column(nullable = false)
    private String name;
    
    private String description;
    
    private String baseUrl;           // optional: use if Eureka disabled
    private String serviceId;         // for Eureka service discovery
    private String generalPath;       // optional: /api/v1
    
    @Column(nullable = false)
    private boolean enabled = true;
    
    @Column(nullable = false)
    private boolean useServiceDiscovery = false; // true = use Eureka, false = use baseUrl
    
    @Embedded
    private HealthCheckConfig healthCheck;
    
    @OneToMany(mappedBy = "backend", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Service> services = new ArrayList<>();
    
    @OneToMany(mappedBy = "backend", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Upstream> upstreams = new ArrayList<>();
    
    // getters and setters
}
```

#### Service Configuration
```java
@Entity
@Table(name = "services")
public class Service {
    @Id
    private String id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String path;              // e.g., /users, /orders
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "backend_id")
    private Backend backend;
    
    @ElementCollection
    @Enumerated(EnumType.STRING)
    private Set<HttpMethod> methods = new HashSet<>();     // GET, POST, PUT, DELETE
    
    @Column(nullable = false)
    private boolean enabled = true;
    
    @ElementCollection
    @CollectionTable(name = "service_metadata")
    @MapKeyColumn(name = "metadata_key")
    @Column(name = "metadata_value")
    private Map<String, String> metadata = new HashMap<>();
    
    // getters and setters
}
```

## Spring Cloud Gateway MVC Implementation

### Gateway Configuration

```java
@Configuration
@EnableConfigurationProperties({GatewayMvcProperties.class})
public class GatewayMvcConfiguration {
    
    private final BackendCacheService backendCache;
    private final GatewayPluginManager pluginManager;
    private final LoadBalancerClient loadBalancerClient;
    
    public GatewayMvcConfiguration(BackendCacheService backendCache,
                                  GatewayPluginManager pluginManager,
                                  @Autowired(required = false) LoadBalancerClient loadBalancerClient) {
        this.backendCache = backendCache;
        this.pluginManager = pluginManager;
        this.loadBalancerClient = loadBalancerClient;
    }
    
    @Bean
    public RouterFunction<ServerResponse> gatewayRoutes() {
        return RouterFunctions.route()
            .add(dynamicRoutes())
            .filter(new LoggingFilter())
            .filter(new TimingFilter())
            .filter(pluginManager.getAuthenticationFilter())
            .filter(pluginManager.getRateLimitFilter())
            .build();
    }
    
    private RouterFunction<ServerResponse> dynamicRoutes() {
        RouterFunction<ServerResponse> routerFunction = RouterFunctions.route()
            .GET("/**", this::handleNotFound)
            .POST("/**", this::handleNotFound)
            .PUT("/**", this::handleNotFound)
            .DELETE("/**", this::handleNotFound)
            .PATCH("/**", this::handleNotFound)
            .build();
        
        // Build dynamic routes from cache
        List<Backend> backends = backendCache.getEnabledBackends();
        
        for (Backend backend : backends) {
            for (Service service : backend.getServices()) {
                if (service.isEnabled()) {
                    routerFunction = routerFunction.and(createServiceRoute(backend, service));
                }
            }
        }
        
        return routerFunction;
    }
    
    private RouterFunction<ServerResponse> createServiceRoute(Backend backend, Service service) {
        String pathPattern = service.getPath() + "/**";
        
        RouterFunction<ServerResponse> serviceRoute = RouterFunctions.route();
        
        for (HttpMethod method : service.getMethods()) {
            switch (method) {
                case GET -> serviceRoute = serviceRoute.and(
                    RouterFunctions.route(RequestPredicates.GET(pathPattern),
                        request -> handleRequest(request, backend, service)));
                case POST -> serviceRoute = serviceRoute.and(
                    RouterFunctions.route(RequestPredicates.POST(pathPattern),
                        request -> handleRequest(request, backend, service)));
                case PUT -> serviceRoute = serviceRoute.and(
                    RouterFunctions.route(RequestPredicates.PUT(pathPattern),
                        request -> handleRequest(request, backend, service)));
                case DELETE -> serviceRoute = serviceRoute.and(
                    RouterFunctions.route(RequestPredicates.DELETE(pathPattern),
                        request -> handleRequest(request, backend, service)));
                case PATCH -> serviceRoute = serviceRoute.and(
                    RouterFunctions.route(RequestPredicates.PATCH(pathPattern),
                        request -> handleRequest(request, backend, service)));
            }
        }
        
        return serviceRoute;
    }
    
    private ServerResponse handleRequest(ServerRequest request, Backend backend, Service service) 
            throws Exception {
        
        // Create gateway context
        GatewayMvcContext context = GatewayMvcContext.builder()
            .request(request)
            .backend(backend)
            .service(service)
            .build();
        
        try {
            // Execute pre-processing plugins
            pluginManager.executePreFilters(context);
            
            // Forward request to backend
            ServerResponse response = forwardRequest(context);
            
            // Execute post-processing plugins
            context.setResponse(response);
            pluginManager.executePostFilters(context);
            
            return response;
            
        } catch (Exception e) {
            context.setError(e);
            pluginManager.executeErrorFilters(context);
            throw e;
        }
    }
    
    private ServerResponse forwardRequest(GatewayMvcContext context) throws Exception {
        String targetUrl = buildTargetUrl(context);
        HttpMethod method = context.getRequest().method();
        
        // Extract headers
        HttpHeaders headers = HttpHeaders.of(
            context.getRequest().headers().asHttpHeaders(),
            (name, value) -> !isHopByHopHeader(name)
        );
        
        // Get request body if present
        byte[] body = null;
        if (hasBody(method)) {
            body = context.getRequest().body(byte[].class);
        }
        
        // Create RestClient request
        RestClient restClient = RestClient.create();
        RestClient.RequestBodySpec requestSpec = restClient
            .method(method)
            .uri(targetUrl)
            .headers(httpHeaders -> httpHeaders.addAll(headers));
        
        if (body != null) {
            requestSpec.body(body);
        }
        
        // Execute request
        ResponseEntity<byte[]> backendResponse = requestSpec
            .retrieve()
            .toEntity(byte[].class);
        
        // Build response
        ServerResponse.BodyBuilder responseBuilder = ServerResponse
            .status(backendResponse.getStatusCode());
        
        // Copy response headers
        HttpHeaders responseHeaders = backendResponse.getHeaders();
        responseHeaders.forEach((name, values) -> {
            if (!isHopByHopHeader(name)) {
                responseBuilder.header(name, values.toArray(new String[0]));
            }
        });
        
        byte[] responseBody = backendResponse.getBody();
        if (responseBody != null && responseBody.length > 0) {
            return responseBuilder.body(responseBody);
        } else {
            return responseBuilder.build();
        }
    }
    
    private String buildTargetUrl(GatewayMvcContext context) {
        Backend backend = context.getBackend();
        Service service = context.getService();
        ServerRequest request = context.getRequest();
        
        String baseUrl;
        if (backend.isUseServiceDiscovery() && loadBalancerClient != null) {
            ServiceInstance instance = loadBalancerClient.choose(backend.getServiceId());
            if (instance == null) {
                throw new ServiceUnavailableException("No available instances for service: " + backend.getServiceId());
            }
            baseUrl = instance.getUri().toString();
        } else {
            baseUrl = backend.getBaseUrl();
        }
        
        String generalPath = Optional.ofNullable(backend.getGeneralPath()).orElse("");
        String servicePath = service.getPath();
        String requestPath = request.path();
        
        // Build final URL
        StringBuilder url = new StringBuilder(baseUrl);
        if (!generalPath.isEmpty() && !generalPath.startsWith("/")) {
            url.append("/");
        }
        url.append(generalPath);
        
        // Replace service path pattern with actual path
        String remainingPath = requestPath;
        if (requestPath.startsWith(servicePath)) {
            remainingPath = requestPath.substring(servicePath.length());
        }
        
        url.append(servicePath);
        if (!remainingPath.isEmpty() && !remainingPath.startsWith("/")) {
            url.append("/");
        }
        url.append(remainingPath);
        
        // Add query parameters
        MultiValueMap<String, String> queryParams = request.params();
        if (!queryParams.isEmpty()) {
            url.append("?");
            queryParams.forEach((name, values) -> {
                for (String value : values) {
                    url.append(name).append("=").append(URLEncoder.encode(value, StandardCharsets.UTF_8)).append("&");
                }
            });
            url.setLength(url.length() - 1); // Remove trailing &
        }
        
        return url.toString();
    }
    
    private boolean hasBody(HttpMethod method) {
        return method == HttpMethod.POST || method == HttpMethod.PUT || method == HttpMethod.PATCH;
    }
    
    private boolean isHopByHopHeader(String headerName) {
        return Set.of("connection", "keep-alive", "proxy-authenticate", 
                     "proxy-authorization", "te", "trailers", "transfer-encoding", "upgrade")
                .contains(headerName.toLowerCase());
    }
    
    private ServerResponse handleNotFound(ServerRequest request) {
        return ServerResponse.notFound().build();
    }
}
```

### Gateway Context

```java
@Data
@Builder
public class GatewayMvcContext {
    private ServerRequest request;
    private ServerResponse response;
    private Backend backend;
    private Service service;
    private Exception error;
    private Map<String, Object> attributes;
    private Principal principal;
    
    public void setAttribute(String key, Object value) {
        if (attributes == null) {
            attributes = new HashMap<>();
        }
        attributes.put(key, value);
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key) {
        return attributes != null ? (T) attributes.get(key) : null;
    }
}
```

## Plugin System for Spring Cloud Gateway MVC

### Plugin Interface

```java
public interface GatewayMvcPlugin {
    
    /**
     * Plugin name
     */
    String getName();
    
    /**
     * Plugin execution order (lower values execute first)
     */
    int getOrder();
    
    /**
     * Whether plugin is enabled
     */
    boolean isEnabled();
    
    /**
     * Plugin execution phase
     */
    PluginPhase getPhase();
    
    /**
     * Execute plugin logic
     */
    void execute(GatewayMvcContext context) throws PluginException;
    
    /**
     * Plugin configuration
     */
    Map<String, Object> getConfiguration();
    
    /**
     * Validate plugin configuration
     */
    void validateConfiguration(Map<String, Object> config) throws ConfigurationException;
}
```

### Plugin Manager

```java
@Service
@Slf4j
public class GatewayPluginManager {
    
    private final List<GatewayMvcPlugin> plugins;
    private final HandlerFilterFunction<ServerResponse, ServerResponse> authenticationFilter;
    private final HandlerFilterFunction<ServerResponse, ServerResponse> rateLimitFilter;
    
    public GatewayPluginManager(List<GatewayMvcPlugin> plugins) {
        this.plugins = plugins.stream()
            .filter(GatewayMvcPlugin::isEnabled)
            .sorted(Comparator.comparingInt(GatewayMvcPlugin::getOrder))
            .collect(Collectors.toList());
        
        this.authenticationFilter = createAuthenticationFilter();
        this.rateLimitFilter = createRateLimitFilter();
    }
    
    public HandlerFilterFunction<ServerResponse, ServerResponse> getAuthenticationFilter() {
        return authenticationFilter;
    }
    
    public HandlerFilterFunction<ServerResponse, ServerResponse> getRateLimitFilter() {
        return rateLimitFilter;
    }
    
    private HandlerFilterFunction<ServerResponse, ServerResponse> createAuthenticationFilter() {
        return (request, next) -> {
            try {
                GatewayMvcContext context = GatewayMvcContext.builder()
                    .request(request)
                    .build();
                
                executePluginsForPhase(context, PluginPhase.AUTH);
                
                return next.handle(request);
            } catch (AuthenticationException e) {
                return ServerResponse.status(HttpStatus.UNAUTHORIZED)
                    .body("Authentication failed: " + e.getMessage());
            } catch (Exception e) {
                log.error("Authentication filter error", e);
                return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error");
            }
        };
    }
    
    private HandlerFilterFunction<ServerResponse, ServerResponse> createRateLimitFilter() {
        return (request, next) -> {
            try {
                GatewayMvcContext context = GatewayMvcContext.builder()
                    .request(request)
                    .build();
                
                executePluginsForPhase(context, PluginPhase.PRE_REQUEST);
                
                return next.handle(request);
            } catch (RateLimitExceededException e) {
                return ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                    .header("X-RateLimit-Limit", e.getLimit())
                    .header("X-RateLimit-Remaining", "0")
                    .body("Rate limit exceeded");
            } catch (Exception e) {
                log.error("Rate limit filter error", e);
                return next.handle(request);
            }
        };
    }
    
    public void executePreFilters(GatewayMvcContext context) {
        executePluginsForPhase(context, PluginPhase.PRE_AUTH);
        executePluginsForPhase(context, PluginPhase.POST_AUTH);
        executePluginsForPhase(context, PluginPhase.PRE_ROUTING);
        executePluginsForPhase(context, PluginPhase.ROUTING);
        executePluginsForPhase(context, PluginPhase.POST_ROUTING);
    }
    
    public void executePostFilters(GatewayMvcContext context) {
        executePluginsForPhase(context, PluginPhase.POST_REQUEST);
    }
    
    public void executeErrorFilters(GatewayMvcContext context) {
        executePluginsForPhase(context, PluginPhase.ERROR);
    }
    
    private void executePluginsForPhase(GatewayMvcContext context, PluginPhase phase) {
        plugins.stream()
            .filter(plugin -> plugin.getPhase() == phase)
            .forEach(plugin -> {
                try {
                    log.debug("Executing plugin: {} for phase: {}", plugin.getName(), phase);
                    plugin.execute(context);
                } catch (Exception e) {
                    log.error("Plugin execution failed: {}", plugin.getName(), e);
                    if (phase != PluginPhase.ERROR) {
                        throw new PluginExecutionException("Plugin failed: " + plugin.getName(), e);
                    }
                }
            });
    }
}
```

## Built-in Plugins (Gateway MVC Implementation)

### JWT Authentication Plugin

```java
@Component
public class JwtAuthenticationMvcPlugin implements GatewayMvcPlugin {
    
    private final JwtTokenValidator jwtValidator;
    
    public JwtAuthenticationMvcPlugin(JwtTokenValidator jwtValidator) {
        this.jwtValidator = jwtValidator;
    }
    
    @Override
    public String getName() {
        return "jwt-auth-mvc";
    }
    
    @Override
    public int getOrder() {
        return 100;
    }
    
    @Override
    public boolean isEnabled() {
        return true;
    }
    
    @Override
    public PluginPhase getPhase() {
        return PluginPhase.AUTH;
    }
    
    @Override
    public void execute(GatewayMvcContext context) throws PluginException {
        ServerRequest request = context.getRequest();
        
        Optional<String> authHeader = request.headers().firstHeader("Authorization");
        
        if (authHeader.isEmpty() || !authHeader.get().startsWith("Bearer ")) {
            throw new AuthenticationException("Missing or invalid Authorization header");
        }
        
        String token = authHeader.get().substring(7);
        
        try {
            Claims claims = jwtValidator.validateToken(token);
            
            // Set principal in context
            JwtPrincipal principal = new JwtPrincipal(claims);
            context.setPrincipal(principal);
            
            // Add user info to context attributes
            context.setAttribute("user.id", claims.getSubject());
            context.setAttribute("user.roles", claims.get("roles"));
            
        } catch (JwtException e) {
            throw new AuthenticationException("Invalid JWT token", e);
        }
    }
    
    @Override
    public Map<String, Object> getConfiguration() {
        return Map.of(
            "secret", "${jwt.secret}",
            "issuer", "${jwt.issuer}",
            "audience", "${jwt.audience}"
        );
    }
    
    @Override
    public void validateConfiguration(Map<String, Object> config) throws ConfigurationException {
        if (!config.containsKey("secret")) {
            throw new ConfigurationException("JWT secret is required");
        }
    }
}
```

### Rate Limiting Plugin

```java
@Component
public class RateLimitingMvcPlugin implements GatewayMvcPlugin {
    
    private final RedisTemplate<String, String> redisTemplate;
    private final RateLimitConfiguration config;
    
    public RateLimitingMvcPlugin(RedisTemplate<String, String> redisTemplate,
                                RateLimitConfiguration config) {
        this.redisTemplate = redisTemplate;
        this.config = config;
    }
    
    @Override
    public String getName() {
        return "rate-limit-mvc";
    }
    
    @Override
    public int getOrder() {
        return 200;
    }
    
    @Override
    public boolean isEnabled() {
        return config.isEnabled();
    }
    
    @Override
    public PluginPhase getPhase() {
        return PluginPhase.PRE_REQUEST;
    }
    
    @Override
    public void execute(GatewayMvcContext context) throws PluginException {
        String clientId = extractClientId(context);
        String key = "rate_limit:" + clientId;
        
        // Use Redis sliding window algorithm
        long currentTime = System.currentTimeMillis();
        long windowStart = currentTime - config.getWindowSizeMs();
        
        // Remove old entries
        redisTemplate.opsForZSet().removeRangeByScore(key, 0, windowStart);
        
        // Count current requests
        Long currentCount = redisTemplate.opsForZSet().count(key, windowStart, currentTime);
        
        if (currentCount != null && currentCount >= config.getMaxRequests()) {
            throw new RateLimitExceededException(
                "Rate limit exceeded for client: " + clientId,
                String.valueOf(config.getMaxRequests()),
                "0"
            );
        }
        
        // Add current request
        redisTemplate.opsForZSet().add(key, UUID.randomUUID().toString(), currentTime);
        redisTemplate.expire(key, Duration.ofMillis(config.getWindowSizeMs()));
    }
    
    private String extractClientId(GatewayMvcContext context) {
        // Try to get client ID from JWT token
        Principal principal = context.getPrincipal();
        if (principal instanceof JwtPrincipal) {
            return principal.getName();
        }
        
        // Fallback to IP address
        return getClientIp(context.getRequest());
    }
    
    private String getClientIp(ServerRequest request) {
        return request.headers().firstHeader("X-Forwarded-For")
            .or(() -> request.headers().firstHeader("X-Real-IP"))
            .orElse(request.remoteAddress()
                .map(address -> address.getAddress().getHostAddress())
                .orElse("unknown"));
    }
    
    @Override
    public Map<String, Object> getConfiguration() {
        return Map.of(
            "maxRequests", config.getMaxRequests(),
            "windowSizeMs", config.getWindowSizeMs(),
            "enabled", config.isEnabled()
        );
    }
    
    @Override
    public void validateConfiguration(Map<String, Object> config) throws ConfigurationException {
        Object maxRequests = config.get("maxRequests");
        if (maxRequests == null || !(maxRequests instanceof Number) || 
            ((Number) maxRequests).intValue() <= 0) {
            throw new ConfigurationException("maxRequests must be a positive number");
        }
        
        Object windowSizeMs = config.get("windowSizeMs");
        if (windowSizeMs == null || !(windowSizeMs instanceof Number) ||
            ((Number) windowSizeMs).longValue() <= 0) {
            throw new ConfigurationException("windowSizeMs must be a positive number");
        }
    }
}
```

### Request/Response Logging Plugin

```java
@Component
public class LoggingMvcPlugin implements GatewayMvcPlugin {
    
    private static final Logger log = LoggerFactory.getLogger(LoggingMvcPlugin.class);
    private final ObjectMapper objectMapper;
    
    public LoggingMvcPlugin(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    @Override
    public String getName() {
        return "request-logging-mvc";
    }
    
    @Override
    public int getOrder() {
        return 1000;
    }
    
    @Override
    public boolean isEnabled() {
        return true;
    }
    
    @Override
    public PluginPhase getPhase() {
        return PluginPhase.POST_REQUEST;
    }
    
    @Override
    public void execute(GatewayMvcContext context) throws PluginException {
        try {
            RequestLogEntry logEntry = RequestLogEntry.builder()
                .timestamp(Instant.now())
                .method(context.getRequest().method().name())
                .path(context.getRequest().path())
                .queryString(getQueryString(context.getRequest()))
                .clientIp(getClientIp(context.getRequest()))
                .userAgent(context.getRequest().headers().firstHeader("User-Agent").orElse(null))
                .statusCode(getStatusCode(context.getResponse()))
                .responseSize(getResponseSize(context.getResponse()))
                .processingTimeMs(getProcessingTime(context))
                .backend(context.getBackend().getName())
                .service(context.getService().getName())
                .userId(getUserId(context))
                .build();
            
            log.info("Gateway Request: {}", objectMapper.writeValueAsString(logEntry));
            
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize log entry", e);
        }
    }
    
    private String getQueryString(ServerRequest request) {
        MultiValueMap<String, String> params = request.params();
        if (params.isEmpty()) {
            return null;
        }
        
        return params.entrySet().stream()
            .flatMap(entry -> entry.getValue().stream()
                .map(value -> entry.getKey() + "=" + URLEncoder.encode(value, StandardCharsets.UTF_8)))
            .collect(Collectors.joining("&"));
    }
    
    private String getClientIp(ServerRequest request) {
        return request.headers().firstHeader("X-Forwarded-For")
            .or(() -> request.headers().firstHeader("X-Real-IP"))
            .orElse(request.remoteAddress()
                .map(address -> address.getAddress().getHostAddress())
                .orElse("unknown"));
    }
    
    private Integer getStatusCode(ServerResponse response) {
        return response != null ? response.statusCode().value() : null;
    }
    
    private Long getResponseSize(ServerResponse response) {
        // ServerResponse doesn't provide direct access to body size in Gateway MVC
        // This would need to be implemented with a custom response wrapper
        return null;
    }
    
    private Long getProcessingTime(GatewayMvcContext context) {
        Long startTime = context.getAttribute("request.startTime");
        if (startTime != null) {
            return System.currentTimeMillis() - startTime;
        }
        return null;
    }
    
    private String getUserId(GatewayMvcContext context) {
        Principal principal = context.getPrincipal();
        return principal != null ? principal.getName() : null;
    }
    
    @Override
    public Map<String, Object> getConfiguration() {
        return Map.of(
            "logLevel", "INFO",
            "includeHeaders", false,
            "includeBody", false
        );
    }
    
    @Override
    public void validateConfiguration(Map<String, Object> config) throws ConfigurationException {
        // No validation needed for this plugin
    }
}
```

## Custom Filters

### Logging Filter

```java
public class LoggingFilter implements HandlerFilterFunction<ServerResponse, ServerResponse> {
    
    private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);
    
    @Override
    public ServerResponse filter(ServerRequest request, HandlerFunction<ServerResponse> next) throws Exception {
        long startTime = System.currentTimeMillis();
        
        log.debug("Incoming request: {} {}", request.method(), request.path());
        
        try {
            ServerResponse response = next.handle(request);
            long duration = System.currentTimeMillis() - startTime;
        
        // Add timing header to response
        return ServerResponse.from(response)
            .header("X-Response-Time", duration + "ms")
            .build();
    }
}
```

## Dynamic Route Locator

### MVC Route Locator

```java
@Component
@Slf4j
public class DynamicMvcRouteLocator {
    
    private final BackendCacheService backendCache;
    private final GatewayPluginManager pluginManager;
    private final LoadBalancerClient loadBalancerClient;
    private final RestClient restClient;
    
    public DynamicMvcRouteLocator(BackendCacheService backendCache,
                                 GatewayPluginManager pluginManager,
                                 @Autowired(required = false) LoadBalancerClient loadBalancerClient) {
        this.backendCache = backendCache;
        this.pluginManager = pluginManager;
        this.loadBalancerClient = loadBalancerClient;
        this.restClient = RestClient.create();
    }
    
    @Bean
    @RefreshScope
    public RouterFunction<ServerResponse> routes() {
        return buildDynamicRoutes();
    }
    
    private RouterFunction<ServerResponse> buildDynamicRoutes() {
        RouterFunction<ServerResponse> routerFunction = RouterFunctions.route();
        
        List<Backend> backends = backendCache.getEnabledBackends();
        
        for (Backend backend : backends) {
            for (Service service : backend.getServices()) {
                if (service.isEnabled()) {
                    routerFunction = routerFunction.and(createServiceRoute(backend, service));
                }
            }
        }
        
        // Add default not found handler
        routerFunction = routerFunction.and(
            RouterFunctions.route(RequestPredicates.all(), this::handleNotFound)
        );
        
        return routerFunction
            .filter(new TimingFilter())
            .filter(new LoggingFilter())
            .filter(pluginManager.getAuthenticationFilter())
            .filter(pluginManager.getRateLimitFilter());
    }
    
    private RouterFunction<ServerResponse> createServiceRoute(Backend backend, Service service) {
        String pathPattern = service.getPath();
        if (!pathPattern.endsWith("/**")) {
            pathPattern += "/**";
        }
        
        RequestPredicate predicate = RequestPredicates.path(pathPattern);
        
        // Add HTTP method predicates
        if (!service.getMethods().isEmpty()) {
            RequestPredicate methodPredicate = service.getMethods().stream()
                .map(this::createMethodPredicate)
                .reduce(RequestPredicate::or)
                .orElse(RequestPredicates.all());
            
            predicate = predicate.and(methodPredicate);
        }
        
        return RouterFunctions.route(predicate, 
            request -> handleServiceRequest(request, backend, service));
    }
    
    private RequestPredicate createMethodPredicate(HttpMethod method) {
        return switch (method) {
            case GET -> RequestPredicates.GET();
            case POST -> RequestPredicates.POST();
            case PUT -> RequestPredicates.PUT();
            case DELETE -> RequestPredicates.DELETE();
            case PATCH -> RequestPredicates.PATCH();
            case OPTIONS -> RequestPredicates.OPTIONS();
            case HEAD -> RequestPredicates.HEAD();
            default -> RequestPredicates.method(method);
        };
    }
    
    private ServerResponse handleServiceRequest(ServerRequest request, Backend backend, Service service) 
            throws Exception {
        
        GatewayMvcContext context = GatewayMvcContext.builder()
            .request(request)
            .backend(backend)
            .service(service)
            .build();
        
        try {
            // Execute pre-processing plugins
            pluginManager.executePreFilters(context);
            
            // Forward request to backend
            ServerResponse response = forwardToBackend(context);
            
            // Execute post-processing plugins
            context.setResponse(response);
            pluginManager.executePostFilters(context);
            
            return response;
            
        } catch (Exception e) {
            context.setError(e);
            pluginManager.executeErrorFilters(context);
            return handleError(e);
        }
    }
    
    private ServerResponse forwardToBackend(GatewayMvcContext context) throws Exception {
        String targetUrl = buildTargetUrl(context);
        ServerRequest request = context.getRequest();
        
        log.debug("Forwarding request to: {}", targetUrl);
        
        // Create RestClient request
        RestClient.RequestBodyUriSpec requestSpec = restClient
            .method(request.method())
            .uri(targetUrl);
        
        // Add headers (excluding hop-by-hop headers)
        HttpHeaders filteredHeaders = filterHeaders(request.headers().asHttpHeaders());
        requestSpec.headers(headers -> headers.addAll(filteredHeaders));
        
        // Add body if present
        RestClient.RequestBodySpec bodySpec = requestSpec;
        if (hasBody(request.method())) {
            try {
                byte[] body = request.body(byte[].class);
                if (body != null && body.length > 0) {
                    bodySpec = requestSpec.body(body);
                }
            } catch (Exception e) {
                log.warn("Could not read request body", e);
            }
        }
        
        try {
            // Execute request
            ResponseEntity<byte[]> backendResponse = bodySpec
                .retrieve()
                .toEntity(byte[].class);
            
            // Build response
            ServerResponse.BodyBuilder responseBuilder = ServerResponse
                .status(backendResponse.getStatusCode());
            
            // Copy response headers (excluding hop-by-hop headers)
            HttpHeaders responseHeaders = filterHeaders(backendResponse.getHeaders());
            responseHeaders.forEach((name, values) -> 
                responseBuilder.header(name, values.toArray(new String[0])));
            
            byte[] responseBody = backendResponse.getBody();
            if (responseBody != null && responseBody.length > 0) {
                return responseBuilder.body(responseBody);
            } else {
                return responseBuilder.build();
            }
            
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            // Return backend error as-is
            ServerResponse.BodyBuilder errorBuilder = ServerResponse.status(e.getStatusCode());
            
            HttpHeaders errorHeaders = filterHeaders(e.getResponseHeaders());
            errorHeaders.forEach((name, values) -> 
                errorBuilder.header(name, values.toArray(new String[0])));
            
            byte[] errorBody = e.getResponseBodyAsByteArray();
            if (errorBody != null && errorBody.length > 0) {
                return errorBuilder.body(errorBody);
            } else {
                return errorBuilder.build();
            }
            
        } catch (ResourceAccessException e) {
            log.error("Backend connection failed: {}", targetUrl, e);
            throw new BackendUnavailableException("Backend service unavailable", e);
        }
    }
    
    private String buildTargetUrl(GatewayMvcContext context) {
        Backend backend = context.getBackend();
        Service service = context.getService();
        ServerRequest request = context.getRequest();
        
        String baseUrl;
        if (backend.isUseServiceDiscovery() && loadBalancerClient != null) {
            ServiceInstance instance = loadBalancerClient.choose(backend.getServiceId());
            if (instance == null) {
                throw new ServiceUnavailableException("No available instances for service: " + backend.getServiceId());
            }
            baseUrl = instance.getUri().toString();
        } else {
            baseUrl = backend.getBaseUrl();
        }
        
        // Remove trailing slash from baseUrl
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        
        String generalPath = Optional.ofNullable(backend.getGeneralPath()).orElse("");
        String servicePath = service.getPath();
        String requestPath = request.path();
        
        // Build final URL
        StringBuilder url = new StringBuilder(baseUrl);
        
        if (!generalPath.isEmpty()) {
            if (!generalPath.startsWith("/")) {
                url.append("/");
            }
            url.append(generalPath);
        }
        
        // Calculate remaining path after service path
        String remainingPath = "";
        if (requestPath.startsWith(servicePath)) {
            remainingPath = requestPath.substring(servicePath.length());
        }
        
        url.append(servicePath);
        if (!remainingPath.isEmpty()) {
            if (!remainingPath.startsWith("/")) {
                url.append("/");
            }
            url.append(remainingPath);
        }
        
        // Add query parameters
        MultiValueMap<String, String> queryParams = request.params();
        if (!queryParams.isEmpty()) {
            url.append("?");
            queryParams.forEach((name, values) -> {
                for (String value : values) {
                    url.append(name).append("=")
                       .append(URLEncoder.encode(value, StandardCharsets.UTF_8))
                       .append("&");
                }
            });
            url.setLength(url.length() - 1); // Remove trailing &
        }
        
        return url.toString();
    }
    
    private HttpHeaders filterHeaders(HttpHeaders headers) {
        HttpHeaders filtered = new HttpHeaders();
        
        headers.forEach((name, values) -> {
            if (!isHopByHopHeader(name)) {
                filtered.addAll(name, values);
            }
        });
        
        return filtered;
    }
    
    private boolean isHopByHopHeader(String headerName) {
        return Set.of("connection", "keep-alive", "proxy-authenticate", 
                     "proxy-authorization", "te", "trailers", "transfer-encoding", "upgrade")
                .contains(headerName.toLowerCase());
    }
    
    private boolean hasBody(HttpMethod method) {
        return method == HttpMethod.POST || method == HttpMethod.PUT || method == HttpMethod.PATCH;
    }
    
    private ServerResponse handleNotFound(ServerRequest request) {
        log.debug("No route found for: {} {}", request.method(), request.path());
        return ServerResponse.notFound().build();
    }
    
    private ServerResponse handleError(Exception e) {
        if (e instanceof AuthenticationException) {
            return ServerResponse.status(HttpStatus.UNAUTHORIZED)
                .body("Authentication failed: " + e.getMessage());
        }
        
        if (e instanceof RateLimitExceededException) {
            RateLimitExceededException rateLimitEx = (RateLimitExceededException) e;
            return ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("X-RateLimit-Limit", rateLimitEx.getLimit())
                .header("X-RateLimit-Remaining", "0")
                .body("Rate limit exceeded");
        }
        
        if (e instanceof BackendUnavailableException) {
            return ServerResponse.status(HttpStatus.BAD_GATEWAY)
                .body("Backend service unavailable");
        }
        
        if (e instanceof ServiceUnavailableException) {
            return ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("Service unavailable: " + e.getMessage());
        }
        
        log.error("Unexpected error in gateway", e);
        return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Internal server error");
    }
    
    private String getQueryString(ServerRequest request) {
        MultiValueMap<String, String> params = request.params();
        if (params.isEmpty()) {
            return null;
        }
        
        return params.entrySet().stream()
            .flatMap(entry -> entry.getValue().stream()
                .map(value -> entry.getKey() + "=" + URLEncoder.encode(value, StandardCharsets.UTF_8)))
            .collect(Collectors.joining("&"));
    }
    
    private String getClientIp(ServerRequest request) {
        return request.headers().firstHeader("X-Forwarded-For")
            .or(() -> request.headers().firstHeader("X-Real-IP"))
            .orElse(request.remoteAddress()
                .map(address -> address.getAddress().getHostAddress())
                .orElse("unknown"));
    }
    
    private Integer getStatusCode(ServerResponse response) {
        return response != null ? response.statusCode().value() : null;
    }
    
    private Long getResponseSize(ServerResponse response) {
        // ServerResponse doesn't provide direct access to body size
        return null;
    }
    
    private Long getProcessingTime(GatewayMvcContext context) {
        Long startTime = context.getAttribute("request.startTime");
        if (startTime != null) {
            return System.currentTimeMillis() - startTime;
        }
        return null;
    }
    
    private String getUserId(GatewayMvcContext context) {
        Principal principal = context.getPrincipal();
        return principal != null ? principal.getName() : null;
    }
    
    @Override
    public Map<String, Object> getConfiguration() {
        return Map.of(
            "logLevel", "INFO",
            "includeHeaders", false,
            "includeBody", false
        );
    }
    
    @Override
    public void validateConfiguration(Map<String, Object> config) throws ConfigurationException {
        // No validation needed for this plugin
    }
}
```

## Health Check System

### Health Check Service

```java
@Service
@Slf4j
public class HealthCheckService {
    
    private final RestClient restClient;
    private final BackendCacheService backendCache;
    private final ScheduledExecutorService scheduler;
    private final Map<String, HealthStatus> healthStatusMap;
    private final LoadBalancerClient loadBalancerClient;
    
    public HealthCheckService(BackendCacheService backendCache,
                             @Autowired(required = false) LoadBalancerClient loadBalancerClient) {
        this.backendCache = backendCache;
        this.loadBalancerClient = loadBalancerClient;
        this.restClient = RestClient.builder()
            .requestFactory(createRequestFactory())
            .build();
        this.scheduler = Executors.newScheduledThreadPool(10);
        this.healthStatusMap = new ConcurrentHashMap<>();
        
        startHealthChecks();
    }
    
    @PostConstruct
    public void startHealthChecks() {
        scheduler.scheduleWithFixedDelay(this::performHealthChecks, 0, 30, TimeUnit.SECONDS);
    }
    
    @PreDestroy
    public void shutdown() {
        scheduler.shutdown();
    }
    
    public void performHealthChecks() {
        List<Backend> backends = backendCache.getEnabledBackends();
        
        backends.parallelStream()
            .filter(backend -> backend.getHealthCheck() != null)
            .forEach(this::checkBackendHealth);
    }
    
    private void checkBackendHealth(Backend backend) {
        HealthCheckConfig healthConfig = backend.getHealthCheck();
        
        try {
            String healthUrl = buildHealthUrl(backend, healthConfig);
            long startTime = System.currentTimeMillis();
            
            ResponseEntity<String> response = restClient
                .get()
                .uri(healthUrl)
                .retrieve()
                .toEntity(String.class);
            
            long responseTime = System.currentTimeMillis() - startTime;
            boolean isHealthy = isHealthyResponse(response, healthConfig);
            
            HealthStatus status = HealthStatus.builder()
                .backendId(backend.getId())
                .healthy(isHealthy)
                .responseTime(responseTime)
                .statusCode(response.getStatusCode().value())
                .lastChecked(Instant.now())
                .message(isHealthy ? "OK" : "Health check failed")
                .build();
            
            healthStatusMap.put(backend.getId(), status);
            
            log.debug("Health check for backend {}: {} ({}ms)", 
                     backend.getName(), isHealthy ? "HEALTHY" : "UNHEALTHY", responseTime);
                     
        } catch (Exception e) {
            HealthStatus status = HealthStatus.builder()
                .backendId(backend.getId())
                .healthy(false)
                .responseTime(-1L)
                .statusCode(-1)
                .lastChecked(Instant.now())
                .message(e.getMessage())
                .build();
            
            healthStatusMap.put(backend.getId(), status);
            
            log.warn("Health check failed for backend {}: {}", backend.getName(), e.getMessage());
        }
    }
    
    private String buildHealthUrl(Backend backend, HealthCheckConfig healthConfig) {
        String baseUrl;
        
        if (backend.isUseServiceDiscovery() && loadBalancerClient != null) {
            ServiceInstance instance = loadBalancerClient.choose(backend.getServiceId());
            if (instance == null) {
                throw new ServiceUnavailableException("No instances available for service: " + backend.getServiceId());
            }
            baseUrl = instance.getUri().toString();
        } else {
            baseUrl = backend.getBaseUrl();
        }
        
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        
        return baseUrl + healthConfig.getHealthPath();
    }
    
    private boolean isHealthyResponse(ResponseEntity<String> response, HealthCheckConfig config) {
        // Check status code
        if (!config.getExpectedStatusCodes().contains(response.getStatusCode().value())) {
            return false;
        }
        
        // Check headers if configured
        if (config.getExpectedHeaders() != null) {
            for (Map.Entry<String, String> expectedHeader : config.getExpectedHeaders().entrySet()) {
                String actualValue = response.getHeaders().getFirst(expectedHeader.getKey());
                if (!expectedHeader.getValue().equals(actualValue)) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    private ClientHttpRequestFactory createRequestFactory() {
        HttpComponentsClientHttpRequestFactory factory = 
            new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(10000);
        return factory;
    }
    
    public HealthStatus getBackendHealth(String backendId) {
        return healthStatusMap.get(backendId);
    }
    
    public Map<String, HealthStatus> getAllHealthStatuses() {
        return new HashMap<>(healthStatusMap);
    }
}

@Data
@Builder
public class HealthStatus {
    private String backendId;
    private boolean healthy;
    private long responseTime;
    private int statusCode;
    private Instant lastChecked;
    private String message;
}
```

## Configuration Refresh

### Dynamic Configuration Updates

```java
@Component
@RefreshScope
public class GatewayConfigurationRefresher {
    
    private final ApplicationContext applicationContext;
    private final BackendCacheService backendCache;
    
    public GatewayConfigurationRefresher(ApplicationContext applicationContext,
                                       BackendCacheService backendCache) {
        this.applicationContext = applicationContext;
        this.backendCache = backendCache;
    }
    
    @EventListener
    public void handleConfigurationChange(BackendConfigurationChangedEvent event) {
        log.info("Backend configuration changed, refreshing routes...");
        refreshRoutes();
    }
    
    public void refreshRoutes() {
        // Trigger refresh of RouterFunction bean
        if (applicationContext instanceof ConfigurableApplicationContext) {
            ConfigurableApplicationContext configurableContext = 
                (ConfigurableApplicationContext) applicationContext;
            
            // Refresh the RouterFunction bean
            RefreshScope refreshScope = configurableContext.getBean(RefreshScope.class);
            refreshScope.refresh("routes");
            
            log.info("Gateway routes refreshed successfully");
        }
    }
    
    @EventListener
    public void handlePluginConfigurationChange(PluginConfigurationChangedEvent event) {
        log.info("Plugin configuration changed: {}", event.getPluginName());
        // Plugin configurations are handled dynamically by the plugin manager
    }
}
```

## Exception Handling

### Custom Exception Classes

```java
public class RateLimitExceededException extends PluginException {
    private final String limit;
    private final String remaining;
    
    public RateLimitExceededException(String message, String limit, String remaining) {
        super(message);
        this.limit = limit;
        this.remaining = remaining;
    }
    
    public String getLimit() {
        return limit;
    }
    
    public String getRemaining() {
        return remaining;
    }
}

public class BackendUnavailableException extends RuntimeException {
    public BackendUnavailableException(String message) {
        super(message);
    }
    
    public BackendUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}

public class ServiceUnavailableException extends RuntimeException {
    public ServiceUnavailableException(String message) {
        super(message);
    }
    
    public ServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}

public class AuthenticationException extends PluginException {
    public AuthenticationException(String message) {
        super(message);
    }
    
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}

public class PluginException extends RuntimeException {
    public PluginException(String message) {
        super(message);
    }
    
    public PluginException(String message, Throwable cause) {
        super(message, cause);
    }
}

public class PluginExecutionException extends RuntimeException {
    public PluginExecutionException(String message) {
        super(message);
    }
    
    public PluginExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}

public class ConfigurationException extends RuntimeException {
    public ConfigurationException(String message) {
        super(message);
    }
    
    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

## Main Application Configuration

### Gateway Application

```java
@SpringBootApplication
@EnableEurekaClient(autoRegister = false) // Optional service discovery
@EnableScheduling
@EnableCaching
@ComponentScan(basePackages = "com.belanova.gateway")
public class BelanovaGatewayApplication {
    
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(BelanovaGatewayApplication.class);
        app.setDefaultProperties(getDefaultProperties());
        app.run(args);
    }
    
    private static Properties getDefaultProperties() {
        Properties properties = new Properties();
        properties.setProperty("server.port", "8080");
        properties.setProperty("spring.application.name", "belanova-gateway");
        properties.setProperty("management.endpoints.web.exposure.include", "health,info,metrics,refresh");
        properties.setProperty("management.endpoint.health.show-details", "always");
        properties.setProperty("spring.cloud.gateway.mvc.enabled", "true");
        return properties;
    }
    
    @Bean
    public RestClient restClient() {
        return RestClient.builder()
            .requestFactory(clientHttpRequestFactory())
            .build();
    }
    
    @Bean
    public ClientHttpRequestFactory clientHttpRequestFactory() {
        HttpComponentsClientHttpRequestFactory factory = 
            new HttpComponentsClientHttpRequestFactory();
        
        // Configure connection pooling
        PoolingHttpClientConnectionManager connectionManager = 
            new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(200);
        connectionManager.setDefaultMaxPerRoute(50);
        connectionManager.setValidateAfterInactivity(2000);
        
        RequestConfig requestConfig = RequestConfig.custom()
            .setConnectionRequestTimeout(5000)
            .setConnectTimeout(5000)
            .setSocketTimeout(30000)
            .build();
        
        CloseableHttpClient httpClient = HttpClients.custom()
            .setConnectionManager(connectionManager)
            .setDefaultRequestConfig(requestConfig)
            .setKeepAliveStrategy((response, context) -> 30000)
            .build();
        
        factory.setHttpClient(httpClient);
        return factory;
    }
}
```

### Application Properties

```yaml
# application.yml
server:
  port: 8080
  servlet:
    context-path: /

spring:
  application:
    name: belanova-gateway
  profiles:
    active: dev
  cloud:
    gateway:
      mvc:
        enabled: true
    loadbalancer:
      ribbon:
        enabled: false
  cache:
    type: apache ignite

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,refresh
  endpoint:
    health:
      show-details: always
    refresh:
      enabled: true
  metrics:
    export:
      prometheus:
        enabled: true

gateway:
  plugin:
    jwt:
      secret: ${JWT_SECRET:default-secret}
      issuer: ${JWT_ISSUER:belanova-gateway}
      audience: ${JWT_AUDIENCE:belanova-api}
    rate-limit:
      enabled: true
      max-requests: 100
      window-size-ms: 60000
  cors:
    allowed-origins: "*"
    allowed-methods: "*"
    allowed-headers: "*"
    allow-credentials: true
    max-age: 3600

# Apache Ignite configuration
apache ignite:
  network:
    port: 5701
    port-auto-increment: true
  cluster-name: belanova-gateway

# Eureka configuration (optional)
eureka:
  client:
    enabled: false
    register-with-eureka: false
    fetch-registry: true
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true

# Logging configuration
logging:
  level:
    com.belanova.gateway: DEBUG
    org.springframework.cloud.gateway: DEBUG
    org.springframework.web: INFO
  pattern:
    console: "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
```

## Advanced Plugin Examples

### Circuit Breaker Plugin

```java
@Component
public class CircuitBreakerMvcPlugin implements GatewayMvcPlugin {
    
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    
    public CircuitBreakerMvcPlugin() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
            .failureRateThreshold(50)
            .waitDurationInOpenState(Duration.ofSeconds(30))
            .slidingWindowSize(10)
            .minimumNumberOfCalls(5)
            .build();
        
        this.circuitBreakerRegistry = CircuitBreakerRegistry.of(config);
    }
    
    @Override
    public String getName() {
        return "circuit-breaker-mvc";
    }
    
    @Override
    public int getOrder() {
        return 300;
    }
    
    @Override
    public boolean isEnabled() {
        return true;
    }
    
    @Override
    public PluginPhase getPhase() {
        return PluginPhase.PRE_REQUEST;
    }
    
    @Override
    public void execute(GatewayMvcContext context) throws PluginException {
        String circuitBreakerName = context.getBackend().getId() + "-cb";
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(circuitBreakerName);
        
        // Check circuit breaker state
        if (circuitBreaker.getState() == CircuitBreaker.State.OPEN) {
            throw new ServiceUnavailableException("Circuit breaker is OPEN for backend: " + 
                context.getBackend().getName());
        }
        
        // Record the circuit breaker for later use
        context.setAttribute("circuit.breaker", circuitBreaker);
    }
    
    @Override
    public Map<String, Object> getConfiguration() {
        return Map.of(
            "failureRateThreshold", 50,
            "waitDurationSeconds", 30,
            "slidingWindowSize", 10,
            "minimumNumberOfCalls", 5
        );
    }
    
    @Override
    public void validateConfiguration(Map<String, Object> config) throws ConfigurationException {
        // Validate circuit breaker configuration
    }
}
```

### Request Transformation Plugin

```java
@Component
public class RequestTransformationMvcPlugin implements GatewayMvcPlugin {
    
    private final ObjectMapper objectMapper;
    
    public RequestTransformationMvcPlugin(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    @Override
    public String getName() {
        return "request-transformation-mvc";
    }
    
    @Override
    public int getOrder() {
        return 400;
    }
    
    @Override
    public boolean isEnabled() {
        return true;
    }
    
    @Override
    public PluginPhase getPhase() {
        return PluginPhase.PRE_REQUEST;
    }
    
    @Override
    public void execute(GatewayMvcContext context) throws PluginException {
        ServerRequest request = context.getRequest();
        Service service = context.getService();
        
        // Add service metadata as headers
        Map<String, String> metadata = service.getMetadata();
        if (metadata != null && !metadata.isEmpty()) {
            metadata.forEach((key, value) -> {
                if (key.startsWith("header.")) {
                    String headerName = key.substring(7);
                    context.setAttribute("add.header." + headerName, value);
                }
            });
        }
        
        // Add gateway headers
        context.setAttribute("add.header.X-Gateway-Name", "Belanova-Gateway");
        context.setAttribute("add.header.X-Gateway-Version", "1.0.0");
        context.setAttribute("add.header.X-Service-Name", service.getName());
        context.setAttribute("add.header.X-Backend-Name", context.getBackend().getName());
        context.setAttribute("add.header.X-Request-ID", UUID.randomUUID().toString());
    }
    
    @Override
    public Map<String, Object> getConfiguration() {
        return Map.of(
            "addGatewayHeaders", true,
            "addServiceHeaders", true,
            "requestIdHeader", "X-Request-ID"
        );
    }
    
    @Override
    public void validateConfiguration(Map<String, Object> config) throws ConfigurationException {
        // No validation needed for this plugin
    }
}
```

## Main Application Class

```java
@SpringBootApplication
@EnableEurekaClient(autoRegister = false)
@EnableScheduling
@EnableCaching
public class BelanovaGatewayApplication {
    
    public static void main(String[] args) {
        System.setProperty("spring.cloud.gateway.mvc.enabled", "true");
        SpringApplication.run(BelanovaGatewayApplication.class, args);
    }
}
```

## Benefits of Spring Cloud Gateway MVC

### 1. **Familiar Development Model**
- Traditional servlet-based architecture
- Synchronous request handling
- Standard Spring MVC patterns an.currentTimeMillis() - startTime;

            log.debug("Request completed: {} {} - Status: {} - Duration: {}ms", 
                     request.method(), request.path(), 
                     response.statusCode(), duration);
            
            return response;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Request failed: {} {} - Duration: {}ms", 
                     request.method(), request.path(), duration, e);
            throw e;
        }
  }
  }
```

### Timing Filter

```java
public class TimingFilter implements HandlerFilterFunction<ServerResponse, ServerResponse> {
    
    @Override
    public ServerResponse filter(ServerRequest request, HandlerFunction<ServerResponse> next) throws Exception {
        long startTime = System.currentTimeMillis();
        
        ServerResponse response = next.handle(request);
        
        long duration = System