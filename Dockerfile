# Multi-stage build: compile with Maven then run with a slim JRE

# ===== Build stage =====
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /workspace

# Copy pom and resolve dependencies first for better layer caching
COPY pom.xml ./
RUN --mount=type=cache,target=/root/.m2 mvn -B -q -e -DskipTests dependency:go-offline

# Copy sources and build
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B -q -e -DskipTests package

# ===== Runtime stage =====
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy the built shaded jar with a deterministic name
COPY --from=build /workspace/target/json-to-excel-1.0.0.jar /app/json-to-excel.jar

# Default data directory
VOLUME ["/data"]

# Usage: pass input and output paths as args
ENTRYPOINT ["java","-jar","/app/json-to-excel.jar"]

# Example: docker run --rm -v %cd%/data:/data image:tag /data/input.json /data/output.xlsx

