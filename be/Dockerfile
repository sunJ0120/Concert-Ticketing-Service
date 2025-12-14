# ------ 빌드 스테이지 : 캐싱 최적화 ------
FROM gradle:8-jdk21 as builder
WORKDIR /app

# 1. 의존성 파일 먼저 복사
COPY . .
#COPY gradlew .
#COPY gradle gradle
#COPY build.gradle settings.gradle

# 2. 의존성 다운로드 레이어 & CRLF 제거
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew
RUN ./gradlew dependencies --no-daemon

# 3. 소스 코드 복사 및 빌드
COPY src src
RUN ./gradlew bootJar -x test --no-daemon

# ------ 실행 스테이지 : 최종 경량화 ------

FROM ubuntu/jre:21-24.04_stable
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]