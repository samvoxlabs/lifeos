# Google OAuth Notes

This branch is intended to prepare FamilyOS for OAuth-based access to Google services such as Gmail and other future Google APIs.

The current work is the foundation for that integration. It adds the dependencies, local configuration, Google OAuth client registration, and basic authentication inspection endpoints needed to start wiring Google OAuth. It does not yet complete token storage, refresh handling, or service-specific Google API flows.

## Goal

FamilyOS will need user-granted access to Google services instead of hard-coded credentials. OAuth gives the app a standard way to:

- Redirect a user to Google for consent.
- Request only the scopes needed by a specific feature.
- Receive an authorization result from Google.
- Exchange that result for tokens.
- Use those tokens to call Google APIs on behalf of the user.

## Architecture

```text
Google Account
      |
      v
Google Cloud Project
      |
      +-- OAuth Consent Screen
      +-- OAuth Client
      +-- Gmail API
      +-- Calendar API
      +-- Drive API
              |
              v
      Spring Boot Application
              |
              v
      Google OAuth Login
              |
              v
      User grants permission
              |
              v
      Access Token / Refresh Token
              |
              v
      Gmail / Calendar / Drive APIs
```

## What Was Added

### OAuth and Google API dependencies

The Maven project now includes dependencies for:

- Spring Security.
- Spring OAuth2 client support.
- Google API client support.
- Gmail API client support.
- Google OAuth credential support.
- dotenv-based local environment loading.

Relevant file:

- `pom.xml`

### Local secret placeholders

Local setup now documents the Google OAuth client values expected by the app:

```env
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
```

An example environment file also exists at:

- `docs/.env.example`

Developers should copy those values into a local `.env` file at the project root or provide them through their IDE/run environment. Real OAuth secrets must not be committed.

### Spring OAuth client registration

`application.yml` now imports local environment values and registers Google as an OAuth2 client.

Relevant file:

- `src/main/resources/application.yml`

The Google registration currently includes:

- `client-id` from `GOOGLE_CLIENT_ID`.
- `client-secret` from `GOOGLE_CLIENT_SECRET`.
- `authorization_code` grant type.
- Redirect URI pattern: `{baseUrl}/login/oauth2/code/{registrationId}`.
- Scopes for `openid`, `email`, `profile`, and Gmail read-only access.

### Google service configuration

Google-specific service settings live in `application.yml` and are bound through `@ConfigurationProperties`.

Relevant file:

- `src/main/resources/application.yml`

Current Gmail defaults:

- Gmail API base URL: `https://gmail.googleapis.com/gmail/v1`
- Gmail user ID: `me`
- Max results: `10`

### Basic auth endpoints

The branch includes a small controller for local OAuth POC checks.

Relevant file:

- `src/main/java/com/familyos/familyos/controller/AuthController.java`

Current endpoints:

- `GET /` returns a simple running message.
- `GET /user` returns the authenticated OAuth user attributes.

### LLM layer

The branch includes a provider-neutral LLM interface with Gemini as the first concrete provider. The LLM layer is intentionally separate from Gmail so services can choose what content to send without coupling Gmail code to any specific model vendor.

Relevant files:

- `src/main/java/com/familyos/familyos/llm/LlmProvider.java`
- `src/main/java/com/familyos/familyos/llm/LlmRequest.java`
- `src/main/java/com/familyos/familyos/llm/LlmResponse.java`
- `src/main/java/com/familyos/familyos/llm/gemini/GeminiProvider.java`
- `src/main/java/com/familyos/familyos/llm/gemini/GeminiClient.java`
- `src/main/java/com/familyos/familyos/config/properties/GeminiProperties.java`
- `src/main/java/com/familyos/familyos/llm/factory/LlmProviderFactory.java`
- `src/main/java/com/familyos/familyos/controller/LlmController.java`

Current structure:

```text
llm/
|-- LlmProvider.java
|-- LlmRequest.java
|-- LlmResponse.java
|-- gemini/
|   |-- GeminiProvider.java
|   |-- GeminiClient.java
|   `-- (uses config/properties/GeminiProperties.java)
|-- openai/
|-- anthropic/
`-- factory/
    `-- LlmProviderFactory.java
```

`GeminiProvider` implements the provider-neutral `LlmProvider` contract. `GeminiClient` calls the Gemini `generateContent` API through Spring's `RestClient`. The API key comes from `GEMINI_API_KEY`, and the model defaults to `gemini-2.5-flash` unless `GEMINI_MODEL` is set. `LlmProviderFactory` resolves providers by name and defaults to `gemini`.

### Local security posture

`SecurityConfig` currently permits all requests and disables CSRF for local POC work. This keeps local endpoints easy to test while the OAuth flow is being built.

Relevant file:

- `src/main/java/com/familyos/familyos/config/SecurityConfig.java`

This is not production-ready security. Before real user data or deployed Google integration is added, the app should lock down endpoints and use a proper OAuth login/resource protection model.

## OAuth Setup Steps

### Prerequisites

- Java 21+
- Spring Boot 3.5.x
- Maven
- Google account
- Google Cloud project

### Step 1 - Create a Google Cloud project

1. Open Google Cloud Console.
2. Create a new project.
3. Give the project a meaningful name.

Current project for this branch:

```text
lifeos-backend-500816
```

The Google account is only the project owner or administrator. The Gmail account used during testing is not tied to the project; it authorizes the application through OAuth.

### Step 2 - Enable APIs

In Google Cloud Console, go to:

```text
APIs & Services -> Library
```

Enable the APIs needed by the app:

- Gmail API
- Google Calendar API
- Google Drive API

Additional APIs can be enabled later as needed.

### Step 3 - Configure OAuth consent

In Google Cloud Console, go to:

```text
Google Auth Platform
```

Current project audience page:

```text
https://console.cloud.google.com/auth/audience?authuser=3&project=lifeos-backend-500816
```

Complete:

- Branding
- Audience
- Data Access

Keep the application in Testing mode while developing locally. Add all developers and test Gmail accounts under Test Users.

Example test user:

```text
parentosfamily@gmail.com
```

### Step 4 - Create OAuth client

In Google Cloud Console, go to:

```text
APIs & Services
-> Credentials
-> Create Credentials
-> OAuth Client ID
```

Use this application type:

```text
Web Application
```

Add this authorized redirect URI:

```text
http://localhost:8080/login/oauth2/code/google
```

Save the generated:

- Client ID
- Client Secret

### Step 5 - Confirm dependencies

The branch includes Spring Security, Spring OAuth2 client support, Spring Web, and Google API dependencies in `pom.xml`.

The minimum Spring dependencies for OAuth login are:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

### Step 6 - Configure secrets

Create a `.env` file in the project root:

```env
GOOGLE_CLIENT_ID=<your-client-id>
GOOGLE_CLIENT_SECRET=<your-client-secret>
GEMINI_API_KEY=<your-gemini-api-key>
GEMINI_MODEL=gemini-2.5-flash
```

Make sure `.env` is ignored by git. Never commit OAuth credentials.

### Step 7 - Configure Spring Boot

`src/main/resources/application.yml` imports `.env` and configures Google OAuth:

```yaml
spring:
  config:
    import:
      - optional:file:.env[.properties]
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
            scope:
              - openid
              - email
              - profile
              - https://www.googleapis.com/auth/gmail.readonly
            authorization-grant-type: authorization_code
            redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"
            client-authentication-method: client_secret_post
```

Google and LLM settings now live in `src/main/resources/application.yml` and are bound through `@ConfigurationProperties`.

The app can start without `GEMINI_API_KEY`, but `/llm/generate` requires it.

### Step 8 - Configure Spring Security

For the completed OAuth login flow, protect endpoints that need a Google user context and enable OAuth2 login:

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/").permitAll()
                .anyRequest().authenticated())
            .oauth2Login(Customizer.withDefaults());

        return http.build();
    }
}
```

The current branch still uses permissive local security for POC work, so this is part of the remaining hardening work.

### Step 9 - Run locally

Run the application from the project root:

```bash
./mvnw spring-boot:run
```

Smoke test the app:

```bash
curl http://localhost:8080/api/test
```

Expected response:

```text
Welcome to Family OS! This is a test endpoint.
```

Open the local app in a browser:

```text
http://localhost:8080/
```

### Step 10 - Test authentication

After OAuth login is enforced in Spring Security, start the flow in a browser:

```text
http://localhost:8080/oauth2/authorization/google
```

Log in using an approved test account, for example:

```text
parentosfamily@gmail.com
```

### Step 11 - Verify login

The branch includes this endpoint:

```java
@GetMapping("/user")
public Map<String, Object> user(@AuthenticationPrincipal OAuth2User user) {
    return user.getAttributes();
}
```

Visit:

```text
http://localhost:8080/user
```

Expected response shape:

```json
{
  "email": "...",
  "email_verified": true,
  "name": "...",
  "picture": "..."
}
```

### Step 12 - Test the LLM layer

After `GEMINI_API_KEY` is configured, call:

```bash
curl -X POST http://localhost:8080/llm/generate \
  -H 'Content-Type: application/json' \
  -d '{
    "provider": "gemini",
    "useCase": "local-smoke-test",
    "systemPrompt": "Answer briefly.",
    "content": "Say hello from FamilyOS.",
    "metadata": {
      "source": "local"
    }
  }'
```

Current behavior:

- Accepts generic content through `LlmController`.
- Converts the request into an `LlmRequest`.
- Resolves the requested provider through `LlmProviderFactory`.
- Uses `GeminiProvider` and `GeminiClient` to call Gemini.
- Returns an `LlmResponse` with provider, model, generated content, and metadata.

## Learnings

- OAuth work needs both app-side configuration and Google Cloud configuration. Adding dependencies is only the first step.
- Google service access should be scope-driven. Gmail, Calendar, Drive, and other APIs should request only the permissions they need.
- Local development should use environment variables or `.env`; secrets should never be checked into source control.
- Spring's OAuth2 client dependency is useful for the web login and authorization code flow, while Google client libraries are useful after tokens exist and the app needs to call Google APIs.
- The current permissive security setup is helpful for early development, but it must be revisited before production usage.
- Token handling is a separate design decision. The app still needs a safe approach for storing, refreshing, and revoking user tokens.
- Keeping Google service settings in `application.yml` makes it easier to grow from Gmail-only POC settings into multiple Google service integrations.
- Source code and Maven dependencies need to stay aligned. If a dependency such as Springdoc is removed, related configuration classes must also be removed or replaced.
- The LLM boundary should stay provider-neutral. Feature services should prepare clean content and metadata; provider adapters should handle model-specific request formats.

## Remaining Work

- Tighten `SecurityConfig` so OAuth login is actually enforced for endpoints that need Google user context.
- Confirm the exact initial Gmail scope set and keep it to the smallest useful set.
- Add service-specific Google API integration, starting with the first target service.
- Define token persistence and refresh strategy.
- Replace permissive local security with endpoint-level authorization rules.
- Add tests around OAuth configuration and any Google service adapters.
- Add refresh token management.
- Add Gmail push notifications or background synchronization if needed.
- Add multi-user support when the app moves beyond a single-user POC.
- Add prompt and response contracts for each Gmail use case, such as summary, action item extraction, and priority classification.
- Add tests for `GeminiClient` request construction and `GeminiProvider` response parsing.

## Notes

- One Google Cloud project can support many Google users.
- Every user authorizes access independently through OAuth.
- Service accounts are not used to access personal Gmail accounts.
- Keep the application in Testing mode until it is ready for production.
- Request only the OAuth scopes required by the application.
