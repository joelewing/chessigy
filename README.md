<img width="512" height="512" alt="icon_512" src="https://github.com/user-attachments/assets/0c9d33bc-4100-443d-8cd9-d99efd989920" /> # Chessigy

A basic chess application built with JavaFX.

<img width="1184" height="912" alt="image" src="https://github.com/user-attachments/assets/96264451-6a17-4828-aa5d-66b4b52e51ec" />

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
