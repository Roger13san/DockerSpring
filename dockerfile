# Build Pakage

FROM maven:3.8.3-openjdk-19 AS build
COPY . .
RUN mvn clean package -DskipTests


#Pakage stage

FROM openjdk:19-jdk-slim
COPY --from=build /target/API_PicPay-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
