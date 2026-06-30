# Building

Prebuilt releases are available on [Modrinth](https://modrinth.com/mod/capey). If you don't want to use those, or Modrinth isn't available, you can build the mod yourself.

## Requirements

- Java 25

### Linux/macOS

```sh
git clone https://codeberg.org/Capey/Capey.git
cd Capey
./gradlew build
```

### Windows

```powershell
git clone https://codeberg.org/Capey/Capey.git
cd Capey
.\gradlew.bat build
```

The built JAR will be located at:

```
versions/{minecraft_version}/build/libs/capey-{version}.jar
```

If there isn't a directory for your exact Minecraft version, but your version is **1.21.9 or newer** (the minimum supported version), use the closest **older** version instead.

Examples:

- `1.21.10` → `1.21.9`
- `26.1.2` → `26.1`
- `26.1.1` → `26.1`