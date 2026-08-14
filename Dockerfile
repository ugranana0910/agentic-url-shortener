FROM maven:3.9.11-eclipse-temurin-21-alpine AS builder

WORKDIR /workspace/repository

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
COPY mvnw.cmd .
COPY src src

RUN chmod +x mvnw \
    && ./mvnw \
        --batch-mode \
        --no-transfer-progress \
        clean package \
        -DskipTests


FROM maven:3.9.11-eclipse-temurin-21-alpine AS runtime

RUN apk add --no-cache curl \
    && addgroup \
        --system \
        --gid 10001 \
        agentic \
    && adduser \
        --system \
        --uid 10001 \
        --ingroup agentic \
        --home /workspace \
        agentic

WORKDIR /workspace/repository

COPY --from=builder \
    --chown=agentic:agentic \
    /workspace/repository/pom.xml \
    ./pom.xml

COPY --from=builder \
    --chown=agentic:agentic \
    /workspace/repository/.mvn \
    ./.mvn

COPY --from=builder \
    --chown=agentic:agentic \
    /workspace/repository/mvnw \
    ./mvnw

COPY --from=builder \
    --chown=agentic:agentic \
    /workspace/repository/src \
    ./src

COPY --from=builder \
    --chown=agentic:agentic \
    /workspace/repository/target/*.jar \
    /workspace/application.jar

RUN chmod +x ./mvnw \
    && mkdir -p /workspace/agent-workspaces \
    && chown -R agentic:agentic /workspace

USER agentic

EXPOSE 8080

ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -Dfile.encoding=UTF-8"

ENTRYPOINT ["java", "-jar", "/workspace/application.jar"]