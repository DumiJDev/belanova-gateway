# belanova-gateway
Belanova Gateway is a high-performance, cloud-native API Gateway built on Spring Cloud Gateway with an integrated Vaadin Flow admin interface. Inspired by Kong Gateway but implemented entirely in Java with Spring ecosystem, it provides dynamic routing, plugin system, and comprehensive management capabilities.

## Features
- Dynamic routing from distributed cache (Apache Ignite)
- Plugin system (authentication, observability, traffic management)
- Built-in plugins: JWT Auth, Logging, etc.
- Load balancing: Round Robin, Weighted, Least Connections, Random, IP Hash
- Upstream health checks with visual status in Admin UI
- Vaadin-based admin UI (CRUD for backends/services/upstreams/plugins)
- Hot-reload configuration via cache
- Modular Maven multi-module structure
- REST API for gateway configuration and monitoring
- Global error handler for unified error responses
- Metrics and tracing (Micrometer/OpenTelemetry)
- Admin authentication and RBAC (planned)

## Admin UI
- Modern Vaadin Flow UI
- Manage Backends, Services, Upstreams, Plugins
- Health check dashboard with color indicators
- CRUD operations for all entities
- Responsive layout and navigation

## REST API
- `/api/gateway/config/backends` - List all backends (for admin UI and monitoring)

## Build & Run

```sh
mvn clean install
# Run gateway
cd belanova-gateway-core && mvn spring-boot:run
# Run admin UI
cd belanova-admin-ui && mvn spring-boot:run
```

## Testing

```sh
mvn test
```

## Project Structure
- `belanova-gateway-core`: Gateway logic, routing, filters, health checks
- `belanova-admin-ui`: Vaadin admin UI
- `belanova-common`: Shared models and interfaces
- `belanova-plugins`: Plugin implementations

See [GEMINI.md](GEMINI.md) for full technical documentation.
