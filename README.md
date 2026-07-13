<p align="center">
  <img alt="Anik-AI Logo" src="docs/images/logo.png" width="180px"/>
</p>

<h1 align="center">Anik AI</h1>

<p align="center">
  <strong>A flexible, extensible, enterprise-grade AI Agent Platform</strong><br/>
  Built on Spring Boot 4 + Spring AI &nbsp;·&nbsp; Multi-model &nbsp;·&nbsp; RAG &nbsp;·&nbsp; Memory &nbsp;·&nbsp; Skills &nbsp;·&nbsp; OpenAPI
</p>

<p align="center">
  <a href="LICENSE"><img src="https://img.shields.io/badge/license-Apache%202.0-blue.svg" alt="License"/></a>
  <img src="https://img.shields.io/badge/Java-25-orange.svg" alt="Java 25"/>
  <img src="https://img.shields.io/badge/Spring%20Boot-4.0-brightgreen.svg" alt="Spring Boot 4"/>
  <img src="https://img.shields.io/badge/Spring%20AI-latest-6DB33F.svg" alt="Spring AI"/>
  <img src="https://img.shields.io/badge/H2-embedded-blueviolet.svg" alt="H2 Database"/>
</p>

---

## 🧠 What is Anik AI?

**Anik AI** is a self-hosted, out-of-the-box enterprise AI agent platform. Connect multiple large language models (LLMs), build and orchestrate intelligent agents, manage knowledge bases with RAG (Retrieval-Augmented Generation), and expose everything through a clean admin UI and standardized OpenAPI — all from a single deployable JAR.

> Think of it as your **self-hosted AI backend** — wire up your models, create agents with persistent memory and reusable skills, and integrate via a clean REST API.

---

## ✨ Core Features

| Feature | Description |
|---|---|
| 🤖 **Multi-model Management** | Connect and switch between multiple LLM providers (OpenAI, etc.) from one place |
| 🧠 **Agent Orchestration** | Build composable, multi-agent workflows with tool and skill support |
| 📚 **RAG Knowledge Base** | Vector-based retrieval over your documents — supports Milvus & Elasticsearch |
| 💾 **Long-term Memory** | Agents remember conversations across sessions for contextual coherence |
| 🔧 **Skill Management** | Extend agent capabilities with configurable, reusable skill definitions |
| 📁 **Resource Management** | Upload and parse documents (PDF, Office); object storage via MinIO or local disk |
| 🌐 **OpenAPI** | Standardized REST API for seamless third-party integrations |
| 🖥️ **Admin Dashboard** | Modern management UI built on SoybeanAdmin (Vue 3) |

---

## 🏗️ Project Structure

```
anik-ai/
├── anik-ai-admin/          # Admin REST API layer (Spring MVC controllers, services)
├── anik-ai-agent/          # Agent orchestration core
├── anik-ai-commons/        # Shared utilities, exceptions, version info
├── anik-ai-features/       # Feature modules: RAG, memory, skills, models, resources
├── anik-ai-openapi/        # Public OpenAPI module for third-party access
├── anik-ai-persistence/    # Database layer (MyBatis-Plus, mappers, POs)
├── anik-ai-starter/        # Spring Boot launcher — packages everything into one JAR
│   └── src/main/resources/
│       ├── application.yml         # Main config (port, DB, storage)
│       ├── anik_ai_schema.sql      # Auto-loaded DB schema + seed data
│       └── admin/                  # Compiled frontend assets (served at /anik-ai/)
└── docs/
    └── sql/anik_ai_schema.sql      # Reference copy of the schema
```

---

## ⚡ Quick Start

### Prerequisites

| Tool | Version | Notes |
|---|---|---|
| **Java JDK** | 25+ | [Download](https://www.oracle.com/java/technologies/downloads/) |
| **Maven** | 3.9+ | Bundled with IntelliJ, or install separately |

> ✅ **No database installation needed!** Anik AI uses an embedded H2 in-memory database that is set up automatically on every start.

---

### 1. Clone the repository

```bash
git clone https://github.com/Anik-tB/anik-ai.git
cd anik-ai
```

### 2. Build the project

Run this once from the project root to compile all modules and package the executable JAR:

```bash
mvn clean package -DskipTests
```

This produces the runnable JAR at:
```
anik-ai-starter/target/anik-ai-server-exec.jar
```

### 3. Start the server

```bash
java -jar anik-ai-starter/target/anik-ai-server-exec.jar
```

Wait about **20–25 seconds** for the server to fully boot. You will see this in the logs when it is ready:

```
:: Anik Ai ::
anik-job server started successfully
Started AnikAiSpringbootApplication in ~21 seconds
```

### 4. Open the Admin Dashboard

👉 **[http://localhost:8080/anik-ai/](http://localhost:8080/anik-ai/)**

| Field | Value |
|---|---|
| Username | `admin` |
| Password | `123456` |

---

## ⚙️ Configuration

All configuration lives in [`anik-ai-starter/src/main/resources/application.yml`](anik-ai-starter/src/main/resources/application.yml).

### Key settings

```yaml
server:
  port: 8080                        # Change the HTTP port here
  servlet:
    context-path: /anik-ai          # App is served under this path

spring:
  profiles:
    active: dev                     # Switch to 'prod' for production

  datasource:
    url: jdbc:h2:mem:anik_ai        # In-memory H2 (default, no setup needed)
    # For MySQL, replace with:
    # driver-class-name: com.mysql.cj.jdbc.Driver
    # url: jdbc:mysql://localhost:3306/anik_ai

anik-ai:
  resource:
    storage-type: LOCAL             # LOCAL or MINIO
    upload-dir: ./upload/resource   # Local file upload directory
  memory:
    short-term:
      store-type: memory            # 'memory' (fast) or 'db' (persistent)
```

---

## 🗄️ Database

### Default — H2 In-Memory

- **No setup required.** The schema and seed data in [`anik_ai_schema.sql`](anik-ai-starter/src/main/resources/anik_ai_schema.sql) are applied automatically on every startup.
- ⚠️ Data is **wiped on restart** (in-memory). Ideal for development and testing.

### Switching to MySQL (production / persistent data)

1. Create a MySQL database named `anik_ai`
2. Run [`docs/sql/anik_ai_schema.sql`](docs/sql/anik_ai_schema.sql) in your MySQL client
3. Update `application.yml`:
   ```yaml
   spring:
     datasource:
       driver-class-name: com.mysql.cj.jdbc.Driver
       url: jdbc:mysql://localhost:3306/anik_ai?useUnicode=true&characterEncoding=utf8
       username: your_user
       password: your_password
     sql:
       init:
         mode: never   # Disable auto schema init (you already ran it manually)
   ```
4. Rebuild and restart the server

---

## 🔌 OpenAPI / REST

The OpenAPI module exposes a standardized REST interface for integrating Anik AI into your own applications.

- **Base URL:** `http://localhost:8080/anik-ai/`
- **Authentication:** Pass a JWT token via the `Anik-Ai-Auth` header, obtained from the login endpoint.

---

## 🛠️ Development Tips

| Topic | Detail |
|---|---|
| **Hot rebuild** | `mvn package -DskipTests -pl anik-ai-starter -am` then restart |
| **Logs** | Configured via [`logback-boot.xml`](anik-ai-starter/src/main/resources/logback-boot.xml) |
| **gRPC** | Internal gRPC service runs on port `18888` |
| **File uploads** | Stored under `./upload/` relative to where the JAR is launched |

---

## 🤝 Contributing

Contributions are welcome! Here is how to get started:

1. **Fork** the repository and create a feature branch from `main`
2. **Make your changes** — ensure the code compiles and tests pass
3. **Open a Pull Request** with a clear description of what you changed and why

Please follow the existing code style and keep PRs focused on a single concern.

---

## 📄 License

Anik AI is open-sourced under the [Apache License 2.0](LICENSE).

When using this project, please note:
1. Do not remove or modify the source attribution comments in the code.
2. Must not be used for any illegal purposes or activities that endanger national security.

---

<p align="center">Made with ❤️ by the Anik AI team</p>
