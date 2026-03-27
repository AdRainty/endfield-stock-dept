# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**终末地调度券交易模拟系统** (Endfield Trading System) - A trading simulation system based on the game "Arknights: Endfield". Features dual exchanges, real-time price fluctuations, AI-generated news, and a Redis-based matching engine.

## Tech Stack

| Layer | Technology |
|-------|------------|
| **Backend** | Spring Boot 3.2.0, JDK 17, MyBatis Plus, Sa-Token |
| **Database** | PostgreSQL 12+ with Liquibase migrations |
| **Cache/Queue** | Redis + Redisson (matching engine uses ZSet) |
| **AI** | Spring AI OpenAI integration |
| **Frontend** | React 18, Vite 5, Ant Design 5, Recharts |

## Build & Run Commands

### Backend (stock-backend/)
```bash
# Build
mvn clean package

# Run (dev)
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Run (prod)
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### Frontend (stock-frontend/)
```bash
# Install deps
npm install

# Dev server
npm start

# Build
npm run build

# Lint
npm run lint
```

## Architecture

### Backend Structure (`stock-backend/src/main/java/com/adrainty/stock/`)
- **controller/** - REST APIs: Auth, Exchange, Market, Trade, Admin, News, User
- **service/** - Business logic with impl classes
- **entity/** - Database entities (User, Order, Position, Exchange, Instrument, News, etc.)
- **mapper/** - MyBatis mappers
- **config/** - Redis, Sa-Token, WebSocket, Knife4j configurations
- **exception/** - Global exception handling

### Frontend Structure (`stock-frontend/src/`)
- **pages/** - Login, Market, Trade, Position, Admin (Users/Exchange), Profile, OrdersHistory, Leaderboard
- **layouts/** - BasicLayout with navigation
- **components/** - Shared components (KlineChart, etc.)
- **services/** - API client wrappers

### Key Systems
1. **Matching Engine** - Redis ZSet-based order matching (price-time priority, 100ms cycle)
2. **News System** - AI-generated news with sentiment analysis affecting prices
3. **Trading Hours** - Morning 9:30-11:30, Afternoon 13:00-15:00, call auction at 9:15
4. **No-Cancel Window** - 20:00 to next day 9:30 order cancellation blocked

## Environment Variables

```bash
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=endfield
DB_USERNAME=endfield
DB_PASSWORD=endfield

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# OpenAI (for news generation)
OPENAI_API_KEY=your-key
OPENAI_BASE_URL=https://api.openai.com/v1
OPENAI_MODEL=gpt-4o-mini
```

## API Documentation

Access Swagger/Knife4j docs at: `http://localhost:8081/api/doc.html`

## Key Files

- `task.md` - Development task list and system documentation
- `stock-backend/src/main/resources/db/changelog/` - Liquibase database migrations
- `stock-backend/src/main/resources/application.yml` - Main configuration
- `stock-backend/src/main/resources/application-dev.yml` - Dev overrides
- `stock-frontend/vite.config.js` - Frontend build config with API proxy
