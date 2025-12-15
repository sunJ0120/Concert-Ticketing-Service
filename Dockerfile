FROM gradle:8-jdk21 as builder
WORKDIR /app

COPY . .

RUN sed -i 's/\r$//' gradlew && chmod +x gradlew
RUN ./gradlew dependencies --no-daemon

COPY src src
RUN ./gradlew bootJar -x test --no-daemon

# ------ 실행 스테이지 : 최종 경량화 ------

FROM ubuntu/jre:21-24.04_stable
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]