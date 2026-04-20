# ================================================================
# Java 后端脚手架 · Dockerfile（JDK 21）
# 构建：docker build -t java-backend-basic -f docker/Dockerfile .
# 运行：docker-compose up -d
# ================================================================

FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# 先复制 pom.xml，利用 Docker 层缓存，依赖只下载一次
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源码并打包
COPY src ./src
RUN mvn clean package -DskipTests -B

# ================================================================
# 运行阶段：精简 JRE 镜像，只含运行时（JDK 21 的 JRE）
# ================================================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# 从构建阶段复制 jar 包
COPY --from=builder /app/target/*.jar app.jar

# 上传文件存储目录
RUN mkdir -p /app/uploads

EXPOSE 8080

# 启动：使用 exec form，SIGTERM 信号正确传递给 Java 进程
ENTRYPOINT ["java", "-jar", "app.jar"]
