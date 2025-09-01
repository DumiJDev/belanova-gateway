# 🚀 Belanova Gateway - Docker Deployment

A high-performance, cloud-native API Gateway built on Spring Cloud Gateway MVC with an integrated Vaadin Flow admin interface, featuring Kong-inspired design and comprehensive monitoring capabilities.

## 📋 Table of Contents

- [Features](#features)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Configuration](#configuration)
- [Services](#services)
- [Monitoring](#monitoring)
- [Development](#development)
- [Production Deployment](#production-deployment)
- [Troubleshooting](#troubleshooting)
- [API Documentation](#api-documentation)

## ✨ Features

### Core Features
- **🔐 JWT Authentication** - Secure token-based authentication
- **📊 Rate Limiting** - Configurable request rate limiting
- **🔄 Dynamic Routing** - Runtime route configuration updates
- **🏥 Health Checks** - Automated backend health monitoring
- **📝 Request Logging** - Comprehensive request/response logging
- **🔌 Plugin System** - Extensible plugin architecture
- **📈 Real-time Metrics** - Live performance monitoring
- **🎨 Kong-Inspired UI** - Professional admin interface

### Technical Features
- **Spring Boot 3.x** - Latest Spring ecosystem
- **PostgreSQL** - Robust data persistence
- **Redis** - High-performance caching
- **Apache Ignite** - Distributed caching
- **Prometheus** - Metrics collection
- **Grafana** - Visualization dashboards
- **Docker Compose** - Container orchestration

## 🏗️ Architecture

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
│  │ • Rate Limiting     │    │ • Real-time Metrics         │  │
│  │ • Authentication    │    │                             │  │
│  └─────────────────────┘    └─────────────────────────────┘  │
│           │                              │                   │
│           └──────────────┬───────────────┘                   │
│                          │                                   │
│  ┌─────────────────────────────────────────────────────────┐  │
│  │             Shared Infrastructure                       │  │
│  │                                                         │  │
│  │ • PostgreSQL (Primary Database)                         │  │
│  │ • Redis (Caching & Rate Limiting)                       │  │
│  │ • Apache Ignite (Distributed Cache)                     │  │
│  │ • Prometheus (Metrics Collection)                       │  │
│  │ • Grafana (Visualization)                               │  │
│  │ • Nginx (Reverse Proxy)                                 │  │
│  └─────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

## 📋 Prerequisites

- **Docker**: Version 20.10 or later
- **Docker Compose**: Version 2.0 or later
- **Git**: For cloning the repository
- **At least 4GB RAM** available for Docker
- **Ports 8080, 8081, 9090, 3000, 5432, 6379** available

## 🚀 Quick Start

### 1. Clone the Repository
```bash
git clone https://github.com/dumijdev/belanova-gateway.git
cd belanova-gateway
```

### 2. Development Environment
```bash
# Copy development environment file
cp .env.development .env

# Start all services
docker-compose up -d

# View logs
docker-compose logs -f
```

### 3. Access the Applications
- **Gateway API**: http://localhost:8080
- **Admin UI**: http://localhost:8081
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (admin/admin)
- **PostgreSQL**: localhost:5432
- **Redis**: localhost:6379

### 4. Test the Gateway
```bash
# Test basic routing
curl http://localhost:8080/api/users

# Test health endpoint
curl http://localhost:8080/actuator/health

# Test rate limiting (make multiple requests quickly)
for i in {1..20}; do curl -s http://localhost:8080/api/users; done
```

## ⚙️ Configuration

### Environment Variables

#### Development (.env.development)
```bash
SPRING_PROFILES_ACTIVE=dev
JWT_SECRET=development-jwt-secret-key-for-belanova-gateway-2024
GATEWAY_RATE_LIMIT_MAX_REQUESTS=1000
LOGGING_LEVEL_COM_BELANOVA_GATEWAY=DEBUG
```

#### Production (.env.production)
```bash
SPRING_PROFILES_ACTIVE=prod
JWT_SECRET=${JWT_SECRET:-production-jwt-secret-key}
POSTGRES_PASSWORD=${POSTGRES_PASSWORD:-change-this-in-production}
REDIS_PASSWORD=${REDIS_PASSWORD:-change-this-in-production}
GRAFANA_ADMIN_PASSWORD=${GRAFANA_ADMIN_PASSWORD:-change-this-in-production}
```

### Service Configuration

#### Gateway Configuration
- **Port**: 8080
- **Health Check**: `/actuator/health`
- **Metrics**: `/actuator/prometheus`
- **Management**: `/actuator/*`

#### Admin UI Configuration
- **Port**: 8081
- **Theme**: Kong-inspired glassmorphism
- **Real-time Updates**: 30-second intervals

## 🐳 Services

### Core Services

#### 1. PostgreSQL Database
- **Image**: postgres:15-alpine
- **Port**: 5432
- **Database**: belanova_gateway
- **User**: belanova
- **Volume**: postgres_data

#### 2. Redis Cache
- **Image**: redis:7-alpine
- **Port**: 6379
- **Password**: belanova123 (configurable)
- **Volume**: redis_data

#### 3. Apache Ignite
- **Image**: apacheignite/ignite:2.16.0
- **Ports**: 10800, 11211, 47100, 47500, 49112
- **Volume**: ignite_data
- **Configuration**: Distributed caching

#### 4. Belanova Gateway
- **Port**: 8080
- **Health Check**: Automatic
- **JVM Options**: Optimized for containers
- **Logging**: Structured JSON

#### 5. Admin UI
- **Port**: 8081
- **Framework**: Vaadin Flow
- **Theme**: Kong-inspired
- **Real-time**: WebSocket updates

### Monitoring Services

#### 6. Prometheus
- **Port**: 9090
- **Configuration**: prometheus.yml
- **Retention**: 200 hours
- **Targets**: All services

#### 7. Grafana
- **Port**: 3000
- **Admin**: admin/admin
- **Dashboards**: Pre-configured
- **Data Source**: Prometheus

### Optional Services

#### 8. Nginx (Production)
- **Ports**: 80, 443
- **SSL**: Configurable
- **Rate Limiting**: Built-in
- **Load Balancing**: Upstream groups

#### 9. Sample Backend (Development)
- **Port**: 8082
- **Purpose**: Testing gateway functionality
- **Endpoints**: /api/users, /api/orders, /api/health

## 📊 Monitoring

### Accessing Monitoring

#### Prometheus
```bash
# Access Prometheus UI
open http://localhost:9090

# Query examples
http_requests_total
jvm_memory_used_bytes
gateway_requests_total
```

#### Grafana
```bash
# Access Grafana
open http://localhost:3000

# Login: admin / admin
# Default dashboards will be available
```

### Health Checks

#### Service Health
```bash
# Gateway health
curl http://localhost:8080/actuator/health

# Admin UI health
curl http://localhost:8081/actuator/health

# Database health
docker-compose exec postgres pg_isready -U belanova
```

#### Custom Health Checks
```bash
# Backend health checks
curl http://localhost:8080/api/health

# Metrics endpoint
curl http://localhost:8080/actuator/metrics
```

## 🛠️ Development

### Development Workflow

#### 1. Start Development Environment
```bash
# Use development configuration
cp .env.development .env

# Start all services
docker-compose up -d

# View specific service logs
docker-compose logs -f gateway
```

#### 2. Code Changes
```bash
# Rebuild specific service
docker-compose build gateway
docker-compose up -d gateway

# Rebuild all services
docker-compose build
docker-compose up -d
```

#### 3. Database Migrations
```bash
# Access database
docker-compose exec postgres psql -U belanova -d belanova_gateway

# View tables
\dt

# Check data
SELECT * FROM backends;
```

#### 4. Testing
```bash
# Run tests
mvn test

# Run specific test
mvn test -Dtest=GatewayTest

# Integration tests
mvn verify
```

### Debugging

#### View Logs
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f gateway

# Last 100 lines
docker-compose logs --tail=100 gateway
```

#### Access Containers
```bash
# Gateway container
docker-compose exec gateway bash

# Database
docker-compose exec postgres psql -U belanova -d belanova_gateway

# Redis
docker-compose exec redis redis-cli -a belanova123
```

## 🚀 Production Deployment

### Production Setup

#### 1. Environment Configuration
```bash
# Copy production environment
cp .env.production .env

# Set production secrets
export JWT_SECRET="your-production-jwt-secret"
export POSTGRES_PASSWORD="your-production-db-password"
export REDIS_PASSWORD="your-production-redis-password"
export GRAFANA_ADMIN_PASSWORD="your-production-grafana-password"
```

#### 2. SSL Configuration (Optional)
```bash
# Create SSL directory
mkdir -p docker/nginx/ssl

# Place certificates
cp your-cert.pem docker/nginx/ssl/cert.pem
cp your-key.pem docker/nginx/ssl/key.pem
```

#### 3. Production Deployment
```bash
# Start production services (including Nginx)
docker-compose --profile production up -d

# Scale services if needed
docker-compose up -d --scale gateway=3
```

#### 4. Backup Configuration
```bash
# Database backup
docker-compose exec postgres pg_dump -U belanova belanova_gateway > backup.sql

# Restore backup
docker-compose exec -T postgres psql -U belanova belanova_gateway < backup.sql
```

### Security Considerations

#### Environment Variables
- Use strong, unique passwords
- Rotate secrets regularly
- Use Docker secrets in production
- Never commit secrets to version control

#### Network Security
- Use internal networks for service communication
- Configure firewall rules
- Enable SSL/TLS for external access
- Use VPN for administrative access

#### Monitoring
- Set up alerts for critical metrics
- Monitor resource usage
- Log security events
- Regular security audits

## 🔧 Troubleshooting

### Common Issues

#### Service Won't Start
```bash
# Check service status
docker-compose ps

# View service logs
docker-compose logs <service-name>

# Restart service
docker-compose restart <service-name>
```

#### Database Connection Issues
```bash
# Check database connectivity
docker-compose exec postgres pg_isready -U belanova

# View database logs
docker-compose logs postgres

# Reset database
docker-compose down -v
docker-compose up -d postgres
```

#### Gateway Not Responding
```bash
# Check gateway health
curl http://localhost:8080/actuator/health

# View gateway logs
docker-compose logs gateway

# Check dependencies
docker-compose ps
```

#### High Memory Usage
```bash
# Monitor resource usage
docker stats

# Adjust JVM settings in docker-compose.yml
environment:
  - JAVA_OPTS=-Xms512m -Xmx1g -XX:+UseG1GC
```

### Performance Tuning

#### JVM Optimization
```yaml
environment:
  - JAVA_OPTS=-Xms1g -Xmx2g -XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0
```

#### Database Optimization
```yaml
environment:
  - POSTGRES_SHARED_BUFFERS=256MB
  - POSTGRES_EFFECTIVE_CACHE_SIZE=1GB
  - POSTGRES_WORK_MEM=4MB
```

#### Redis Optimization
```yaml
command: redis-server --appendonly yes --maxmemory 256mb --maxmemory-policy allkeys-lru
```

## 📚 API Documentation

### Gateway Endpoints

#### Health and Monitoring
- `GET /actuator/health` - Service health status
- `GET /actuator/info` - Service information
- `GET /actuator/metrics` - Application metrics
- `GET /actuator/prometheus` - Prometheus metrics

#### API Routing
- `GET /api/*` - Routed to configured backends
- `POST /api/*` - Routed to configured backends
- `PUT /api/*` - Routed to configured backends
- `DELETE /api/*` - Routed to configured backends

### Admin UI Endpoints

#### Dashboard
- `GET /` - Main dashboard with real-time metrics
- `GET /dashboard` - Alternative dashboard access

#### Management
- `GET /routes` - Route management
- `GET /plugins` - Plugin management
- `GET /backends` - Backend management
- `GET /consumers` - Consumer management
- `GET /monitoring` - Monitoring dashboard

### Configuration Examples

#### Adding a New Route
```json
{
  "routeId": "my-api",
  "path": "/api/my-service/**",
  "targetUri": "http://my-backend:8080",
  "methods": ["GET", "POST"],
  "enabled": true
}
```

#### Configuring Rate Limiting
```json
{
  "maxRequests": 100,
  "windowSizeMs": 60000,
  "enabled": true
}
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🆘 Support

- **Documentation**: https://belanova-gateway.dev/docs
- **Issues**: https://github.com/dumijdev/belanova-gateway/issues
- **Discussions**: https://github.com/dumijdev/belanova-gateway/discussions

---

**🎉 Happy Gateway-ing with Belanova!**

Built with ❤️ using Spring Boot, Vaadin Flow, and Docker
