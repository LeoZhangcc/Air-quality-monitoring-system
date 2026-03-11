# 第一阶段：使用基于 Java 8 的 Maven 镜像进行编译打包
FROM maven:3.8-openjdk-8 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# 运行打包命令，跳过测试以加快部署速度
RUN mvn clean package -DskipTests

# 第二阶段：使用轻量级的 Java 8 运行环境 (JRE) 启动项目
FROM openjdk:8-jre-slim
WORKDIR /app
# 把第一阶段打包出来的 jar 包拷贝过来
COPY --from=build /app/target/*.jar app.jar
# 告诉 Render 服务器暴露 8080 端口
EXPOSE 8080
# 启动你的 Spring Boot 应用
ENTRYPOINT ["java","-jar","app.jar"]