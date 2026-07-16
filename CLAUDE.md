# CLAUDE.md

This file provides guidance to kscc (otsffssent.com/code) when working with code in this repository.

## Project Overview

LyraSuite is a Java security research tool — a Spring Boot web application that generates Java deserialization exploit payloads and hosts JNDI exploitation servers. It is a single-module Maven project (not a git repository).

## Build & Run Commands

```bash
# Build (produces executable JAR at target/LyraSuite-Web-2.0.0.jar)
./mvnw clean package

# Run via Maven
./mvnw spring-boot:run

# Run the built JAR directly
java -jar target/LyraSuite-Web-2.0.0.jar

# Run tests (none exist currently — no src/test/ directory)
./mvnw test
```

No linting tools (Checkstyle, SpotBugs, PMD) are configured.

## Runtime Configuration

- Server: `0.0.0.0:8080` (configured in `src/main/resources/application.properties`)
- Auth: Spring Security with in-memory user (`admin`/`admin123` from properties)
- CSRF is disabled; session cookie name is `lyrasuite_session`
- The main class sets two system properties in a static block to enable unsafe deserialization: `sun.rmi.registry.registryFilter=*` and `org.apache.commons.collections.enableUnsafeSerialization=true`

## Architecture

### Layer Structure

The codebase follows a standard Spring Boot MVC pattern under `com.xyuxu.javasec`:

```
controller/     → REST API endpoints (/api/*) + view routing (ViewController)
service/        → Business logic, delegates to core generators
core/
  payload/gadgets/   → Deserialization gadget chain implementations (the heart of the project)
  payload/jndi/      → JNDI bypass techniques
  generator/         → Higher-level payload generators (serialize gadget → bytes)
  server/            → Embedded HTTP/JNDI (RMI+LDAP) servers
dto/            → Request/response objects (Lombok @Data)
utils/          → Crypto, serialization, reflection, networking helpers
config/         → Spring Security configuration
```

### Two Core Payload Interfaces

Every gadget chain implements one or both of these interfaces in `core/payload/gadgets/`:

- **`ObjectPayload<T>`** — `getObject(String command)`: generates a deserialization object from a command string. All gadget chains implement this.
- **`BytecodePayload<T>`** — `getObjectWithBytecode(byte[] bytecode)`: generates a deserialization object from raw bytecode (for echo/memory-shell injection). Only some chains implement both (e.g., CommonsCollectionsK1/K2, SpringJackson variants).

### Gadget Discovery

Gadget chains are discovered at startup via the Reflections library (`PayloadUtils`). All concrete (non-abstract, non-interface) classes implementing `ObjectPayload` in the `gadgets` package are auto-registered. The class simple name is the gadget identifier used in API calls (e.g., `"CommonsCollections6"`, `"SpringJacksonHashMap"`).

To add a new gadget chain: create a class in `core/payload/gadgets/` implementing `ObjectPayload` (and optionally `BytecodePayload`). It will be auto-discovered — no registration code needed.

### Generator Layer

`core/generator/` contains higher-level generators that orchestrate the flow:
1. `GadgetsGenerator.generateBytes(gadgetType, command, useDirtyData)` — instantiates an `ObjectPayload`, calls `getObject()`, optionally wraps with dirty data, serializes to bytes.
2. `GadgetsGenerator.generateEchoBytes(gadgetType, echoType)` — same but for `BytecodePayload` with echo bytecode injection.
3. Other generators: `FastjsonPaylodGenerator`, `SnakeYamlPayloadGenerator`, `RemotePayloadGenerator`, `EchoByteCodeGenerator`, `TemplatesBytecodeGenerator`.

### Web Modules (7 modules)

Each module has a view page (Thymeleaf template in `templates/pages/`), a REST controller (`/api/*`), and a service:

| Module | Controller | Service | Purpose |
|---|---|---|---|
| gadget | `GadgetController` | `GadgetService` | Generate serialized deserialization payloads |
| jndi | `JndiController` | `JndiManageService` + `JNDIExploitService` | Start/stop embedded RMI+LDAP servers |
| fastjson | `FastjsonController` | `FastjsonService` | Fastjson exploitation payloads |
| shiro | `ShiroController` | `ShiroExploitService` | Apache Shiro key scanning & gadget exploitation |
| snakeyaml | `SnakeYamlController` | `SnakeYamlService` | SnakeYAML deserialization payloads |
| codec | (inline JS) | — | Encoding/decoding utilities (Base64, Hex, URL) |
| memshell | (inline JS) | — | Memory shell generation |

`ViewController` serves the Thymeleaf pages; all mutation logic lives in the `/api/*` REST controllers.

### Frontend

- Thymeleaf server-side templates with a shared layout (`layout.html`) and fragments (`fragments/sidebar.html`, `fragments/footer.html`)
- Tabler CSS framework for UI
- Vanilla JS with axios for AJAX calls to the REST API; some pages use htmx
- JS modules in `static/js/modules/` (gadget.js, jndi.js, shiro.js)

### Key Utilities

- `PayloadUtils` — gadget class discovery, caching, instantiation (uses Reflections library)
- `Serializer` — Java object serialization to byte arrays
- `Reflections` — reflective field setting and constructor invocation (wrapper around `java.lang.reflect`)
- `DirtyDataWrapper` — wraps payloads with padding data to evade detection
- `ShiroCryptoUtil` — Shiro encryption/decryption operations
- `ProbeUtil` — help text and metadata for gadget chains

## JDK 21 Module Exports

The `pom.xml` configures extensive `--add-exports` flags to access internal JDK APIs (com.sun.org.apache.xalan, sun.rmi, sun.reflect.annotation, etc.). These are required for the gadget chains to compile and run. If you add code that uses internal JDK APIs, add the corresponding `--add-exports` to the compiler plugin configuration.

## Language

Code comments and UI strings are primarily in Chinese (Simplified). The codebase uses Chinese for error messages, help text, and log output.
