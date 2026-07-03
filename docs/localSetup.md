# Local Setup — FamilyOS

This document describes how to prepare your machine and run the project locally.

Pre-requisites
- Git (to clone the repository)
- JDK 21 (project `pom.xml` uses Java 21)
- Maven wrapper (included in repo as `mvnw` / `mvnw.cmd`)

1) Clone the repository

```bash
git clone <repository-url>
cd familyos
```

2) Verify JDK 21 is installed

Run:

```bash
java -version
```

Expected output should show a Java 21 distribution. If not installed, see the IntelliJ setup doc for recommended distributions and install commands:

`docs/intellijSetup.md`

3) Environment variables / .env

This project uses `dotenv-java` to read environment variables from the environment or a `.env` file. Typical steps:

- Create a `.env` file at the project root (do NOT commit secrets).
- Example `.env` contents (replace with real values):

```env
# Example keys
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
LLM_DEFAULT_PROVIDER=gemini
GEMINI_API_KEY=your-gemini-api-key
GEMINI_MODEL=gemini-2.5-flash
# Other keys used by the app
```

- Alternatively, set environment variables in your OS or IDE run configuration.

4) Build and run

From the project root:

```bash
# Run using the Maven wrapper
./mvnw spring-boot:run

# Or build a jar and run
./mvnw package
java -jar target/*.jar
```

Default server port

By default Spring Boot starts on port 8080. You can change it in `src/main/resources/application.yml` or by setting `SERVER_PORT` env variable or `--server.port` command-line argument.

5) Run tests

```bash
./mvnw test
```

Quick verification (smoke test)

After the application starts (default port 8080), verify it's running by opening the health/test endpoint in your browser or using curl:

```bash
# In a browser:
http://localhost:8080/api/test

# Or with curl:
curl -v http://localhost:8080/api/test

# Expected response: Hello World
```


6) Common troubleshooting

- "Unsupported Java version": ensure IntelliJ and your terminal use JDK 21.
- Maven resolves slowly or fails to download: check network/proxy and try again; run `./mvnw -U clean package`.
- Port 8080 in use: specify another port `./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=9090` or set environment variable.

7) Helpful file locations

- Main application class: `src/main/java/com/familyos/familyos/FamilyosApplication.java`
- Application config: `src/main/resources/application.yml`
- Maven wrapper: `mvnw` (Linux/macOS), `mvnw.cmd` (Windows)

If you'd like, I can add a sample `.env.example` to the repository, or create an importable IntelliJ run configuration file that preloads environment variables. Tell me which you'd prefer.
