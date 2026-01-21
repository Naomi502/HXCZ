FROM maven:3.8.8-eclipse-temurin-8 AS build

WORKDIR /workspace
COPY . .
RUN mvn -pl ruoyi-admin -am clean package -DskipTests

FROM eclipse-temurin:8-jre-jammy

WORKDIR /app
ENV JAVA_OPTS=""
EXPOSE 8080

COPY --from=build /workspace/ruoyi-admin/target/ruoyi-admin.jar /app/ruoyi-admin.jar

CMD ["bash", "-lc", "java $JAVA_OPTS -jar /app/ruoyi-admin.jar"]

