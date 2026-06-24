# syntax=docker/dockerfile:1

# ---- build stage : 멀티모듈을 한 번에 빌드해 rest/admin jar 동시 생성 ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
# BuildKit 캐시 마운트로 ~/.m2 를 빌드 간 유지 (1 OCPU 인스턴스에서 재빌드 가속)
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -DskipTests clean package

# ---- runtime stage : JRE만, 두 jar를 함께 포함 ----
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/rest/target/rest-*.jar   rest.jar
COPY --from=build /app/admin/target/admin-*.jar admin.jar

# compose 가 APP_JAR 로 실행 대상(rest.jar / admin.jar)을 지정.
# mem_limit 과 MaxRAMPercentage 로 컨테이너별 힙을 제한.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=50.0"
ENV APP_JAR="rest.jar"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/$APP_JAR"]
