# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

拾壹博客 (Shiyi Blog) - a full-stack blog platform with admin dashboard, public blog, and mobile app.

## Architecture

### Backend (`blog/`)
Spring Boot 2.7.0 multi-module Maven project (JDK 1.8):

| Module | Purpose |
|--------|---------|
| `mojian-server` | Application entry point (`NeatAdminApplication.java`) |
| `mojian-admin` | Admin API controllers (dashboard, articles, messages, monitoring, system config) |
| `mojian-api` | Public portal API (articles, comments, chat, albums, moments, user) |
| `mojian-commom` | Shared entities, mappers, DTOs, configs, utilities |
| `mojian-auth` | Authentication (QQ, Weibo, Gitee, GitHub OAuth) |
| `mojian-file` | File storage abstraction (七牛云 OSS or local) |
| `mojian-quartz` | Scheduled tasks |

Key packages in `mojian-commom`:
- `entity/` - Database entities (MyBatis-Plus `@TableName` annotations)
- `mapper/` - MyBatis mapper interfaces
- `dto/` - Data transfer objects
- `config/` - Spring configs (Redis, MyBatis-Plus, CORS, WebMvc)

### Frontend

| Directory | Stack | Port |
|-----------|-------|------|
| `blog-web/` | Vue 2 + Vuex + Element UI (Vite) | 3000 |
| `blog-admin/` | Vue 3 + Pinia + Element Plus + TypeScript (Vite) | 3000 |
| `uniapp-blog/` | UniApp mobile app | - |

### Tech Stack
- **Auth**: Sa-Token 1.39.0 (not Spring Security)
- **ORM**: MyBatis-Plus 3.5.2 with XML mappers in `mojian-server/src/main/resources/mapper/`
- **API Docs**: Knife4j (Swagger)
- **Cache**: Redis (required)
- **Search**: Elasticsearch 7.9.2 (optional, can use MySQL instead)
- **File Storage**: x-file-storage library (七牛云 OSS or local upload)

## Build & Run Commands

### Backend (Maven)
```bash
cd blog
mvn clean package              # Build all modules
mvn spring-boot:run -pl mojian-server  # Run server (port 8800)
```

### Frontend (npm)
```bash
cd blog-web && npm install && npm run dev    # Dev server (port 3000)
cd blog-admin && npm install && npm run dev  # Admin dev server (port 3000)
npm run build  # Production build (in respective directory)
```

### API Documentation
- Local: http://127.0.0.1:8800/shiyi/doc.html
- Credentials: test / 123456

## Database Setup

1. Import `mj-blog.sql` into MySQL database named `blog`
2. Configure connection in `blog/mojian-server/src/main/resources/application-dev.yml`
3. Default credentials: admin / 123456

## Configuration Files

| File | Purpose |
|------|---------|
| `blog/mojian-server/src/main/resources/application.yml` | Sa-Token, MyBatis-Plus, Swagger config |
| `blog/mojian-server/src/main/resources/application-dev.yml` | Database, Redis, Knife4j, OAuth credentials |
| `blog-web/.env.development` | Frontend dev environment (API URL, WebSocket) |
| `blog-admin/.env.development` | Admin dev environment |

## Key Patterns

- **Controller naming**: `Sys*Controller` for admin APIs, plain names for public APIs
- **Service layer**: Interface + `*Impl` pattern in each module
- **Mapper XML**: Complex queries in XML, simple CRUD via MyBatis-Plus annotations
- **Entity base fields**: `createBy`, `createTime`, `updateBy`, `updateTime` (auto-filled by `MyMetaObjectHandler`)
- **Response wrapper**: All APIs return `Result<T>` object
- **WebSocket**: Chat functionality at `/websocket/` endpoint

## Important Notes

- **Start backend first** - frontend config is dynamically loaded from backend API
- **Elasticsearch optional** - can switch to MySQL search mode via configuration
- **File upload configurable** - supports 七牛云 OSS or local storage
- **Module typo**: `mojian-commom` is intentional (not `mojian-common`)
- **Context path**: Backend API prefixed with `/moian/` in production
