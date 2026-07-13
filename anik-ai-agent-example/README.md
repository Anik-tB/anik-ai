# Anik AI Agent Example

OpenAPI client usage example project demonstrates how to call the Anik AI server interface through OpenAPI Client.

## Features

- **Agent Management**: Query Agent list and details
- **Session Management**: Create, query, Delete session, view message history
- **synchronous conversation**: Send a message and wait for a complete reply
- **streaming conversation**: Receive AI response streams in real time via SSE

## quick start

### 1. Start the server

Make sure the Anik AI main service is running on the default port:

```bash
# Start the anik-ai-admin service (default port 17888)
cd anik-ai-admin
mvn spring-boot:run
```

### 2. Configure the client

Edit `src/main/resources/application.yml`:

```yaml
anik-ai:
  openapi-client:
    server-host: localhost # Server host
    server-port: 17888 # Server port
    connect-timeout: 30000 #Connection timeout (milliseconds)
    read-timeout: 300000 # Read timeout (milliseconds)
```

### 3. Start the sample application

```bash
cd anik-ai-agent-example
mvn spring-boot:run
```

The application will be launched at `http://localhost:17889`

### 4. Access Swagger UI

Open a browser and visit:

```
http://localhost:17889/swagger-ui.html
```

## API interface description

### Agent related

- `GET /demo/agents` - Get a list of all Agents
- `GET /demo/agent/{agentId}` - Get Agent details

### Session management

- `POST /demo/agent/{agentId}/conversation` - Create session
- `GET /demo/agent/{agentId}/conversations` - Get session list (pagination)
- `GET /demo/agent/{agentId}/conversation/{conversationId}/messages` - Get message history
- `DELETE /demo/agent/{agentId}/conversation/{conversationId}` - Delete session

### conversational interface

- `POST /demo/agent/{agentId}/chat/sync` - synchronous conversation
- `GET /demo/agent/{agentId}/chat/stream` - streaming conversation（SSE）

## Usage example

### 1. Get the Agent list

```bash
curl -X GET "http://localhost:17889/demo/agents" \
  -H "App-Id: your-app-id" \
  -H "Token: your-token"
```

### 2. Create session

```bash
curl -X POST "http://localhost:17889/demo/agent/1/conversation" \
  -H "App-Id: your-app-id" \
  -H "Token: your-token" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123"
  }'
```

### 3. synchronous conversation

```bash
curl -X POST "http://localhost:17889/demo/agent/1/chat/sync" \
  -H "App-Id: your-app-id" \
  -H "Token: your-token" \
  -H "Content-Type: application/json" \
  -d '{
    "conversationId": "conv-123",
    "content": "Hello, introduce yourself"
  }'
```

### 4. streaming conversation

```bash
curl -X GET "http://localhost:17889/demo/agent/1/chat/stream?content=Hello" \
  -H "App-Id: your-app-id" \
  -H "Token: your-token" \
  --no-buffer
```

Or use EventSource in the browser:

```javascript
const eventSource = new EventSource(
  'http://localhost:17889/demo/agent/1/chat/stream?content=Hello'
);

eventSource.addEventListener('text', (e) => {
  console.log('AI reply:', e.data);
});

eventSource.addEventListener('thinking', (e) => {
  console.log('AI thinking:', e.data);
});

eventSource.addEventListener('done', (e) => {
  console.log('Conversation completed:', e.data);
  eventSource.close();
});

eventSource.addEventListener('error', (e) => {
  console.error('Error:', e.data);
  eventSource.close();
});
```

## Things to note

1. **Authentication information**: All requests need to carry `App-Id` and `Token` in the Header
2. **Timeout configuration**: It is recommended to set a longer timeout (5 minutes) for streaming conversation
3. **Session ID**: conversationId format is a string (such as "conv-123"), non-numeric ID
4. **SSE support**: The streaming interface returns SSE event stream, and the client needs to support Server-Sent Events

## technology stack

- Spring Boot 3.4.1
- Anik AI OpenAPI Client
- Springdoc OpenAPI (Swagger UI)
- Lombok

## Project structure

```
anik-ai-agent-example/
├── src/main/java/
│   └── com/aianik/anik/ai/agent/example/
│ ├── AnikAiAgentExampleApplication.java # Startup class
│       ├── config/
│ │ └── SwaggerConfig.java # Swagger configuration
│       └── controller/
│ └── OpenApiDemoController.java # Example Controller
└── src/main/resources/
    └── application.yml # Configuration file
```

## Development suggestions

1. Use Swagger UI for interface testing without writing test code
2. Refer to the sample code in `OpenApiDemoController` and integrate it into your own project
3. It is recommended to use asynchronous processing for streaming conversation to avoid blocking the main thread
4. It is recommended to add complete abnormal processing and logging in the production environment.

## FAQ

**Q: What should I do if compilation fails? **

A: Make sure to compile the parent project first:
```bash
cd anik-ai
mvn clean install -DskipTests
```

**Q: Failed to connect to the server? **

A: Check the following points:
- Whether the server starts normally
- Are `server-host` and `server-port` in `application.yml` correct?
- Whether the firewall allows access

**Q: Swagger UI cannot be opened? **

A: Confirm that port 17889 is not occupied, you can modify `server.port` in `application.yml`

## More resources

- [Anik AI official document](https://github.com/openanik/anik-ai)
- [OpenAPI Specification](https://swagger.io/specification/)
- [SSE Standard](https://html.spec.whatwg.org/multipage/server-sent-events.html)
