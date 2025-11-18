# MaxBot

An AI-powered conversational portfolio assistant built on Retrieval-Augmented Generation (RAG). MaxBot combines a language model with vector embeddings to provide intelligent, context-aware responses about professional background through natural language conversations.

## Core Concept

MaxBot leverages two key AI technologies:
- **Large Language Model (LLM)**: Processes natural language questions and generates conversational responses
- **Retrieval-Augmented Generation (RAG)**: Vector embeddings with semantic search to ground responses in indexed documents, eliminating hallucination and ensuring accuracy

The system enables visitors to discover professional information by asking natural language questions, with all responses backed by indexed PDF documents.

## Architecture

### Technology Stack

**Full-Stack Architecture**:
```
Next.js Frontend (TypeScript, React 19)
       ↓ HTTP/HTTPS
   API Routes (proxy)
       ↓
Spring Boot Backend (Java 21, Spring AI)
       ├─ RAG Pipeline (Spring AI abstraction)
       │  ├─ Embedding Generation (configurable provider)
       │  ├─ Vector Store (configurable)
       │  └─ QuestionAnswerAdvisor (top-10 retrieval)
       │
       └─ LLM Integration (configurable provider)
          └─ Chat Completions via Spring AI
```

### LLM & RAG Workflow

**1. Document Ingestion (PDF → Vector Store)**:
- User uploads PDF via `/api/embedding/uploadPdf`
- Backend extracts text and chunks by token boundaries
- Embeddings are generated for each chunk via configured provider
- Embeddings stored in configured vector store with metadata

**2. User Query Processing (Natural Language → Response)**:
- Frontend sends user message to `/api/chat` endpoint
- Backend receives question and converts to embedding via configured provider
- Vector store performs semantic search (returns top 10 similar chunks)
- System prompt + retrieved context + user question sent to configured LLM
- LLM generates response grounded in actual documents
- Response streamed back to frontend as Server-Sent Events

**3. Response Delivery (Stream → UI)**:
- Response chunks arrive in real-time
- Markdown rendering with syntax highlighting
- User can edit messages and regenerate responses
- Complete conversation history maintained

### Backend Architecture

**Spring Boot 3.5.7 (Java 21)**

**Configuration & Profiles**:
- `application.yaml` (shared) - LLM provider configuration, vector store index, rate limiting (450 req/60s), system prompt
- `application-dev.yaml` - HTTP port 8080, DEBUG logging
- `application-prod.yaml` - HTTPS port 8443, Let's Encrypt SSL with auto-reload

**Components**:

*REST Controllers*:
- **ChatController** (`POST /api/chat`) - Orchestrates LLM interaction with RAG
  - Accepts user message via ChatRequest
  - Calls ChatService to process through RAG pipeline
  - Returns streaming response (Flux<String>) via Server-Sent Events
  - Authenticated via X-API-KEY header (APP_CHAT_KEY)
  - Rate limited: 450 requests per 60 seconds

- **EmbeddingController** (`POST /api/embedding/uploadPdf`, `DELETE /api/embedding/*`) - Manages knowledge base
  - `/api/embedding/uploadPdf` - Accepts PDF, extracts text, generates embeddings, stores in configured vector store
  - `/api/embedding/deleteByIds` - Deletes documents by ID
  - `/api/embedding/deleteByFileName` - Deletes documents by filename
  - Authenticated via X-API-KEY header (APP_EMBEDDING_KEY)
  - Rate limited: 450 requests per 60 seconds

*AI & LLM Services*:
- **ChatService** - RAG pipeline orchestration
  - Uses Spring AI's `ChatClient.Builder` for LLM integration
  - Implements `QuestionAnswerAdvisor` for RAG (top-K=10 retrieval from configured vector store)
  - Uses `SimpleLoggerAdvisor` for debug logging (shows retrieved documents)
  - Loads system prompt from `ai.system-prompt` configuration
  - Method: `Flux<String> chatRequest(ChatRequest)` - Returns streaming response

- **EmbeddingService** - Document processing for RAG
  - Uses `PagePdfDocumentReader` for PDF text extraction
  - Uses `TokenTextSplitter` for intelligent chunking
  - Generates embeddings via configured embedding provider
  - Stores vectors in configured vector store with file metadata
  - Methods: `pdfToVectorStore()`, `deleteByIds()`, `deleteByFileName()`

*Security & Filtering*:
- **ApiKeyFilter** - HTTP filter for API key validation
  - Extracts X-API-KEY header from requests
  - Routes to correct key based on endpoint (APP_CHAT_KEY vs APP_EMBEDDING_KEY)
  - Returns 401 Unauthorized if validation fails
  - Skips OPTIONS preflight requests

- **CorsConfig** - CORS filter configuration
  - Allowed Origins: `http://localhost:3000` (dev), `https://YOUR_DOMAIN`, `https://www.YOUR_DOMAIN`, `https://YOUR_APP_DOMAIN` (prod)
  - Allowed Methods: POST, DELETE, OPTIONS
  - Allowed Headers: Content-Type, X-API-KEY

- **GlobalExceptionHandler** - Centralized exception handling
  - Catches `RequestNotPermitted` (rate limit exceeded) → 429 status
  - Catches generic exceptions → 500 status

*Data Models*:
- **ChatRequest** - Nested record structure for messages
  - messages[] (validated non-empty)
  - message.content[] (validated non-empty)
  - content[].text (1-10000 characters)

**External Integrations**:
- **LLM Provider** - Configurable via Spring AI (examples: OpenAI, Claude, Ollama, etc.)
- **Vector Store** - Configurable via Spring AI (examples: Pinecone, Weaviate, PgVector, etc.)
- **Spring AI 1.0.3** - Framework for LLM and vector store integration
- **Resilience4j** - Rate limiting and circuit breaker patterns

### Frontend Architecture

**Next.js 16.0.0 (React 19.2.0, TypeScript 5.9.3)**

**Pages**:
- `/app/page.tsx` - Root page that renders AssistantModal
- `/app/layout.tsx` - Root layout with MyRuntimeProvider wrapper
- `/app/api/chat/route.ts` - Server-side proxy to backend `/api/chat`

**Core Components**:

*Main Interface*:
- **AssistantModal** - Floating chat button (44px, bottom-right)
  - Toggles chat dialog on click
  - Icon transitions (Bot → Chevron)
  - Smooth animations (zoom, slide)


- **Thread** - Main conversation interface (from @assistant-ui/react)
  - Displays welcome message on first load
  - Shows suggested prompts
  - Renders user and assistant messages
  - Supports message editing and regeneration
  - Auto-scroll to latest message

*Message Rendering*:
- **MarkdownText** - Markdown renderer with GitHub-flavored markdown (remark-gfm)
  - Code blocks with syntax highlighting
  - Copy-to-clipboard for code
  - Custom styled headings, lists, tables
  - Link handling

*State Management*:
- **Zustand 5.0.8** - Client-side state management
- **MyRuntimeProvider** - React context providing AI runtime
  - Integrates with @assistant-ui/react
  - Implements streaming response handling via ReadableStream API
  - Converts backend Flux<String> to frontend message format

*UI Components* (Radix UI wrappers):
- Button, Avatar, Dialog, Tooltip components with Tailwind styling

**API Integration**:
- **Next.js API Route** (`/app/api/chat`) - Server-side proxy
  - Reads `NEXT_PRIVATE_CHAT_API_KEY` from environment (server-side only)
  - Reads `NEXT_PUBLIC_BACKEND_URL` from environment
  - Forwards POST request to backend with X-API-KEY header
  - Pipes backend streaming response back to client
  - Handles authentication transparently from frontend

**Styling & Animation**:
- **Tailwind CSS 4** with PostCSS for utility-first styling
- **Motion 12.23.24** for smooth animations and transitions
- **lucide-react** for icons
- Responsive design for mobile and desktop

## Configuration & Environment

**Note on Provider Flexibility**:  This documentation uses OpenAI for LLM and Pinecone for vector storage as examples. However, Spring AI provides a provider-agnostic abstraction layer. You can easily switch to alternative implementations (e.g., Claude, Ollama for LLM; Weaviate, Qdrant for vector storage) by updating the dependency declarations and Spring AI configurations.
### Spring Profiles (Activation & Configuration)

**Development Profile** (`SPRING_PROFILES_ACTIVE=dev`):
- Loads: `application.yaml` + `application-dev.yaml`
- Server: HTTP on port 8080
- Logging: DEBUG for `SimpleLoggerAdvisor` (shows RAG retrieval)
- Certificates: Not required
- CORS: Allows `http://localhost:3000`

**Production Profile** (`SPRING_PROFILES_ACTIVE=prod`):
- Loads: `application.yaml` + `application-prod.yaml`
- Server: HTTPS on port 8443
- Certificates: Let's Encrypt PEM format with auto-reload
- Logging: INFO level
- CORS: Allows production domains

### Configuration Files

**application.yaml** (Shared across profiles):
```yaml
spring.profiles.default: dev
spring.ai.openai.api-key: ${OPENAI_API_KEY}
spring.ai.openai.chat.options.model: gpt-5.1          # GPT-5.1 LLM
spring.ai.openai.chat.options.temperature: 0          # Deterministic
spring.ai.vectorstore.pinecone.api-key: ${PINECONE_API_KEY}
spring.ai.vectorstore.pinecone.index-name: YOUR_PINECONE_INDEX_NAME
spring.ai.vectorstore.pinecone.project-id: YOUR_PINECONE_PROJECT_ID
app.chat-key: ${APP_CHAT_KEY}
app.embedding-key: ${APP_EMBEDDING_KEY}
resilience4j.ratelimiter.instances.globalRateLimiter:
  limit-for-period: 450
  limit-refresh-period: 60s
  timeout-duration: 0ms
spring.servlet.multipart:
  max-file-size: 20MB
  max-request-size: 25MB
ai.system-prompt: [Persona guidelines for MaxBot]
```

**application-dev.yaml** (Development):
```yaml
server.port: 8080
logging.level.org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor: DEBUG
logging.level.root: INFO
```

**application-prod.yaml** (Production):
```yaml
server.port: 8443
server.ssl.bundle: maxbot-back
spring.ssl.bundle.pem.maxbot-back:
  reload-on-update: true
  keystore:
    certificate: "${LETSENCRYPT_CERT_PATH}"
    private-key: "${LETSENCRYPT_KEY_PATH}"
```

### Environment Variables

**Backend Variables** (all environments):
```
SPRING_PROFILES_ACTIVE=dev|prod
OPENAI_API_KEY=sk-proj-YOUR_OPENAI_API_KEY      # OpenAI API key
PINECONE_API_KEY=pcsk_YOUR_PINECONE_API_KEY     # Pinecone API key
APP_CHAT_KEY=YOUR_CHAT_API_KEY                  # Chat endpoint auth key
APP_EMBEDDING_KEY=YOUR_EMBEDDING_API_KEY        # Embedding endpoint auth key
```

**Backend Variables** (production only):
```
LETSENCRYPT_CERT_PATH=file:/etc/letsencrypt/live/YOUR_DOMAIN/fullchain.pem
LETSENCRYPT_KEY_PATH=file:/etc/letsencrypt/live/YOUR_DOMAIN/privkey.pem
```

**Frontend Variables** (all environments):
```
NEXT_PRIVATE_CHAT_API_KEY=YOUR_CHAT_API_KEY     # Must match APP_CHAT_KEY (server-side)
NEXT_PUBLIC_BACKEND_URL=http://localhost:8080   # dev or https://YOUR_API_DOMAIN:8443 (prod)
```

## Getting Started

### Prerequisites

**Backend**:
- Java 21+
- Maven 3.9+
- OpenAI API key (https://platform.openai.com/api-keys)
- Pinecone account with API key (https://app.pinecone.io/)

**Frontend**:
- Node.js 20+
- npm or yarn

### Backend Setup (Development)

1. Navigate to backend:
   ```bash
   cd maxbot/back
   ```

2. Copy environment template and fill API keys:
   ```bash
   cp .env.example.dev .env
   # Edit .env with your OpenAI and Pinecone keys
   ```

3. Start backend:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

4. Verify startup:
   - http://localhost:8080 (HTTP from application-dev.yaml)
   - SimpleLoggerAdvisor DEBUG logs show RAG retrieval in console

### Frontend Setup (Development)

1. Navigate to frontend:
   ```bash
   cd maxbot/front
   ```

2. Copy environment template and configure backend URL:
   ```bash
   cp .env.example.development .env.development
   # Edit .env.development with matching APP_CHAT_KEY from backend
   ```

3. Start frontend:
   ```bash
   npm install
   npm run dev
   ```

4. Verify startup:
   - http://localhost:3000 (Next.js dev server)
   - CORS allows localhost:3000 from backend
   - Ask questions about the portfolio to test RAG

### Backend Setup (Production)

1. Navigate to backend:
   ```bash
   cd maxbot/back
   ```

2. Copy environment template and fill production values:
   ```bash
   cp .env.example.prod .env
   # Edit .env with production OpenAI, Pinecone keys
   # Add Let's Encrypt certificate paths
   ```

3. Start backend:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

4. Verify startup:
   - https://YOUR_DOMAIN:8443 (HTTPS from application-prod.yaml)
   - Let's Encrypt SSL auto-reloads on certificate renewal

### Frontend Setup (Production)

1. Navigate to frontend:
   ```bash
   cd maxbot/front
   ```

2. Configure production environment:
   ```bash
   cp .env.example.production .env.production
   # Set NEXT_PUBLIC_BACKEND_URL to https://YOUR_API_DOMAIN:8443
   ```

3. Build and deploy:
   ```bash
   npm run build
   npm start
   ```

   Or deploy to Vercel for automatic Next.js hosting.

## API Endpoints

### Chat Endpoint (LLM + RAG)

**POST** `/api/chat`

Process user question through RAG pipeline and return streaming response from configured LLM.

- **Port**: 8080 (dev), 8443 (prod)
- **Authentication**: X-API-KEY header with APP_CHAT_KEY value
- **Rate Limit**: 450 requests per 60-second window
- **Response Type**: Server-Sent Events (Flux<String>)

**Request**:
```json
{
  "messages": [
    {
      "content": [
        {
          "text": "What is your experience with microservices?"
        }
      ]
    }
  ]
}
```

**Process**:
1. Question converted to vector embedding via configured embedding provider
2. Vector store performs semantic search (returns top 10 similar chunks)
3. System prompt + retrieved context + user question → configured LLM
4. Response streamed back as text chunks

### Knowledge Base Management (Embedding Endpoints)

**POST** `/api/embedding/uploadPdf`

Upload PDF for RAG knowledge base indexing.
- **Authentication**: X-API-KEY header with APP_EMBEDDING_KEY value
- **Rate Limit**: 450 requests per 60-second window
- **Request**: Multipart form with `file` parameter (max 20MB)
- **Response**: 201 CREATED

**Process**:
1. Extract text from PDF
2. Split into chunks by token boundaries
3. Generate embeddings for each chunk via configured embedding provider
4. Store vectors in configured vector store with file metadata

**DELETE** `/api/embedding/deleteByIds`

Remove embeddings by document IDs.
- **Authentication**: X-API-KEY header with APP_EMBEDDING_KEY value
- **Rate Limit**: 450 requests per 60-second window
- **Request Body**: `["id1", "id2"]`
- **Response**: 204 NO CONTENT

**DELETE** `/api/embedding/deleteByFileName`

Remove embeddings by PDF filename.
- **Authentication**: X-API-KEY header with APP_EMBEDDING_KEY value
- **Rate Limit**: 450 requests per 60-second window
- **Query Parameter**: `fileName=document.pdf`
- **Response**: 204 NO CONTENT

## Features & Capabilities

### AI-Powered Conversation

- **Streaming Responses**: Real-time response delivery via Server-Sent Events from configured LLM
- **Context-Aware Answers**: Responses grounded in RAG-retrieved documents, not LLM hallucinations
- **Message Editing**: Users can edit any previous message and regenerate response with new context
- **Conversation Branches**: Explore alternative conversation paths
- **Markdown Rendering**: GitHub-flavored markdown with syntax highlighting and copy-to-clipboard

### RAG Implementation

- **Semantic Search**: Vector store finds most relevant documents using vector similarity (top-10 retrieval)
- **Document Chunking**: Token-aware splitting for optimal LLM context window usage
- **Embeddings**: Professional-grade embeddings for accurate semantic matching via configured embedding provider
- **Document Lifecycle**: Upload, index, query, and delete documents via REST API
- **Metadata Tracking**: Store and manage PDF filenames and document metadata

### Security & API Management

- **Dual API Keys**: Separate authentication for `/api/chat` (APP_CHAT_KEY) and `/api/embedding` (APP_EMBEDDING_KEY)
- **CORS Protection**: Whitelisted origins in Java CorsConfig (4 production domains + localhost)
- **Rate Limiting**: Resilience4j enforces 450 requests per 60-second window globally
- **Input Validation**: Message length constraints (1-10000 characters) via Spring Validation
- **HTTPS Production**: Let's Encrypt SSL certificates with automatic reload
- **Error Handling**: Consistent error responses with appropriate HTTP status codes

### Developer Experience

- **Hot Reload**: Spring DevTools (backend) and Next.js dev mode (frontend) for instant updates
- **Debug Logging**: SimpleLoggerAdvisor shows RAG document retrieval in development
- **Type Safety**: Full TypeScript with strict mode across frontend
- **Fast Builds**: Turbopack compilation for rapid iteration

## Technology Versions

**Backend**:
- Spring Boot 3.5.7
- Spring AI 1.0.3 (OpenAI + Pinecone integration)
- Java 21
- Resilience4j (rate limiting)
- Maven 3.x

**Frontend**:
- Next.js 16.0.0 (Turbopack)
- React 19.2.0
- TypeScript 5.9.3
- Tailwind CSS 4
- Zustand 5.0.8 (state management)
- @assistant-ui/react 0.11.35 (AI UI primitives)

**AI Services**:
- OpenAI GPT-5.1 (language model)
- OpenAI Embeddings (vector generation)
- Pinecone (vector database)

## Deployment

### Backend Build & Deployment

**Building the JAR**:
```bash
cd maxbot/back
mvn clean package
```

This generates: `target/maxbot-back-0.0.1-SNAPSHOT.jar`

**Running the JAR**:
```bash
# Development
SPRING_PROFILES_ACTIVE=dev \
OPENAI_API_KEY=YOUR_OPENAI_API_KEY \
PINECONE_API_KEY=YOUR_PINECONE_API_KEY \
APP_CHAT_KEY=YOUR_CHAT_API_KEY \
APP_EMBEDDING_KEY=YOUR_EMBEDDING_API_KEY \
java -jar target/maxbot-back-0.0.1-SNAPSHOT.jar

# Production
SPRING_PROFILES_ACTIVE=prod \
OPENAI_API_KEY=YOUR_OPENAI_API_KEY \
PINECONE_API_KEY=YOUR_PINECONE_API_KEY \
APP_CHAT_KEY=YOUR_CHAT_API_KEY \
APP_EMBEDDING_KEY=YOUR_EMBEDDING_API_KEY \
LETSENCRYPT_CERT_PATH=file:/etc/letsencrypt/live/YOUR_DOMAIN/fullchain.pem \
LETSENCRYPT_KEY_PATH=file:/etc/letsencrypt/live/YOUR_DOMAIN/privkey.pem \
java -jar target/maxbot-back-0.0.1-SNAPSHOT.jar
```

### Frontend Build & Deployment

**Building for production**:
```bash
cd maxbot/front
npm install
npm run build
```

This generates optimized files in `.next/` directory.

**Running locally**:
```bash
NEXT_PRIVATE_CHAT_API_KEY=YOUR_CHAT_API_KEY \
NEXT_PUBLIC_BACKEND_URL=http://localhost:8080 \
npm start
```

**Deploying to Vercel** (recommended for Next.js):
1. Connect repository to Vercel
2. Set environment variables in Vercel dashboard:
   - `NEXT_PRIVATE_CHAT_API_KEY`
   - `NEXT_PUBLIC_BACKEND_URL`
3. Push to main branch for automatic deployment

**Self-hosted Node.js**:
```bash
# Build
npm run build

# Run on production server
NEXT_PRIVATE_CHAT_API_KEY=YOUR_CHAT_API_KEY \
NEXT_PUBLIC_BACKEND_URL=https://YOUR_API_DOMAIN:8443 \
npm start
```

### Production Configuration Checklist

1. **Backend**:
   - Set `SPRING_PROFILES_ACTIVE=prod` to enable HTTPS on port 8443
   - Configure Let's Encrypt certificate paths (auto-reload enabled)
   - Generate secure APP_CHAT_KEY and APP_EMBEDDING_KEY values
   - Verify CORS origins in CorsConfig.java match your domain(s)

2. **Frontend**:
   - Update `NEXT_PUBLIC_BACKEND_URL` to production backend URL
   - Ensure `NEXT_PRIVATE_CHAT_API_KEY` matches backend's `APP_CHAT_KEY`
   - Deploy via Vercel or self-hosted Node.js

3. **Verification**:
   - Test RAG retrieval with sample PDF documents
   - Verify streaming responses work end-to-end
   - Check rate limiting doesn't affect normal usage
   - Monitor backend logs for errors
## Troubleshooting

### RAG & LLM Issues

**"Responses don't match uploaded documents"**:
- Verify PDF uploaded successfully (no console errors)
- Check Pinecone index `YOUR_VECTOR_INDEX` contains documents
- Ensure OpenAI embeddings generated (SimpleLoggerAdvisor shows retrieval)
- Verify system prompt in application.yaml directs use of retrieved context

**"Rate limit exceeded (429)"**:
- Global limit: 450 requests per 60 seconds
- Check Resilience4j metrics for current usage
- Adjust `limit-for-period` in application.yaml if needed

### Backend Errors

**"Failed to authenticate with Pinecone"**:
- Verify PINECONE_API_KEY is correct
- Confirm index `maxbot-vector` exists in Pinecone account
- Check project ID: YOUR_PINECONE_PROJECT_ID

**"X-API-KEY validation failed (401)"**:
- Verify X-API-KEY header matches APP_CHAT_KEY or APP_EMBEDDING_KEY
- Use correct key for endpoint (different keys for `/api/chat` vs `/api/embedding`)

**HTTPS errors in production**:
- Verify LETSENCRYPT_CERT_PATH and LETSENCRYPT_KEY_PATH are correct file:// paths
- Ensure Java process has read permissions for certificate files

### Frontend Errors

**"Failed to connect to backend"**:
- Verify NEXT_PUBLIC_BACKEND_URL is correct
- Check backend service is running on specified port and protocol
- Browser console may show CORS errors if origins mismatch

**"API key mismatch"**:
- NEXT_PRIVATE_CHAT_API_KEY must exactly match backend APP_CHAT_KEY
- Verify both .env files have identical values

**CORS errors in browser**:
- Development: Verify `http://localhost:3000` in CorsConfig.java allowed origins
- Production: Verify your domain in CorsConfig.java allowed origins
- Edit `setAllowedOrigins()` method to add new domains

## Support

**Configuration**:
- Backend config: `application.yaml`, `application-dev.yaml`, `application-prod.yaml`
- Frontend config: `.env.example.development`, `.env.example.production`

**Debugging**:
- Backend logs: Console output from `mvn spring-boot:run`
- Frontend logs: Browser DevTools console (F12)
- Enable SimpleLoggerAdvisor DEBUG logging to see RAG document retrieval
