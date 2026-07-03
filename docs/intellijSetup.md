IntelliJ Community — Setup Guide
================================

This document helps a first-time contributor install IntelliJ IDEA Community Edition, configure a Java Development Kit (JDK) compatible with this project, and enable useful plugins/settings for working with this repository.

Checklist (follow these steps)
- Install IntelliJ IDEA Community (macOS or Windows)
- Install JDK 21 (project uses Java 21 as defined in `pom.xml`)
- Configure IntelliJ Project SDK and language level
- Enable recommended plugins (optional but helpful)
- Import the project as a Maven project and run the app

Why Java 21?
- The project `pom.xml` defines `<java.version>21</java.version>` so you should use a JDK 21 distribution for compiling and running.

1) Install IntelliJ IDEA Community

macOS (recommended: JetBrains Toolbox or direct download)

- Option A — JetBrains Toolbox (recommended if you manage multiple JetBrains IDEs):
  1. Download JetBrains Toolbox from https://www.jetbrains.com/toolbox-app/
  2. Open the downloaded DMG and move the Toolbox app to your Applications folder.
  3. Run Toolbox, find IntelliJ IDEA Community and click Install.

- Option B — Direct download:
  1. Visit https://www.jetbrains.com/idea/download/
  2. Choose Community edition and download the macOS DMG.
  3. Open the DMG and drag IntelliJ IDEA to `Applications`.

Windows

- Download the Community edition installer from https://www.jetbrains.com/idea/download/
- Run the installer `.exe`, accept defaults (or adjust installation path), and allow it to add desktop and Start Menu shortcuts if you like.

2) Install JDK 21

We recommend any modern, supported distribution (Eclipse Temurin / Adoptium, Azul Zulu, BellSoft Liberica). Below are quick commands and links.

macOS — Homebrew (Temurin) or SDKMAN

- Homebrew (if you use Homebrew):

```bash
# Install Temurin 21
brew tap homebrew/cask-versions
brew install --cask temurin21

# Verify
java -version
```

- SDKMAN (if you prefer):

```bash
# Install SDKMAN (if not installed)
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

# Install Java 21
sdk install java 21-tem

# Verify
java -version
```

Windows — Adoptium MSI or Chocolatey

- Direct (Adoptium / Eclipse Temurin):
  1. Visit https://adoptium.net and choose Temurin 21 for Windows (x64 or x86 depending on your system).
  2. Download the MSI and run the installer.

- Chocolatey (if you use Chocolatey):

```powershell
# Run in an elevated PowerShell
choco install temurin21 -y
java -version
```

Set JAVA_HOME (important for some tools and IntelliJ)

- macOS (bash/zsh): add to `~/.zshrc` or `~/.bash_profile` (adjust path if different):

```bash
export JAVA_HOME="/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
source ~/.zshrc
```

- Windows (PowerShell, set for current session):

```powershell
# For the current session (PowerShell)
$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-21'

# To set JAVA_HOME permanently (requires admin):
#setx JAVA_HOME "C:\Program Files\Eclipse Adoptium\jdk-21" -m
```

Note: paths vary by distribution. After installing, run `java -version` and `echo $JAVA_HOME` (mac) or `echo %JAVA_HOME%` (cmd) / `Get-Item Env:JAVA_HOME` (PowerShell) to confirm.

3) Configure IntelliJ for this project

- Open IntelliJ IDEA Community.
- Choose "Open" and select the project root (the folder that contains `pom.xml`).
- IntelliJ should detect a Maven project and show a popup to enable auto-import — accept it. If not:
  - Open the Maven tool window (View → Tool Windows → Maven), then click the refresh button.

Set Project SDK and language level

1. File → Project Structure (or press Cmd+, on mac / Ctrl+Alt+Shift+S on Windows).
2. In 'Project' select 'Project SDK' → Add JDK → navigate to the JDK 21 installation directory and select it.
3. Set 'Project language level' to 21 (or 'Project default') to match the JDK.

Run configuration (Spring Boot)

- This is a Spring Boot app. The main class is `com.familyos.familyos.FamilyosApplication` (file: `src/main/java/com/familyos/familyos/FamilyosApplication.java`).
- IntelliJ may automatically create an 'Application' run configuration. If not, create a new 'Application' run configuration, set the main class above and use the Project SDK.

Alternatively you can run via Maven wrapper from the terminal:

```bash
# From project root
./mvnw spring-boot:run

# or package and run
./mvnw package
java -jar target/*.jar
```

4) Recommended plugins

The Community edition already includes Git and basic Maven support. These additional plugins can improve experience:

- EnvFile — allows loading `.env` files into run configurations (helpful if you store credentials or local config in environment files). Useful since this project uses `dotenv-java`.
- YAML — better YAML editing (IntelliJ bundles good YAML support; install if missing).
- .ignore — helps manage `.gitignore` files.
- GitToolBox — enhanced Git integration (optional).
- Key Promoter X — learn keyboard shortcuts (optional).

How to install plugins

1. IntelliJ → Preferences (Settings on Windows) → Plugins.
2. Search for the plugin name, click Install, then restart IntelliJ if prompted.

5) Useful tips for working with this repo

- Environment files: If you use a `.env` file, don't commit secrets. The project includes `dotenv-java` to read local environment variables — use the EnvFile plugin or set environment variables in your run configuration.
- Application configuration: main app config is under `src/main/resources/application.yml` or `application.properties` if present.
- Tests: run unit tests via the IDE or `./mvnw test`.

Troubleshooting

- IntelliJ doesn't detect Maven or dependencies:
  - Make sure Project SDK is set to JDK 21.
  - In Maven tool window click the reload/refresh button.
  - Delete the `.idea` folder and re-open the project (only as a last resort).

- "Unsupported Java version" errors:
  - Verify the JDK installation and that IntelliJ is pointed to that JDK in Project Structure.

Additional Resources

- IntelliJ IDEA downloads: https://www.jetbrains.com/idea/download/
- Adoptium (Temurin) JDK downloads: https://adoptium.net/
- SDKMAN: https://sdkman.io/

If you want, I can also provide a short script or checklist to automate JDK checks and a sample run configuration JSON you can import into IntelliJ. Let me know which OS you want the automation for.
