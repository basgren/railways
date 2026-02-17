# Railways

Railways is a plugin for RubyMine that provides a convenient way to navigate between route actions.

## Features

* Displays routes of Ruby On Rails application/engine in a separate "Routes" panel
* Adds quick navigation to action implementation from routes panel or "Go to route action" popup available in code editor by pressing **Ctrl + Shift + G** (or **Cmd + Shift + G** on Mac)
* Provides quick routes filtering by route path, controller/action or route name

## Compatibility

* **RubyMine:** 2024.1 - 2025.3
* **Java:** 17+

## Installation

### From JetBrains Marketplace (Coming Soon)

1. Open RubyMine
2. Go to **Settings → Plugins → Marketplace**
3. Search for "Railways"
4. Click **Install**

### Manual Installation

1. Download the latest release from [Releases](https://github.com/basgren/railways/releases)
2. Open RubyMine
3. Go to **Settings → Plugins → ⚙️ → Install Plugin from Disk...**
4. Select the downloaded `.zip` file
5. Restart RubyMine

## Usage

1. Open a Ruby on Rails project in RubyMine
2. Open the **Routes** tool window (usually on the right side panel)
3. Click the refresh button to load routes from your application
4. Use the search field to filter routes by path, controller/action, or route name
5. Double-click a route to navigate to its controller action
6. Use **Ctrl + Shift + G** (or **Cmd + Shift + G** on Mac) to open the "Go to route action" popup from anywhere

## Development

### Prerequisites

* JDK 17+
* IntelliJ IDEA (Community or Ultimate)

### Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/basgren/railways.git
   cd railways
   ```

2. Open the project in IntelliJ IDEA

3. The project uses Gradle with the IntelliJ Platform Gradle Plugin 2.x. Dependencies will be downloaded automatically.

### Building

```bash
./gradlew buildPlugin
```

The plugin will be built to `build/distributions/railways-*.zip`

### Running/Debugging

```bash
./gradlew runIde
```

This will launch a sandboxed RubyMine instance with the plugin installed.

### Testing

```bash
./gradlew test
```

## Version History

### 0.9.0
* Migrate to Gradle build system with IntelliJ Platform Gradle Plugin 2.x
* Add compatibility with RubyMine 2024.1 - 2025.3
* Fix Ruby plugin API changes (RubyProjectAndLibrariesScope, getQualifiedName)
* Replace deprecated ServiceManager API

### 0.8.17 and earlier
See [CHANGES.md](CHANGES.md) for full version history.

## Credits

Created by [Basil Gren](https://github.com/basgren/railways).

0.9.0 modernization by [Srdjan Sin Jovanovic](https://github.com/mp3063).

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request
