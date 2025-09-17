# Java Version

## Features
- Loads local `users.csv` in this directory
- Filtering users by minimum age (default 30)
- Counting users by country
- Calculating average age (1 decimal)
- Top N oldest users (default 3)
- Region aggregation (Europe, North America, South America, Asia, Oceania, Other)

## Prerequisites
- Java 17+ (uses switch expressions and records)

Check version:
```powershell
java -version
javac -version
```

## Run (simple javac/java)
From root directory:
```powershell
# Compile
javac -d out src/main/java/users/Main.java
# Run (default summary)
java -cp out users.Main

# Other operations
java -cp out users.Main summary
java -cp out users.Main filter
java -cp out users.Main group
java -cp out users.Main avg
java -cp out users.Main top
java -cp out users.Main region
```

## Maven (optional)
A minimal `pom.xml` is included. From inside root directory:
```powershell
mvn -q compile exec:java -Dexec.mainClass=users.Main -Dexec.args=summary
```
(Install the `exec-maven-plugin` if you don't have it cached; the POM config handles it.)

## Notes
- CSV file is local (`users.csv`). Run commands from inside root directory.
- Pure standard library; no external dependencies aside from the optional Maven build plugins.
