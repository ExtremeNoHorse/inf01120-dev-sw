# libs/ — In-tree Maven Repository

Holds JARs not available on Maven Central. Wired into `pom.xml` via a `file:${project.basedir}/libs` repository.

## Contents

| Artifact | Version | Source |
|----------|---------|--------|
| org.jfugue:jfugue | 5.0.9 | http://www.jfugue.org/jfugue-5.0.9.jar |

SHA-256 of `jfugue-5.0.9.jar`: `5f6989bdc3897772a5c659a6194d7c8aabae16e7134e69d2e4149b653fa4a2df`

## Adding a new vendored JAR

1. Place it under `libs/<groupId-as-path>/<artifactId>/<version>/<artifactId>-<version>.jar`.
2. Add a minimal `<artifactId>-<version>.pom` next to it with `groupId`, `artifactId`, `version`, `packaging`.
3. Reference it in root `pom.xml` `<dependencies>` normally — Maven finds it through the `project-local-libs` repository.
