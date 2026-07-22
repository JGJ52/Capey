# Building

Prebuilt releases are available on [Modrinth](https://modrinth.com/mod/capey). If you don't want to use those, or Modrinth isn't available, you can build the mod yourself.

While Modrinth is reviewing, download JARs from here:
- [26.2](https://cdn.modrinth.com/data/CyJJ10KQ/versions/lJszNP1H/capey-1.2.0.jar?mr_download_reason=standalone)
- [26.1-26.1.2](https://cdn.modrinth.com/data/CyJJ10KQ/versions/jcdSORZS/capey-1.2.0.jar?mr_download_reason=standalone)
- [1.21.11](https://cdn.modrinth.com/data/CyJJ10KQ/versions/vj4ve4jj/capey-1.2.0.jar?mr_download_reason=standalone)
- [1.21.9-1.21.10](https://cdn.modrinth.com/data/CyJJ10KQ/versions/cjPGEBjz/capey-1.2.0.jar?mr_download_reason=standalone)

(for some reason you can still download the jars with the link even if the mod is under review)

## Requirements

- Java 25 on & above 26.1
- Java 21 below 26.1

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
\
Examples:
- `1.21.10` → `1.21.9`
- `26.1.2` → `26.1`
- `26.1.1` → `26.1`