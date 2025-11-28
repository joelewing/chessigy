# Chessigy
<img width="128" height="128" alt="icon_128" src="https://github.com/user-attachments/assets/cb21a2ca-6c17-43f2-ab19-e0f39f51d827" />


A basic chess application built with JavaFX.

<img width="1184" height="912" alt="image" src="https://github.com/user-attachments/assets/c03d4a22-d20b-4518-8e01-9d2038036c24" />


## Playing against the computer
Chessigy supports the [Serendipity](https://github.com/xu-shawn/Serendipity) chess engine. Download the latest .jar file for Serendipity, and load it from Chessigy (Engine > Load Engine JAR...). Chessigy will remember the engine after you have loaded it for ths first time.

## Prerequisites

- Java 17 or higher
- Maven 3.6+

## Building

```bash
mvn clean compile
```

## Running

```bash
mvn javafx:run
```

## Packaging

Packaging is a work-in-progress, an Arch Linux package can be built with: 
```bash
mvn clean install -Ppackage-arch
```
