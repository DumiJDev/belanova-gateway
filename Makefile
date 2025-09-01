# Belanova Gateway - Docker Compose Management
.PHONY: help build up down restart logs clean dev prod test health backup restore

# Default target
help: ## Show this help message
	@echo "Belanova Gateway - Docker Compose Management"
	@echo ""
	@echo "Available commands:"
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / {printf "  %-15s %s\n", $$1, $$2}' $(MAKEFILE_LIST)

# Development commands
dev: ## Start development environment
	@echo "🚀 Starting Belanova Gateway (Development)..."
	cp .env.development .env
	docker-compose up -d
	@echo "✅ Development environment started!"
	@echo "📊 Gateway: http://localhost:8080"
	@echo "🎨 Admin UI: http://localhost:8081"
	@echo "📈 Prometheus: http://localhost:9090"
	@echo "📊 Grafana: http://localhost:3000"

dev-build: ## Build and start development environment
	@echo "🔨 Building and starting development environment..."
	cp .env.development .env
	docker-compose build --no-cache
	docker-compose up -d
	@echo "✅ Development environment built and started!"

dev-logs: ## View development logs
	docker-compose logs -f

# Production commands
prod: ## Start production environment
	@echo "🚀 Starting Belanova Gateway (Production)..."
	cp .env.production .env
	docker-compose --profile production up -d
	@echo "✅ Production environment started!"

prod-build: ## Build and start production environment
	@echo "🔨 Building and starting production environment..."
	cp .env.production .env
	docker-compose --profile production build --no-cache
	docker-compose --profile production up -d
	@echo "✅ Production environment built and started!"

# Core commands
build: ## Build all services
	@echo "🔨 Building all services..."
	docker-compose build --no-cache

up: ## Start all services
	@echo "🚀 Starting all services..."
	docker-compose up -d

down: ## Stop all services
	@echo "🛑 Stopping all services..."
	docker-compose down

restart: ## Restart all services
	@echo "🔄 Restarting all services..."
	docker-compose restart

# Service-specific commands
gateway-logs: ## View gateway logs
	docker-compose logs -f gateway

admin-logs: ## View admin UI logs
	docker-compose logs -f admin-ui

db-logs: ## View database logs
	docker-compose logs -f postgres

redis-logs: ## View Redis logs
	docker-compose logs -f redis

# Health and monitoring
health: ## Check health of all services
	@echo "🏥 Checking service health..."
	@echo "Gateway: $$(curl -s http://localhost:8080/actuator/health | jq -r '.status' 2>/dev/null || echo 'DOWN')"
	@echo "Admin UI: $$(curl -s http://localhost:8081/actuator/health | jq -r '.status' 2>/dev/null || echo 'DOWN')"
	@echo "PostgreSQL: $$(docker-compose exec -T postgres pg_isready -U belanova >/dev/null 2>&1 && echo 'UP' || echo 'DOWN')"
	@echo "Redis: $$(docker-compose exec -T redis redis-cli ping >/dev/null 2>&1 && echo 'UP' || echo 'DOWN')"

status: ## Show status of all services
	docker-compose ps

# Database commands
db-connect: ## Connect to PostgreSQL database
	docker-compose exec postgres psql -U belanova -d belanova_gateway

db-backup: ## Backup database
	@echo "💾 Creating database backup..."
	docker-compose exec postgres pg_dump -U belanova belanova_gateway > backup_$$(date +%Y%m%d_%H%M%S).sql
	@echo "✅ Backup created: backup_$$(date +%Y%m%d_%H%M%S).sql"

db-restore: ## Restore database from backup (usage: make db-restore FILE=backup.sql)
	@echo "🔄 Restoring database from $(FILE)..."
	docker-compose exec -T postgres psql -U belanova belanova_gateway < $(FILE)
	@echo "✅ Database restored!"

# Redis commands
redis-connect: ## Connect to Redis
	docker-compose exec redis redis-cli -a belanova123

redis-flush: ## Flush Redis cache
	docker-compose exec redis redis-cli -a belanova123 FLUSHALL

# Testing commands
test: ## Run tests
	@echo "🧪 Running tests..."
	mvn test

test-gateway: ## Run gateway tests
	@echo "🧪 Running gateway tests..."
	mvn test -pl gateway

test-admin: ## Run admin UI tests
	@echo "🧪 Running admin UI tests..."
	mvn test -pl admin-ui

integration-test: ## Run integration tests
	@echo "🧪 Running integration tests..."
	mvn verify

# Cleanup commands
clean: ## Clean up containers and volumes
	@echo "🧹 Cleaning up..."
	docker-compose down -v
	docker system prune -f

clean-logs: ## Clean up log files
	@echo "🧹 Cleaning up logs..."
	rm -rf logs/*
	@echo "✅ Logs cleaned!"

clean-all: ## Clean up everything including images
	@echo "🧹 Deep cleaning..."
	docker-compose down -v --rmi all
	docker system prune -f --volumes
	@echo "✅ Everything cleaned!"

# Utility commands
shell-gateway: ## Get shell access to gateway container
	docker-compose exec gateway bash

shell-admin: ## Get shell access to admin UI container
	docker-compose exec admin-ui bash

shell-db: ## Get shell access to database container
	docker-compose exec postgres bash

shell-redis: ## Get shell access to Redis container
	docker-compose exec redis bash

# Development helpers
format: ## Format code
	@echo "🎨 Formatting code..."
	mvn spotless:apply

lint: ## Run linting
	@echo "🔍 Running linting..."
	mvn spotless:check

deps: ## Update dependencies
	@echo "📦 Updating dependencies..."
	mvn versions:display-dependency-updates

# Quick test commands
test-api: ## Test API endpoints
	@echo "🧪 Testing API endpoints..."
	curl -s http://localhost:8080/api/users | jq . || echo "Gateway not responding"
	curl -s http://localhost:8080/actuator/health | jq . || echo "Health check failed"

test-rate-limit: ## Test rate limiting
	@echo "🧪 Testing rate limiting..."
	for i in {1..15}; do \
		curl -s -w "%{http_code}\n" http://localhost:8080/api/users -o /dev/null; \
	done | grep -c "429" | xargs echo "Rate limit hits:"

# Information commands
info: ## Show system information
	@echo "ℹ️  Belanova Gateway Information"
	@echo "================================="
	@echo "Environment: $$(cat .env | grep SPRING_PROFILES_ACTIVE | cut -d'=' -f2)"
	@echo "Services running: $$(docker-compose ps --services --filter "status=running" | wc -l)"
	@echo "Total containers: $$(docker-compose ps --services | wc -l)"
	@echo "Database size: $$(docker-compose exec -T postgres psql -U belanova -d belanova_gateway -c "SELECT pg_size_pretty(pg_database_size('belanova_gateway'));" 2>/dev/null | tail -1 | tr -d ' ' || echo 'N/A')"
	@echo ""
	@echo "📊 Service URLs:"
	@echo "  Gateway:     http://localhost:8080"
	@echo "  Admin UI:    http://localhost:8081"
	@echo "  Prometheus:  http://localhost:9090"
	@echo "  Grafana:     http://localhost:3000"
	@echo "  PostgreSQL:  localhost:5432"
	@echo "  Redis:       localhost:6379"

version: ## Show version information
	@echo "Belanova Gateway v1.0.0"
	@echo "Built with Spring Boot 3.x and Vaadin Flow"
	@echo "Docker Compose deployment ready"

# Emergency commands
emergency-stop: ## Emergency stop all services
	@echo "🚨 Emergency stop!"
	docker-compose down --timeout 10
	docker stop $$(docker ps -q) 2>/dev/null || true

emergency-clean: ## Emergency cleanup
	@echo "🚨 Emergency cleanup!"
	docker-compose down -v --remove-orphans
	docker system prune -f --volumes